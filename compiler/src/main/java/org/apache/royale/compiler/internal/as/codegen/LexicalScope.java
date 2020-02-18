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

import static org.apache.royale.abc.ABCConstants.OP_findproperty;
import static org.apache.royale.abc.ABCConstants.OP_findpropstrict;
import static org.apache.royale.abc.ABCConstants.OP_getlex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Instruction;
import org.apache.royale.abc.semantics.InstructionFactory;
import org.apache.royale.abc.semantics.Metadata;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.abc.semantics.PooledValue;
import org.apache.royale.abc.semantics.Trait;
import org.apache.royale.abc.visitors.IABCVisitor;
import org.apache.royale.abc.visitors.IMetadataVisitor;
import org.apache.royale.abc.visitors.IMethodBodyVisitor;
import org.apache.royale.abc.visitors.IMethodVisitor;
import org.apache.royale.abc.visitors.ITraitVisitor;
import org.apache.royale.abc.visitors.ITraitsVisitor;
import org.apache.royale.abc.visitors.IVisitor;
import org.apache.royale.compiler.common.IMetaInfo;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IParameterDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.metadata.MetaTag;
import org.apache.royale.compiler.internal.definitions.metadata.MetaTagAttribute;
import org.apache.royale.compiler.internal.semantics.MethodBodySemanticChecker;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.BaseVariableNode;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode.LanguageIdentifierKind;

import com.google.common.collect.ImmutableList;

public class LexicalScope
{    
    /** The "*" type. */
    public static final Name anyType = null;

    /** Constant initializer not present. */
    public static final Object noInitializer = null;

    /**
     *  Manifest constant for "debug line number not known."
     */
    public static final int DEBUG_LINE_UNKNOWN = -1;

    private static final Name NAME_Array = new Name(IASLanguageConstants.Array);
    private static final Name NAME_arguments = new Name(IASLanguageConstants.arguments);

    private static final IMetaInfo[] EMPTY_META_INFO = new IMetaInfo[0];

    /**
     * Utility class to manage the allocation and merging of temps within
     * lexical scopes.
     */
    protected static class TempManager
    {
        /**
         *  Temporary registers allocated at this scope.
         */
        private final ArrayList<Binding> allocatedTemps;

        /**
         *  Allocated temporary registers that are free for re-use.
         */
        private final ArrayList<Binding> freeTemps;

        /**
         *  Index used to create unique names for temporary registers.
         *  @warn does not correspond to the temp's register number.
         */
        private int tempNum;

        /**
         * default constructor
         */
        protected TempManager()
        {
            allocatedTemps = new ArrayList<Binding>();
            freeTemps = new ArrayList<Binding>();
            tempNum = 0;
        }

        /**
         * Construct a TempManager which can be merged back into the supplied
         * tempManager.  This basically means that the temp numbering will never
         * overlap.
         * 
         * @param tempManager the TempManager which this tempManager may be merged into
         */
        protected TempManager(TempManager tempManager)
        {
            this();
            tempNum = tempManager.tempNum;
        }

        protected Binding allocateTemp(boolean reuse_free)
        {
            Binding result;

            if ( this.freeTemps.isEmpty() || !reuse_free )
            {
                result = new Binding(null, new Name("Temp #" + tempNum++), null);
                result.setIsLocal(true);
                addAllocatedTemp(result);
            }
            else
            {
                result = this.freeTemps.remove(0);
            }

            return result;
        }

        protected void addAllocatedTemp(Binding temp)
        {
            this.allocatedTemps.add(temp);
        }

        protected void addAllocatedTemps(ImmutableList<Binding> temps)
        {
            for (Binding temp : temps)
                this.allocatedTemps.add(temp);
        }

        protected ImmutableList<Binding> getAllocatedTemps()
        {
            return ImmutableList.copyOf(this.allocatedTemps);
        }

        protected void clearAllocatedTemps()
        {
            this.allocatedTemps.clear();
        }

        protected void releaseTemp(Binding temp)
        {
            assert( !freeTemps.contains(temp) ): "Temp " + temp + " is already freed.";
            freeTemps.add(temp);
        }

        protected int initializeTempRegisters(int base)
        {
            for (Binding temp: allocatedTemps)
            {
                temp.setLocalRegister(base++);
            }

            return base;
        }

        protected void mergeTemps(TempManager tempManagerToMerge)
        {
            // first release all the temps in the TempManager being merged
            for (Binding temp : tempManagerToMerge.allocatedTemps)
            {
                if (!tempManagerToMerge.freeTemps.contains(temp))
                    tempManagerToMerge.releaseTemp(temp);
            }

            tempNum += tempManagerToMerge.tempNum;
            freeTemps.addAll(tempManagerToMerge.freeTemps);
            allocatedTemps.addAll(tempManagerToMerge.allocatedTemps);
        }
    }

    /**
     * Back reference to the global lexical scope
     */
    private final GlobalLexicalScope globalLexicalScope;

    /**
     *  Local variables.
     */
    private final Map <String,Binding>localBindings = new LinkedHashMap<String,Binding>();

    /**
     * Inlined functions local variables.
     */
    private List<Binding> inlinedBindings = null;

    /**
     *  LexicalScope (if present) that lexically encloses this one.
     */
    private final LexicalScope enclosingFrame;

    /**
     *  The MethodInfo of this scope's anonymous function,
     *  or null if this is not an anonymous function scope.
     */
    private MethodInfo methodInfo = null;

    /**
     *  Set if the function needs "this" on the scope stack.
     */
    private boolean needsThis = true;

    /**
     *  Parameter types of this scope's explicit parameters,
     *  not including any "..." parameter.
     */
    private Vector<Name> paramTypes = null;

    /**
     *  Set if the function has a "..." parameter.
     */
    private boolean hasRestParam = false;

    /**
     *  All implicit or explicit parameters, including a "..." parameter,
     *  defined by this scope's method on activation.
     */
    private final ArrayList<Name> allParams = new ArrayList<Name>();

    /**
     *  This scope's ControlFlowContextManager.
     *  The LexicalScope manages the ControlFlowContextManager
     *  because the CFCM depends on the LexicalScope to manage
     *  its temporaries.
     */
    private ControlFlowContextManager flowMgr = null;

    /**
     * The Binding that represents the register that the activation object is stored in.
     * This is used to restore the activation object when necessary - at the beginning 
     * of a catch block for example. The value is null if the activation object is not 
     * stored in a local.
     */
    private Binding activationStorage = null;

    /**
     *  Hasnext2Wapper objects (hasnext2 mangement objects)
     *  in use by this scope's code.  These are kept here so
     *  that the hasnext2 instruction can be informed of its
     *  allocated temps at function wrapup time.
     */
    private final ArrayList<Hasnext2Wrapper> hasnexts = new ArrayList<Hasnext2Wrapper>();

    /**
     * The syntax tree node for which this lexical scope was created, which is
     * also the node that scopes the visibility of labels for goto statements.
     * This can be null.
     */
    private IASNode initialControlFlowRegionNode;

    /**
     * Next slot id to allocate for traits.  Start at 1, as
     * 0 denotes VM allocated id.
     */
    private int slotId = 1;

