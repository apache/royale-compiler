/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.royale.abc;

import org.apache.royale.abc.diagnostics.AbstractDiagnosticVisitor;
import org.apache.royale.abc.graph.IFlowgraph;
import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.optimize.DeadCodeFilter;
import org.apache.royale.abc.optimize.PeepholeOptimizerMethodBodyVisitor;
import org.apache.royale.abc.semantics.ClassInfo;
import org.apache.royale.abc.semantics.InstanceInfo;
import org.apache.royale.abc.semantics.Instruction;
import org.apache.royale.abc.semantics.Label;
import org.apache.royale.abc.semantics.Metadata;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.abc.visitors.DelegatingClassVisitor;
import org.apache.royale.abc.visitors.DelegatingMetadataVisitor;
import org.apache.royale.abc.visitors.DelegatingMethodBodyVisitor;
import org.apache.royale.abc.visitors.DelegatingMethodVisitor;
import org.apache.royale.abc.visitors.DelegatingScriptVisitor;
import org.apache.royale.abc.visitors.DelegatingTraitVisitor;
import org.apache.royale.abc.visitors.DelegatingTraitsVisitor;
import org.apache.royale.abc.visitors.IABCVisitor;
import org.apache.royale.abc.visitors.IClassVisitor;
import org.apache.royale.abc.visitors.IDiagnosticsVisitor;
import org.apache.royale.abc.visitors.IMetadataVisitor;
import org.apache.royale.abc.visitors.IMethodBodyVisitor;
import org.apache.royale.abc.visitors.IMethodVisitor;
import org.apache.royale.abc.visitors.IScriptVisitor;
import org.apache.royale.abc.visitors.ITraitVisitor;
import org.apache.royale.abc.visitors.ITraitsVisitor;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnreachableBlockProblem;

import java.io.File;
import java.util.Collection;

/**
 * The ABCLinker links a sequence of ABC blocks into a single ABC block,
 * and contains utility methods to perform various transformations on the ABC.
 */
public class ABCLinker
{
    public static byte[] linkABC(Iterable<byte[]> inputABCs, int majorVersion, int minorVersion, ABCLinkerSettings settings) throws Exception
    {
        ABCEmitter emitter = new ABCEmitter();
        // ABCs from 4.5 may have non-sensical jumps past the end of a method
        // so allow those, instead of throwin java exceptions
        emitter.setAllowBadJumps(true);
        emitter.visit(majorVersion, minorVersion);
        for (byte[] inputABC : inputABCs)
        {
            ABCParser abcParser = new ABCParser(inputABC);
            abcParser.parseABC(new LinkingVisitor(emitter, settings));
        }
        emitter.visitEnd();
        return emitter.emit();
    }

    public static class ABCLinkerSettings
    {
        private boolean optimize = false;
        private boolean enableInlining = false;
        private boolean stripDebug = false;
        private boolean stripFileAttributeFromGotoDefinitionHelp = false;
        private boolean stripGotoDefinitionHelp = false;
        private boolean removeDeadCode = false;
        private Collection<String> meta_names = null;
        @SuppressWarnings("unused")
        private int minorVersion = ABCConstants.VERSION_ABC_MINOR_FP10;
        @SuppressWarnings("unused")
        private int majorVersion = ABCConstants.VERSION_ABC_MAJOR_FP10;
        private Collection<ICompilerProblem> problems;

        /**
         * Tell the linker whether it should run the peephole optimizer defaults
         * to false
         * 
         * @param b true if the ABCs should be optimized
         */
        public void setOptimize(boolean b)
        {
            optimize = b;
        }

        /**
         * Tell the linker whether it should enable inlining of functions.
         * defaults to false
         * @param b true if the functions should be inlined
         */
        public void setEnableInlining(boolean b)
        {
            enableInlining = b;
        }

        /**
         * Tell the linker whether is should strip out debug opcodes defaults to
         * false
         * 
         * @param b true if the ABCs should have the debug opcodes stripped out
         */
        public void setStripDebugOpcodes(boolean b)
        {
            stripDebug = b;
        }

