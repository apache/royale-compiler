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

package org.apache.royale.compiler.internal.as.codegen;

import static org.apache.royale.abc.ABCConstants.*;

import java.util.*;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.ClassInfo;
import org.apache.royale.abc.semantics.InstanceInfo;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.abc.semantics.PooledValue;
import org.apache.royale.abc.semantics.Trait;
import org.apache.royale.abc.visitors.IABCVisitor;
import org.apache.royale.abc.visitors.IClassVisitor;
import org.apache.royale.abc.visitors.IMethodBodyVisitor;
import org.apache.royale.abc.visitors.IMethodVisitor;
import org.apache.royale.abc.visitors.ITraitVisitor;
import org.apache.royale.abc.visitors.ITraitsVisitor;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.IMetaInfo;
import org.apache.royale.compiler.common.ModifiersSet;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.definitions.*;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.exceptions.CodegenInterruptedException;
import org.apache.royale.compiler.internal.definitions.*;
import org.apache.royale.compiler.internal.tree.as.*;
import org.apache.royale.compiler.problems.*;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.*;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode.LanguageIdentifierKind;
import org.apache.royale.compiler.internal.abc.FunctionGeneratorHelper;
import org.apache.royale.compiler.internal.as.codegen.ICodeGenerator.IConstantValue;
import org.apache.royale.compiler.internal.definitions.metadata.MetaTag;
import org.apache.royale.compiler.internal.definitions.metadata.ResourceBundleMetaTag;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.MethodBodySemanticChecker;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.utils.ASTUtil;
import org.apache.royale.utils.ArrayLikeUtil;

/**
 * A ClassDirectiveProcessor generates an ABC class
 * from a ClassNode and its contents.
 */
class ClassDirectiveProcessor extends DirectiveProcessor
{
    
    /**
     * The namespace to put compiler generated skin part object into so that it does not conflict with any user defined
     * members.
     */
    private static final Namespace skinPartPrivateNamespace = new Namespace(CONSTANT_PrivateNs, ".SkinPartNamespace");
    private static final Name NAME_OBJECT = new Name(IASLanguageConstants.Object);


    /**
     * Get all the user defined metadata plus 
     * "go to definition help" metadata.
     * 
     * @param definition The definition to get the metadata for.
     * @return An array of {@link IMetaTag}.
     */
    protected static IMetaInfo[] getAllMetaTags(IDefinition definition)
    {
        assert definition != null;
        
        IMetaInfo[] metaTags = definition.getAllMetaTags();
        MetaTag metaTag = MetaTag.createGotoDefinitionHelp(definition, 
                definition.getContainingFilePath(), 
                Integer.toString(definition.getNameStart()), false);
        metaTags = MetaTag.addMetaTag(metaTags, metaTag);
        return metaTags;
    }

    /** The class' definition. */
    ClassDefinition classDefinition;

    /** The class' instance lexical scope. */
    LexicalScope  classScope;
    
    /** The class' static lexical scope. */
    LexicalScope classStaticScope;
    
    /** The class' name. */
    Name className;
    /** The class' superclass' name. */
    Name superclassName;
    
    /** The class' instance traits */
    ITraitsVisitor itraits;
    /** The class' class (static) traits. */
    ITraitsVisitor ctraits;
    
    /** The AET visitor implementing this class. */
    IClassVisitor cv;
    /** The class' AET ClassInfo. */
    ClassInfo cinfo = new ClassInfo();
    /** The class' AET InstanceInfo. */
    InstanceInfo iinfo = new InstanceInfo();

    /** The class' initial AST node; used for diagnostics. */
    IASNode definitionSource;
    
    /**
     *  Instructions to place in the class' initializer.
     *  Note that these are part of the class itself, as
     *  opposed to the above instructions which create 
     *  the class at the global scope. 
     */
    InstructionList cinitInsns = new InstructionList();
    
    /**
     * Instructions to place in the class' constructor.
     */
    InstructionList iinitInsns = new InstructionList();
    
    /**
     *  The constructor definition; it's saved for code-gen
     *  after the CG has generated code for all instance
     *  variables that need initializers.
     */
    FunctionNode ctorFunction = null;

    /** The AET IABCVisitor emitting this class' ABC. */
    IABCVisitor emitter;

    /**
     * Collection of static variables with initial values which need to be initialized
     * before other static initialization code.
     */
    protected final Collection<VariableNode> staticVariableInitializers = new ArrayList<VariableNode>();

    /**
     * Constructor.
     * Initializes the ClassDirectiveProcessor and its
     * associated AET data structures.
     * @param c - the class' AST.
     * @param enclosing_scope - the immediately enclosing lexical scope.
     * @param emitter - the active ABC emitter.
     */
    ClassDirectiveProcessor(ClassNode c, LexicalScope enclosing_scope, IABCVisitor emitter)
    {
        this(c, c.getDefinition(), enclosing_scope, emitter);
    }
    