    /**
     *  Initialization instructions for hoisted definitions
     *  (such as functions) that appear in the body of this
     *  scope but should occur at the routine prologue.
     *  Contrast initInsns, which is set only on scopes
     *  that collect results from many routines or code
     *  snippets (e.g., class scopes, the global scope).
     */
    private InstructionList hoistedInitInstructions = null;

    /**
     *  Local scope of the function (if any) under compilation.
     */
    private IASScope localScope = null;

    /**
     *  Names explicitly declared at this scope.
     */
    private final Set<Name>declaredVariables = new HashSet<Name>();

    /**
     *  Debug info: current file name.
     */
    private String currentDebugFile = null;

    /**
     *  Debug info: current line number.
     */
    private int currentDebugLine = -1;

    /**
     *  The nesting state of a function.
     */
    public enum NestingState { NotNested, Nested };

    /** Nesting state of this LexicalScope. */
    private NestingState nestingState = NestingState.NotNested;

    /**
     * {@link List} of {@link IVisitor}'s whose
     * {@link IVisitor#visitEnd()} method calls have been deferred
     * until we are known to be on the main code generation thread.
     */
    private final List<IVisitor>deferredVisitEnds = new LinkedList<IVisitor>();


    /** Enum to keep track of what references/decls of 'arguments' we have seen so far. */
    private enum ArgumentsState
    {
        INITIAL,    // seen neither a ref or a decl
        REFERENCED, // seen a ref, but not decl
        DECLARED,   // seen a decl
    }

    /**
     * The current state of 'arguments' referenced and declared
     */
    private ArgumentsState argumentsState = ArgumentsState.INITIAL;

    /**
     * Variables with visibility to nested scopes
     * are defined here.
     */
    ITraitsVisitor traitsVisitor = null;

    /**
     *  If this LexicalScope is the outer scope of a method
     *  compilation, this is its method body visitor.
     *  @see #getMethodBodyVisitor()
     */
    IMethodBodyVisitor methodBodyVisitor = null;

    /**
     *  If this LexicalScope is the outer scope of a method
     *  compilation, this is its method visitor.
     */
    IMethodVisitor methodVisitor = null;

    /**
     *  The semantic checker for this compilation.
     */
    protected MethodBodySemanticChecker methodBodySemanticChecker = null;

    /**
     * The temporary register manager for this lexical scope
     */
    private final TempManager tempManager;

    /**
     * constructor which should only ever be called
     * by {@link GlobalLexicalScope}
     */
    protected LexicalScope()
    {
        assert (this instanceof GlobalLexicalScope) : "LexicalScope() should only ever be called by GlobalLexicalScope()";
        this.globalLexicalScope = (GlobalLexicalScope)this;
        // GlobalLexicalScope should never have an enclosingFrame
        this.enclosingFrame = null;
        this.tempManager = new TempManager();
    }

    /**
     * constructor which is called whenever pushing a new
     * lexical scope
     * 
     * @param enclosingFrame the parent frame
     */
    protected LexicalScope(LexicalScope enclosingFrame)
    {
        this(enclosingFrame, false);
    }

    /**
     * constructor which is called whenever pushing a new
     * lexical scope, and the temps are potentially going
     * to be shared with enclosing scope.
     * 
     * @param enclosingFrame the parent frame
     */
    protected LexicalScope(LexicalScope enclosingFrame, boolean mergableTempManager)
    {
        assert (!(this instanceof GlobalLexicalScope)) : "LexicalScope(LexicalScope) should never be called by GlobalLexicalScope()";
        this.enclosingFrame = enclosingFrame;
        this.globalLexicalScope = enclosingFrame.globalLexicalScope;
        this.nestingState = enclosingFrame.nestingState;
        if (mergableTempManager)
            this.tempManager = new TempManager(enclosingFrame.tempManager);
        else
            this.tempManager = new TempManager();            
    }

    /*
     * ** Variable Management -- Creation **
     */
    
    /**
     * Create a variable with potential visibility
     * to nested scopes.
     * 
     * @param var The variable binding
     */
    public void makeVariable(Binding var)
    {
        makeVariable(var, anyType, EMPTY_META_INFO);
    }

    /**
     * Create a variable with potential visibility
     * to nested scopes.
     * 
     * @param var The variable binding
     * @param var_type The Name of the type of the variable
     * @param meta_tags The metadata on the variable
     */
    public void makeVariable(Binding var, Name var_type, IMetaInfo[] meta_tags)
    {
        makeVariable(var, var_type, meta_tags, noInitializer);
    }

    /**
     * Create a variable with potential visibility
     * to nested scopes.
     * 
     * @param var The variable binding
     * @param var_type The Name of the type of the variable
     * @param meta_tags The metadata on the variable
     * @param initializer An initializer. null if no initializer. only consts can have an initializer
     */
    public void makeVariable(Binding var, Name var_type, IMetaInfo[] meta_tags, Object initializer)
    {
        makeVariable(var, var_type, meta_tags, initializer, VariableMutability.Default);
    }

    /**
     *  Mutability settings for a variable declaration.
     */
    public enum VariableMutability { Default, Variable, Constant };

    /**
     * Check to see if this name is declared as a variable within this scope
     * @param var_name A Name instance to check
     * @return true if the var_name is a declared variable.
     */
    public boolean hasDeclaredVariableName(Name var_name) {
        if ( var_name != null && this.declaredVariables.contains(var_name) )
        {
            return true;
        }
        //consider: do we also need to check non-reference equality?
        return false;
    }

    /**
     * Create a variable with potential visibility
     * to nested scopes.
     * 
     * @param var The variable binding
     * @param var_type The Name of the type of the variable
     * @param meta_tags The metadata on the variable
     * @param initializer An initializer. null if no initializer. only consts can have an initializer
     * @param mutability - the caller's desired mutability.
     */
    public void makeVariable(Binding var, Name var_type, IMetaInfo[] meta_tags, Object initializer, VariableMutability mutability)
    {
        assert (initializer == null || SemanticUtils.isConstDefinition(var.getDefinition())) : "only consts can have an initializer";

        Name var_name = var.getName();
        if ( var_name == null || this.declaredVariables.contains(var_name) )
        {
            return;
        }

        declareVariableName(var_name);

        IDefinition var_def = var.getDefinition();
        if ( this.localsInRegisters() )
        {
            //  The Binding will already be in localBindings if it was referenced
            //  before it was set; if not, add it.
            if ( !this.localBindings.containsKey(var_name.getBaseName()) )
            {
                if ( var.getNode() instanceof BaseVariableNode )
                {
                    IExpressionNode name_node = ((BaseVariableNode)var.getNode()).getNameExpressionNode();
                    Binding var_binding = getBinding(name_node, var_name, var_def);
                    this.localBindings.put(var_name.getBaseName(), var_binding);
                    var_binding.setIsLocal(true);
                }
                else
                {
                    //  Error case, synthesize a Binding.
                    Binding error_binding = new Binding(var.getNode(), new Name("error in binding"), null);
                    this.localBindings.put(var_name.getBaseName(), error_binding);
                }
            }
        }
        else
        {
            
            int trait_kind;
            if ( SemanticUtils.isConstDefinition(var_def) || mutability == VariableMutability.Constant )
                trait_kind = ABCConstants.TRAIT_Const;
            else
                trait_kind = ABCConstants.TRAIT_Var;

            assert(this.traitsVisitor != null): "No traits";
            ITraitVisitor tv = this.traitsVisitor.visitSlotTrait(
                    trait_kind,
                    ensureQName(var_name),
                    getSlotId(var),
                    var_type,
                    initializer
            );
            tv.visitStart();
            processMetadata(tv, meta_tags);
            this.deferredVisitEnds.add(tv);
        }
    }