        /**
         * Tell the linker which metadata names it should keep. If null, it will
         * keep them all except for "go to definition help" which is control by
         * the stripGotoDefinitionHelp flag. Defaults to null.
         * 
         * @param metadata_names A collection of metadata names that the linker
         * will keep.
         */
        public void setKeepMetadata(Collection<String> metadata_names)
        {
            meta_names = metadata_names;
        }

        /**
         * @return true if we should be stripping some metadata
         */
        boolean shouldStripMetadata()
        {
            return meta_names != null || stripGotoDefinitionHelp ||
                   stripFileAttributeFromGotoDefinitionHelp;
        }

        public void setTargetABCVersion(int major, int minor)
        {
            this.majorVersion = major;
            this.minorVersion = minor;
        }

        /**
         * If the metadata to keep is null, this flag determines if
         * "go to definition help" metadata is removed.
         * 
         * @param stripGotoDefinitionHelp <code>true</code> to strip the metadata,
         * <code>false</code> to leave it.
         */
        public void setStripGotoDefinitionHelp(boolean stripGotoDefinitionHelp)
        {
            this.stripGotoDefinitionHelp = stripGotoDefinitionHelp;
        }

        /**
         * If "go to definition help" meta is being kept, this flag determines
         * if we should strip the file attribute from the metadata.
         * 
         * @param stripFileAttributeFromGotoDefinitionHelp <code>true</code> to strip
         * the file attribute, <code>false</code> to leave it.
         */
        public void setStripFileAttributeFromGotoDefinitionHelp(boolean stripFileAttributeFromGotoDefinitionHelp)
        {
            this.stripFileAttributeFromGotoDefinitionHelp = stripFileAttributeFromGotoDefinitionHelp;
        }

        /**
         * Enable or disable the DeadCodeFilter optimization step.
         * @param removeDeadCode true if the DeadCodeFilter should be run.
         */
        public void setRemoveDeadCode(final boolean removeDeadCode)
        {
            this.removeDeadCode = removeDeadCode;
        }

        /**
         * Set a problems collection for errors or warnings during link.
         * @param problems the problems collection to receive errors or warnings.
         */
        public void setProblemsCollection(Collection<ICompilerProblem> problems)
        {
            this.problems = problems;
        }
    }

    /**
     * IMethodBodyVisitor implementation that will strip out Debug opcodes
     */
    private static final class DebugStrippingMethodBodyVisitor extends DelegatingMethodBodyVisitor
    {
        public DebugStrippingMethodBodyVisitor(IMethodBodyVisitor delegate)
        {
            super(delegate);
        }

        // flag to keep track of if the last instruction was one we stripped out
        // needed for figuring out where some labels land - see labelCurrent()
        private boolean strippedLastInstruction;

        /**
         * Determine if the opcode should be stripped out, or passed along to
         * the next visitor. This also flags whether the opcode was stripped or
         * not, so if you're calling this make sure you do what it tells you to
         * 
         * @param opcode The instruction to check
         * @return true if the instruction should be stripped out.
         */
        private boolean stripInstruction(int opcode)
        {
            switch (opcode)
            {
                case ABCConstants.OP_debug:
                case ABCConstants.OP_debugfile:
                case ABCConstants.OP_debugline:
                    strippedLastInstruction = true;
                    break;
                default:
                    strippedLastInstruction = false;
                    break;
            }
            return strippedLastInstruction;
        }

        @Override
        public void visitInstructionList(InstructionList new_list)
        {
            // for the delegates that run after this IVisitor, they will be processing
            // a new copy of the instruction list, which will not have the debug opcodes
            InstructionList strippedInstructionList = new InstructionList(new_list.size());
            for (Instruction inst : new_list.getInstructions())
            {
                if (!stripInstruction(inst.getOpcode()))
                    strippedInstructionList.addInstruction(inst);
            }
            super.visitInstructionList(strippedInstructionList);
        }

