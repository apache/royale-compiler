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

package org.apache.royale.compiler.internal.abc;

import static org.apache.royale.abc.ABCConstants.OP_dup;
import static org.apache.royale.abc.ABCConstants.OP_findpropstrict;
import static org.apache.royale.abc.ABCConstants.OP_getlocal0;
import static org.apache.royale.abc.ABCConstants.OP_getproperty;
import static org.apache.royale.abc.ABCConstants.OP_getscopeobject;
import static org.apache.royale.abc.ABCConstants.OP_initproperty;
import static org.apache.royale.abc.ABCConstants.OP_newclass;
import static org.apache.royale.abc.ABCConstants.OP_popscope;
import static org.apache.royale.abc.ABCConstants.OP_pushscope;
import static org.apache.royale.abc.ABCConstants.OP_returnvoid;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.ClassInfo;
import org.apache.royale.abc.semantics.InstanceInfo;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.PooledValue;
import org.apache.royale.abc.semantics.Trait;
import org.apache.royale.abc.visitors.IABCVisitor;
import org.apache.royale.abc.visitors.IClassVisitor;
import org.apache.royale.abc.visitors.IMethodBodyVisitor;
import org.apache.royale.abc.visitors.IMethodVisitor;
import org.apache.royale.abc.visitors.IScriptVisitor;
import org.apache.royale.abc.visitors.ITraitVisitor;
import org.apache.royale.abc.visitors.ITraitsVisitor;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.internal.as.codegen.LexicalScope;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * Utility class for writing a class definition into an IABCVisitor.
 * <p>
 * After construction:
 * <ul>
 * <li>Instance trait's can be added to the class by calling
 * {@link #getITraitsVisitor()} and visiting each trait to be added.</li>
 * <li>Class trait's can be added to the class by calling
 * {@link #getCTraitsVisitor()} and visiting each trait to be added.</li>
 * <li>Instance methods can be added to the class by calling
 * {@link #addITraitsMethod(Name, Collection, Name, Collection, boolean, boolean, boolean, InstructionList)}
 * .</li>
 * </ul>
 * <p>
 * After all the trait's and instructions have been added to the class, either
 * {@link #finishClass(InstructionList)} or {@link #finishScript()} must be
 * called.
 */
public class ClassGeneratorHelper
{
    /**
     * @return An instruction list with only one "returnvoid" instruction.
     */
    public static InstructionList returnVoid()
    {
        final InstructionList inst = new InstructionList(1);
        inst.addInstruction(OP_returnvoid);
        return inst;
    }
    
    /**
     * Constuctor that thunks to
     * {@link #ClassGeneratorHelper(ICompilerProject, IABCVisitor, Name, ClassDefinition, Collection, Collection, InstructionList, boolean)}.
     * 
     * @param project A compiler project.
     * @param visitor An ABC visitor.
     * @param className The ABC name of the new class.
     * @param baseClass The definition of the class being extended.
     */
    public ClassGeneratorHelper(ICompilerProject project, IABCVisitor visitor, Name className, ClassDefinition baseClass, InstructionList constructorInstructions)
    {
        this(project, visitor, className, baseClass, Collections.<Name> emptyList(), Collections.<Name> emptyList(), constructorInstructions, false);
    }

    /**
     * Constuctor that thunks to
     * {@link #ClassGeneratorHelper(ICompilerProject, IABCVisitor, Name, ClassDefinition, Collection, Collection, InstructionList, boolean)}.
     * 
     * @param project A compiler project.
     * @param visitor An ABC visitor.
     * @param className The ABC name of the new class.
     * @param baseClass The definition of the class being extended.
     * @param implementedInterfaces The ABC name of the interfaces being implemented.
     */
    public ClassGeneratorHelper(ICompilerProject project, IABCVisitor visitor, Name className, ClassDefinition baseClass, Collection<Name> implementedInterfaces, InstructionList constructorInstructions)
    {
        this(project, visitor, className, baseClass, implementedInterfaces, Collections.<Name> emptyList(), constructorInstructions, false);
    }

    /**
     * Generate an ABC class with constructor instructions.
     * 
     * @see #ClassGeneratorHelper(ICompilerProject, IABCVisitor, Name, ClassDefinition, Collection, Collection, InstructionList, InstructionList, boolean)
     */
    public ClassGeneratorHelper(ICompilerProject project, IABCVisitor visitor, Name className, ClassDefinition baseClass, Collection<Name> implementedInterfaces, Collection<Name> constructorParamTypes, InstructionList constructorInstructions, boolean hasProtectedMembers)
    {
        this(project, visitor, className, baseClass, implementedInterfaces, constructorParamTypes, constructorInstructions, returnVoid(), hasProtectedMembers);
    }
    
    /**
     * Constructor
     * 
     * @param project {@link ICompilerProject} project for which ABC is being
     * generated.
     * @param visitor {@link IABCVisitor} to write the new class into.
     * @param className {@link Name} of the class to generate
     * @param baseClass {@link ClassDefinition} for the base class the generated
     * class extends.
     * @param implementedInterfaces Collection of {@link Name}'s that at runtime
     * will refer to interfaces the generated class implement.
     * @param iinitParameterTypes Collection of {@link Name}'s that at runtime
     * will refer to the types of the constructor arguments.
     * @param iinitInstructions Instructions for the constructor.
     * @param cinitInstructions Instructions for the {@code cinit()} method.
     * @param hasProtectedMembers whether or not this class has protected members
     * There must be one {@code returnvoid} instruction.
     */
    public ClassGeneratorHelper(final ICompilerProject project,
                                final IABCVisitor visitor,
                                final Name className,
                                final ClassDefinition baseClass,
                                final Collection<Name> implementedInterfaces,
                                final Collection<Name> iinitParameterTypes,
                                final InstructionList iinitInstructions,
                                final InstructionList cinitInstructions,
                                boolean hasProtectedMembers)
    {
        if (iinitInstructions.canFallThrough())
            throw new IllegalArgumentException("Expected a 'returnvoid' instruction in the iinit() instructions.");
        
        if (cinitInstructions.canFallThrough())
            throw new IllegalArgumentException("Expected a 'returnvoid' instruction in the cinit() instructions.");

        this.project = project;
        this.className = className;
        this.baseClass = baseClass;
        cinfo = new ClassInfo();
        cinfo.cInit = new MethodInfo();
        IMethodVisitor cInitVisitor = visitor.visitMethod(cinfo.cInit);
        cInitVisitor.visit();
        MethodBodyInfo cInitMethodBodyInfo = new MethodBodyInfo();
        cInitMethodBodyInfo.setMethodInfo(cinfo.cInit);
        IMethodBodyVisitor cInitMethodBodyVisitor = cInitVisitor.visitBody(cInitMethodBodyInfo);
        cInitMethodBodyVisitor.visit();
        cInitMethodBodyVisitor.visitInstructionList(cinitInstructions);
        cInitMethodBodyVisitor.visitEnd();
        cInitVisitor.visitEnd();

        iinfo = new InstanceInfo();
        
        if(hasProtectedMembers) 
        {
            iinfo.flags |= ABCConstants.CONSTANT_ClassProtectedNs;
            iinfo.protectedNs = new Namespace(ABCConstants.CONSTANT_ProtectedNs, 
                    className.getSingleQualifier().getName() + ":" +className.getBaseName());
        }
        
        iinfo.interfaceNames = implementedInterfaces.toArray(new Name[implementedInterfaces.size()]);
        iinfo.name = className;
        iinfo.superName = baseClass.getMName(project);
        iinfo.iInit = new MethodInfo();
        iinfo.iInit.setParamTypes(new Vector<Name>(iinitParameterTypes));
        iTraitsInitMethodVisitor = visitor.visitMethod(iinfo.iInit);
        iTraitsInitMethodVisitor.visit();
        MethodBodyInfo iTraitsInitMethodBodyInfo = new MethodBodyInfo();
        iTraitsInitMethodBodyInfo.setMethodInfo(iinfo.iInit);
        iTraitsInitMethodBodyVisitor = iTraitsInitMethodVisitor.visitBody(iTraitsInitMethodBodyInfo);
        iTraitsInitMethodBodyVisitor.visit();
        iTraitsInitMethodBodyVisitor.visitInstructionList(iinitInstructions);

        this.visitor = visitor;
        classVisitor = visitor.visitClass(iinfo, cinfo);
        classVisitor.visit();
        itraits = classVisitor.visitInstanceTraits();
        itraits.visit();

        ctraits = classVisitor.visitClassTraits();
        ctraits.visit();
    }
    
    private final ICompilerProject project;
    private final Name className;
    private final ClassDefinition baseClass;
    private final ClassInfo cinfo;
    private final InstanceInfo iinfo;
    private final IABCVisitor visitor;
    private final IClassVisitor classVisitor;
    private final ITraitsVisitor itraits;
    private final ITraitsVisitor ctraits;
    private final IMethodVisitor iTraitsInitMethodVisitor;
    private final IMethodBodyVisitor iTraitsInitMethodBodyVisitor;
    
    /**
     * Adds a new script to the ABC with a trait for the class closure and an
     * init with instructions to create the class closure for the generated
     * class.
     * 
     * <p>
     * After this method is called no other methods on this class should be called.
     */
    public void finishScript()
    {
        IScriptVisitor sv = visitor.visitScript();
        sv.visit();
        ITraitsVisitor scriptTraits = sv.visitTraits();
        scriptTraits.visit();
        scriptTraits.visitClassTrait(ABCConstants.TRAIT_Class, className, 0, cinfo);
        scriptTraits.visitEnd();
        MethodInfo scriptInitMethodInfo = new MethodInfo();
        IMethodVisitor scriptInitMethodVisitor = visitor.visitMethod(scriptInitMethodInfo);
        scriptInitMethodVisitor.visit();
        MethodBodyInfo scriptInitMethodBodyInfo = new MethodBodyInfo();
        scriptInitMethodBodyInfo.setMethodInfo(scriptInitMethodInfo);
        IMethodBodyVisitor scriptInitMethodBodyVisitor = scriptInitMethodVisitor.visitBody(scriptInitMethodBodyInfo);
        scriptInitMethodBodyVisitor.visit();
        InstructionList scriptInitInstructions = new InstructionList();
        scriptInitInstructions.addInstruction(OP_getlocal0);
        scriptInitInstructions.addInstruction(OP_pushscope);
        finishClass(scriptInitInstructions);
        if (scriptInitInstructions.canFallThrough())
            scriptInitInstructions.addInstruction(OP_returnvoid);
        scriptInitMethodBodyVisitor.visitInstructionList(scriptInitInstructions);
        scriptInitMethodBodyVisitor.visitEnd();
        scriptInitMethodVisitor.visitEnd();
        sv.visitInit(scriptInitMethodInfo);
        sv.visitEnd();
    }
    
    /**
     * Generates the instructions to create the class closure and adds them to
     * the specified {@link InstructionList}.
     * 
     * @param scriptInitInstructions {@link InstructionList} to add the
     * instructions that create the class closure to.
     */
    public void finishClass(InstructionList scriptInitInstructions)
    {
        iTraitsInitMethodBodyVisitor.visitEnd();
        iTraitsInitMethodVisitor.visitEnd();
        
        LinkedList<Name> ancestorNames = new LinkedList<Name>();
        
        for (IClassDefinition ancestorIClass : baseClass.classIterable(project, true))
        {
            final ClassDefinition ancestorClass = (ClassDefinition) ancestorIClass;
            ancestorNames.addFirst(ancestorClass.getMName(project));
        }
        
        scriptInitInstructions.addInstruction(OP_getscopeobject, 0);
        
        //  Push ancestor classes onto the scope stack in
        //  order by superclass relationship; the immediate
        //  superclass is handled specially just below.
        
        assert ancestorNames.size() > 0;
        Name superclassName = ancestorNames.removeLast();
        for (Name ancestorName : ancestorNames)
        {
            scriptInitInstructions.addInstruction(OP_findpropstrict, ancestorName);
            scriptInitInstructions.addInstruction(OP_getproperty, ancestorName);
            scriptInitInstructions.addInstruction(OP_pushscope);
        }
        
        scriptInitInstructions.addInstruction(OP_findpropstrict, superclassName);
        scriptInitInstructions.addInstruction(OP_getproperty, superclassName);
        scriptInitInstructions.addInstruction(OP_dup);
        scriptInitInstructions.addInstruction(OP_pushscope);
     
        scriptInitInstructions.addInstruction(OP_newclass, cinfo);
        
        for ( int i = 0; i < ancestorNames.size(); i++ )
            scriptInitInstructions.addInstruction(OP_popscope);

        scriptInitInstructions.addInstruction(OP_popscope);
        
        scriptInitInstructions.addInstruction(OP_initproperty, className);
        
        ctraits.visitEnd();
        itraits.visitEnd();
        
        classVisitor.visitEnd();
        
    }
    
    /**
     * @return The {@link ITraitsVisitor} for the instance trait's of the generated class.
     */
    public ITraitsVisitor getITraitsVisitor()
    {
        return itraits;
    }
    
    /**
     * @return The {@link ITraitsVisitor} for the class trait's of the generated class.
     */
    public ITraitsVisitor getCTraitsVisitor()
    {
        return ctraits;
    }
    
    private ITraitVisitor addMethodToTraits(ITraitsVisitor traits, Name methodName,
            Collection<Name> parameterTypes,
            Name returnType,
            Collection<Object> defaultParameterValues,
            boolean needsRest,
            int functionTraitKind,
            InstructionList body)
    {
        MethodInfo mi = new MethodInfo();
        for (Object defaultParameterValue : defaultParameterValues)
            mi.addDefaultValue(new PooledValue(defaultParameterValue));
        mi.setParamTypes(new Vector<Name>(parameterTypes));
        mi.setReturnType(returnType);
        if (needsRest)
            mi.setFlags(mi.getFlags() | ABCConstants.METHOD_Needrest);
        FunctionGeneratorHelper.generateFunction(visitor, mi, body);
        return traits.visitMethodTrait(functionTraitKind, methodName, 0, mi);
    }
    
    /**
     * Utility method to add an instance method to the generated class.
     * 
     * @param methodName {@link Name} of the method to add.
     * @param parameterTypes Collection of {@link Name}'s of the parameters to
     * the method.
     * @param returnType {@link Name} of the return type of the method.
     * @param defaultParameterValues Collection of object's that can be
     * converted to ABC constants for the default values of parameters.
     * @param needsRest true if the method needs the rest parameter, false otherwise
     * @param isFinal true if the method is final, false otherwise
     * @param isOverride true if the method is an override of another method, false otherwise
     * @param body An {@link InstructionList} for the body of the method.
     */
    public void addITraitsMethod(Name methodName,
            Collection<Name> parameterTypes,
            Name returnType,
            Collection<Object> defaultParameterValues,
            boolean needsRest,
            boolean isFinal,
            boolean isOverride,
            InstructionList body)
    {
        addITraitsMethod(methodName, parameterTypes, returnType, defaultParameterValues,
                needsRest, isFinal, isOverride, body, ABCConstants.TRAIT_Method);
    }
    
    /**
     * Utility method to add an instance method to the generated class.
     * 
     * @param methodName {@link Name} of the method to add.
     * @param parameterTypes Collection of {@link Name}'s of the parameters to
     * the method.
     * @param returnType {@link Name} of the return type of the method.
     * @param defaultParameterValues Collection of object's that can be
     * converted to ABC constants for the default values of parameters.
     * @param needsRest true if the method needs the rest parameter, false otherwise
     * @param isFinal true if the method is final, false otherwise
     * @param isOverride true if the method is an override of another method, false otherwise
     * @param body An {@link InstructionList} for the body of the method.
     * @param functionKindTrait One of ABCConstants, TRAIT_Method, TRAIT_Getter, 
     * TRAIT_Setter.
     */
    public void addITraitsMethod(Name methodName,
            Collection<Name> parameterTypes,
            Name returnType,
            Collection<Object> defaultParameterValues,
            boolean needsRest,
            boolean isFinal,
            boolean isOverride,
            InstructionList body,
            int functionKindTrait)
    {
        ITraitVisitor traitVisitor = addMethodToTraits(itraits, methodName, parameterTypes, 
                returnType, defaultParameterValues, needsRest, functionKindTrait, body);
        traitVisitor.visitStart();
        if (isFinal)
            traitVisitor.visitAttribute(Trait.TRAIT_FINAL, true);
        if (isOverride)
            traitVisitor.visitAttribute(Trait.TRAIT_OVERRIDE, true);
        traitVisitor.visitEnd();
    }

    /**
     * Utility method to add a static method to the generated class.
     * 
     * @param methodName {@link Name} of the method to add.
     * @param parameterTypes Collection of {@link Name}'s of the parameters to
     * the method.
     * @param returnType {@link Name} of the return type of the method.
     * @param defaultParameterValues Collection of object's that can be
     * converted to ABC constants for the default values of parameters.
     * @param needsRest true if the method needs the rest parameter, false otherwise
     * @param body An {@link InstructionList} for the body of the method.
     */
    public void addCTraitsMethod(Name methodName,
            Collection<Name> parameterTypes,
            Name returnType,
            Collection<Object> defaultParameterValues,
            boolean needsRest,
            InstructionList body) 
    {
        ITraitVisitor traitVisitor = addMethodToTraits(ctraits, methodName, parameterTypes, returnType, defaultParameterValues, needsRest, ABCConstants.TRAIT_Method, body);
        traitVisitor.visitStart();
        traitVisitor.visitEnd();
    }

    /**
     * Utility method to add an instance getter to the generated class.
     * 
     * @param getterName {@link Name} of the method to add.
     * @param returnType {@link Name} of the return type of the method.
     * @param body An {@link InstructionList} for the body of the method.
     */
    public void addITraitsGetter(Name getterName, Name returnType, InstructionList body)
    {
        ITraitVisitor traitVisitor = addMethodToTraits(itraits, getterName, Collections.<Name>emptyList(), returnType, Collections.<Object>emptyList(), false, ABCConstants.TRAIT_Getter, body);
        traitVisitor.visitStart();
        traitVisitor.visitEnd();
    }
    
    /**
     * Utility method to add a static getter to the generated class.
     * 
     * @param getterName {@link Name} of the method to add.
     * @param returnType {@link Name} of the return type of the method.
     * @param body An {@link InstructionList} for the body of the method.
     */
    public void addCTraitsGetter(Name getterName, Name returnType, InstructionList body)
    {
        ITraitVisitor traitVisitor = addMethodToTraits(ctraits, getterName, Collections.<Name>emptyList(), returnType, Collections.<Object>emptyList(), false, ABCConstants.TRAIT_Getter, body);
        traitVisitor.visitStart();
        traitVisitor.visitEnd();
    }

    /**
     * Utility method to add a member variable to a class.
     * 
     * @param variableName {@link Name} of the member variable to add.
     */
    public void addMemberVariable(Name variableName, Name type)
    {
        ITraitVisitor traitVisitor = itraits.visitSlotTrait(ABCConstants.TRAIT_Var, variableName, ITraitsVisitor.RUNTIME_SLOT, type, LexicalScope.noInitializer);
        traitVisitor.visitStart();
        traitVisitor.visitEnd();
    }

    /**
     * @return protected namespace if it is asked for while creating this helper class
     */
    public Namespace getProtectedNamespace() {
        assert iinfo.protectedNs != null : "protected namespace is only available if you pass true for the hasProtectedMembers argument of this class' constructor";
        return iinfo.protectedNs;
    }
}