    private int getSlotId(Binding binding)
    {
        // only need to allocate slot ids when there's an activation
        // in the room, as we need to be able to reference the id when codegening setslot
        // and debug ops.  otherwise just let the VM handle the allocation.
        if (!needsActivation())
            return ITraitsVisitor.RUNTIME_SLOT;

        binding.setSlotId(slotId++);

        return binding.getSlotId();
    }

    /**
     * Helper method to determine if a variable binding has already been referenced.  When called before
     * makeVariable, it can be used to determine if the var was referenced before it was initialized, which means
     * it will need hoisted init instructions to make sure it has the right initial value.
     * @param var_name   The name for the  variable
     *
     * @return              true if the binding for var_name is already in the localBindings table
     */
    public boolean needsHoistedInitInsns (Name var_name, boolean has_initializer)
    {
        if ( this.localsInRegisters() )
        {
            // If it's a re-decl of a parameter, then don't add hoisted init instructions
            if( allParams.contains(var_name) )
                return false;

            // If there is no explicit initializer, then we need to init the local
            // could do better here - if it's never ref'ed before being assigned to
            // might be able to omit the initializer too
            if( !has_initializer )
                return true;

            //  The Binding will already be in localBindings if it was referenced
            //  before it was set
            Binding b = this.localBindings.get(var_name.getBaseName());

            return b != null ;
        }
        return false;
    }
    /**
     *  Add a variable name to the set of known names.
     *  @param n - the Name to add.
     */
    public void declareVariableName(Name n)
    {
        checkForArgumentsDecl(n);
        this.declaredVariables.add(n);
    }


    /**
     * Create a bindable property. This will create a getter/setter that will do
     * the bindable magic, and a backing property that will store the actual
     * value.
     * <p>
     * This method must only be called on the thread that started code
     * generation of the syntax tree that contains the bindable variable. This
     * constraint is met today, because we do not generate code for classes in
     * parallel and bindable variables are always be class members.
     * 
     * @param metaTags The IMetaTagsNode representing the metadata nodes for
     * this definition. If it is null, this method will ask the varDef for the
     * metadata - some callers of this method will not have an MetaTagsNode in
     * the AST, and want the metadata from the definition
     */
    public void makeBindableVariable(Binding var, Name var_type, IMetaInfo[] metaTags)
    {
        Name var_name = var.getName();
        if ( var_name == null || this.declaredVariables.contains(var_name) )
        {
            return;
        }

        declareVariableName(var_name);

        Name backingPropertyName = BindableHelper.getBackingPropertyName(var_name);

        assert(this.traitsVisitor != null): "No traits";

        this.traitsVisitor.visitSlotTrait(
            ABCConstants.TRAIT_Var,
            ensureQName(backingPropertyName),
            getSlotId(var),
            var_type,
            noInitializer
        );
        IDefinition bindableVarDef = var.getDefinition();
        
        generateBindableGetter(bindableVarDef, var_name, backingPropertyName, var_type, metaTags);
        generateBindableSetter(bindableVarDef, var_name, backingPropertyName, var_type, metaTags);
    }
    
    public void generateBindableGetter(IDefinition bindableVarDef, Name var_name, Name backingPropertyName, Name var_type, IMetaInfo[] metaTags)
    {
        ITraitVisitor getterTv = BindableHelper.generateBindableGetter(this, var_name, backingPropertyName, var_type);

        IMetaTag gotoDefinitionMetaTag = MetaTag.createGotoDefinitionHelp(bindableVarDef, 
                bindableVarDef.getContainingFilePath(), 
                Integer.toString(bindableVarDef.getNameStart()), false);
        metaTags = MetaTag.addMetaTag(metaTags, gotoDefinitionMetaTag);
        
        // If we have an IMetaTagsNode use that, otherwise get the metadata from the definition
            processMetadata(getterTv, metaTags);

            if (bindableVarDef.isOverride())
                getterTv.visitAttribute(Trait.TRAIT_OVERRIDE, Boolean.TRUE);

        // We don't codegen classes in parallel right now,
        // so we know that we are on the main code generation thread
        // because bindable variables are always members of a class.
        // Since we know are on the main code generation thread we can immediately
        // call visitEnd here and the vistEnd calls in generateBindableSetter
        // are ok too.
        getterTv.visitEnd();
    }
    
    public void generateBindableSetter(IDefinition bindableVarDef, Name var_name, Name backingPropertyName, Name var_type, IMetaInfo[] metaTags)
    {

        ITraitVisitor setterTv = BindableHelper.generateBindableSetter(this, var_name, backingPropertyName, var_type, bindableVarDef);
        
        IMetaTag gotoDefinitionMetaTag = MetaTag.createGotoDefinitionHelp(bindableVarDef, 
                bindableVarDef.getContainingFilePath(), 
                Integer.toString(bindableVarDef.getNameStart()), false);
        metaTags = MetaTag.addMetaTag(metaTags, gotoDefinitionMetaTag);
        
        processMetadata(setterTv, metaTags);
        
        if (bindableVarDef.isOverride())
            setterTv.visitAttribute(Trait.TRAIT_OVERRIDE, Boolean.TRUE);

        setterTv.visitEnd();
    }

    /**
     *  Workaround namerezo's habit of returning a multiname
     *  for a local definition if the definition was referenced
     *  before it was declared.
     *  @param n - the name per name resolution.
     *  @return the input name if it was a QName, or a QName
     *    with the same base name and a more or less appropriate
     *    qualifier if it was not a QName.
     */
    private Name ensureQName(Name n)
    {
        Name result;

        if ( n != null ) 
        {
            if ( n.getKind() == ABCConstants.CONSTANT_Qname )
            {
                result = n;
            }
            else 
            {
                result = new Name(ABCConstants.CONSTANT_Qname, new Nsset(new Namespace(ABCConstants.CONSTANT_PackageNs)), n.getBaseName());
            }
        }
        else
        {
            result = null;
        }

        return result;
    }

    /**
     *  Declare and define a namespace.
     *  @param ns_binding - the namespace declaration's binding.
     *  @param ns_init - the namespace definition's initial AET Namespace.  May not be null.
     */
    public void makeNamespace(Binding ns_binding, Namespace ns_init)
    {
        Name ns_name = ns_binding.getName();

        //  TODO: Semantic analysis will issue a diagnostic.
        //  Repair locally by ignoring the second declaration.
        if ( this.declaredVariables.contains(ns_name))
            return;
        else
            declareVariableName(ns_name);


        assert ( ns_init != null );
        assert(this.traitsVisitor != null): "No traits";

        if ( this.localsInRegisters() )
        {
            this.localBindings.put(ns_name.getBaseName(), ns_binding);
            ns_binding.setIsLocal(true);
        }
        else
        {
            this.traitsVisitor.visitSlotTrait(ABCConstants.TRAIT_Const, ns_name, getSlotId(ns_binding), anyType, ns_init);
        }
    }