        @Override
        public void visitInstruction(int opcode)
        {

            if (stripInstruction(opcode))
                return;
            super.visitInstruction(opcode);
        }

        @Override
        public void visitInstruction(int opcode, int immediate_operand)
        {
            if (stripInstruction(opcode))
                return;

            super.visitInstruction(opcode, immediate_operand);
        }

        @Override
        public void visitInstruction(int opcode, Object[] operands)
        {
            if (stripInstruction(opcode))
                return;
            super.visitInstruction(opcode, operands);
        }

        @Override
        public void visitInstruction(int opcode, Object single_operand)
        {
            if (stripInstruction(opcode))
                return;
            super.visitInstruction(opcode, single_operand);
        }

        @Override
        public void visitInstruction(Instruction instruction)
        {
            if (stripInstruction(instruction.getOpcode()))
                return;
            super.visitInstruction(instruction);
        }

        @Override
        public void labelCurrent(Label l)
        {
            // if we're trying to label a debug instruction that got stripped out
            // then we really want to label the next instruction, and not whatever the
            // last instruction happens to be.
            // We only do this if the label may target a non-executable instruction - this
            // is because we are stripping non executable instructions here - if the label must
            // target an executable instruction, then the behavior of labelCurrent is correct -
            // it would have walked back over the instructions until it found the first non-executable
            // instruction.
            if (strippedLastInstruction && !l.targetMustBeExecutable())
                super.labelNext(l);
            else
                super.labelCurrent(l);
        }
    }

    /**
     * IMethodVisitor that will create a DebugStrippingMethodBodyVisitor - used
     * when you want to remove debug opcodes from the method body.
     */
    private static class DebugStrippingMethodVisitor extends DelegatingMethodVisitor
    {
        public DebugStrippingMethodVisitor(IMethodVisitor delegate)
        {
            super(delegate);
        }

        @Override
        public IMethodBodyVisitor visitBody(MethodBodyInfo mbi)
        {
            return new DebugStrippingMethodBodyVisitor(super.visitBody(mbi));
        }
    }

    /**
     * IMethodVisitor that will create an PeepholeOptimizerMethodBodyVisitor for
     * the method body - used when you want to run the peephole optimizer for
     * the method body
     */
    private static class OptimizingMethodVisitor extends DelegatingMethodVisitor
    {
        public OptimizingMethodVisitor(IMethodVisitor delegate, Collection<ICompilerProblem> problems, final boolean removeDeadCode)
        {
            super(delegate);
            this.problems = problems;
            this.removeDeadCode = removeDeadCode;
        }

        /**
         * Sink for problems generated during this phase.
         */
        final Collection<ICompilerProblem> problems;

        /**
         * When true, run a DeadCodeFilter as part of the optimization pipeline.
         */
        final boolean removeDeadCode;

        @Override
        public IMethodBodyVisitor visitBody(MethodBodyInfo mbi)
        {
            //  Set up the optimizer pipeline.
            IMethodBodyVisitor delegate = super.visitBody(mbi);

            if ( removeDeadCode )
            {
                IDiagnosticsVisitor diagnostics = new AbstractDiagnosticVisitor()
                {
                    @Override
                    public void unreachableBlock(MethodBodyInfo methodBodyInfo, IFlowgraph cfg, IBasicBlock block)
                    {
                        if ( problems != null )
                        {
                            String fileName = cfg.findSourcePath(block);

                            if ( fileName != null && new File(fileName).isFile() )
                                problems.add(new UnreachableBlockProblem(fileName, cfg.findLineNumber(block)));
                        }
                    }
                };
                delegate = new DeadCodeFilter(mbi, delegate, diagnostics);
            }

            return new PeepholeOptimizerMethodBodyVisitor(delegate);
        }
    }