    /**
     * Constructor.
     * Initializes the ClassDirectiveProcessor and its
     * associated AET data structures.
     * @param node - the AST that starts the class' definition
     *    in source; used for diagnostics.
     * @param class_definition - the class' definition
     * @param enclosing_scope - the immediately enclosing lexical scope.
     * @param emitter - the active ABC emitter.
     */
    ClassDirectiveProcessor(ICommonClassNode node, ClassDefinition class_definition,
                            LexicalScope enclosing_scope, IABCVisitor emitter)
    {
        super(enclosing_scope.getProblems());

        this.emitter = emitter;
        this.definitionSource = node;
        assert(this.definitionSource != null): "Class definition AST must be provided.";

        this.classScope = enclosing_scope.pushFrame();
        this.classStaticScope = enclosing_scope.pushFrame();

        if (node.getNodeID() == ASTNodeID.ClassID)
        {
            classScope.setInitialControlFlowRegionNode(((ClassNode)node).getScopedNode());
            classStaticScope.setInitialControlFlowRegionNode(((ClassNode)node).getScopedNode());
        }
        
        ICompilerProject project = classScope.getProject();
        
        // Set the class Name.
        this.classDefinition = class_definition;
        this.className = classDefinition.getMName(project);
        iinfo.name = className;

        // Check for a duplicate class name.
        switch(SemanticUtils.getMultiDefinitionType(this.classDefinition, project))
        {   
            case AMBIGUOUS:
            classScope.addProblem(new DuplicateClassDefinitionProblem(node, class_definition.getBaseName()));
                break;
            case NONE:
                break;
            default:
               assert false;       // I don't think classes can have other type of multiple definitions
        }
        
        if (node instanceof BaseDefinitionNode)     // test doesn't work for MXML, which is OK.
        {
            BaseDefinitionNode n = (BaseDefinitionNode)node; 
            SemanticUtils.checkScopedToDefaultNamespaceProblem(classScope, n, classDefinition, null);
        }
        // Resolve the super class, checking that it exists,
        // that it is a class rather than an interface,
        // that it isn't final, and that it isn't the same as this class.
        ClassDefinition superclassDefinition =
            SemanticUtils.resolveBaseClass(node, class_definition, project, classScope.getProblems());

        // Check that the superclass isn't a forward reference, but only need to do this if both
        // definitions come from the same containing source.  getContainingFilePath() returns the file
        // from the ASFileScope, so no need to worry about included files.
        
        // XXX (mschmalle) Added for JS Object impl, shouldn't have side effects
        if (superclassDefinition != null)
        {
            if (!classDefinition.isGeneratedEmbedClass() && classDefinition.getContainingFilePath().equals(superclassDefinition.getContainingFilePath()))
            {
                // If the absolute offset in the class is less than the
                // offset of the super class, it must be a forward reference in the file
                int classOffset = classDefinition.getAbsoluteStart();
                int superClassOffset = superclassDefinition.getAbsoluteEnd();
                if (classOffset < superClassOffset)
                    classScope.addProblem(new ForwardReferenceToBaseClassProblem(node, superclassDefinition.getQualifiedName()));
            }

            // Set the superclass Name.
            this.superclassName = superclassDefinition.getMName(project);
            iinfo.superName = superclassName;
        }
        
        // Resolve the interfaces.
        IInterfaceDefinition[] interfaces = classDefinition.resolveImplementedInterfaces(
            project, classScope.getProblems());
        
        // Set the interface Names.
        int n_interfaces = interfaces.length;
        ArrayList<Name> interface_names = new ArrayList<Name>(n_interfaces);
        for (int i = 0; i < n_interfaces; i++)
        {
            InterfaceDefinition idef = (InterfaceDefinition)interfaces[i];
            if (idef != null)
            {
                Name interfaceName = ((InterfaceDefinition)interfaces[i]).getMName(project);
                interface_names.add(interfaceName);
            }
        }
        iinfo.interfaceNames = interface_names.toArray(new Name[interface_names.size()]);
        
        // Set the flags corresponding to 'final' and 'dynamic'.
        if (classDefinition.isFinal())
            iinfo.flags |= ABCConstants.CLASS_FLAG_final;
        if (!classDefinition.isDynamic())
            iinfo.flags |= ABCConstants.CLASS_FLAG_sealed;
        
        iinfo.protectedNs = ((NamespaceDefinition)classDefinition.getProtectedNamespaceReference()).getAETNamespace();
        
        this.cv = emitter.visitClass(iinfo, cinfo);
        cv.visit();
        
        this.itraits = cv.visitInstanceTraits();
        this.ctraits = cv.visitClassTraits();

        this.classScope.traitsVisitor = this.itraits;
        this.classStaticScope.traitsVisitor = this.ctraits;

        // Build an array of the names of all the ancestor classes.
        ArrayList<Name> ancestorClassNames = new ArrayList<Name>();
        
        // Walk the superclass chain, starting with this class
        // and (unless there are problems) ending with Object.
        // This will accomplish three things:
        // - find loops;
        // - build the array of names of ancestor classes;
        // - set the needsProtected flag if this class or any of its ancestor classes needs it.

        boolean needsProtected = false;

        //  Remember the most recently examined class in case there's a cycle in the superclass
        //  chain, in which case we'll need it to issue a diagnostic.
        ClassDefinition c = null;

        IClassDefinition.IClassIterator classIterator =
            classDefinition.classIterator(project, true);

        while (classIterator.hasNext())
        {
            c = (ClassDefinition)classIterator.next();
            needsProtected |= c.getOwnNeedsProtected();
            if (c != classDefinition)
                ancestorClassNames.add(c.getMName(project));
        }
        
        // Report a loop in the superclass chain, such as A extends B and B extends A.
        // Note: A extends A was found previously by SemanticUtils.resolveBaseClass().
        if (classIterator.foundLoop())
            classScope.addProblem(new CircularTypeReferenceProblem(c, c.getQualifiedName()));
        
        // In the case of class A extends A, ancestorClassNames will be empty at this point.
        // Change it to be Object to prevent "Warning: Stack underflow" in the script init code below.
        if (ancestorClassNames.isEmpty())
        {
            ClassDefinition objectDefinition = (ClassDefinition)project.getBuiltinType(
                IASLanguageConstants.BuiltinType.OBJECT);
            ancestorClassNames.add(objectDefinition.getMName(project));
        }

        //handle the case where this class needs an EventDispatcher implementation for binding support
        //we check if we can replace EventDispatcher as the super class instead of Object, and if yes,
        //reset the ancestorClassNames to those of the binding EventDispatcher class
        if (class_definition.needsEventDispatcher(project)) {
            if (ancestorClassNames.size() == 1) {
                ClassDefinition objectClassDefinition = (ClassDefinition)project.getBuiltinType(
                        IASLanguageConstants.BuiltinType.OBJECT);

                if (objectClassDefinition.equals(superclassDefinition)) {
                    //the immediate and only ancestor is Object, we have a candidate for 'upgrading' the
                    //ancestor chain to be the Binding EventDispatcher (which can be set via compiler configuration)
                    IDefinition eventDispatcherCheck = project.resolveQNameToDefinition(BindableHelper.STRING_EVENT_DISPATCHER);
                    if (eventDispatcherCheck !=null && eventDispatcherCheck instanceof ClassDefinition) {

                        ClassDefinition eventDispatcherClass = (ClassDefinition) eventDispatcherCheck;

                        // reset the superclass Name.
                        // This can be used for testing to avoid adding IEventDispatcher implementation later
                        this.superclassName = eventDispatcherClass.getMName(project);
                        iinfo.superName = superclassName;
                        //replace "Object" at the first position in ancestorClassNames
                        // with the Binding EventDispatcher class
                        // (which can sometimes be configured via compiler directives)
                        ancestorClassNames.set(0, this.superclassName);

                        //now get the ancestors of the binding EventDispatcher class
                        c = null;
                        IClassDefinition.IClassIterator eventDispatcherIterator =
                                eventDispatcherClass.classIterator(project, false);

                        needsProtected = class_definition.getOwnNeedsProtected() ||
                                                eventDispatcherClass.getOwnNeedsProtected();

                        while (eventDispatcherIterator.hasNext())
                        {
                            c = (ClassDefinition)eventDispatcherIterator.next();
                            needsProtected |= c.getOwnNeedsProtected();
                            if (c != classDefinition)
                                ancestorClassNames.add(c.getMName(project));
                        }

                        //This may not ever be needed here, but duping the safety logic from above, just in case (GD)
                        if (eventDispatcherIterator.foundLoop())
                            classScope.addProblem(new CircularTypeReferenceProblem(c, c.getQualifiedName()));

                        //Add the implicit dependency for EventDispatcher. This also adds IEventDispatcher
                        addBindableDependencies(true);
                    }
                }
            }
        }



        // If this class or any of its ancestor classes needs the protected flag set, set it.
        if (needsProtected)
            iinfo.flags |= ABCConstants.CLASS_FLAG_protected;

        // Add the class initialization logic to the script init.
        // For class B extends A, where class A extends Object, this looks like
        // getscopeobject
        // findpropstrict Object
        // getproperty Object
        // pushscope
        // findpropstrict A
        // getproperty A
        // dup
        // pushscope
        // newclass
        // popscope
        // popscope
        // initproperty B
        InstructionList initInstructions = this.classScope.getInitInstructions();
        initInstructions.addInstruction(OP_getscopeobject, 0);

        // Push ancestor classes onto the scope stack.
        for (int i = ancestorClassNames.size() - 1; i >= 0; i--)
        {
            Name ancestorClassName = ancestorClassNames.get(i);
            initInstructions.addInstruction(OP_getlex, ancestorClassName);
            // The newclass instruction below also needs the superclass on the stack, so dup it
            if (i == 0)
                initInstructions.addInstruction(OP_dup);
            initInstructions.addInstruction(OP_pushscope);
        }

        initInstructions.addInstruction(OP_newclass, cinfo);

        for (int i = 0; i < ancestorClassNames.size(); i++)
            initInstructions.addInstruction(OP_popscope);
        
        initInstructions.addInstruction(OP_initproperty, className);
        
        implementedInterfaceSemanticChecks(class_definition);
        implementedAbstractClassSemanticChecks(class_definition);

        processResourceBundles(class_definition, project, classScope.getProblems());
    }