    /*
     * ** Variable Management -- Local Variables **
     */
    
    
    /**
     *  Declare an explicit parameter.
     *  @param def - the parameter's definition.
     *  @param param_type - the name of the parameter's type.
     */
    public void makeParameter(IDefinition def, Name param_type)
    {
        Name param_name = ((DefinitionBase)def).getMName(getProject());

        if (((IParameterDefinition)def).isRest())
            hasRestParam = true;
        else
            getParamTypes().add(param_type);
        
        allParams.add(param_name);

        Binding var = getBinding(def);
        makeVariable(var, param_type, EMPTY_META_INFO);
    }

    /**
     *  Declare an implicit parameter.
     *  The only implicit parameter in AS3 is <code>arguments</code>.
     *  @param param_name - the parameter's name.
     *  @param param_type - the name of the parameter's type.
     */
    private void makeImplicitParameter(Name param_name, Name param_type)
    {
        allParams.add(param_name);

        //  Add this parameter's name to the set of
        //  locally defined variables.
        Binding var = getBinding(null, param_name, null);
        makeVariable(var, param_type, EMPTY_META_INFO);
    }

    /**
     *  Add a default value to the currently compiling method.
     *  @see #makeParameter which is called first.
     *  @note Why is this not folded into makeParameter()?  Because
     *    null is a valid initial value.
     */
    public void addDefaultValue(PooledValue value)
    {
        if( value != null )
            this.methodInfo.addDefaultValue(value);
    }

    /**
     * @return true if "this" needed.
     */
    public boolean needsThis()
    {
        return this.needsThis;
    }

    /**
     *  @return the types of this scope's function parameters.
     */
    public Vector<Name> getParamTypes()
    {
        if ( this.paramTypes == null)
            this.paramTypes = new Vector<Name>();
        return this.paramTypes;
    }

    /**
     *  @return this scope's MethodInfo, or null if none present.
     */
    public MethodInfo getMethodInfo()
    {
        return this.methodInfo;
    }

    /**
     *  sets this scope's MethodInfo,
     */
    public void setMethodInfo(MethodInfo methodInfo)
    {
        assert (this.methodInfo == null) : "trying to set an already set methodInfo";
        this.methodInfo = methodInfo;

        //  Setting a MethodInfo means we're generating a method body,
        //  so we need a fresh MethodBodySemanticChecker.
        this.methodBodySemanticChecker = new MethodBodySemanticChecker(this);
    }

    /**
     *  Reset this scope's MethodInfo to null; called from
     *  a ClassDirectiveProcessor's static initialization 
     *  error recovery logic.
     */
    public void resetMethodInfo()
    {
        this.methodInfo = null;
        this.methodBodySemanticChecker = null;
    }

    /**
     *  Allocate a temporary register.  The temp is
     *  assumed to hold the "*" type.
     *  @return the temp register's Binding.
     *    A temp's Binding is used to hold
     *    the get/set/kill, etc., instructions
     *    that relate to it.
     */
    public Binding allocateTemp()
    {
        return allocateTemp(true);
    }

    /**
     * Allocate a temporary register.
     * @param reuse_free - re-use a free temp if set.
     * @return the temp's local number.
     */
    public Binding allocateTemp(boolean reuse_free)
    {
        return tempManager.allocateTemp(reuse_free);
    }

    /**
     * Release a temporary register.
     * @param temp - the temp's Binding.
     * @see #allocateTemp()
     */
    public void releaseTemp(Binding temp)
    {
        tempManager.releaseTemp(temp);
    }

    /**
     * Merge the temps from the specified lexical scope into this lexical scope.
     * 
     * @param scopeToMerge the scope containing the temps to merge
     */
    protected void mergeTemps(LexicalScope scopeToMerge)
    {
        tempManager.mergeTemps(scopeToMerge.tempManager);
    }

    /**
     * Add a collection of bindings to the collection of inlined bindings
     * in this lexical scope.
     * 
     * @param bindings the bindings to add
     */
    protected void addInlinedBindings(Collection<Binding> bindings)
    {
        if (inlinedBindings == null)
            inlinedBindings = new ArrayList<Binding>();

        inlinedBindings.addAll(bindings);
    }

    /**
     * Add a collection of hasnext wrappers to the collection of
     * hasnexts in this lexical scope.
     * 
     * @param nexts the hasnexts to add
     */
    protected void addHasNexts(Collection<Hasnext2Wrapper> nexts)
    {
        hasnexts.addAll(nexts);
    }

    /**
     * @return collection of Hasnext2Wrappers
     */
    protected Collection<Hasnext2Wrapper> getHasNexts()
    {
        return hasnexts;
    }

    /**
     * @return All the local bindings in the lexical scope.
     */
    protected Collection<Binding> getLocalBindings()
    {
        return localBindings.values();
    }

    /**
     *  Allocate a hasnext2 management object.
     *  @return the new hasnext2 management object.
     */
    Hasnext2Wrapper hasnext2()
    {
        Hasnext2Wrapper hasnext = new Hasnext2Wrapper();
        this.hasnexts.add(hasnext);

        hasnext.stem_temp  = allocateTemp(false);
        hasnext.index_temp = allocateTemp(false);

        return hasnext;
    }

    /**
     *  Hasnext2Wapper is a struct-like class
     *  that holds the temps and instruction
     *  that comprise a hasnext2 operation.
     */
    class Hasnext2Wrapper
    {
        Binding stem_temp, index_temp;
        Instruction instruction = InstructionFactory.getHasnext2Instruction();

        /**
         *  Release the temps from a hasnext2.
         */
        public void release()
        {
            releaseTemp(index_temp);
            releaseTemp(stem_temp);
        }
    }

    /**
     * @return the LexicalScope's corresponding ASScope.
     */
    public IASScope getLocalASScope()
    {
        return this.localScope;
    }

    /**
     *  Set this LexicalScope's corresponding ASScope.
     */
    public void setLocalASScope(IASScope scope)
    {
        assert this.localScope == null || this.localScope == scope : "Local scope already set.";
        this.localScope = scope;
    }

    /**
     *  @return true if the candidate definition is local to this LexicalScope's corresponding ASScope.
     *  @param candidate - the IDefinition under examination.
     */
    public boolean isLocalDefinition(IDefinition candidate)
    {
        return candidate != null && candidate.getContainingScope() != null && candidate.getContainingScope() == this.localScope;
    }