    /**
     * IScriptVisitor that will create a MetadataStrippingTraitsVisitor for the
     * script traits
     */
    private static class MetadataStrippingScriptVisitor extends DelegatingScriptVisitor
    {
        MetadataStrippingScriptVisitor(IScriptVisitor d, Collection<String> meta_names,
                                       boolean stripGotoDefinitionHelp,
                                       boolean stripFileAttribute)
        {
            super(d);
            this.meta_names = meta_names;
            this.stripGotoDefinitionHelp = stripGotoDefinitionHelp;
            this.stripFileAttribute = stripFileAttribute;
        }

        private Collection<String> meta_names;
        private boolean stripGotoDefinitionHelp;
        private boolean stripFileAttribute;

        @Override
        public ITraitsVisitor visitTraits()
        {
            return new MetadataStrippingTraitsVisitor(super.visitTraits(), meta_names,
                    stripGotoDefinitionHelp,
                    stripFileAttribute);
        }

    }

    /**
     * IClassVisitor that will create a MetadataStrippingTraitsVisitor for the
     * class and instance traits.
     */
    private static class MetadataStrippingClassVisitor extends DelegatingClassVisitor
    {
        MetadataStrippingClassVisitor(IClassVisitor d, Collection<String> meta_names,
                                      boolean stripGotoDefinitionHelp,
                                      boolean stripFileAttribute)
        {
            super(d);
            this.meta_names = meta_names;
            this.stripGotoDefinitionHelp = stripGotoDefinitionHelp;
            this.stripFileAttribute = stripFileAttribute;
        }

        private Collection<String> meta_names;
        private boolean stripGotoDefinitionHelp;
        private boolean stripFileAttribute;

        @Override
        public ITraitsVisitor visitClassTraits()
        {
            return new MetadataStrippingTraitsVisitor(super.visitClassTraits(), meta_names,
                    stripGotoDefinitionHelp,
                    stripFileAttribute);
        }

        @Override
        public ITraitsVisitor visitInstanceTraits()
        {
            return new MetadataStrippingTraitsVisitor(super.visitInstanceTraits(), meta_names,
                    stripGotoDefinitionHelp,
                    stripFileAttribute);
        }
    }

    /**
     * ITraitVisitor that will create a metadata visitor that will strip out some
     * metadata.
     */
    private static class MetadataStrippingTraitVisitor extends DelegatingTraitVisitor
    {
        MetadataStrippingTraitVisitor(ITraitVisitor d, Collection<String> meta_names,
                                      boolean stripGotoDefinitionHelp,
                                      boolean stripFileAttribute)
        {
            super(d);
            this.meta_names = meta_names;
            this.stripGotoDefinitionHelp = stripGotoDefinitionHelp;
            this.stripFileAttribute = stripFileAttribute;
        }

        private Collection<String> meta_names;
        private boolean stripGotoDefinitionHelp;
        private boolean stripFileAttribute;

        @Override
        public IMetadataVisitor visitMetadata(int count)
        {
            return new MetadataStrippingVisitor(super.visitMetadata(count), meta_names,
                    stripGotoDefinitionHelp,
                    stripFileAttribute);
        }
    }

    /**
     * IMetadataVisitor that will only emit certain metadata, and will strip out
     * the rest of the metadata. If no metadata is supposed to be stripped, then
     * you shouldn't create one of these
     */
    private static class MetadataStrippingVisitor extends DelegatingMetadataVisitor
    {

        /**
         * Constants for removing the __go_to_definition_help and __go_to_definition_ctor
         * help that get injected for go to definition support
         */
        private static final String GO_TO_DEFINITION_HELP = "__go_to_definition_help";
        private static final String GO_TO_DEFINITION_CTOR_HELP = "__go_to_ctor_definition_help";
        private static final String GO_TO_DEFINITION_HELP_FILE = "file";

        /**
         * If the meta tag is "__go_to_ctor_definition_help" or
         * "__go_to_definition_help", strip off the "file" attribute.
         * 
         * @param metadata The data to strip the "file" attribute from.
         */
        static Metadata stripFileAttributeFromGotoDefinitionHelp(Metadata metadata)
        {
            String name = metadata.getName();
            if (GO_TO_DEFINITION_HELP.equals(name) ||
                GO_TO_DEFINITION_CTOR_HELP.equals(name))
            {
                metadata = removeKey(metadata, GO_TO_DEFINITION_HELP_FILE);
            }

            return metadata;
        }