    protected void processResourceBundles (IClassDefinition class_definition, ICompilerProject project, Collection<ICompilerProblem> problems)
    {
        
        IMetaTag[] rbs = class_definition.getMetaTagsByName(IMetaAttributeConstants.ATTRIBUTE_RESOURCEBUNDLE);

        if( rbs != null )
        {
            for ( IMetaTag meta : rbs )
            {
                if( meta instanceof ResourceBundleMetaTag)
                {
                    try
                    {
                        ((ResourceBundleMetaTag)meta).resolveDependencies(problems, project);
                    }
                    catch( InterruptedException ie)
                    {
                        throw new CodegenInterruptedException(ie);
                    }
                }
            }
        }
    }

    private Boolean hasAddedDependency = false;

    /**
     * Supports the late addition of related dependencies for an implicit Bindable implementation.
     * @param implementationExtends true if the dependency implementation is to support extending (otherwise
     *                              it is for 'implements')
     */
    void addBindableDependencies(Boolean implementationExtends) {
        if (!hasAddedDependency) {
            DependencyType dependencyType = implementationExtends ? DependencyType.INHERITANCE : DependencyType.EXPRESSION;

            //Add the implicit dependency for EventDispatcher. This also adds IEventDispatcher
            ASScope containingScope = (ASScope) getClassDefinition().getContainingScope();
            containingScope.findPropertyQualified(classScope.getProject(),
                    NamespaceDefinition.createPackagePublicNamespaceDefinition(
                            BindableHelper.NAME_EVENT_DISPATCHER.getQualifiers().getSingleQualifier().getName()),
                    BindableHelper.NAME_EVENT_DISPATCHER.getBaseName(),
                    dependencyType);
            hasAddedDependency = true;
        }
    }

