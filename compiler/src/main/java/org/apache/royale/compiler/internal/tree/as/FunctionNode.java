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

package org.apache.royale.compiler.internal.tree.as;

import static com.google.common.base.Predicates.and;
import static com.google.common.collect.Collections2.filter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.royale.compiler.common.ASImportTarget;
import org.apache.royale.compiler.common.IImportTarget;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition.FunctionClassification;
import org.apache.royale.compiler.definitions.IParameterDefinition;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.ParameterDefinition;
import org.apache.royale.compiler.internal.definitions.VariableDefinition;
import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.internal.parsing.as.ConfigProcessor;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.ClosureScope;
import org.apache.royale.compiler.internal.scopes.FunctionScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.parts.FunctionContentsPart;
import org.apache.royale.compiler.internal.tree.as.parts.IFunctionContentsPart;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.problems.CanNotInsertSemicolonProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem2;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.ICommonClassNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.as.ITypeNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.workspaces.IWorkspace;

import com.google.common.base.Predicate;

/**
 * ActionScript parse tree node representing a function definition
 */
public class FunctionNode extends BaseTypedDefinitionNode implements IFunctionNode
{
    /**
     * Constructor.
     * <p>
     * Creates a {@code FunctionNode} from the "new" keyword token and the
     * function name node.
     * 
     * @param functionKeyword function keyword
     * @param nameNode node containing the name of this function
     */
    public FunctionNode(IASToken functionKeyword, IdentifierNode nameNode)
    {
        init(nameNode);
        
        if (functionKeyword != null)
            contentsPart.setKeywordNode(new KeywordNode(functionKeyword));
    }

    /**
     * Constructor.
     * <p>
     * Creates a new FunctionNode with a custom part for this functions
     * contents.
     * 
     * @param node the name of the node
     * @param part the {@link IFunctionContentsPart}
     */
    public FunctionNode(IdentifierNode node, IFunctionContentsPart part)
    {
        super.init(node);
        contentsPart = part;
    }

    /**
     * Contents of the function, including args, etc
     */
    protected IFunctionContentsPart contentsPart;
    
    /**
     * Does this method need a Definition added for "arguments". This will be
     * set to true during scope building if we encounter an IdentifierNode that
     * refers to "arguments".
     */
    boolean needsArguments = false;

    /**
     * The configuration processor used to re-parse the function body.
     */
    private ConfigProcessor configProcessor = null;
    
    /**
     * The open curly token of the function body.
     */
    private ASToken openT = null;
    
    /**
     * True if the function body is a deferred node.
     */
    private boolean isBodyDeferred = false;

    /**
     * Lock used when parsing the function body
     */
    private ReentrantLock deferredBodyParsingLock = new ReentrantLock();

    /**
     * A count of the number of calls to parse the function body.  The body
     * won't actually be thrown away until the count reaches zero again.
     */
    private int deferredBodyParsingReferenceCount = 0;

    /**
     * Cached function body text. If it's empty, the function body has to be
     * reloaded from the file using a seek-able reader, which might be slow for
     * large file.
     */
    private String functionBodyText;


    /**
     * Save the problems until later if we were parsed from somewhere we don't have a problems collection
     */
    private Collection<ICompilerProblem> parseProblems;