        /**
         * Remove a key from this metadata.
         * 
         * @param key The key to remove. May not be null.
         */
        static Metadata removeKey(Metadata metadata, String key)
        {
            assert metadata != null;
            assert key != null;

            String[] keys = metadata.getKeys();
            for (int i = 0; i < keys.length; i++)
            {
                if (key.equals(keys[i]))
                {
                    String[] values = metadata.getValues();
                    String[] newKeys = new String[keys.length - 1];
                    String[] newValues = new String[keys.length - 1];

                    // Copy attributes up to found attribute.
                    if (i > 0)
                    {
                        System.arraycopy(keys, 0, newKeys, 0, i);
                        System.arraycopy(values, 0, newValues, 0, i);

                    }

                    // Copy attributes after the found attribute.
                    if (i < (keys.length - 1))
                    {
                        System.arraycopy(keys, i + 1, newKeys, i,
                                keys.length - i - 1);
                        System.arraycopy(values, i + 1, newValues, i,
                                keys.length - i - 1);
                    }

                    metadata = new Metadata(metadata.getName(), newKeys, newValues);
                    break;
                }
            }

            return metadata;
        }

        /**
         * Constructor
         * 
         * @param d the delegate visitor to wrap
         * @param meta_names the names of the metadata which should be emitted.
         * If null, all the names are emitted except for "go to definition help"
         * metadata which is controlled by the stripGotoDefinitionHelp parameter
         * and stripFileAttribute parameter.
         * @param stripGotoDefinitionHelp true if "go to definition help"
         * metadata should be stripped out, false otherwise.
         * @param stripFileAttribute true if the "file" attribute of the goto
         * definition metadata should be stripped.
         */
        MetadataStrippingVisitor(IMetadataVisitor d, Collection<String> meta_names,
                                 boolean stripGotoDefinitionHelp,
                                 boolean stripFileAttribute)
        {
            super(d);
            this.metaNames = meta_names;
            this.stripGotoDefinitionHelp = stripGotoDefinitionHelp;
            this.stripFileAttribute = stripFileAttribute;
        }

        private Collection<String> metaNames;
        private boolean stripGotoDefinitionHelp;
        private boolean stripFileAttribute;

        @Override
        public void visit(Metadata md)
        {
            if (shouldKeep(md))
            {
                if (!stripGotoDefinitionHelp && stripFileAttribute)
                    md = stripFileAttributeFromGotoDefinitionHelp(md);
                super.visit(md);
            }
            return;
        }

        boolean shouldKeep(Metadata md)
        {
            if (metaNames == null)
            {
                if (GO_TO_DEFINITION_HELP.equals(md.getName()) ||
                    GO_TO_DEFINITION_CTOR_HELP.equals(md.getName()))
                {
                    return !stripGotoDefinitionHelp;
                }

                return true;
            }
            return metaNames.contains(md.getName());
        }
    }

    /**
     * ITraitsVisitor that will wrap any generated ITraitVisitor in a
     * MetadataStrippingTraitVisitor
     */
    private static class MetadataStrippingTraitsVisitor extends DelegatingTraitsVisitor
    {
        MetadataStrippingTraitsVisitor(ITraitsVisitor d, Collection<String> meta_names,
                                       boolean stripGotoDefinitionHelp,
                                       boolean stripFileAttribute)
        {
            super(d);
            this.meta_names = meta_names;
            this.stripGotoDefinitionHelp = stripGotoDefinitionHelp;
            this.stripFileAttribute = stripFileAttribute;
        }

        private Collection<String> meta_names;
        private boolean stripGotoDefinitionHelp;
        private boolean stripFileAttribute;