    /**
     * Finish the class' definition.
     */
    void finishClassDefinition()
    {
        // should be able to pass null here because GlobalDirectiveProcessor
        // already called getSkinsParts and collected problems.  This
        // call should get the cached array.
        IMetaTag[] skinParts = classDefinition.findSkinParts(classScope.getProject(), null);
        if (skinParts.length > 0)
        {
            Name var_name = new Name(CONSTANT_Qname, new Nsset(skinPartPrivateNamespace), "skinParts");
            classStaticScope.declareVariableName(var_name);
            ITraitVisitor tv = classStaticScope.traitsVisitor.visitSlotTrait(TRAIT_Var, var_name, 
                    ITraitsVisitor.RUNTIME_SLOT, NAME_OBJECT, LexicalScope.noInitializer);
            tv.visitEnd();

            cinitInsns.addInstruction(OP_findproperty, var_name);
            
            for (IMetaTag skinPart : skinParts)
            {
                cinitInsns.addInstruction(OP_pushstring, skinPart.getDecoratedDefinition().getBaseName());
                cinitInsns.addInstruction(OP_convert_s);
                IMetaTagAttribute attr = skinPart.getAttribute("required");
                if (attr == null || attr.getValue().equals("true"))
                    cinitInsns.addInstruction(OP_pushtrue);
                else
                    cinitInsns.addInstruction(OP_pushfalse);
            }
            cinitInsns.addInstruction(OP_newobject, skinParts.length);
            cinitInsns.addInstruction(OP_setproperty, var_name);
            
            // Equivalent AS:
            //
            //      protected function get skinParts():Object
            //      {
            //          return ClassName._skinParts;
            //      }
            //
            MethodInfo mi = new MethodInfo();
            mi.setMethodName("skinParts");

            mi.setReturnType(NAME_OBJECT);

            InstructionList insns = new InstructionList(3);
            insns.addInstruction(OP_getlocal0);
            insns.addInstruction(OP_findpropstrict, var_name);
            insns.addInstruction(OP_getproperty, var_name);
            insns.addInstruction(OP_returnvalue);

            FunctionGeneratorHelper.generateFunction(classScope.getEmitter(), mi, insns);

            NamespaceDefinition nd = (NamespaceDefinition)classDefinition.getProtectedNamespaceReference();
            Name func_name = new Name(nd.getAETNamespace(), "skinParts");
            tv = classScope.traitsVisitor.visitMethodTrait(TRAIT_Getter, func_name, 0, mi);
            tv.visitAttribute(Trait.TRAIT_OVERRIDE, Boolean.TRUE);
            tv.visitEnd();

        }
        
        // the generation of instructions for variable initialization is delayed
        // until now, so we can add that initialization to the front of
        // the cinit instruction list.
        if (!staticVariableInitializers.isEmpty())
        {
            InstructionList exisitingCinitInsns = null;
            if (!this.cinitInsns.isEmpty())
            {
                exisitingCinitInsns = new InstructionList();
                exisitingCinitInsns.addAll(this.cinitInsns);
                this.cinitInsns = new InstructionList();
            }

            for (VariableNode var : staticVariableInitializers)
                generateInstructions(var, true);

            if (exisitingCinitInsns != null)
                this.cinitInsns.addAll(exisitingCinitInsns);
        }

        // add "goto_definition_help" metadata to user defined metadata.
        ITraitVisitor tv = classScope.getGlobalScope().traitsVisitor.visitClassTrait(
                TRAIT_Class, className, 0, cinfo);
        IMetaInfo[] metaTags = getAllMetaTags(classDefinition);

        // Add "goto definition help" metadata for the constructor.
        if (this.ctorFunction != null)
        {
            FunctionDefinition ctorDef = this.ctorFunction.getDefinition();
            MetaTag metaTag = MetaTag.createGotoDefinitionHelp(classDefinition,
                    classDefinition.getContainingFilePath(),
                    Integer.toString(ctorDef.getNameStart()), true);
            if (metaTag != null)
                metaTags = MetaTag.addMetaTag(metaTags, metaTag);
        }
    
        if (ArrayLikeUtil.definitionIsArrayLike(classDefinition)) {
            ArrayList<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
            boolean valid = ArrayLikeUtil.validateArrayLikeDefinition(classDefinition, classScope.getProject(), problems);
            if (!valid){
                classScope.getProject().getProblems().addAll(problems);
            }
        }
        
        this.classScope.processMetadata(tv, metaTags);
        tv.visitEnd();
        
        // Need to do this before generating the CTOR as the generated code may result in insns that
        // need to be added to the ctor.
        generateBindableImpl();

        generateRequiredContingentDefinitions();
        
        addAnyEmbeddedAsset();

        // Make any vistEnd method calls
        // that were deferred.
        // callVisitEnds must be called on the same thread
        // that started code generation.  Since we don't generate
        // classes in parallel yet, we know that we are on the thread
        // that started code generation here.
        classScope.callVisitEnds();
        classStaticScope.callVisitEnds();
        
        //  Create the class' constructor function.
        if ( this.ctorFunction != null )
        {
            MethodInfo mi = classScope.getGenerator().generateFunction(this.ctorFunction, classScope, this.iinitInsns, null);

            if ( mi != null )
                this.iinfo.iInit = mi;
        }
        else if ( !this.iinitInsns.isEmpty() )
        {
            //  Synthesize a constructor.

            this.iinfo.iInit = new MethodInfo();
            MethodBodyInfo iinit = new MethodBodyInfo();
            iinit.setMethodInfo(this.iinfo.iInit);
            
            IMethodVisitor mv = emitter.visitMethod(this.iinfo.iInit);
            mv.visit();
            IMethodBodyVisitor mbv = mv.visitBody(iinit);
            InstructionList ctor_insns = new InstructionList();
            ctor_insns.addInstruction(OP_getlocal0);
            ctor_insns.addInstruction(OP_pushscope);
            
            ctor_insns.addAll(this.iinitInsns);

            //  Call the superclass' constructor after the instance
            //  init instructions; this doesn't seem like an abstractly
            //  correct sequence, but it's what ASC does.
            ctor_insns.addInstruction(OP_getlocal0);
            ctor_insns.addInstruction(OP_constructsuper, 0);

            ctor_insns.addInstruction(OP_returnvoid);
            mbv.visit();
            mbv.visitInstructionList(ctor_insns);
            mbv.visitEnd();
            mv.visitEnd();
        }

        //  If the class has static initialization
        //  logic, emit a cinit routine.
        if ( ! this.cinitInsns.isEmpty() )
        {
            InstructionList cinit_insns   = new InstructionList();
            cinit_insns.addInstruction(OP_getlocal0);
            cinit_insns.addInstruction(OP_pushscope);

            //  TODO: Examine other end-of-function processing
            //  and ensure it's completed.
            this.classStaticScope.finishClassStaticInitializer(this.cinitInsns);
            cinit_insns.addAll(this.cinitInsns);

            cinit_insns.addInstruction(OP_returnvoid);
            
            createCInitIfNeeded();
            
            this.classStaticScope.methodBodyVisitor.visitInstructionList(cinit_insns);
            this.classStaticScope.methodBodyVisitor.visitEnd();
            this.classStaticScope.methodVisitor.visitEnd();
        }
        else
        {
            //  Ensure nothing got dropped.
            assert( this.classStaticScope.methodBodyVisitor == null);
        }
        
        itraits.visitEnd();
        ctraits.visitEnd();
        
        cv.visitEnd();
    }

    @Override
    public void declareBindableVariable(VariableNode varNode)
    {
	//  Declaration may have side effects on the traits
	//  and the initialization instructions.
        generateInstructions(varNode, varNode.getDefinition().isStatic());
    }
    
    /**
     * accessor function used by code-gen helper classes.
     */
    public LexicalScope getInstanceScope()
    {
        return this.classScope;
    }
    
    /**
     * accessor function used by code-gen helper classes.
     */
    public ClassDefinition getClassDefinition()
    {
        return this.classDefinition;
    }

    protected void generateBindableImpl()
    {
        //  initially double-check that this class has not already been set to be an
        //  implicit subclass of EventDispatcher in this ClassDirectiveProcessor's constructor
        //  before the needsEventDispatcher check (which doesn't know about the implementation)
        if((this.superclassName == null || !this.superclassName.equals(BindableHelper.NAME_EVENT_DISPATCHER))
                && classDefinition.needsEventDispatcher(classScope.getProject()) )
        {

            //Add the implicit dependency for EventDispatcher. This also adds IEventDispatcher
            addBindableDependencies(false);

            // Generate a EventDispatcher member, equivalent to:
            //   private var _bindingEventDispatcher : flash.events.EventDispatcher;
            //
            // Note that it is in a separate private namespace, so it won't conflict with user defined private members

            // Add init code for the _bindingEventDispatcher to the ctor
            // this is the equivalent of:
            //   _bindingEventDispatcher = new flash.events.EventDispatcher(this);

            iinitInsns.addAll(BindableHelper.generateBindingEventDispatcherInit(itraits, false));
            BindableHelper.generateAddEventListener(classScope);
            BindableHelper.generateDispatchEvent(classScope);
            BindableHelper.generateHasEventListener(classScope);
            BindableHelper.generateRemoveEventListener(classScope);
            BindableHelper.generateWillTrigger(classScope);
        }

        if( classDefinition.needsStaticEventDispatcher(classScope.getProject()) )
        {
            cinitInsns.addAll(BindableHelper.generateBindingEventDispatcherInit(ctraits, true));
            BindableHelper.generateStaticEventDispatcherGetter(classStaticScope);
            //Add the implicit dependency for EventDispatcher. This also adds IEventDispatcher
            addBindableDependencies(false);
        }
    }

    protected void generateRequiredContingentDefinitions()
    {
        List<IDefinition> definitons = classDefinition.getContingentDefinitions();
        for (IDefinition definition : definitons)
        {
            if (!definition.isContingentNeeded(classScope.getProject()))
                continue;

            assert (definition instanceof VariableDefinition) : "The code generator only supports contingent variable definitions";
            
            final IDefinitionNode node = definition.getNode();
            declareVariable((VariableNode) node, (VariableDefinition)definition, definition.isStatic(),
                            definition instanceof IConstantDefinition, LexicalScope.noInitializer);
        }
    }