    /**
     * Indicates whether we've called rememberLocalFunction() on the parent
     * function yet (if a parent function even exists in the first place) -JT
     */
    private boolean isRemembered = false;
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.FunctionID;
    }
    
    @Override
    public int getSpanningStart()
    {
        return getNodeStartForTooling();
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        addDecorationChildren(fillInOffsets);
        addChildInOrder(contentsPart.getFunctionKeywordNode(), fillInOffsets);
        addChildInOrder(nameNode, fillInOffsets);
        addChildInOrder(contentsPart.getParametersNode(), fillInOffsets);
        addChildInOrder(typeNode, fillInOffsets);
        addChildInOrder(contentsPart.getContents(), fillInOffsets);
    }
    
    @Override
    public void normalize(boolean fillInOffsets)
    {
        super.normalize(fillInOffsets);
        contentsPart.optimize();
    }

    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        if (!isRemembered)
        {
            //previously, we remembered local functions only during
            //POPULATE_SCOPE. however, in MXML, functions get created multiple
            //times, and the second time around, analyze() is NOT called with
            //POPULATE_SCOPE. this causes our function to be forgotten.
            //better to check if we've remembered or not no matter which steps
            //were passed in. -JT
            final IFunctionNode parentFunctionNode = (IFunctionNode)getAncestorOfType(IFunctionNode.class);
            if (parentFunctionNode != null)
            {
                isRemembered = true;
                parentFunctionNode.rememberLocalFunction(this);
            }
        }

        if (set.contains(PostProcessStep.POPULATE_SCOPE))
        {
            FunctionDefinition definition = buildDefinition();
            setDefinition(definition);

            // if the parent is an anonymous function, then don't add the function definition to the scope
            // we have already added the anonymous function to the scope and this function definition does not
            // belong to the scope
            if (this.getParent() instanceof FunctionObjectNode)
            {
                String funcName = definition.getBaseName();  
                if (funcName.length() == 0)
                    scope.setAsContainingScopeOfAnonymousFunction(definition);
                else 
                {
                    // If the parent is an "anonymous" function with a name, then
                    // the name will have to be visibly within the function body, so it can 
                    // call itself recursively. So make a special closure scope
                    // Add a closure scope below the containing scope
                    ASScope closureScope = new ClosureScope(scope);
                    scope = closureScope;               // now build the function scope below this..
                    scope.addDefinition(definition);    // This sets the containing scope of the def
                }
            }
            else
                scope.addDefinition(definition);    // This sets the containing scope of the def

            ScopedBlockNode contents = contentsPart.getContents();
            if (contents != null)
            {
                ASScope localScope = new FunctionScope(scope, contents);
                definition.setContainedScope(localScope);
                scope = localScope;
            }
        }

        if (set.contains(PostProcessStep.RECONNECT_DEFINITIONS))
        {
            reconnectDef(scope);
            final FunctionDefinition functionDef = this.getDefinition();
            if (functionDef != null)
            {
                scope = functionDef.getContainedScope();
                ScopedBlockNode contents = contentsPart.getContents();
                // scope can be null for generated binding wrappers of
                // getters and setters
                if (contents != null && scope != null)
                {
                    contents.reconnectScope(scope);
                }
            }
        }

        // Recurse on the function parameters.
        ContainerNode parameters = contentsPart.getParametersNode();
        if (parameters != null)
        {
            parameters.analyze(set, scope, problems);
            if (set.contains(PostProcessStep.POPULATE_SCOPE))
            {
                // Set the parameters.
                IParameterNode[] argumentNodes = getParameterNodes();
                int n = argumentNodes.length;
                ParameterDefinition[] arguments = new ParameterDefinition[n];
                for (int i = 0; i < n; i++)
                {
                    if (argumentNodes[i] instanceof ParameterNode)
                        arguments[i] = (ParameterDefinition)((ParameterNode)argumentNodes[i]).getDefinition();
                }

                ((FunctionDefinition)this.definition).setParameters(arguments);
            }

        }
        // Recurse on the function block.
        BlockNode contents = contentsPart.getContents();
        if (contents != null)
            contents.analyze(set, scope, problems);

        if (set.contains(PostProcessStep.POPULATE_SCOPE))
            tryAddDefaultArgument();        
    }
    
    /*
     * For debugging only.
     * Builds a string such as <code>"doSomething(int, String):void"</code>
     * from the signature of the function being defined.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append(getName());

        sb.append('(');
        IVariableNode[] args = getParameterNodes();
        for (int i = 0; i < args.length; i++)
        {
            IVariableNode arg = args[i];
            sb.append(arg.getVariableType());
            if (i < args.length - 1)
                sb.append(", ");
        }
        sb.append(')');

        if (getReturnType().length() > 0)
            sb.append(":" + getReturnType());

        return true;
    }
    
    //
    // TreeNode overrides
    //

    @Override
    protected int getInitialChildCount()
    {
        return 4;
    }

    //
    // BaseDefinitionNode overrides overrides
    //

    @Override
    protected void init(ExpressionNodeBase idNode)
    {
        super.init(idNode);
        
        contentsPart = createContentsPart();
    }
    
    @Override
    public INamespaceDecorationNode getNamespaceNode()
    {
        INamespaceDecorationNode namespaceNode = super.getNamespaceNode();
        
        if (isConstructor())
        {
            if (namespaceNode != null
                    && (namespaceNode.getName().equals(INamespaceConstants.public_) || namespaceNode.getName().equals(INamespaceConstants.private_)))
            {
                // if the existing node is already public or private, return it
                return namespaceNode;
            }
            IASNode parentNode = getParent();
            if (parentNode instanceof IDefinitionNode)
            {
                IDefinitionNode defNode = (IDefinitionNode) parentNode;
                IMetaTagNode[] metaTagNodes = defNode.getMetaTags().getTagsByName(IMetaAttributeConstants.ATTRIBUTE_PRIVATE_CONSTRUCTOR);
                if (metaTagNodes != null && metaTagNodes.length > 0)
                {
                    // if the parent class has [RoyalePrivateConstructor]
                    // metadata, the constructor should be considered private
                    // and we should generate a fake namespace node
                    NamespaceIdentifierNode priv = new NamespaceIdentifierNode(INamespaceConstants.private_);
                    priv.span(-1, -1, -1, -1, -1, -1);
                    priv.setDecorationTarget(this);
                    return priv;
                }
            }
            // if there is no namespace node, the namespace defaults to public
            // and we'll generate a fake node
            NamespaceIdentifierNode pub = new NamespaceIdentifierNode(INamespaceConstants.public_);
            pub.span(-1, -1, -1, -1, -1, -1);
            pub.setDecorationTarget(this);
            return pub;
        }
        
        return namespaceNode;
    }
    
    @Override
    public String getNamespace()
    {
        INamespaceDecorationNode ns = getNamespaceNode();
        if (ns != null)
        {
            String nameString = ns.getName();
            // If public or private, just return it.
            if (nameString.equals(INamespaceConstants.public_) || nameString.equals(INamespaceConstants.private_))
                return nameString;

            // Otherwise, check to see if we are a constructor and always return
            // public
            if (isConstructor())
                return INamespaceConstants.public_;
            
            // Just return the value.
            return nameString;
        }
        
        // If we are null, make sure to check if we are a constructor.
        if (isConstructor())
            return INamespaceConstants.public_;
        
        return null;
    }
    
    @Override
    public boolean hasNamespace(String namespace)
    {
        if (isConstructor())
            return namespace.compareTo(INamespaceConstants.public_) == 0;

        return super.hasNamespace(namespace);
    }
    
    @Override
    public FunctionDefinition getDefinition()
    {
        return (FunctionDefinition)super.getDefinition();
    }
    
    @Override
    protected void setDefinition(IDefinition def)
    {
        assert def instanceof FunctionDefinition;
        super.setDefinition(def);
    }

    //
    // IFunctionNode implementations
    //
    
    @Override
    public boolean isImplicit()
    {
        if (getParent() != null)
        {
            if (getParent().getParent() instanceof ClassNode)
            {
                ClassNode containingClass = (ClassNode)getParent().getParent();
                if (containingClass.getDefaultConstructorNode() == this)
                    return true;
            }
            else if (getParent().getParent() instanceof InterfaceNode)
            {
                InterfaceNode containingInterface = (InterfaceNode)getParent().getParent();
                if (containingInterface.getCastFunctionNode() == this)
                    return true;
            }
        }
        return false;
    }
    
    @Override
    public String getQualifiedName()
    {
        String qualifiedName = null;
        if (isPackageLevelFunction())
        {
            IImportTarget importTarget = ASImportTarget.buildImportFromPackageName(getWorkspace(), getPackageName());
            qualifiedName = importTarget.getQualifiedName(getName());
        }
        if (qualifiedName == null)
            qualifiedName = getName();
        return qualifiedName;
    }

    @Override
    public String getShortName()
    {
        return getName();
    }
    
    @Override
    public final ScopedBlockNode getScopedNode()
    {
        return contentsPart.getContents();
    }
    
    @Override
    public FunctionClassification getFunctionClassification()
    {
        IScopedNode scopedNode = getScopeNode();
        IASNode node = scopedNode;
        
        if (node instanceof ICommonClassNode || node.getParent() instanceof ICommonClassNode)
            return FunctionClassification.CLASS_MEMBER;
        
        if (node.getParent() instanceof InterfaceNode)
            return FunctionClassification.INTERFACE_MEMBER;
        
        if (node.getParent() instanceof PackageNode)
            return FunctionClassification.PACKAGE_MEMBER;
        
        if (node instanceof FileNode)// this is an include
            return FunctionClassification.FILE_MEMBER;
        
        return FunctionClassification.LOCAL;
    }

    @Override
    public boolean isGetter()
    {
        return this instanceof GetterNode;
    }

    @Override
    public boolean isSetter()
    {
        return this instanceof SetterNode;
    }
    
    @Override
    public boolean isConstructor()
    {
        String name = getName();
        String returnType = getReturnType();
        
        // Allow constructors that have a (bogus) return type
        if (!returnType.equals("") &&
            !returnType.equals(name) &&
            !isAnyType() &&
            !isVoidType())
        {
            return false;
        }
        
        if (getParent() != null &&
            getParent().getParent() != null && 
            (getParent().getParent() instanceof ClassNode ||
             getParent().getParent() instanceof InterfaceNode))
        {
            if (name.equals(((IDefinitionNode) getParent().getParent()).getShortName()))
                return true;
        }
        
        return false;
    }

    @Override
    public boolean isCastFunction()
    {
        String name = getName();
        String returnType = getReturnType();
        
        if (!returnType.equals("") && !returnType.equals(name))
            return false;
        
        if (getParent() != null &&
            getParent().getParent() != null &&
            getParent().getParent() instanceof ITypeNode)
        {
            if (name.equals(((ITypeNode)getParent().getParent()).getShortName()))
                return true;
        }
        
        return false;
    }
    
    @Override
    public ContainerNode getParametersContainerNode()
    {
        return contentsPart.getParametersNode();
    }

    @Override
    public IParameterNode[] getParameterNodes()
    {
        IParameterNode[] variables = {};
        
        ContainerNode arguments = contentsPart.getParametersNode();
        
        if (arguments != null)
        {
            int argumentscount = arguments.getChildCount();
            variables = new IParameterNode[argumentscount];
            for (int i = 0; i < argumentscount; i++)
            {
                IASNode argument = arguments.getChild(i);
                if (argument instanceof IParameterNode)
                    variables[i] = (IParameterNode)argument;
            }
        }
        
        return variables;
    }

    @Override
    public IExpressionNode getReturnTypeNode()
    {
        return getTypeNode();
    }
    
    @Override
    public String getReturnType()
    {
        return getTypeName();
    }
    
    @Override
    public boolean hasBody()
    {
        ScopedBlockNode sbn = getScopedNode();
        
        return sbn.getChildCount() > 0 ||
               sbn.getContainerType() != IContainerNode.ContainerType.SYNTHESIZED;
    }
    
    //
    // Other methods
    //

    protected IFunctionContentsPart createContentsPart()
    {
        return new FunctionContentsPart();
    }

    private void tryAddDefaultArgument()
    {
        FunctionDefinition def = getDefinition();
        ASScope funcScope = def.getContainedScope();

        if (needsArguments && funcScope.getLocalDefinitionSetByName(IASLanguageConstants.arguments) == null)
        {
            // Add the arguments Array to the function scope
            // only do this if there is not already a local property, or parameter that is named arguments
            // and something in the function body references arguments - this should avoid creating the 
            // definition when it's not needed.
            VariableDefinition argumentsDef = new VariableDefinition(IASLanguageConstants.arguments);
            argumentsDef.setNamespaceReference(NamespaceDefinition.getDefaultNamespaceDefinition(funcScope));
            argumentsDef.setTypeReference(ReferenceFactory.builtinReference(IASLanguageConstants.BuiltinType.ARRAY));

            argumentsDef.setImplicit();

            funcScope.addDefinition(argumentsDef);
        }
    }

    private void setConstructorIfNeeded(FunctionDefinition funcDef)
    {
        if (isConstructor())
        {
            IASNode parentParent = getParent().getParent();
            if( parentParent instanceof ClassNode)
            {
                ClassNode classNode = (ClassNode)parentParent;
                if (classNode.getConstructorNode() == null)
                {
                    if (nameNode instanceof IdentifierNode)
                    {
                        ((IdentifierNode)nameNode).setReferenceValue(classNode.getDefinition());
                        classNode.constructorNode = this;
                    }
                }
            }
            funcDef.setNamespaceReference(NamespaceDefinition.getCodeModelImplicitDefinitionNamespace());
        }
    }

    FunctionDefinition buildDefinition()
    {
        String definitionName = getName();
      //  System.out.println("buildDefinition: " + definitionName);
        FunctionDefinition definition = createFunctionDefinition(definitionName);
        definition.setNode(this);

        fillInNamespaceAndModifiers(definition);
        fillInMetadata(definition);

        // Set the return type. If a type annotation doesn't appear in the source,
        // the return type in the definition will be "".
        IReference returnType = typeNode != null ? typeNode.computeTypeReference() : null;
        definition.setReturnTypeReference(returnType);

        definition.setTypeReference(ReferenceFactory.builtinReference(IASLanguageConstants.BuiltinType.FUNCTION));
        setConstructorIfNeeded(definition);
        return definition;
    }

    /**
     * Method to create the correct Definition class - needed so we can create
     * different Definition types for getters, setters, and plain old functions
     * 
     * @return A Definition object to represent this function
     */
    protected FunctionDefinition createFunctionDefinition(String name)
    {
        return new FunctionDefinition(name);
    }

    /**
     * Get the function keyword
     * 
     * @return node containing the function keyword
     */
    public KeywordNode getFunctionKeywordNode()
    {
        return contentsPart.getFunctionKeywordNode();
    }

    /**
     * Determine whether this is a package-level function (i.e. a function
     * defined in a package, as opposed to a class or some other scope)
     * 
     * @return true if this is a package-level function
     */
    public boolean isPackageLevelFunction()
    {
        // regular package-level function
        IASNode parent = getParent();
        IASNode parent2 = parent.getParent();
       
        if (parent instanceof BlockNode && parent2 instanceof PackageNode)
            return true;
        
        // constructor
        if (parent2 != null)
        {
            IASNode parent3 = parent2.getParent();
            
            if (isConstructor() &&
                parent2 instanceof ClassNode &&
                parent3 instanceof BlockNode &&
                parent3.getParent() instanceof PackageNode)
            {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Get the real namespace node, or null if there isn't one. Used by
     * semantics. This differs from getNamespaceNode above, as it will not
     * construct an implicit public namespace if one is missing
     */
    public INamespaceDecorationNode getActualNamespaceNode()
    {
        return super.getNamespaceNode();
    }

    /**
     * Is this a constructor of the specified class (assumes that this function
     * definition is actually located in the body of the specified class)
     */
    public boolean isConstructorOf(ClassNode classNode)
    {
        return this.equals(classNode.getConstructorNode());
    }

    /**
     * Get any saved parsing problems from lazily parsing the function body.
     * This list will be nulled out after this method is called,
     * so we don't start leaking parser problems.
     * 
     * @return Collection of problems encountered while parsing the function
     *  or an empty list if there were none.
     *
     */
    public Collection<ICompilerProblem> getParsingProblems()
    {
        if (parseProblems != null)
        {
            Collection<ICompilerProblem> problems = parseProblems;
            parseProblems = null;
            return problems;
        }
        
        return Collections.emptyList();
    }
    /**
     * Build AST for the function body from the buffered function body text.
     * <p>
     * Make sure {@link PostProcessStep#POPULATE_SCOPE} has been applied to the
     * containing {@code FileNode} This method always populate scopes of the
     * rebuilt function node. If the scopes for the containing nodes weren't
     * initialized, the rebuilt scopes can attach itself to it's parent.
     */
    public final void parseFunctionBody(final Collection<ICompilerProblem> problems)
    {
        if (!isBodyDeferred)
            return;

        deferredBodyParsingLock.lock();
        try
        {
            deferredBodyParsingReferenceCount++;

            assert problems != null : "Problems collection can't be null";

            final ScopedBlockNode contents = contentsPart.getContents();
            assert contents != null : "Function body node can't be null: function " + getName();

            // Only re-parse if the function body text is not empty, and the 
            // function body node doesn't have any children.
            if (contents.getChildCount() > 0)
                return;
            assert deferredBodyParsingReferenceCount == 1;

            assert openT != null : "Expected '{' token.";

            final String sourcePath = getSourcePath();
            assert sourcePath != null && !sourcePath.isEmpty() : "Source path not set.";

            final FileNode fileNode = (FileNode)getAncestorOfType(FileNode.class);
            assert fileNode != null : "FileNode not found: function " + getName();
            final IWorkspace workspace = fileNode.getWorkspace();

            ASFileScope fileScope = fileNode.getFileScope();
            fileScope.addParsedFunctionBodies(this);

            try
            {
                // select function body source
                final Reader sourceReader;
                if (functionBodyText != null)
                {
                    // from cached body text
                    sourceReader = new StringReader(functionBodyText);
                }
                else
                {
                    // from file using offset
                	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
                		System.out.println("FunctionNode waiting for lock in parseFunctionBody");
                    sourceReader = workspace.getFileSpecification(sourcePath).createReader();
                	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
                		System.out.println("FunctionNode done with lock in parseFunctionBody");
                    sourceReader.skip(openT.getLocalEnd());
                }

                assert !anyNonParametersInScope(contents);
                
                // rebuild function body AST
                
                // The incoming "problems" collection might be under modifications from
                // other code generation threads. In order to filter problems after parsing,
                // a local problem collection is created and added to the main problem
                // collection later.
                final List<ICompilerProblem> functionLocalProblems = new ArrayList<ICompilerProblem>();
                ASParser.parseFunctionBody(
                        contents,
                        sourceReader,
                        sourcePath,
                        openT,
                        functionLocalProblems,
                        workspace,
                        fileNode,
                        configProcessor);
                filterObsoleteProblems(fileNode, functionLocalProblems);
                problems.addAll(functionLocalProblems);

                // dispose cached function body info
                
                functionBodyText = null;
                
                // We should release "openT" as well. However, incremental compilation
                // needs this to redo code-generation.
                // openT = null;

                // connect function and its body scope
                final EnumSet<PostProcessStep> postProcess = EnumSet.of(
                    PostProcessStep.CALCULATE_OFFSETS,
                    PostProcessStep.POPULATE_SCOPE,
                    PostProcessStep.RECONNECT_DEFINITIONS);
                
                problems.addAll(contents.runPostProcess(postProcess, contents.getASScope()));
                
                // add implicit "arguments" argument to the local scope
                tryAddDefaultArgument();
            }
            catch (IOException e)
            {
                problems.add(new InternalCompilerProblem2(this.getSourcePath(), e, "function body parser"));
            }
        }
        finally
        {
            deferredBodyParsingLock.unlock();
        }
    }

    /**
     * {@link BaseASParser} is stateful. The state is essential in compiler
     * problem creation. However, when re-parsing a deferred function body, the
     * parser state can't be fully restored. As a result, some compiler problems
     * can be obsolete. This function removes those unwanted problems.
     * 
     * @param fileNode AST root node.
     * @param problems compiler problems found in the deferred function body.
     */
    private void filterObsoleteProblems(FileNode fileNode, Collection<ICompilerProblem> localProblems)
    {
        final int functionStartLine = this.getLine();
        final Collection<ICompilerProblem> problems = fileNode.getProblems();
        final Collection<ICompilerProblem> filteredLocalProblems = filter(
                localProblems,
                and(problemAtLine(functionStartLine), problemOfType(CanNotInsertSemicolonProblem.class)));

        // If the function signature has syntax error, the first "unterminated statement" problem
        // from function body is then obsolete.
        if (!filteredLocalProblems.isEmpty())
        {
            final Collection<ICompilerProblem> functionSignatureProblems =
                    filter(problems, problemAtLine(functionStartLine));
            if (!functionSignatureProblems.isEmpty())
            {
                localProblems.removeAll(filteredLocalProblems);
            }
        }
    }

    /**
     * IFilter {@link ICompilerProblem} collections by line number.
     */
    private static Predicate<ICompilerProblem> problemAtLine(final int line)
    {
        return new Predicate<ICompilerProblem>()
        {
            @Override
            public boolean apply(ICompilerProblem problem)
            {
                return problem.getLine() == line;
            }
            @Override
            public boolean test(ICompilerProblem input)
            {
                return apply(input);
            }
        };
    }

    /**
     * IFilter {@link ICompilerProblem} collections by class.
     */
    private static Predicate<ICompilerProblem> problemOfType(final Class<? extends ICompilerProblem> problemClass)
    {
        return new Predicate<ICompilerProblem>()
        {
            @Override
            public boolean apply(ICompilerProblem problem)
            {
                return problemClass.isInstance(problem);
            }
            @Override
            public boolean test(ICompilerProblem input)
            {
                return apply(input);
            }
        };
    }

    private static boolean anyNonParametersInScope(ScopedBlockNode contents)
    {
        IASScope sc = contents.getScope();
        Collection<IDefinition> ldfs = sc.getAllLocalDefinitions();
        for (IDefinition def : ldfs)
        {
            if (!(def instanceof IParameterDefinition))
                return true;
        }
        return false;
    }

    /**
     * Delete all children nodes in a function body.
     */
    public final void discardFunctionBody()
    {
        if (!isBodyDeferred || containsLocalFunctions())
            return;

        deferredBodyParsingLock.lock();
        try
        {
            deferredBodyParsingReferenceCount--;
            // only discard the body once there are 0 reference to it
            if (deferredBodyParsingReferenceCount > 0)
                return;

            final ScopedBlockNode contents = getScopedNode();
            if (contents.getChildCount() > 0)
            {
                final FileNode fileNode = (FileNode)getAncestorOfType(FileNode.class);
                ASFileScope fileScope = fileNode.getFileScope();
                fileScope.removeParsedFunctionBodies(this);

                contents.removeAllChildren();

                // Now we need to remove all the definitions in this function scope, except
                // we keep the parameters. This is because the initial "skeleton parse" goes as
                // far as the parameters.
                // So when we throw away the body, we still need to keep the parameter definitions
                IASScope functionScope = contents.getScope();
                Collection<IDefinition> localDefs = functionScope.getAllLocalDefinitions();
                for (IDefinition def : localDefs)
                {
                    if (! (def instanceof IParameterDefinition))
                    {
                        ASScope asScope = (ASScope)functionScope;
                        asScope.removeDefinition(def);
                    }
                }
            }
            assert (contents.getScope() == null) || (!anyNonParametersInScope(contents));
        }
        finally
        {
            deferredBodyParsingLock.unlock();
        }
    }

    public final boolean hasBeenParsed()
    {
        if (!isBodyDeferred)
            return true;

        deferredBodyParsingLock.lock();
        try
        {
            return deferredBodyParsingReferenceCount > 0;
        }
        finally
        {
            deferredBodyParsingLock.unlock();
        }
    }

    /**
     * Store the function body text on the function node so that the AST nodes
     * can be rebuilt later.
     */
    public final void setFunctionBodyInfo(ASToken openT, ASToken lastTokenInBody,
                                          ConfigProcessor configProcessor,
                                          StringBuilder bodyCache)
    {
        assert openT != null : "Open curly token can't be null";
        assert openT.getType() == ASTokenTypes.TOKEN_BLOCK_OPEN : "Expected '{' token.";
        assert lastTokenInBody != null : "Last token in function body can't be null.";
        assert configProcessor != null : "Project config variables can't be null.";

        this.openT = openT.clone();
        this.configProcessor = configProcessor;
        this.isBodyDeferred = true;
        
        if (bodyCache == null)
            this.functionBodyText = null;
        else
            this.functionBodyText = bodyCache.toString();
    }
    
    private ArrayList<IFunctionNode> localFunctions;
    
    @Override
    public List<IFunctionNode> getLocalFunctions()
    {
        return localFunctions;
    }

    @Override
    public boolean containsLocalFunctions()
    {
        return localFunctions != null;
    }

    @Override
    public void rememberLocalFunction(IFunctionNode value)
    {
        if (localFunctions == null)
            localFunctions = new ArrayList<IFunctionNode>();
        
        localFunctions.add(value);
    }
}