    /*
     * ** Name Management **
     */
    /**
     * Resolve a name, and return a Binding for it.
     * The Binding will include information such as
     * the multiname (or qname), and local register info
     * @param id  The IdentifierNode you need a Binding for
     * @return  A Binding
     */
    public Binding resolveName(IdentifierNode id)
    {
        ICompilerProject project = getProject();
        if(id instanceof ILanguageIdentifierNode)
        {
            ILanguageIdentifierNode lang_id = (ILanguageIdentifierNode)id;
            LanguageIdentifierKind kind = lang_id.getKind();

            if ( kind == LanguageIdentifierKind.THIS )
            {
                return getThisBinding(id);
            }
            else if ( kind == LanguageIdentifierKind.ANY_TYPE )
            {
                return new Binding(id, id.getMName(project), id.resolve(project));
            }
            else if ( kind == LanguageIdentifierKind.VOID )
            {
                return new Binding(id, id.getMName(project), id.resolve(project));
            }
            else
            {
                assert false: "Unhandled LanguageIdentifierKind " + kind;
            }
        }

        IDefinition def = id.resolve(project);
        Name name;
        if ( id.getName().length() == 0 )
            name = new Name(getGlobalScope().getSyntheticName("anonymous"));
        else
            name = id.getMName(project);

        if( SemanticUtils.isRefToClassBeingInited(id, def) && !insideInlineFunction())
        {
            // Return a slightly modified binding
            // this binding will have the name and node that created the binding,
            // but its local will be set to 0, so that getlocal0 will be used instead of a name lookup
            // this is because we are in code that may be run as part of the class cinit method, and the
            // class slot won't have been initialized yet, so a named lookup will fail.
            // class C
            // {
            //    static var a = C; //C must be codegen'ed as getlocal0 instead of findprop, getprop
            // }

            Binding b = getBinding(id, name, def);
            b.setIsLocal(true);
            b.setLocalRegister(0);
            return b;
        }
        else if ( name != null )
        {
            return getBinding(id, name, def);
        }
	else
	{
	    //  Error case, return trivial name
	    return new Binding(id, new Name(id.getName()), def);
	}
    }

    /**
     *  Get or create the Binding for an IDefinition.
     *  @param def - the IDefinition.
     *  @return said Binding.
     */
    public Binding getBinding(IDefinition def)
    {
        return getBinding(null, getNameFromDefinition(def), def);
    }

    /**
     *  Get or create the Binding for a Name and its associated IDefinition.
     *  @param name -  the name under consideration.
     *  @param def - the name's IDefinition.
     *  @return said Binding.
     */
    public Binding getBinding(IASNode node, Name name, IDefinition def)
    {
        if (name != null && isLocalDefinition(def) && this.localBindings.containsKey(name.getBaseName()) )
        {
            Binding b = localBindings.get(name.getBaseName());

            // return a new Binding to make sure it has the right Node.
            return new Binding(node, b);
        }
        else
        {
            Binding result = new Binding(node, name, def);

            if ( isLocalDefinition(def) )
            {
                this.localBindings.put(name.getBaseName(), result);

                if ( this.localsInRegisters() )
                    result.setIsLocal(true);
            }

            checkForArgumentsReference(result);

            return result;
        }
    }

    /**
     * Determine if this method needs to set the NEEDS_Arguments flag.  It only needs
     * to if arguments is referenced somewhere in the method, and arguments is not declared in the method.
     * @return
     */
    private boolean needsImplicitArguments ()
    {
        // We only need the arguments flag if arguments was referenced, but never declared.
        return argumentsState == ArgumentsState.REFERENCED;
    }


    /**
     * Check if the node and name passed in are declaration of a property named 'arguments'.
     * Necessary so we can generate NEEDS_Arguments flag on the MethodInfo if needed.
     * @param name  The Name of the reference
     */
    private void checkForArgumentsDecl (Name name)
    {
        if( name != null && IASLanguageConstants.arguments.equals(name.getBaseName()) )
        {
            switch(argumentsState)
            {
                case INITIAL:
                case REFERENCED:
                    argumentsState = ArgumentsState.DECLARED;
                    break;
                case DECLARED:
                    break;
            }
        }
    }

    /**
     * Check if the binding is a potential reference to the arguments object
     * Necessary so we can generate NEEDS_Arguments flag on the MethodInfo if needed.
     * @param b  The node that produced the reference
     */
    private void checkForArgumentsReference (Binding b)
    {
        if( SemanticUtils.isArgumentsReference(b) )
        {
            switch(argumentsState)
            {
                case INITIAL:
                    argumentsState = ArgumentsState.REFERENCED;
                    break;
                case REFERENCED:
                case DECLARED:
                    break;
            }
        }
    }

    /**
     * Get the local binding for the given name, if one exists
     * @param name  the name of the local
     * @return      the Binding for the local property, or null if there isn't one.
     */
    Binding getLocalBinding(Name name)
    {
        if ( name != null )
            return localBindings.get(name.getBaseName());
        else
            return null;
    }

    /**
     *  Extract a definition's name.
     */
    private Name getNameFromDefinition(IDefinition idef)
    {
        DefinitionBase def = (DefinitionBase)idef;
        return def.getMName(getProject());
    }

    /**
     *  Fetch the Binding that represents "this."
     *  @return said Binding.
     */
    protected Binding getThisBinding(IdentifierNode id)
    {
        Binding thisBinding = new Binding(id, null, null);
        thisBinding.setIsLocal(true);
        thisBinding.setLocalRegister(0);
        return thisBinding;
    }

    /**
     * Get the property for the specified binding.  Use this method rather
     * than using findprop directly, because inlining needs to do some magic
     *
     * @param binding Binding of the property to find
     * @param useStrict Use OP_findpropstrict
     * @return InstructionList
     */
    public InstructionList findProperty(Binding binding, boolean useStrict)
    {
        return findProperty(binding.getName(), binding.getDefinition(), useStrict);
    }

    /**
     * Get the property for the specified name.  Use this method rather
     * than using findprop directly, because inlining needs to do some magic
     *
     * @param name Name of the property to find
     * @param def Definition of the property to find
     * @param useStrict Use OP_findpropstrict
     * @return InstructionList
     */
    public InstructionList findProperty(Name name, IDefinition def, boolean useStrict)
    {
        InstructionList result = new InstructionList(1);

        if (useStrict)
            result.addInstruction(OP_findpropstrict, name);
        else
            result.addInstruction(OP_findproperty, name);

        return result;
    }

    /**
     * Get the property value for the specified binding. Use this method rather
     * than using getlex directly, because inlining needs to do some magic
     *
     * @param binding Binding of the property to find
     * @return InstructionList
     */
    public InstructionList getPropertyValue(Binding binding)
    {
        return getPropertyValue(binding.getName(), binding.getDefinition());
    }

    /**
     * Get the property value for the specified name. Use this method rather
     * than using getlex directly, because inlining needs to do some magic
     *
     * @param name Name of the property to find
     * @param def Definition of the property to find
     * @return InstructionList
     */
    public InstructionList getPropertyValue(Name name, IDefinition def)
    {
        InstructionList result = new InstructionList(1);
        result.addInstruction(OP_getlex, name);
        return result;
    }

    /**
     *  @return this scope's enclosing scope.
     */
    public LexicalScope getEnclosingFrame()
    {
        return enclosingFrame;
    }