    private void addAnyEmbeddedAsset()
    {
        ICompilerProject project = classScope.getProject();
        if (!(project instanceof CompilerProject))
            return;

        EmbedData embedData = classDefinition.getEmbeddedAsset((CompilerProject)project, classScope.getProblems());
        if (embedData != null)
            classScope.getGlobalScope().getEmbeds().add(embedData);
    }

    /**
     * Declare a function.
     * TODO: static vs. instance.
     */
    @Override
    void declareFunction(FunctionNode func)
    {   
        func.parseFunctionBody(classScope.getProblems());

        final FunctionDefinition funcDef = func.getDefinition();

        final boolean is_constructor = func.isConstructor();
        
        ICompilerProject project = classScope.getProject();
        ASTUtil.processFunctionNode(func, project);
        boolean isBindable = false;
        if (funcDef instanceof AccessorDefinition)
        {
            boolean isClassBindable = BindableHelper.isClassCodeGenBindable(getClassDefinition());

            AccessorDefinition definitionWithBindable = null;
            boolean foundExplicitBindableTag = false;

            isBindable = BindableHelper.isCodeGenBindableMember(funcDef, isClassBindable);
            if (isBindable) {
                definitionWithBindable = (AccessorDefinition) funcDef;
            }
            foundExplicitBindableTag = BindableHelper.hasExplicitBindable(funcDef);

            AccessorDefinition otherDef =
                    ((AccessorDefinition)funcDef).resolveCorrespondingAccessor(classScope.getProject());
            if (!isBindable && !foundExplicitBindableTag)
            {
                if (otherDef != null && otherDef.getContainingScope().equals(funcDef.getContainingScope()))
                {
                    isBindable = BindableHelper.isCodeGenBindableMember(otherDef, isClassBindable);
                    if (isBindable) {
                        definitionWithBindable = otherDef;
                    }
                    foundExplicitBindableTag = BindableHelper.hasExplicitBindable(otherDef);
                    //if a) class is Bindable, and b)there is a [Bindable] tag, and c) there is also a [Bindable(eventName)] tag
                    //then we can retain the [Bindable] codegen and reset foundExplicitBinding to false (assumption, check in flex)
                    if (isClassBindable && isBindable && foundExplicitBindableTag) {
                        foundExplicitBindableTag = false; //we will ignore it and keep codegen as well
                    }
                }
            }
            if (isBindable && otherDef == null) {
                //no other definition
                isBindable = false;
                //warning if explicit [Bindable] ?
            }

            if (isBindable
                    && otherDef instanceof ISetterDefinition
                    && !(otherDef.getContainingScope().equals(funcDef.getContainingScope()))) {
                //if the setter is not defined in the same scope as this getter, that is an Error (same as Flex)
                isBindable = false;
                classScope.addProblem(new BindableGetterCodeGenProblem((IGetterDefinition) funcDef));
            }
            if (isClassBindable) {
                //add a warning if redundant local 'codegen' [Bindable] tag
                if (BindableHelper.hasCodegenBindable(definitionWithBindable)) {
                    classScope.addProblem(new RedundantBindableTagProblem(
                            BindableHelper.getProblemReportingNode(definitionWithBindable)
                    ));
                }
                if (isBindable) {
                    if (!foundExplicitBindableTag && BindableHelper.hasExplicitBindable(otherDef)) foundExplicitBindableTag = true;
                }
                if (foundExplicitBindableTag) {
                    //logic:
                    //when class is [Bindable],
                    //if either getter/setter member has at least one explicit Bindable tag (Bindable(event='something'))
                    //then no code-gen is applied,
                    //even if one of them also has a local 'codegen' [Bindable] tag
                    isBindable = false;
                }
            }
        }

        functionSemanticChecks(func);

        Name funcName = funcDef.getMName(classScope.getProject());
        Name bindableName = null;
        boolean wasOverride = false;
        //from here on we are only interested in the setter definitions, leave getters as-is
        isBindable = isBindable && funcDef instanceof ISetterDefinition;
        if (isBindable)
        {
            // move function into bindable namespace
            bindableName = BindableHelper.getBackingPropertyName(funcName, "_" + this.classDefinition.getQualifiedName());
            wasOverride = funcDef.isOverride();
            funcDef.unsetOverride();
        }
       
        //  Save the constructor function until
        //  we've seen all the instance variables
        //  that might need initialization.
        if ( is_constructor )
        {
            if (this.ctorFunction == null)
                this.ctorFunction = func;
            else
            {
                // If we already have a ctor, must be multiply defined. Ignore it and generate problem 
                String name = this.className.getBaseName();
                classScope.addProblem( new MultipleContructorDefinitionsProblem(func, name));
            }
        }
        else
        {
            LexicalScope ls = funcDef.isStatic()? classStaticScope: classScope;

            MethodInfo mi = classScope.getGenerator().generateFunction(func, ls, null, bindableName);
            
            if ( mi != null )
            {
                ITraitVisitor tv = ls.traitsVisitor.visitMethodTrait(functionTraitKind(func, TRAIT_Method), 
                        bindableName != null ? bindableName : funcName, 0, mi);
                
                if (funcName != null && bindableName == null)
                    classScope.getMethodBodySemanticChecker().checkFunctionForConflictingDefinitions(func, funcDef);

                if ( ! funcDef.isStatic() && bindableName == null)
                    if (funcDef.getNamespaceReference() instanceof NamespaceDefinition.IProtectedNamespaceDefinition)
                        this.iinfo.flags |= ABCConstants.CLASS_FLAG_protected;

                ls.processMetadata(tv, getAllMetaTags(funcDef));
                
                if ( func.hasModifier(ASModifier.FINAL))
                    tv.visitAttribute(Trait.TRAIT_FINAL, Boolean.TRUE);
                // don't set override if we've moved it to the bindable namespace
                if (!wasOverride && (func.hasModifier(ASModifier.OVERRIDE) || funcDef.isOverride()))
                    tv.visitAttribute(Trait.TRAIT_OVERRIDE, Boolean.TRUE);
                tv.visitEnd();
            }
        }
        if (isBindable)
        {   //only setters get modified in this case, getters remain the same as source code
            if (wasOverride)
                funcDef.setOverride();

            Name funcTypeName;
            TypeDefinitionBase typeDef = funcDef.resolveType(project);
            if ( SemanticUtils.isType(typeDef) )
                funcTypeName = typeDef.getMName(project);
            else
                funcTypeName = NAME_OBJECT;
            ASScope funcScope = (ASScope)funcDef.getContainingScope();
            DefinitionBase bindableSetter = func.buildBindableSetter(funcName.getBaseName(),
                    funcScope,
                    funcDef.getTypeReference());
            bindableSetter.setContainingScope(funcScope);
            LexicalScope ls = funcDef.isStatic()? classStaticScope: classScope;
            ls.generateBindableSetter(bindableSetter, funcName, bindableName,
                    funcTypeName, getAllMetaTags(funcDef));
        }
    }