        @Override
        public ITraitVisitor visitSlotTrait(int kind, Name name, int slot_id, Name slot_type, Object slot_value)
        {
            return new MetadataStrippingTraitVisitor(
                    super.visitSlotTrait(kind, name, slot_id, slot_type, slot_value),
                    meta_names,
                    stripGotoDefinitionHelp,
                    stripFileAttribute);
        }

        @Override
        public ITraitVisitor visitClassTrait(int kind, Name name, int slot_id, ClassInfo clazz)
        {
            return new MetadataStrippingTraitVisitor(
                    super.visitClassTrait(kind, name, slot_id, clazz),
                    meta_names,
                    stripGotoDefinitionHelp,
                    stripFileAttribute);
        }

        @Override
        public ITraitVisitor visitMethodTrait(int kind, Name name, int disp_id, MethodInfo method)
        {
            return new MetadataStrippingTraitVisitor(
                    super.visitMethodTrait(kind, name, disp_id, method),
                    meta_names,
                    stripGotoDefinitionHelp,
                    stripFileAttribute);
        }
    }

    /**
     * IABCVisitor implementation to do various transformations on an ABC file -
     * stripping debug opcodes, optimizing, etc.
     */
    private static class LinkingVisitor implements IABCVisitor
    {
        public LinkingVisitor(ABCEmitter delegate, ABCLinkerSettings linkSettings)
        {
            this.delegate = delegate;
            this.settings = linkSettings;
        }

        private final ABCEmitter delegate;
        private final ABCLinkerSettings settings;

        @Override
        public void visit(int major_version, int minor_version)
        {
            // Don't call into the delegate, the linker has done that!
        }

        @Override
        public void visitEnd()
        {
            // Do nothing... This will be called once by the linker on the
            // delegate.
        }

        @Override
        public IScriptVisitor visitScript()
        {
            IScriptVisitor sv = delegate.visitScript();
            if (settings.shouldStripMetadata())
                sv = new MetadataStrippingScriptVisitor(sv, settings.meta_names,
                        settings.stripGotoDefinitionHelp,
                        settings.stripFileAttributeFromGotoDefinitionHelp);
            return sv;
        }

        @Override
        public IClassVisitor visitClass(InstanceInfo iinfo, ClassInfo cinfo)
        {
            IClassVisitor cv = delegate.visitClass(iinfo, cinfo);
            if (settings.shouldStripMetadata())
                cv = new MetadataStrippingClassVisitor(cv, settings.meta_names,
                        settings.stripGotoDefinitionHelp,
                        settings.stripFileAttributeFromGotoDefinitionHelp);
            return cv;
        }

        @Override
        public IMethodVisitor visitMethod(MethodInfo minfo)
        {
            IMethodVisitor mv = delegate.visitMethod(minfo);
            if (settings.optimize)
                mv = new OptimizingMethodVisitor(mv, settings.problems, settings.removeDeadCode);

            // Run the debug stripping visitor first, so the debug
            // instructions won't confuse the peephole optimizer
            // building a chain of visitors, so the debug stripping will
            // wrap the optimizing visitor
            if (settings.stripDebug)
                mv = new DebugStrippingMethodVisitor(mv);

            return mv;
        }

        @Override
        public void visitPooledInt(Integer i)
        {
            // emitter automatically pools values.
        }

        @Override
        public void visitPooledUInt(Long l)
        {
            // emitter automatically pools values.
        }

        @Override
        public void visitPooledDouble(Double d)
        {
            // emitter automatically pools values.
        }

        @Override
        public void visitPooledString(String s)
        {
            // emitter automatically pools values.
        }

        @Override
        public void visitPooledNamespace(Namespace ns)
        {
            if (settings.enableInlining)
                ns.setMergePrivateNamespaces(true);
        }

        @Override
        public void visitPooledNsSet(Nsset nss)
        {
            // emitter automatically pools values.
        }

        @Override
        public void visitPooledName(Name n)
        {
            // emitter automatically pools values.
        }

        @Override
        public void visitPooledMetadata(Metadata md)
        {
            // emitter automatically pools values.
        }
    }
}