    /**
     *  Finalize a method's declarations.
     *  @param has_body - true if the method
     *    has a non-empty body.
     *  @return an InstructionList containing
     *    the method's prologue instructions.
     */
    InstructionList finishMethodDeclaration(final boolean hasBody, String source_path)
    {
        InstructionList result = new InstructionList();

        // TODO: The scope stack can be optimized out if
        // no instruction (e.g., findprop) requires one.
        if ( this.needsThis )
            // && this.needsScopeStack() )
        {
            result.addInstruction(ABCConstants.OP_getlocal0);
            result.addInstruction(ABCConstants.OP_pushscope);
        }

        // Add a debugfile to start the method.
        if( source_path != null )
        {
            String encoded_filename = getGlobalScope().getEncodedDebugFile(source_path);
            result.addInstruction(ABCConstants.OP_debugfile, encoded_filename);
        }


        // TODO: Set this more intelligently;
        // it's only necessary if arguments is read but not written.
        if (needsImplicitArguments())
        {
            this.setNeedsArguments();
        }

        if ( this.needsArguments() )
        {
            makeImplicitParameter(NAME_arguments, NAME_Array);
        }

        if ( !isGlobalScope() )
        {
            // temp_register_count is the total number of temp registers
            // which need to be initialized.
            int temp_register_count = 0;
            if ( this.needsActivation() )
            {
                //  Create a new activation object, store it in a local (so it can be restored
                //  if needed later, like in a catch block), and push the activation object
                //  onto the scope stack.
                result.addInstruction(ABCConstants.OP_newactivation);

                if( this.activationStorage != null )
                {
                    result.addInstruction(ABCConstants.OP_dup);
                    result.addInstruction(this.activationStorage.setlocal());
                }

                result.addInstruction(ABCConstants.OP_pushscope);

                //  TODO: More fine-grained determination
                //  of which parameters need to live in the activation record.
                for ( int param_num = 0; param_num < this.allParams.size(); param_num++ )
                {
                    Name param_name = this.allParams.get(param_num);
                    result.addInstruction(ABCConstants.OP_findpropstrict, param_name);
                    //  Parameter numbering in the AVM is 1-based
                    //  since local 0 holds "this."
                    result.addInstruction(ABCConstants.OP_getlocal, param_num + 1);
                    result.addInstruction(ABCConstants.OP_setproperty, param_name);
                }

                temp_register_count = this.allParams.size() + 1;
            }
            else
            {
                // Find the bindings for the parameters
                // and give them their corresponding local number.
                // There is a complication, however. The same parameter name may occur more than
                // Once in the parameter list. In this case, ECMA says the last one wins.
                // So in the loop below will go from last parameter to first, and skip any 
                // that we have already seen
                
                Set<String> uniqueParamNames = new HashSet<String>();
                for (int param_num = this.allParams.size()-1; param_num >= 0; param_num--)
                {
                    Name param_name = this.allParams.get(param_num);
                    String param_base_name = param_name.getBaseName();
                    Binding param_binding = this.localBindings.get(param_base_name);
                    if (param_binding!=null && !uniqueParamNames.contains(param_base_name))
                    {
                        param_binding.setLocalRegister(param_num+1);
                        uniqueParamNames.add(param_base_name);
                    }
                }

                //  Now traverse the entire set of local bindings and
                //  give local numbers to those that weren't parameters.
                int local_num = this.allParams.size() + 1;
                for ( Binding local_binding : this.localBindings.values() )
                {
                    if ( ! local_binding.localNumberIsSet() )
                    {
                        local_binding.setLocalRegister(local_num++);
                    }
                }

                temp_register_count = local_num;
            }

            // Normally nothing should be called after initializeTempRegisters(), as bad things will happen
            // if another register is added.  The addDebugNamesToDefinitions() is a special case, as it needs
            // to have all registers set.
            this.initializeTempRegisters(temp_register_count);
        }

        // Need to call this after initializeTempRegisters to ensure that the registers are set in all cases.
        addDebugNamesToDefinitions(result);

        if ( this.hasHoistedInitInstructions() )
            result.addAll(this.getHoistedInitInstructions());

        return result;
    }

    /**
     *  Finish a class static initializer method.
     *  @param cinit_insns - initialization instructions.
     *  Classes may have ad-hoc static initialization code,
     *  so the content of this list is arbitrary.
     */
    public void finishClassStaticInitializer(InstructionList cinit_insns)
    {
        this.initializeTempRegisters(1);
        addDebugNamesToDefinitions(cinit_insns);
    }

    private void addDebugNamesToDefinitions(InstructionList result)
    {
        if (needsActivation())
        {
            for (int param_num = 0; param_num < allParams.size(); param_num++)
            {
                // create debug op codes for the params.  if we don't do this, builder won't
                // filter out the param names, and we get will show the formals as _argN and
                // then again in the activation record.
                addDebugNameToDefinition(allParams.get(param_num).getBaseName(), param_num, result);
            }

            // add the debug info for an activation record.  One debug op tells the VM to add the data for whole
            // activation record.  Need to set the name of the debug field to the funcName$0, as builder keys of
            // this when displaying the variables.
            if (activationStorage != null)
                addDebugNameToDefinition(methodInfo.getMethodName() + "$0", activationStorage.getLocalRegister() - 1, result);
        }
        else
        {
            for (Binding local_binding : localBindings.values())
            {
                if (SemanticUtils.isConstDefinition(local_binding.getDefinition()))
                    continue;

                // Need to -1 the index, as the local register starts at 1 to handle the this
                // but that's not included in the bindings list
                int index = local_binding.getLocalRegister() - 1;
                addDebugNameToDefinition(local_binding.getName().getBaseName(), index, result);
            }
        }
    }

    private void addDebugNameToDefinition(String name, int index, InstructionList result)
    {
        Object[] args = new Object[] { ABCConstants.DI_LOCAL, name, index };
        result.addInstruction(ABCConstants.OP_debug,  args);
    }

    /**
     *  Give all temporaries a register number.
     */
    public void initializeTempRegisters(int base)
    {
        base = tempManager.initializeTempRegisters(base);

        if (inlinedBindings != null)
        {
            for (Binding inlined_binding : inlinedBindings)
            {
                if (!inlined_binding.localNumberIsSet())
                    inlined_binding.setLocalRegister(base++);
            }
        }

        //  Now that all temps have been assigned
        //  to registers, give the hasnext2 insns
        //  their proper operands.
        for ( Hasnext2Wrapper hasnext: this.hasnexts )
        {
            hasnext.instruction.setTempRegisters(
                new Object[] 
                {
                    hasnext.stem_temp.getLocalRegister(), 
                    hasnext.index_temp.getLocalRegister()
                }
            );
        }
    }
    
    /*
     * **  Scope Management **
     */

    /**
     * Push a new lexical scope.
     * @return the new scope.
     */
    public LexicalScope pushFrame()
    {
        return new LexicalScope(this);
    }

    /**
     * Push a new lexical scope for a function being inlined.
     * @return the new scope.
     */
    public InlineFunctionLexicalScope pushInlineFunctionFrame(IASScope containingScope, boolean storeClassBinding, FunctionNode functionNode)
    {
        return new InlineFunctionLexicalScope(this, containingScope, storeClassBinding, functionNode);
    }

    /**
     * Test whether we are currently inside the scope of a function being inlined.
     * @return true if currently inside an inline function.
     */
    public boolean insideInlineFunction()
    {
        return false;
    }

    /**
     * Pop the current lexical scope.
     * @return the enclosing lexical scope.
     * @post this LexicalScope still remembers its enclosing LexicalScope.
     */
    public LexicalScope popFrame()
    {
        assert(!isGlobalScope()): "popping global scope";
        LexicalScope result = this.enclosingFrame;
        result.deferredVisitEnds.addAll(deferredVisitEnds);
        return result;
    }

    /**
     * @return true if this scope is the root of a scope chain.
     */
    public boolean isGlobalScope()
    {
        return false;
    }

    /**
     * @return the root of this scope chain.
     */
    public final GlobalLexicalScope getGlobalScope()
    {
        return globalLexicalScope;
    }