    /**
     * This method performs the semantic analysis of a function declared in a class.
     * @param node  the FunctionNode to semantically analyze
     */
    void functionSemanticChecks(FunctionNode node)
    {
        verifyFunctionModifiers(node);

        FunctionDefinition func = node.getDefinition();

        verifyFunctionNamespace(node, func);

        Collection<ICompilerProblem> problems = classScope.getProblems();

        // code model has some peculiar ideas about what makes a function a constructor or not
        boolean looks_like_ctor = func.isConstructor();
        looks_like_ctor = looks_like_ctor || ( func.getBaseName() != null && this.className != null && func.getBaseName().equals(this.className.getBaseName()) );

        if (! looks_like_ctor && (func.getBaseName() != null))
        {
            SemanticUtils.checkScopedToDefaultNamespaceProblem(classScope, node, func, this.classDefinition.getQualifiedName());
        }
        if( looks_like_ctor )
        {
            // If a constructor has a namespace as part of it's declaration, it must be declared public.
            // It is ok to omit the namespace
            // We must check the AST, as CM treats all ctors as public no matter what the user typed in
            // so the FunctionDefinition will always be in the public namespace
            if( node.getActualNamespaceNode() != null )
            {
                if (classScope.getProject().getAllowPrivateConstructors())
                {
                    if (node.getActualNamespaceNode().getName() != IASKeywordConstants.PUBLIC
                            && !func.isPrivate())
                    {
                        problems.add(new ConstructorMustBePublicOrPrivateProblem(node.getActualNamespaceNode()));
                    }
                }
                else if (node.getActualNamespaceNode().getName() != IASKeywordConstants.PUBLIC || func.isPrivate())
                {
                    problems.add(new ConstructorMustBePublicProblem(node.getActualNamespaceNode()));
                }
            }

            // A constructor cannot be static
            if( func.isStatic() )
                problems.add(new ConstructorIsStaticProblem(node));

            // A constructor cannot declare a return type, other than void.
            IExpressionNode returnTypeExpression = node.getReturnTypeNode();
            if (returnTypeExpression != null)
            {
                // We cannot check whether node.resolveReturnType() returns the definition
                // for the void type, because  the return type of a constructor is considered
                // to be the class of the object being constructed, rather than void.
                // So instead we simply check whether the type annotation was void.
                boolean returnTypeIsVoid = false;
                if (returnTypeExpression instanceof ILanguageIdentifierNode)
                {
                    LanguageIdentifierKind kind = ((ILanguageIdentifierNode)returnTypeExpression).getKind();
                    if (kind == LanguageIdentifierKind.VOID)
                        returnTypeIsVoid = true;
                }
                if (!returnTypeIsVoid)
                {
                    ICompilerProblem problem = new ConstructorCannotHaveReturnTypeProblem(returnTypeExpression);
                    problems.add(problem);
                }
            }

            // Is it a getter or setter that appears to be the constructor?
            if( func instanceof IAccessorDefinition )
                problems.add(new ConstructorIsGetterSetterProblem(node.getNameExpressionNode()));
        }
        else if( !func.isStatic() )
        {
            //check conflicting language namespaces
            if (func.getNamespaceReference().isLanguageNamespace()) {
                IDefinitionSet others = func.getContainingScope().getLocalDefinitionSetByName(func.getBaseName());
                if (others.getSize() > 1) {
                    for (int i=0;i< others.getSize(); i++) {
                        IDefinition other = others.getDefinition(i);
                        if (other == func) continue;
                        //ignore code-gen Bindable setters if we are checking against the original setter
                        if ((func instanceof SyntheticBindableSetterDefinition && other instanceof ISetterDefinition)
                                || (other instanceof SyntheticBindableSetterDefinition && func instanceof ISetterDefinition)
                                && (func.getNode().equals(other.getNode()))
                            ) {
                            continue;
                        }
                        if (other.getNamespaceReference().isLanguageNamespace()) {
                            if (!SemanticUtils.isGetterSetterPair(func, other, classScope.getProject())) {
                                boolean issueProblem = true;
                                if (func instanceof IAccessorDefinition) {
                                    if (((IAccessorDefinition)func).isProblematic()) issueProblem = false;
                                    ((IAccessorDefinition)func).setIsProblematic(true);
                                    if (other instanceof IAccessorDefinition) {
                                        if (((IAccessorDefinition)other).isProblematic()) issueProblem = false;
                                        //don't explicitly set it as problematic here, that will happen when it undergoes its own checks
                                    }
                                }
                                if (issueProblem) {
                                    if (other instanceof IFunctionDefinition)
                                        problems.add(new DuplicateFunctionDefinitionProblem(func.getFunctionNode(), func.getBaseName()));
                                    else {
                                        //in legacy compiler, this is also reported as a DuplicateFuncitonDefinitionProblem, but this seems more correct:
                                        problems.add(new ConflictingDefinitionProblem(func.getFunctionNode(), func.getBaseName(), this.classDefinition.getQualifiedName(), true));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // We have to find the (potentially) overridden function whether we are an override or
            // not/
            FunctionDefinition override = func.resolveOverriddenFunction(classScope.getProject());
            if( func.isOverride() )
            {
                if( override == null )
                {
                    // Didn't find the function we are supposed to be overriding
                    problems.add(new OverrideNotFoundProblem(node.getNameExpressionNode()));
                }
                else
                {
                    if( !func.hasCompatibleSignature(override, classScope.getProject()) )
                    {
                        // Signatures didn't match
                        problems.add(new IncompatibleOverrideProblem(node.getNameExpressionNode()));
                    }
                    if( override.isFinal() )
                    {
                        // overriding final
                        problems.add(new OverrideFinalProblem(node.getNameExpressionNode()));
                    }
                }
            }
            else if( override != null)
            {
                if (func.getBaseName().equals("toString") &&
                        classDefinition.getContainedScope().hasAnyBindableDefinitions())
                    func.setOverride();
                else
                {
                    boolean ignore = false;
                    if (classScope.getProject().getAllowPrivateNameConflicts()) {
                        if (override.isPrivate()) ignore = true;
                    }
                    // found overriden function, but function not marked as override
                    if (!ignore) problems.add(new FunctionNotMarkedOverrideProblem(node.getNameExpressionNode()));
                }
            }
        }
    }
    
    /**
     * Check the class definition for various errors related to implemented interfaces,
     * such as making sure that all interface methods are implemented
     * @param cls  the class definition to check
     */
    void implementedInterfaceSemanticChecks(ClassDefinition cls)
    {
        Iterator<IInterfaceDefinition> it = cls.interfaceIterator(classScope.getProject());
        while( it.hasNext() )
        {
            IInterfaceDefinition interf = it.next();

            if( interf instanceof InterfaceDefinition )
            {
                ((InterfaceDefinition)interf).validateClassImplementsAllMethods(classScope.getProject(), cls, classScope.getProblems());
            }
        }
    }
    
    /**
     * Check the class definition for various errors related to extended
     * abstract classes, such as making sure that all abstract methods are
     * implemented
     * 
     * @param cls  the class definition to check
     */
    void implementedAbstractClassSemanticChecks(ClassDefinition cls)
    {
        if(!classScope.getProject().getAllowAbstractClasses())
        {
            //don't do these checks if abstract classes aren't enabled
            return;
        }
        if(cls.isAbstract())
        {
            // concrete classes don't need to implement abstract methods
            return;
        }
        Iterator<IClassDefinition> it = cls.classIterator(classScope.getProject(), false);
        while( it.hasNext() )
        {
            IClassDefinition otherClass = it.next();
            if(!otherClass.isAbstract())
            {
                //if a subclass is already concrete, then we don't need to check
                //this class for abstract methods too
                break;
            }

            if( otherClass instanceof ClassDefinition && otherClass.isAbstract())
            {
                ((ClassDefinition)otherClass).validateClassImplementsAllMethods(classScope.getProject(), cls, classScope.getProblems());
            }
        }
    }

    /**
    * Verify that abstract function has an appropriate namespace. If it doesn't,
    * print the appropriate error
    *
    * @param func is the function node do be analyzed
    * @param func_def is the definition for func
    */
    private void verifyFunctionNamespace(FunctionNode func, FunctionDefinition func_def)
    {
        if(!classScope.getProject().getAllowAbstractClasses())
        {
            //if abstract classes aren't allowed, other errors should take
            //precedence
            return;
        }

        if(!func_def.isAbstract())
        {
            return;
        }

        INamespaceDecorationNode nsNode = func.getActualNamespaceNode();
        
        // if we have no "actual" node, then there is no namespace in front of our function
        if (nsNode != null)
        {
            if (!INamespaceConstants.internal_.equals(nsNode.getName()))
            {
                INamespaceReference ns_ref = func_def.getNamespaceReference();
                INamespaceDefinition ns_def = ns_ref.resolveNamespaceReference(classScope.getProject());
                if (ns_def != null && ns_def instanceof INamespaceDefinition.IPrivateNamespaceDefinition)
                {
                    classScope.addProblem(new BadAccessAbstractMethodProblem(func));
                }
            }
        }
    }

    /**
     */
    protected void verifyFunctionModifiers(FunctionNode f)
    {
        IExpressionNode site = f.getNameExpressionNode();
        IDefinition functionDef = f.getDefinition();

        boolean isStatic = false; //used below
        ModifiersSet modifiersSet = f.getModifiers();
        if (modifiersSet != null)
        {
            isStatic = modifiersSet.hasModifier(ASModifier.STATIC);
            if(isStatic)
            {
                if( modifiersSet.hasModifier(ASModifier.FINAL) )
                {
                    classScope.addProblem(new FinalOutsideClassProblem(site) );
                }
                if( modifiersSet.hasModifier(ASModifier.OVERRIDE) )
                {
                    classScope.addProblem(new StaticAndOverrideProblem(site) );
                }
                if( modifiersSet.hasModifier(ASModifier.DYNAMIC) )
                {
                    classScope.addProblem(new DynamicNotOnClassProblem(site) );
                }
                if( modifiersSet.hasModifier(ASModifier.VIRTUAL) )
                {
                    classScope.addProblem(new VirtualOutsideClassProblem(site) );
                }
            }
            classScope.getMethodBodySemanticChecker().checkForDuplicateModifiers(f);
            // Functions in a class allow all modifiers
        }

        if (functionDef.isAbstract())
        {
            if (classScope.getProject().getAllowAbstractClasses())
            {
                if (!SemanticUtils.canBeAbstract(f, classScope.getProject()))
                {
                    classScope.addProblem(new AbstractOutsideClassProblem(site) );
                }
            }
            else
            {
                classScope.addProblem(new SyntaxProblem(site, IASKeywordConstants.ABSTRACT));
            }
        }
    }

    protected void verifyVariableModifiers(VariableNode v)
    {
        ModifiersSet modifiersSet = v.getModifiers();
        if (modifiersSet == null)
            return;

        ASModifier[] modifiers = modifiersSet.getAllModifiers();
        IExpressionNode site = v.getNameExpressionNode();
        for (ASModifier modifier : modifiers)
        {
            if( modifier == ASModifier.NATIVE )
            {
                classScope.addProblem(new NativeVariableProblem(site));
            }
            else if (modifier == ASModifier.DYNAMIC )
            {
                classScope.addProblem(new DynamicNotOnClassProblem(site));
            }
            else if( modifier == ASModifier.FINAL )
            {
                classScope.addProblem(new FinalOutsideClassProblem(site));
            }
            else if( modifier == ASModifier.OVERRIDE )
            {
                classScope.addProblem(new InvalidOverrideProblem(site));
            }
            else if( modifier == ASModifier.VIRTUAL )
            {
                classScope.addProblem(new VirtualOutsideClassProblem(site));
            }
            else if( modifier == ASModifier.ABSTRACT )
            {
                if(classScope.getProject().getAllowAbstractClasses())
                {
                    classScope.addProblem(new AbstractOutsideClassProblem(site));
                }
                else
                {
                    classScope.addProblem(new SyntaxProblem(site, IASKeywordConstants.ABSTRACT));
                }
            }
        }
        classScope.getMethodBodySemanticChecker().checkForDuplicateModifiers(v);
    }

    /**
     * Declare a variable.
     */
    @Override
    void declareVariable(VariableNode var)
    {
        verifyVariableModifiers(var);

        VariableDefinition varDef = (VariableDefinition)var.getDefinition();

        boolean is_static = var.hasModifier(ASModifier.STATIC);
        boolean is_const =  SemanticUtils.isConst(var, classScope.getProject());
        
        final ICompilerProject project = this.classScope.getProject();
        
        ICodeGenerator codeGenerator = classScope.getGenerator();
        IExpressionNode assignedValueNode = var.getAssignedValueNode(); 
        IConstantValue constantValue = codeGenerator.generateConstantValue(assignedValueNode, project);

        //  initializer is null if no constant value
        //  can be generated, and null is the correct value for "no value."
        Object initializer = constantValue != null ? constantValue.getValue() : null;
        
        // Reducing the constant value may have produced problems in the
        // LexicalScope used for constant reduction. Transfer them over
        // to the LexicalScope for this class.
        Collection<ICompilerProblem> problems = constantValue != null ? constantValue.getProblems() : null;
        if (problems != null)
            classScope.addProblems(problems);
        
        final MethodBodySemanticChecker checker = new MethodBodySemanticChecker(this.classScope);
        
        DefinitionBase varType = (DefinitionBase)varDef.resolveType(project);
        
        Object transformed_initializer = null;
        
        if ((initializer != null) && (varType != null))
        {
            transformed_initializer =
                checker.checkInitialValue(var, new Binding(null, varType.getMName(this.classScope.getProject()), varType), new PooledValue(initializer)).getValue();
        }
        else
        {
            transformed_initializer = initializer;
        }
        
        ITraitVisitor tv = declareVariable(var, varDef, is_static, is_const, transformed_initializer);
        if ( is_static )
            this.classStaticScope.processMetadata(tv, getAllMetaTags(varDef));
        else
            this.classScope.processMetadata(tv, getAllMetaTags(varDef));
        tv.visitEnd();
        
        //  Generate variable initializers and append them to the 
        //  proper initialization list.
        if ( transformed_initializer == null && var.getAssignedValueNode() != null )
        {
            // Emit initialization instructions for non-static vars.  Static var
            // instructions will be emitted during finishClassDefinition()
            if (is_static)
                staticVariableInitializers.add(var);
            else
                generateInstructions(var, false);
        }
        else
        {
            checker.checkClassField(var);
            //  Massive kludge -- grovel over chained variable decls and add them one by one
            for ( int i = 0; i < var.getChildCount(); i++ )
            {
                IASNode candidate = var.getChild(i);

                if ( candidate instanceof VariableNode )
                {
                    declareVariable((VariableNode)candidate);
                }
            }
        }
    }


    ITraitVisitor declareVariable(VariableNode varNode, DefinitionBase varDef, boolean is_static, boolean is_const, Object initializer)
    {
        final ICompilerProject project = this.classScope.getProject();
        Name var_name = varDef.getMName(project);

        TypeDefinitionBase typeDef = varDef.resolveType(project);
        Name var_type = typeDef != null ? typeDef.getMName(project) : null;

        int trait_kind = is_const? TRAIT_Const: TRAIT_Var;

        LexicalScope ls = is_static? this.classStaticScope: this.classScope;

        ls.declareVariableName(var_name);

        ITraitVisitor tv = ls.traitsVisitor.visitSlotTrait(trait_kind, var_name, ITraitsVisitor.RUNTIME_SLOT, var_type, initializer);

        if ( ! is_static )
            if (varDef.getNamespaceReference() instanceof NamespaceDefinition.IProtectedNamespaceDefinition)
                this.iinfo.flags |= ABCConstants.CLASS_FLAG_protected;
        
        SemanticUtils.checkScopedToDefaultNamespaceProblem(this.classScope, varNode, varDef, this.className.getBaseName());

        return tv;
    }

    /**
     * Process a namespace identifier.
     */
    @Override
    void processNamespaceIdentifierDirective(NamespaceIdentifierNode ns)
    {
        super.traverse(ns);
    }
    
    /**
     * Process an import directive.
     */
    @Override
    void processImportDirective(ImportNode imp)
    {
        // Run the BURM, but for the purpose of semantic checking not code generation.
        classScope.getGenerator().generateInstructions(imp, CmcEmitter.__statement_NT, this.classScope);
    }
    
    /**
     * Ignore modifier nodes that are in the AST, but processed
     * as attributes of the definition nodes.  Other loose
     * directives are processed as statements and added
     * to the class' static init method.
     */
    @Override
    void processDirective(IASNode n)
    {
        switch ( n.getNodeID() )
        {
            case StaticID:
            case FinalID:
            case OverrideID:
                break;

            case NamespaceID:
            {
                NamespaceNode ns = (NamespaceNode) n;

                if ( ns.hasModifier(ASModifier.STATIC) )
                {
                    this.classScope.addProblem(new StaticNamespaceDefinitionProblem(ns));
                }
                else
                {
		    //  This type of node won't generate instructions,
		    //  but it will add the namespace to the class'
		    //  static traits.
                    generateInstructions(n, GENERATE_STATIC_INITIALIZER);
                }
                break;
            }
            default:
            {
		//  Handle a static initialization statement:
                //  Generate instructions as required.
                generateInstructions(n, GENERATE_STATIC_INITIALIZER);
            }
        }
    }

    /**
     *  @see {@link #generateInstructions(IASNode node, boolean isStatic)}
     */
    private static final boolean GENERATE_STATIC_INITIALIZER = true;

    /**
     *  Generate instructions for field initializers or static initialization statements.
     *  @param node - the AST at the root of the statement.
     *  @param isStatic - true if the code should be generated in a static context.
     */
    protected void generateInstructions(IASNode node, final boolean isStatic)
    {
        //  Do we need to create new information for the class'
        //  static initialization method?  Note that this may
        //  be undone if the codgen fails or doesn't produce 
        //  any instructions.
        final boolean createNewCinit = isStatic && this.cinfo.cInit == null;

        if ( createNewCinit )
        {
            createCInitIfNeeded();
        }

        InstructionList cgResult = null;

        LexicalScope ls = isStatic? this.classStaticScope: this.classScope;

        ls.resetDebugInfo();
        cgResult = ls.getGenerator().generateInstructions( node, CmcEmitter.__statement_NT, ls );

        //  If nothing came back, revert any change made to the cinit information.
        if ( (cgResult == null || cgResult.isEmpty() ) && createNewCinit )
            {
                this.cinfo.cInit = null;
            this.classStaticScope.resetMethodInfo();
            this.classStaticScope.methodVisitor = null;
            this.classStaticScope.methodBodyVisitor = null;
            }

	//  Save the generated instructions, if present.
        if ( cgResult != null )
        {
            if ( isStatic )
                this.cinitInsns.addAll(cgResult);
            else
                this.iinitInsns.addAll(cgResult);
        }
    }
    
    /**
     * Create a class init method and associated structure if it does already
     * exist.
     */
    private void createCInitIfNeeded() 
    {
        if (this.cinfo.cInit == null)
        {
            //  Speculatively initialize the class' cinit 
            //  (static class initializer routine)'s data
            //  structures; the code generator may need to
            //  store information in them.
            this.cinfo.cInit = new MethodInfo();
            MethodBodyInfo cinit_info = new MethodBodyInfo();
            cinit_info.setMethodInfo(this.cinfo.cInit);
        
            this.classStaticScope.setMethodInfo(this.cinfo.cInit);
            this.classStaticScope.methodVisitor = emitter.visitMethod(this.cinfo.cInit);
            this.classStaticScope.methodVisitor.visit();
            this.classStaticScope.methodBodyVisitor = this.classStaticScope.methodVisitor.visitBody(cinit_info);
            this.classStaticScope.methodBodyVisitor.visit();
        }        
    }
    
}