    /**
     * @return true if this scope's method
     * needs an activation record.
     */
    public boolean needsActivation()
    {
        boolean result = this.methodInfo != null && (this.methodInfo.getFlags() & ABCConstants.NEED_ACTIVATION) != 0;
        return result;
    }

    /**
     *  Turn on thie scope's method's NEED_ACTIVATION flag;
     *  this will cause the AVM to allocate an activation
     *  object when the method is called.
     */
    public void setNeedsActivation()
    {
        if ( this.methodInfo != null )
            this.methodInfo.setFlags(this.methodInfo.getFlags() | ABCConstants.NEED_ACTIVATION);
    }

    /**
     *  @return true if this scope stores its locals in registers.
     */
    private boolean localsInRegisters()
    {
        return !needsActivation() && localScope != null;
    }

    /**
     * @return true if this scope's method
     * needs an "arguments" array.
     */
    public boolean needsArguments()
    {
        return this.methodInfo != null && (this.methodInfo.getFlags() & ABCConstants.NEED_ARGUMENTS) != 0;
    }

    /**
     *  Turn on this scope's method's NEED_ARGUMENTS flag;
     *  this will cause the AVM to allocate an implicit
     *  arguments parameter when the method is called.
     */
    public void setNeedsArguments()
    {
        if ( this.methodInfo != null )
            this.methodInfo.setFlags(this.methodInfo.getFlags() | ABCConstants.NEED_ARGUMENTS);
    }

    /**
     *  Turn on this scope's NEED_REST flag.
     */
    public void setNeedsRest()
    {
        assert this.methodInfo != null : "methodInfo should not be null here";
        this.methodInfo.setFlags(this.methodInfo.getFlags() | ABCConstants.NEED_REST);
    }
    
    /**
     *  Turn on this scope's SETS_DXNS flag.
     */
    public void setSetsDxns()
    {
        if ( this.methodInfo != null )
        {
            this.methodInfo.setFlags(this.methodInfo.getFlags() | ABCConstants.SETS_DXNS);
        }
    }
          
    /*
     *  **  Control flow  **
     */

    /**
     *  Create a ControlFlowContextManager on demand.
     *  @return the ControlFlowContextManager associated
     *    with this scope.
     */
    public ControlFlowContextManager getFlowManager()
    {
        if ( null == flowMgr )
            flowMgr = new ControlFlowContextManager(this);
        return flowMgr;
    }

    /**
     *  Fetch the Binding that refers to a local reserved for 
     *  this scope's function's activation record.
     *  @return said Binding.  Created if not already present.
     */
    Binding getActivationStorage()
    {
        assert this.needsActivation(): "no activation storage present";

        if ( this.activationStorage == null )
        {
            this.activationStorage = new Binding(null, new Name("activation"),null);
            this.activationStorage.setIsLocal(true);
            this.tempManager.addAllocatedTemp(this.activationStorage);
        }

        return this.activationStorage;
    }

    /**
     *  @return true if any hoisted init instructions
     *  have been generated by the body of the routine.
     */
    boolean hasHoistedInitInstructions()
    {
        return this.hoistedInitInstructions != null;
    }

    /**
     *  Fetch the list used to hoist initialization
     *  instructions to the top of the routine.
     *  @return the hoisted init list; created on demand.
     *  @see #hasHoistedInitInstructions() which checks
     *    for hoisted instructions without creating them.
     */
    InstructionList getHoistedInitInstructions()
    {
        if ( this.hoistedInitInstructions == null )
            this.hoistedInitInstructions = new InstructionList();

        return this.hoistedInitInstructions;
    }

    /**
     *  @return the active IMethodBodyVisitor.
     */
    IMethodBodyVisitor getMethodBodyVisitor()
    {
        
        IMethodBodyVisitor result = this.methodBodyVisitor;

        if ( result == null && this.enclosingFrame != null )
        {
            result = this.enclosingFrame.getMethodBodyVisitor();
        }

        assert result != null;
        return result;
    }

    /**
     *  Set up this scope's data structures
     *  that support anonymous functions.
     */
    void declareAnonymousFunction()
    {
        declareNestedFunction();
        setFunctionName(getGlobalScope().getSyntheticName("anonymous"));
    }
    
    /**
     *  Set up this scope's data structures
     *  that support named function closures.
     */
    void declareFunctionObject(final String name)
    {
        assert name != null: "Name must be specified.";

        declareNestedFunction();
        setFunctionName(name);
    }

    void declareNestedFunction()
    {
        setMethodInfo(new MethodInfo());
        this.methodVisitor = getEmitter().visitMethod(this.methodInfo);
        this.methodVisitor.visit();

        MethodBodyInfo mbi = new MethodBodyInfo();
        mbi.setMethodInfo(this.methodInfo);

        this.methodBodyVisitor = this.methodVisitor.visitBody(mbi);
        this.methodBodyVisitor.visit();
        this.traitsVisitor = this.methodBodyVisitor.visitTraits();

        //  nested functions don't push "this" onto
        //  the scope stack.
        this.needsThis = false;

        //  Note that this function is nested.
        this.nestingState = NestingState.Nested;
    }

    void setFunctionName(String func_name)
    {
        assert(this.methodInfo != null && this.methodInfo.getMethodName() == null);
        this.methodInfo.setMethodName(func_name);
    }
    
    /**
     *  Add the method body to a nested function,
     *  and finish generating it in other respects.
     */
    void generateNestedFunction(InstructionList body)
    {
        assert this.methodInfo != null && this.methodInfo.getMethodName() != null:
            String.format(
                "this.methodInfo %s, this.methodInfo.getMethodName %s",
                this.methodInfo,
                this.methodInfo != null? this.methodInfo.getMethodName(): "n/a"
            )
        ;

        //  Finish generating the method:
        //  Set its parameter types
        this.methodInfo.setParamTypes(getParamTypes());
        
        //  Set its NEED_REST flag if there is a ... parameter.
        if (this.hasRestParam)
            setNeedsRest();
        
        //  Set its NEED_ARGUMENTS flag if the method uses
        //  the special 'arguments' implicit parameter.
        if ( !this.declaredVariables.contains(NAME_arguments) &&
             this.localBindings.containsKey(NAME_arguments.getBaseName()))
        {
            setNeedsArguments();
        }

        //  Set its body's instructions.
        if ( body != null )
            this.methodBodyVisitor.visitInstructionList(body);

        finishMethod();
    }
    
    /**
     * Arrange for the {@link IVisitor#visitEnd()} methods of
     * any visitors related to this lexical scope to be called.
     */
    private void finishMethod()
    {
        this.deferredVisitEnds.add(this.traitsVisitor);
        this.deferredVisitEnds.add(this.methodBodyVisitor);
        this.deferredVisitEnds.add(this.methodVisitor);
    }

    /**
     * Metadata management - works on IMetaInfo,
     * which is a common interface of IMetaTagNode and IMetaTag.
     */
    public void processMetadata(ITraitVisitor tv, IMetaInfo[] meta_infos)
    {
        if (meta_infos == null)
            return;
        if (meta_infos.length == 0)
            return;
        
            IMetadataVisitor mv = tv.visitMetadata(meta_infos.length);
        processMetadata(mv, meta_infos);

    }
    
    private static void processMetadata(IMetadataVisitor mv, IMetaInfo[] meta_infos)
    { 
            for ( IMetaInfo meta_info: meta_infos )
            {
                String name = meta_info.getTagName();
                
                IMetaTagAttribute[] attrs = meta_info.getAllAttributes();
                if (name.equals(BindableHelper.BINDABLE) && attrs.length == 0)
                {
                    attrs = new MetaTagAttribute[1];
                    attrs[0] = new MetaTagAttribute("event", BindableHelper.PROPERTY_CHANGE);
                }

                String[] keys   = new String[attrs.length];
                String[] values = new String[attrs.length];

                for ( int i = 0; i < attrs.length; i++ )
                {
                    keys[i]   = attrs[i].getKey();
                    values[i] = attrs[i].getValue();
                }
                
                Metadata metadata = new Metadata(name, keys, values);
                mv.visit(metadata);
            }
        }

    /*
     *  *************************
     *  **  Debug Information  **
     *  *************************
     */

    /**
     *  @return true if the new file name is valid and does
     *    not match the existing file name, in which case 
     *    the caller should emit a debugfile instruction and
     *    reset the file name.
     */
    public boolean emitFile(String candidate)
    {
        return candidate != null && (!getGlobalScope().getEncodedDebugFile(candidate).equals(currentDebugFile) );
    }

    /**
     *  @return true if the current file name is valid, and the new
     *    line number is valid and does not equal the existing line number,
     *    in which case the caller should emit a debugline instruction and
     *    reset the line number.
     */
    public boolean emitLine(final int candidate)
    {
        return candidate > 0 && currentDebugFile != null && candidate != currentDebugLine;
    }

    /**
     *  Set the debug info file name.
     *  @param file_name - the name to set.  May be null.
     */
    public void setDebugFile(String file_name)
    {
        this.currentDebugFile = getGlobalScope().getEncodedDebugFile(file_name);
        setDebugLine(DEBUG_LINE_UNKNOWN);
    }

    /**
     * @return the current debug info file name.  May be null
     */
    public String getDebugFile()
    {
        return currentDebugFile;
    }

    /**
     *  Set the debug info line number.
     *  @param line_num - the line number to set.
     */
    public void setDebugLine(final int line_num)
    {
        this.currentDebugLine = line_num;
    }

    /**
     *  Reset the debug info to initial values.
     */
    public void resetDebugInfo()
    {
        setDebugFile(null);
        setDebugLine(DEBUG_LINE_UNKNOWN);
    }

    public NestingState getNestingState()
    {
        return this.nestingState;
    }

    /*
     *  *************************
     *  **  Driver Interfaces  **
     *  *************************
     */

    /**
     * Sets the syntax tree node which establishes the initial scope for labels
     * visible to goto statements.  The passed in node should usually be the syntax
     * tree node which established this lexical scope.
     * <p>
     * This method should only be called once on each instance of this class.
     * 
     * @param node the syntax tree node which establishes the initial scope for labels
     * visible to goto statements.
     */
    void setInitialControlFlowRegionNode(IASNode node)
    {
        assert this.initialControlFlowRegionNode == null : "The syntax tree node should only be set once.";
        assert node != null : "The syntax tree node should never be set to null.";
        this.initialControlFlowRegionNode = node;
    }
    
    /**
     * Gets the syntax tree node which establishes the initial scope for labels
     * visible to goto statements.
     * 
     * @return the syntax tree node which establishes the initial scope for labels
     * visible to goto statements.
     */
    IASNode getInitialControlFlowRegionNode()
    {
        return this.initialControlFlowRegionNode;
    }
    
    /**
     * Add all the {@link IVisitor}s whose {@link IVisitor#visitEnd()} method calls
     * have been deferred to the specified {@link List} of {@link IVisitor}s.
     * @param visitEnds List of {@link IVisitor}s to add to.
     */
    void addVisitEndsToList(List<IVisitor> visitEnds)
    {
        visitEnds.addAll(this.deferredVisitEnds);
    }
    
    /**
     * Make any deferred {@link IVisitor#visitEnd()} calls. This method must only
     * be called from the thread that started code generation of the syntax tree
     * that contains this lexical scope.
     */
    void callVisitEnds()
    {
        for (IVisitor v : this.deferredVisitEnds)
            v.visitEnd();
    }

    /**
     *  Transfer information about local initializers from the
     *  scope in which they were declared (the class or instance scope)
     *  to this scope (a constructor).
     *  @pre the declaring scope must be the immediately enclosing scope.
     */
    public void transferInitializerData()
    {
        this.hasnexts.addAll(this.enclosingFrame.hasnexts);
        this.enclosingFrame.hasnexts.clear();

        this.tempManager.addAllocatedTemps(this.enclosingFrame.tempManager.getAllocatedTemps());
        this.enclosingFrame.tempManager.clearAllocatedTemps();
    }

    /*
     *  ***********************************************
     *  **  Methods to delegate to the global scope  **
     *  ***********************************************
     */

    /**
     *  @return this scope's compiler project.
     */
    public ICompilerProject getProject()
    {
        return getGlobalScope().getProject();
    }

    /**
     * @return  the code generator that this lexical scope is using.
     */
    public ICodeGenerator getGenerator()
    {
        return getGlobalScope().getGenerator();
    }

    /**
     *  @return the IABCVisitor backing this code generation phase.
     */
    IABCVisitor getEmitter()
    {
        return getGlobalScope().getEmitter();
    }

    /**
     * @return true if this scope is for an invisible compilation unit.
     * In this case, {@link IDefinition}'s for package/file classes,
     * interfaces, function's, var's, const's, and namespaces need to be
     * normalized before comparing them by identity in some semantic checks (
     * namely the type conversion checks ).
     * Also, in this case, we must do additional checking to determine
     * whether an import is valid.
     */
    public boolean getInInvisibleCompilationUnit()
    {
        return getGlobalScope().getInInvisibleCompilationUnit();
    }

    /**
     * @return any initial instructions for this code generation phase.
     */
    public InstructionList getInitInstructions()
    {
        return getGlobalScope().getInitInstructions();
    }

    /**
     *  @return the MethodBodySemanticChecker covering this code generation phase.
     */
    public MethodBodySemanticChecker getMethodBodySemanticChecker()
    {
        if ( this.methodBodySemanticChecker != null )
        {
            return this.methodBodySemanticChecker;
        }
        else if ( this.enclosingFrame != null )
        {
            return this.enclosingFrame.getMethodBodySemanticChecker();
        }
        else
        {
            assert false: "No MethodBodySemanticChecker found.";
            return null;
        }
    }

    /**
     * @return the problems covering this code generation phase.
     */
    public Collection<ICompilerProblem> getProblems()
    {
        return getGlobalScope().getProblems();
    }

    /**
     * Add a problem to this code generation phase.
     *
     * @param problem The {@link ICompilerProblem} to add.
     */
    public void addProblem(ICompilerProblem problem)
    {
        getProblems().add(problem);
    }
    
    /**
     * Add a collection of problems to this code generation phase.
     *
     * @param problems The collection of {@link ICompilerProblem} objects to add.
     */
    public void addProblems(Collection<ICompilerProblem> problems)
    {
        getProblems().addAll(problems);
    }
}
