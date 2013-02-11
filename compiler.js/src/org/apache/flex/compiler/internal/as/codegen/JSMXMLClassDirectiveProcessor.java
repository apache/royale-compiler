package org.apache.flex.compiler.internal.as.codegen;

import static org.apache.flex.abc.ABCConstants.CONSTANT_PrivateNs;
import static org.apache.flex.abc.ABCConstants.OP_applytype;
import static org.apache.flex.abc.ABCConstants.OP_callproperty;
import static org.apache.flex.abc.ABCConstants.OP_callpropvoid;
import static org.apache.flex.abc.ABCConstants.OP_construct;
import static org.apache.flex.abc.ABCConstants.OP_constructprop;
import static org.apache.flex.abc.ABCConstants.OP_dup;
import static org.apache.flex.abc.ABCConstants.OP_finddef;
import static org.apache.flex.abc.ABCConstants.OP_findpropstrict;
import static org.apache.flex.abc.ABCConstants.OP_getlex;
import static org.apache.flex.abc.ABCConstants.OP_getlocal0;
import static org.apache.flex.abc.ABCConstants.OP_getlocal3;
import static org.apache.flex.abc.ABCConstants.OP_getproperty;
import static org.apache.flex.abc.ABCConstants.OP_newarray;
import static org.apache.flex.abc.ABCConstants.OP_newobject;
import static org.apache.flex.abc.ABCConstants.OP_pushdouble;
import static org.apache.flex.abc.ABCConstants.OP_pushfalse;
import static org.apache.flex.abc.ABCConstants.OP_pushnull;
import static org.apache.flex.abc.ABCConstants.OP_pushstring;
import static org.apache.flex.abc.ABCConstants.OP_pushtrue;
import static org.apache.flex.abc.ABCConstants.OP_pushuint;
import static org.apache.flex.abc.ABCConstants.OP_setproperty;
import static org.apache.flex.abc.ABCConstants.TRAIT_Const;
import static org.apache.flex.abc.ABCConstants.TRAIT_Method;
import static org.apache.flex.abc.ABCConstants.TRAIT_Var;
import static org.apache.flex.compiler.mxml.IMXMLTypeConstants.ADD_EVENT_LISTENER_CALL_OPERANDS;
import static org.apache.flex.compiler.mxml.IMXMLTypeConstants.CREATE_XML_DOCUMENT_CALL_OPERANDS;
import static org.apache.flex.compiler.mxml.IMXMLTypeConstants.GET_INSTANCE_CALL_OPERANDS;
import static org.apache.flex.compiler.mxml.IMXMLTypeConstants.NAME_HANDLER_FUNCTION;
import static org.apache.flex.compiler.mxml.IMXMLTypeConstants.NAME_ID;
import static org.apache.flex.compiler.mxml.IMXMLTypeConstants.NAME_NAME;
import static org.apache.flex.compiler.mxml.IMXMLTypeConstants.NAME_OVERRIDES;
import static org.apache.flex.compiler.mxml.IMXMLTypeConstants.NAME_TARGET;
import static org.apache.flex.compiler.mxml.IMXMLTypeConstants.NAME_VALUE;
import static org.apache.flex.compiler.mxml.IMXMLTypeConstants.SET_STYLE_CALL_OPERANDS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.flex.abc.ABCConstants;
import org.apache.flex.abc.instructionlist.InstructionList;
import org.apache.flex.abc.semantics.MethodInfo;
import org.apache.flex.abc.semantics.Name;
import org.apache.flex.abc.semantics.Namespace;
import org.apache.flex.abc.semantics.Nsset;
import org.apache.flex.abc.semantics.Trait;
import org.apache.flex.abc.visitors.IABCVisitor;
import org.apache.flex.abc.visitors.ITraitVisitor;
import org.apache.flex.abc.visitors.ITraitsVisitor;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.flex.compiler.css.ICSSDocument;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.exceptions.CodegenInterruptedException;
import org.apache.flex.compiler.internal.as.codegen.ICodeGenerator.IConstantValue;
import org.apache.flex.compiler.internal.as.codegen.MXMLClassDirectiveProcessor.Context;
import org.apache.flex.compiler.internal.as.codegen.MXMLClassDirectiveProcessor.IL;
import org.apache.flex.compiler.internal.caches.CSSDocumentCache;
import org.apache.flex.compiler.internal.css.codegen.CSSCompilationSession;
import org.apache.flex.compiler.internal.css.codegen.CSSEmitter;
import org.apache.flex.compiler.internal.css.codegen.CSSReducer;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.internal.definitions.DefinitionBase;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.internal.definitions.NamespaceDefinition;
import org.apache.flex.compiler.internal.definitions.TypeDefinitionBase;
import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.resourcebundles.ResourceBundleUtils;
import org.apache.flex.compiler.internal.scopes.ASProjectScope;
import org.apache.flex.compiler.internal.scopes.ASScope;
import org.apache.flex.compiler.internal.semantics.SemanticUtils;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.tree.as.NodeBase;
import org.apache.flex.compiler.internal.tree.as.VariableNode;
import org.apache.flex.compiler.mxml.IMXMLLanguageConstants;
import org.apache.flex.compiler.mxml.IMXMLTypeConstants;
import org.apache.flex.compiler.problems.CSSCodeGenProblem;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.MXMLNotImplementedProblem;
import org.apache.flex.compiler.problems.MXMLOuterDocumentAlreadyDeclaredProblem;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.projects.IFlexProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLArrayNode;
import org.apache.flex.compiler.tree.mxml.IMXMLBooleanNode;
import org.apache.flex.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLClassNode;
import org.apache.flex.compiler.tree.mxml.IMXMLClassReferenceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLComponentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLConcatenatedDataBindingNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDeferredInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDesignLayerNode;
import org.apache.flex.compiler.tree.mxml.IMXMLEffectSpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLEmbedNode;
import org.apache.flex.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFactoryNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFunctionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLIntNode;
import org.apache.flex.compiler.tree.mxml.IMXMLNode;
import org.apache.flex.compiler.tree.mxml.IMXMLNumberNode;
import org.apache.flex.compiler.tree.mxml.IMXMLObjectNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLRegExpNode;
import org.apache.flex.compiler.tree.mxml.IMXMLRemoteObjectMethodNode;
import org.apache.flex.compiler.tree.mxml.IMXMLResourceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLSingleDataBindingNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStateNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStringNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStyleNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStyleSpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLUintNode;
import org.apache.flex.compiler.tree.mxml.IMXMLVectorNode;
import org.apache.flex.compiler.tree.mxml.IMXMLWebServiceOperationNode;
import org.apache.flex.compiler.tree.mxml.IMXMLXMLListNode;
import org.apache.flex.compiler.tree.mxml.IMXMLXMLNode;
import org.apache.flex.compiler.units.ICompilationUnit;

import com.google.common.base.Strings;

/**
 * {@link MXMLClassDirectiveProcessor} is a subclass of
 * {@code ClassDirectiveProcessor} that generates an ABC class
 * from an {@link IMXMLClassDefinitionNode} and its contents.
 * 
 * Here are some register usage conventions for Code Generation:
 *      local3: during parts of the constructor, local3 is used for a 
 *          lookup array of DeferredInstanceFromFunction.
 *      local1 and 2: Used during the constructor as temporaries
 *          for some data-binding init.
 */
public class JSMXMLClassDirectiveProcessor extends MXMLClassDirectiveProcessor
{
    /**
     * Initialize the {@link MXMLClassDirectiveProcessor} and its
     * associated AET data structures.
     * @param d - the MXML document's AST.
     * @param enclosingScope - the immediately enclosing lexical scope.
     * @param emitter - the active ABC emitter.
     */
    JSMXMLClassDirectiveProcessor(IMXMLClassDefinitionNode classDefinitionNode,
                                LexicalScope enclosingScope, IABCVisitor emitter)
    {
        super(classDefinitionNode, 
              enclosingScope, emitter);  
        
        JSMXMLEmitter jsMXMLEmitter = (JSMXMLEmitter)emitter;
        jsMXMLEmitter.register(this);
    }
    
    /**
     * A Map mapping an event node to the Name of the event handler
     * method (>0, >1, etc.) associated with that node.
     * <p>
     * The handler method may or may not exist at the time
     * that the handler name is assigned to the event node.
     * For example, when a State tag appears before
     * an instance tag with a state-dependent event,
     * the name will get assigned and the code generated later.
     * <p>
     * This map is managed ONLY by getEventHandlerName().
     */
    private final Map<IMXMLEventSpecifierNode, Name> eventHandlerMap =
        new HashMap<IMXMLEventSpecifierNode, Name>();

    /**
     * An incrementing counter used to create the names of the
     * auto-generated event handler methods.
     */
    private int eventHandlerCounter = 0;

    /**
     * Autogenerated event handler methods are named $EH0, $EH1, etc.
     * Using short names, and using the same names in each MXML document,
     * decreases SWF size.
     * Using a character that is not legal in ActionScript identifiers
     * means that even if the event handler must be public
     * (because it is referenced in a descriptor)
     * the name will not collide with the name of a developer-written
     * method and cannot be accessed from developer code.
     */
    private static final String EVENT_HANDLER_NAME_BASE = "$EH";

    private String NEWLINE = "\n";
    public ArrayList<Name> variableTraits;
    private ArrayList<String> instanceData = new ArrayList<String>();
    public ArrayList<String> fragments = new ArrayList<String>();
    private FragmentList properties = new FragmentList();
    private FragmentList styles = new FragmentList();
    private FragmentList events = new FragmentList();
    private FragmentList children = new FragmentList();
    private FragmentList model = new FragmentList();
    private FragmentList beads = new FragmentList();
    private FragmentList currentList;
    private boolean inContentFactory;
    private String className;
    
    @Override
    public void processMainClassDefinitionNode(IMXMLClassDefinitionNode node)
    {
        // Leave a reference to the class on the stack.
        ClassDefinition classDefinition =
            (ClassDefinition)((IMXMLClassDefinitionNode)node).getClassDefinition();
        className = classDefinition.getQualifiedName();
    	currentList = properties;
    	super.processMainClassDefinitionNode(node);
    }

    @Override
    public void addVariableTrait(Name varName, Name varType)
    {
    	if (variableTraits == null)
    		variableTraits = new ArrayList<Name>();
    	variableTraits.add(varName);
    	variableTraits.add(varType);
    }

    @Override
    public void addBindableVariableTrait(Name varName, Name varType, IDefinition def)
    {
    	String var = /*"_" + */varName.getBaseName();
    	Name backingVar = new Name(new Namespace(JSSharedData.CONSTANT_PrivateNs), var);
    	variableTraits.add(backingVar);
    	variableTraits.add(varType);

    	fragments.add("/**"
    	    	 + NEWLINE +  " * @this {" + className + "}"
    	    	 + NEWLINE +  " * @return {" + varType.getBaseName() + "}"
    	    	 + NEWLINE +  " */"
    	    	 + NEWLINE +  className + ".prototype.get_" + varName.getBaseName() + " = function()"
    	    	 + NEWLINE +  "{"
    	    	 + NEWLINE +  "    return this." + var + ";"
    	    	 + NEWLINE +  "};");
    	
    	fragments.add("/**"
   	    	 + NEWLINE +  " * @this {" + className + "}"
   	    	 + NEWLINE +  " * @param {" + varType.getBaseName() + "} value"
   	    	 + NEWLINE +  " */"
   	    	 + NEWLINE +  className + ".prototype.set_" + varName.getBaseName() + " = function(value)"
   	    	 + NEWLINE +  "{"
   	    	 + NEWLINE +  "    if (value != this." + var + ")"
   	    	 + NEWLINE +  "        this." + var + " = value;"
   	    	 + NEWLINE +  "};");
    }
    
    @Override
    protected void processMXMLClassDefinition(IMXMLClassDefinitionNode node, Context context)
    {
        // Create the <Component> or <Definition> class.
        MXMLClassDirectiveProcessor dp = new MXMLClassDirectiveProcessor(node, globalScope, emitter);
        dp.processMainClassDefinitionNode(node);
        dp.finishClassDefinition();
    }
    
    @Override
    void setDocument(IMXMLClassReferenceNode node, boolean conditionally, Context context)
    {
        if (node.isVisualElementContainer() || node.isContainer())
        {
        	currentList.add("document");
        	currentList.add(true);
        	currentList.addExplicit("this");
        }	
    }
    
    /**
     * Adds the current set of childEvents, childStyles, and childProperties to currentList;
     * @param context
     * @param addCounters
     * @param skipContentFactory
     * @return
     */
    @Override
    int setSpecifiers(Context context, Boolean addCounters, Boolean skipContentFactory)
    {
    	if (skipContentFactory)
    		return 0;
    	
    	int numProperties = properties.size() / 3;
    	if (context.hasBeads)
    		numProperties++;
    	if (context.hasModel)
    		numProperties++;
		currentList.add(numProperties);
		if (context.hasModel)
		{
			currentList.add("model");
			currentList.addObjectTypeMarker();
			currentList.addExplicit("[" + model.toString() + "]");
		}
		currentList.addAll(properties);
		if (context.hasBeads)
		{
			currentList.add("beads");
			currentList.addExplicit(beads.toString());
		}
    	currentList.add(styles.size() / 3);
    	currentList.addAll(styles);
    	currentList.add(events.size() / 2);
    	currentList.addAll(events);
    	if (children.size() == 0)
    		currentList.addNull();
    	else
    		currentList.addExplicit("[" + children.toString() + "]");
    	return (properties.size() + styles.size() + events.size()) / 3;
    }

    @Override
    void overrideMXMLDescriptorGetter(IMXMLClassDefinitionNode node, Context context)
    {
        // Leave a reference to the class on the stack.
        ClassDefinition classDefinition =
            (ClassDefinition)((IMXMLClassDefinitionNode)node).getClassDefinition();
        String className = classDefinition.getQualifiedName();

    	fragments.add("/**"
    	 + NEWLINE +  " * @override"
    	 + NEWLINE +  " * @this {" + className + "}"
    	 + NEWLINE +  " * @return {Array} the Array of UI element descriptors."
    	 + NEWLINE +  " */"
    	 + NEWLINE +  className + ".prototype.get_MXMLDescriptor = function()"
    	 + NEWLINE +  "{"
    	 + NEWLINE +  "    if (this.mxmldd == undefined)"
    	 + NEWLINE +  "    {"
    	 + NEWLINE +  "         /** @type {Array} */"
    	 + NEWLINE +  "         var arr = goog.base(this, 'get_MXMLDescriptor');"
    	 + NEWLINE +  "         /** @type {Array} */"
    	 + NEWLINE +  "         var data = " + addInstanceData()
    	 + NEWLINE +  "         if (arr)"
    	 + NEWLINE +  "             this.mxmldd = arr.concat(data);"
    	 + NEWLINE +  "         else"
    	 + NEWLINE +  "             this.mxmldd = data;"
    	 + NEWLINE +  "    }"
    	 + NEWLINE +  "    return this.mxmldd;"
    	 + NEWLINE +  "};");
    	 
    }

    private String addInstanceData()
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("[" + NEWLINE);
    	int n = children.size();
    	for (int i = 0; i < n; i++)
    	{
    		String s = children.get(i);
    		sb.append(s);
    		if (i < n - 1)
    			sb.append("," + NEWLINE);
    		else
    			sb.append(NEWLINE);
    	}
        sb.append("];" + NEWLINE);
    	return sb.toString();
    }

    void overrideMXMLPropertiesGetter(IMXMLClassDefinitionNode node, Context context, int numElements)
    {
        // Leave a reference to the class on the stack.
        ClassDefinition classDefinition =
            (ClassDefinition)((IMXMLClassDefinitionNode)node).getClassDefinition();
        String className = classDefinition.getQualifiedName();

        fragments.add("/**"
    	    	 + NEWLINE +  " * @override"
    	    	 + NEWLINE +  " * @this {" + className + "}"
    	    	 + NEWLINE +  " * @return {Array} the Array of UI element descriptors."
    	    	 + NEWLINE +  " */"
    	    	 + NEWLINE +  className + ".prototype.get_MXMLProperties = function()"
    	    	 + NEWLINE +  "{"
    	    	 + NEWLINE +  "    if (this.mxmldp == undefined)"
    	    	 + NEWLINE +  "    {"
    	    	 + NEWLINE +  "         /** @type {Array} */"
    	    	 + NEWLINE +  "         var arr = goog.base(this, 'get_MXMLProperties');"
    	    	 + NEWLINE +  "         /** @type {Array} */"
    	    	 + NEWLINE +  "         var data = " + addPropertiesData(context)
    	    	 + NEWLINE +  "         if (arr)"
    	    	 + NEWLINE +  "             this.mxmldp = arr.concat(data);"
    	    	 + NEWLINE +  "         else"
    	    	 + NEWLINE +  "             this.mxmldp = data;"
    	    	 + NEWLINE +  "    }"
    	    	 + NEWLINE +  "    return this.mxmldp;"
    	    	 + NEWLINE +  "};");
    	    	 
    }
    
    private String addPropertiesData(Context context)
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("[" + NEWLINE);
    	int n = properties.size();
    	int numProperties = n / 3;
    	if (context.hasModel)
    		numProperties++;
    	if (context.hasBeads)
    		numProperties++;
    	sb.append(new Integer(numProperties).toString() + ',' + NEWLINE);
    	if (context.hasModel)
    	{
    		sb.append("'model'");
    		sb.append("," + NEWLINE);
    		sb.append(model.toString());
    		sb.append("," + NEWLINE);
    	}
    	for (int i = 0; i < n; i++)
    	{
    		String s = properties.get(i);
    		sb.append(s);
    		sb.append("," + NEWLINE);
    	}
    	if (context.hasBeads)
    	{
    		sb.append("'beads'");
    		sb.append("," + NEWLINE);
    		sb.append(beads.toString());
    		sb.append("," + NEWLINE);
    	}
    	n = styles.size();
    	sb.append(new Integer(n / 3).toString() + ',' + NEWLINE);
    	for (int i = 0; i < n; i++)
    	{
    		String s = styles.get(i);
    		sb.append(s);
    		sb.append("," + NEWLINE);
    	}
    	n = events.size();
    	if (n == 0)
        	sb.append("0" + NEWLINE);
    	else
    		sb.append(new Integer(n / 2).toString() + ',' + NEWLINE);
    	for (int i = 0; i < n; i++)
    	{
    		String s = events.get(i);
    		sb.append(s);
    		if (i < n - 1)
    			sb.append("," + NEWLINE);
    		else
    			sb.append(NEWLINE);
    	}
        sb.append("];" + NEWLINE);
    	return sb.toString();
    }

    @Override
    void processMXMLBoolean(IMXMLBooleanNode booleanNode, Context context)
    {
        if (booleanNode.getParent().getNodeID() == ASTNodeID.MXMLPropertySpecifierID)
            currentList.addSimpleTypeMarker(); // simple type

        boolean value = isDataBound(booleanNode) ? false : booleanNode.getValue();       
        currentList.add(value);
        traverse(booleanNode, context);
    }
    
    @Override
    void processMXMLInt(IMXMLIntNode intNode, Context context)
    {
        if (intNode.getParent().getNodeID() == ASTNodeID.MXMLPropertySpecifierID)
            currentList.addSimpleTypeMarker(); // simple type

        int value = isDataBound(intNode) ? 0 : intNode.getValue();
        currentList.add(value);
        traverse(intNode, context);
     }
    
    @Override
    void processMXMLUint(IMXMLUintNode uintNode, Context context)
    {
        if (uintNode.getParent().getNodeID() == ASTNodeID.MXMLPropertySpecifierID)
            currentList.addSimpleTypeMarker(); // simple type

        long value = isDataBound(uintNode) ? 0 : uintNode.getValue();
        currentList.add(value);
        traverse(uintNode, context);
     }

    @Override
    void processMXMLNumber(IMXMLNumberNode numberNode, Context context)
    {
        if (numberNode.getParent().getNodeID() == ASTNodeID.MXMLPropertySpecifierID)
            currentList.addSimpleTypeMarker(); // simple type

        double value = isDataBound(numberNode) ? Double.NaN : numberNode.getValue();
        currentList.add(value);
        traverse(numberNode, context);
    }
    
    @Override
    void processMXMLString(IMXMLStringNode stringNode, Context context)
    {
        if (stringNode.getParent().getNodeID() == ASTNodeID.MXMLPropertySpecifierID)
            currentList.addSimpleTypeMarker(); // simple type

        String value = isDataBound(stringNode) ? null : stringNode.getValue();
        if (value != null)
            currentList.add(value);
        else
            currentList.addNull();
        
        traverse(stringNode, context);
    }

    @Override
    void processMXMLClass(IMXMLClassNode classNode, Context context)
    {        
    	MXMLNotImplementedProblem problem = new MXMLNotImplementedProblem(classNode, "MXML Class");
        getProblems().add(problem);
        /*
    	// Don't call skipCodegen() because a null Class is represented
        // by the expression node being null.
        if (isDataBindingNode(classNode))
            return;
        
        IExpressionNode expressionNode = (IExpressionNode)classNode.getExpressionNode();
        if (expressionNode != null)
        {
            InstructionList init_expression = classScope.getGenerator().generateInstructions(
                expressionNode, CmcEmitter.__expression_NT, this.classScope);
            context.addAll(init_expression);
        }
        else
        {
            context.addInstruction(OP_pushnull);
        }
        */
    }

    /**
     * Generates an instruction in the current context
     * to push the value of an {@code IMXMLFunctionNode}.
     *
     * Will also generate the function, if the FunctionNode specifies a function expression.
     *
     * If no expression is provided in the function node, this will push null
     */
    void processMXMLFunction(IMXMLFunctionNode functionNode, Context context)
    {
    	MXMLNotImplementedProblem problem = new MXMLNotImplementedProblem(functionNode, "MXML Function");
        getProblems().add(problem);
        /*
        // Don't call skipCodegen() because a null Function is represented
        // by the expression node being null.
        if (isDataBindingNode(functionNode))
            return;
        
        IExpressionNode expressionNode = (IExpressionNode)functionNode.getExpressionNode();
        if (expressionNode != null)
        {
            InstructionList init_expression = classScope.getGenerator().generateInstructions(
                expressionNode, CmcEmitter.__expression_NT, this.classScope);
            context.addAll(init_expression);
        }
        else
        {
            context.addInstruction(OP_pushnull);
        }
        */
    }

    @Override
    void processMXMLRegExp(IMXMLRegExpNode regexpNode, Context context)
    {
    	MXMLNotImplementedProblem problem = new MXMLNotImplementedProblem(regexpNode, "MXML RegExp");
        getProblems().add(problem);
        /*
        // Don't call skipCodegen() because a parameterless RegExp is represented
        // by the expression node being null.
        if (isDataBindingNode(regexpNode))
            return;
        
        IExpressionNode expressionNode = (IExpressionNode)regexpNode.getExpressionNode();
        if (expressionNode != null )
        {
            InstructionList init_expression = classScope.getGenerator().generateInstructions(
                expressionNode, CmcEmitter.__expression_NT, this.classScope);
            context.addAll(init_expression);
        }
        else
        {
            context.addInstruction(OP_findpropstrict, ABCGeneratingReducer.regexType);
            context.addInstruction(OP_constructprop, new Object[] {ABCGeneratingReducer.regexType, 0});
        }
        */
    }

    @Override
    void processMXMLDesignLayer(IMXMLDesignLayerNode node, Context context)
    {
    	MXMLNotImplementedProblem problem = new MXMLNotImplementedProblem(node, "MXML DesignLayer");
        getProblems().add(problem);
    }
    
    @Override
    void processMXMLWebServiceOperation(IMXMLWebServiceOperationNode node, Context context)
    {
    	MXMLNotImplementedProblem problem = new MXMLNotImplementedProblem(node, "MXML WebServices");
        getProblems().add(problem);
    }
    
    @Override
    void processMXMLRemoteObjectMethod(IMXMLRemoteObjectMethodNode node, Context context)
    {
    	MXMLNotImplementedProblem problem = new MXMLNotImplementedProblem(node, "MXML Remote Object");
        getProblems().add(problem);
    }
    
    @Override
    void processMXMLObject(IMXMLObjectNode objectNode, Context context)
    {
    	FragmentList savedCurrentList = currentList;
    	currentList = new FragmentList();
    	
        traverse(objectNode, context);
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int n = currentList.size();
        for (int i = 0; i < n; i++)
        {
        	String s = currentList.get(i++);
        	sb.append(s + ":");
        	sb.append(currentList.get(i));
        	if (i < n - 1)
        		sb.append(",");
        }
        sb.append("}");        
        currentList = savedCurrentList;
        currentList.addExplicit(sb.toString());
    }
    
    @Override
    void processMXMLArray(IMXMLArrayNode arrayNode, Context context)
    {
        boolean savedMakingArrayValues = context.makingArrayValues;
        
    	if (!inContentFactory)
        {
            if (context.parentContext.isStateDescriptor)
                currentList.addArrayTypeMarker(); // array of descriptors
            else
            {
                boolean isSimple = true;
                
                for (int i = 0; i < arrayNode.getChildCount(); i++)
                {
                    final IASNode child = arrayNode.getChild(i);
                    ASTNodeID nodeID = child.getNodeID();
                    if (nodeID == ASTNodeID.MXMLArrayID || nodeID == ASTNodeID.MXMLInstanceID)
                    {
                        isSimple = false;
                        break;
                    }
                }
                context.makingArrayValues = true;
                if (isSimple)
                    currentList.addSimpleTypeMarker(); // arrays are simple values      
                else
                	currentList.addArrayTypeMarker();
            }
        }

    	FragmentList savedCurrentList = currentList;
    	currentList = new FragmentList();

    	traverse(arrayNode, context);
        
    	FragmentList childList = currentList;
    	currentList = savedCurrentList;
    	
        // TODO: can we do better?
        // Now that stack will have the array children on it.
        // But we may not have created one for every child of arrayNode.
        // It would be best if we could remember how many we created, but
        // we can't easily do that. So we use our "knowledge" that children
        // are always created unless they are state dependent instances.
        int nMax = arrayNode.getChildCount();
        int numStateDependentChildren=0;
        for (int i=0; i<nMax; ++i)
        {
            IASNode ch = arrayNode.getChild(i);
            if (isStateDependentInstance(ch))
            {
                ++numStateDependentChildren;
            }
        }
        
        if (inContentFactory)
        {
            // pass the number of things we found up to the parent context. In spark controls
            // the array of children is buried by a layer or two
            currentList.addAll(childList);
        }
        else if (context.parentContext.isStateDescriptor)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(childList.toString());
            sb.append("]");        
            currentList.addExplicit(sb.toString());     
        }
        else if (context.makingArrayValues)
        {
        	currentList.addAll(childList);
        }
        context.makingArrayValues = savedMakingArrayValues;
    }
    
    @Override
    void processMXMLVector(IMXMLVectorNode vectorNode, Context context)
    {
    	MXMLNotImplementedProblem problem = new MXMLNotImplementedProblem(vectorNode, "MXML Vector");
        getProblems().add(problem);
    }
    
    void processMXMLInstance(IMXMLInstanceNode instanceNode, Context context)
    {       
        FragmentList savedList = currentList;
        FragmentList savedProperties = properties;
        FragmentList savedStyles = styles;
        FragmentList savedEvents = events;
        FragmentList savedChildren = children;
        FragmentList savedBeads = beads;
        FragmentList savedModel = model;
        boolean savedHasBeads = context.hasBeads;
        boolean savedHasModel = context.hasModel;
        
        properties = new FragmentList();
        styles = new FragmentList();
        events = new FragmentList();
        children = new FragmentList();
        beads = new FragmentList();
        model = new FragmentList();
        context.hasBeads = false;
        context.hasModel = false;
        
        if (!context.isStateDescriptor && !inContentFactory && !context.parentContext.makingArrayValues)
            currentList.addObjectTypeMarker(); // complex type

        traverse(instanceNode, context);        

        int numElements = 0;

        currentList = properties;
        
        setDocument(instanceNode, false, context);
        
        // Sets the id property if the instance
        // implements IDeferredInstantiationUIComponent.
        String id = instanceNode.getID();
        if (id != null)
        {
            currentList.add("id");
            currentList.addSimpleTypeMarker();
            currentList.add(id);
        }
        else
        {
            id = instanceNode.getEffectiveID();
            if (id != null)
            {
                currentList.add("_id");
                currentList.addSimpleTypeMarker();
                currentList.add(id);
            }
        }
        // bail out now.  Other properties will be added in processMXMLState
        if (context.isStateDescriptor)
            return;
        
        
        if (!inContentFactory)
        	currentList = new FragmentList();
        else
        	currentList = savedList;
        
    	currentList.add(context.instanceClassName);
        numElements += setSpecifiers(context, true, false);
        
        if (!inContentFactory)
        {
        	StringBuilder sb = new StringBuilder();
        	sb.append("[");
        	sb.append(currentList.toString());
        	sb.append("]");
            currentList = savedList;
        	currentList.addExplicit(sb.toString());
        }
        
        currentList = savedList;
        properties = savedProperties;
        styles = savedStyles;
        events = savedEvents;
        children = savedChildren;
        beads = savedBeads;
        model = savedModel;
        context.hasBeads = savedHasBeads;
        context.hasModel = savedHasModel;
    }

    @Override
    void processMXMLFactory(IMXMLFactoryNode factoryNode, Context context)
    {
        // Get the Name for the mx.core.ClassFactory class.
        ICompilerProject project = getProject();
        ClassDefinition classReference = (ClassDefinition)factoryNode.getClassReference(project);
        Name factoryClassName = classReference.getMName(project);
        
        // Push this class.
        currentList.add(factoryClassName);
        
        // Push the class to be used as the generator,
        // by traversing the child MXMLClassNode.
        traverse(factoryNode, context);
        
        // Call new ClassFactory(generator), leaving the new instance on the stack.
        currentList.addExplicit("new " + factoryClassName.toString());
    }
    
    @Override
    void processMXMLDeferredInstance(IMXMLDeferredInstanceNode deferredInstanceNode, Context context)
    {
        // Push the class or function to be used as the generator,
        // by traversing the child MXMLClassNode or MXMLInstanceNode.
        traverse(deferredInstanceNode, context);
    }
    
    @Override
    void processMXMLPropertySpecifier(IMXMLPropertySpecifierNode propertyNode, Context context)
    {
        // State-dependent nodes are handled by processMXMLState().
        if (isStateDependent(propertyNode))
            return;
                
        String propertyName = propertyNode.getName();
        
        if (propertyName.equals("mxmlContentFactory") || propertyName.equals("mxmlContent"))
        {
        	FragmentList savedList = currentList;
            currentList = children;
            inContentFactory = true;
            
            traverse(propertyNode, context);
            
            inContentFactory = false;
            currentList = savedList;
        }
        else if (propertyName.equals("states"))
        {
            context.isStateDescriptor = true;
            
        	FragmentList savedList = currentList;
            currentList = properties;
            
            currentList.add(propertyName);
            
            traverse(propertyNode, context);
                            
            currentList = savedList;
        }
        else if (propertyName.equals("model"))
        {
            context.hasModel = true;
            
        	FragmentList savedList = currentList;
            currentList = model;
            
            traverse(propertyNode, context);
                            
            currentList = savedList;
        }
        else if (propertyName.equals("beads"))
        {
            context.hasBeads = true;
            
        	FragmentList savedList = currentList;
            currentList = beads;
            boolean savedInContentFactory = inContentFactory;
            inContentFactory = false;
            
            traverse(propertyNode, context);
            
            inContentFactory = savedInContentFactory;
            currentList = savedList;
        }
        else
        {
        	FragmentList savedList = currentList;
            currentList = properties;

        	currentList.add(propertyName);
            
            traverse(propertyNode, context);
            
            currentList = savedList;
        }
    }
    
    @Override
    void processMXMLStyleSpecifier(IMXMLStyleSpecifierNode styleNode, Context context)
    {        
        // State-dependent nodes are handled by processMXMLState().
        if (isStateDependent(styleNode))
            return;
        
        // Data bound styles don't need this processing either
        IMXMLInstanceNode value = styleNode.getInstanceNode();
        if (isDataBindingNode(value))
        {
            return;
        }
        
        // Style specifiers on the class definition node
        // generate code in the moduleFactory setter.
        if (styleNode.getParent() instanceof IMXMLClassDefinitionNode)
        {
        	FragmentList savedList = currentList;
        	currentList = styles;
        	
            // Push the second argument: the value of the style.
            // Do this by codegen'ing sole child, which is an IMXMLInstanceNode.
            traverse(styleNode, context);

            currentList = savedList;
            
        }
        
        else
        {
        	FragmentList savedList = currentList;
        	currentList = styles;

        	String styleName = styleNode.getName();
            
            // Push the first argument: the name of the style.
            currentList.add(styleName);
            
            // Push the second argument: the value of the style.
            // Do this by codegen'ing sole child, which is an IMXMLInstanceNode.
            traverse(styleNode, context);
            
            currentList = savedList;
        }
    }
    
    @Override
    void processMXMLEffectSpecifier(IMXMLEffectSpecifierNode effectNode, Context context)
    {
    	MXMLNotImplementedProblem problem = new MXMLNotImplementedProblem(effectNode, "MXML Effect");
        getProblems().add(problem);
    }
    
    @Override
    void processMXMLEventSpecifier(IMXMLEventSpecifierNode eventNode, Context context)
    {  
        // Event nodes (including state-dependent ones)
        // generate a new event handler method.
        // Create a MethodInfo and a method trait for the handler.
        Name name = getEventHandlerName(eventNode);
        MethodInfo methodInfo = createEventHandlerMethodInfo(
            getProject(), eventNode, name.getBaseName());
        ITraitVisitor traitVisitor =
            itraits.visitMethodTrait(TRAIT_Method, name, 0, methodInfo);
        
        // Use ABCGenerator to codegen the handler body from the
        // ActionScript nodes that are the children of the event node.
        classScope.getGenerator().generateMethodBodyForFunction(
            methodInfo, eventNode, classScope, null);

        // Otherwise, state-dependent nodes are handled by processMXMLState().
        if (isStateDependent(eventNode))
            return;
        
        String eventName = eventNode.getName();
        Name eventHandler = getEventHandlerName(eventNode);

    	FragmentList savedList = currentList;
    	currentList = events;
    	
        currentList.add(eventName);
        
        currentList.addExplicit("this." + eventHandler.getBaseName());

        currentList = savedList;
    }
    
    @Override
    void processMXMLResource(IMXMLResourceNode node, Context context)
    {
    	MXMLNotImplementedProblem problem = new MXMLNotImplementedProblem(node, "MXML Resources");
        getProblems().add(problem);
    }
    
    @Override
    void processMXMLState(IMXMLStateNode stateNode, Context context)
    {
        int numElements = 1;
        
        context.isStateDescriptor = true;
        
        // First process the State node as an instance node,
        // so that properties (name, stateGroups, basedOn) get set
        // and event handlers (enterState, etc.) get set.
        processMXMLInstance(stateNode, context);
        
        // Init the name property of the state (it's not a normal property specifier nodes
        // TODO: should we make this a property node?
        String name = stateNode.getStateName();
        if (name != null)
        {
            currentList.add("name");
            currentList.addSimpleTypeMarker();
            currentList.add(name);
        }
        
        // In MXML 2009 and later, a state's 'overrides' property is implicitly
        // determined by the nodes that are dependent on this state.
        // We use these nodes to autogenerate runtime IOverride objects
        // and set them as the value of the 'overrides' property.
        IMXMLClassDefinitionNode classDefinitionNode = stateNode.getClassDefinitionNode();
        List<IMXMLNode> nodes = classDefinitionNode.getNodesDependentOnState(stateNode.getStateName());
        if (nodes != null)
        {
            currentList.add("overrides");
            currentList.addArrayTypeMarker();  // complex array
           
            // First step: process all instance overrides before any other overrides.
            //  why: because a) ensure instances exists before trying to apply property overrides
            //               b) because the old compiler did
            // Do it in reverse order
            //              a) because the way old compiler generated destination and relativeTo
            //              properties relies on doing it backwards.
            //
            // Each one will generate code to push an IOverride instance.
            for (int i=nodes.size()-1; i>=0; --i)
            {
                IMXMLNode node = nodes.get(i);
                if (node.getNodeID() == ASTNodeID.MXMLInstanceID)
                {
                    processInstanceOverride((IMXMLInstanceNode)node, context);
                }
            }
            // Next process the non-instance overrides dependent on this state.
            // Each one will generate code to push an IOverride instance.
            for (IMXMLNode node : nodes)
            {
                switch (node.getNodeID())
                {
                    case MXMLPropertySpecifierID:
                    {
                        processPropertyOverride((IMXMLPropertySpecifierNode)node, context);
                        break;
                    }
                    case MXMLStyleSpecifierID:
                    {
                        processStyleOverride((IMXMLStyleSpecifierNode)node, context);
                        break;
                    }
                    case MXMLEventSpecifierID:
                    {
                        processEventOverride((IMXMLEventSpecifierNode)node, context);
                        break;
                    }
                }
            }
            
            numElements += setSpecifiers(context, true, false);
        }
    }
    
    @Override
    void processPropertyOrStyleOverride(Name overrideName, IMXMLPropertySpecifierNode propertyOrStyleNode, Context context)
    {
        IASNode parentNode = propertyOrStyleNode.getParent();
        String id = parentNode instanceof IMXMLInstanceNode ?
                    ((IMXMLInstanceNode)parentNode).getEffectiveID() :
                    "";
        
        String name = propertyOrStyleNode.getName();        
        
        IMXMLInstanceNode propertyOrStyleValueNode = propertyOrStyleNode.getInstanceNode();
        
        // Construct the SetProperty or SetStyle object.
        currentList.add(overrideName);
        
        currentList.add(3);
        // Set its 'target' property to the id of the object
        // whose property or style this override will set.
        currentList.add("target");
        currentList.addSimpleTypeMarker();
        currentList.add(id);

        // Set its 'name' property to the name of the property or style.
        currentList.add("name");
        currentList.addSimpleTypeMarker();
        currentList.add(name);

        // Set its 'value' property to the value of the property or style.
        currentList.add("value");
        processNode(propertyOrStyleValueNode, context); // push value

        currentList.add(0); // styles
        currentList.add(0); // effects
        currentList.add(0); // events
        currentList.addNull(); // children
    }
    
    /**
     * Generates instructions in the current context
     * to create an instance of mx.states.SetEventHandler
     * with its <code>target</code>, <code>name</code>,
     * and <code>handlerFunction</code> properties set.
     */
    void processEventOverride(IMXMLEventSpecifierNode eventNode, Context context)
    {
        FlexProject project = getProject();
        Name eventOverride = project.getEventOverrideClassName();
        
        IASNode parentNode = eventNode.getParent();
        String id = parentNode instanceof IMXMLInstanceNode ?
                    ((IMXMLInstanceNode)parentNode).getEffectiveID() :
                    "";
        
        String name = eventNode.getName();
        
        Name eventHandler = getEventHandlerName(eventNode);

        // Construct the SetEventHandler object.
        currentList.add(eventOverride);
        currentList.add(3);
        
        // Set its 'target' property to the id of the object
        // whose event this override will set.
        currentList.add(NAME_TARGET);
        currentList.addSimpleTypeMarker();
        currentList.add(id);

        // Set its 'name' property to the name of the event.
        currentList.add(NAME_NAME);
        currentList.addSimpleTypeMarker();
        currentList.add(name);

        // Set its 'handlerFunction' property to the autogenerated event handler.
        currentList.add(NAME_HANDLER_FUNCTION);
        currentList.addSimpleTypeMarker();
        currentList.add(eventHandler);
    }
    
    /**
     * Generates instructions in the current context
     * to create an instance of mx.states.AddItems...
     * 
     * Assumes lookup table is still in local3
     */

    void processInstanceOverride(IMXMLInstanceNode instanceNode, Context context)
    {
        FlexProject project = getProject();
        Name instanceOverrideName = project.getInstanceOverrideClassName();
        
        currentList.add(instanceOverrideName);

        // Now set properties on it!
        
        //----------------------------------------------------------------------
        // First property: set itemsFactory to the deferredInstanceFunction we created earlier
        Integer index = nodeToIndexMap.get(instanceNode);
        assert index != null;

        FragmentList addItemsList = new FragmentList();
        addItemsList.add("itemsDescriptor");
        addItemsList.addSimpleTypeMarker();  // the value is an array of descriptor data that will be parsed later
        FragmentList savedList = currentList;
        currentList = new FragmentList();
        InstructionList il = nodeToInstanceDescriptorMap.get(instanceNode);
        addItemsList.addExplicit("[" + currentList.toString() + "]");
        currentList = savedList;

        //-----------------------------------------------------------------------------
        // Second property set: maybe set destination and propertyName
        
        // get the property specifier node for the property the instanceNode represents
        IMXMLPropertySpecifierNode propertySpecifier = (IMXMLPropertySpecifierNode) 
            instanceNode.getAncestorOfType( IMXMLPropertySpecifierNode.class);
    
        if (propertySpecifier == null)
        {
           assert false;        // I think this indicates an invalid tree...
        }
        else
        {
            // Check the parent - if it's an instance then we want to use these
            // nodes to get our property values from. If not, then it's the root
            // and we don't need to specify destination
            
            IASNode parent = propertySpecifier.getParent();
            if (parent instanceof IMXMLInstanceNode)
            {
               IMXMLInstanceNode parentInstance = (IMXMLInstanceNode)parent;
               String parentId = parentInstance.getEffectiveID();
               assert parentId != null;
               String propName = propertySpecifier.getName();
               
               
               addItemsList.add("destination");
               addItemsList.addSimpleTypeMarker(); // simple type
               addItemsList.add(parentId); 
               addItemsList.add("propertyName");
               addItemsList.addSimpleTypeMarker(); // simple type
               addItemsList.add(propName); 
            }
        }  
        
        //---------------------------------------------------------------
        // Third property set: position and relativeTo
        String positionPropertyValue = null;
        String relativeToPropertyValue = null;
       
        // look to see if we have any sibling nodes that are not state dependent
        // that come BEFORE us
        IASNode instanceParent = instanceNode.getParent();
        IASNode prevStatelessSibling=null;
        for (int i=0; i< instanceParent.getChildCount(); ++i)
        {
            IASNode sib = instanceParent.getChild(i);
            assert sib instanceof IMXMLInstanceNode;    // surely our siblings are also instances?
           
            // stop looking for previous nodes when we find ourself
            if (sib == instanceNode)
                break;

            if (!isStateDependent(sib))
            {
                prevStatelessSibling = sib;
            }
        }
        
        if (prevStatelessSibling == null) {
            positionPropertyValue = "first";        // TODO: these should be named constants
        }
        else {
            positionPropertyValue = "after";
            relativeToPropertyValue = ((IMXMLInstanceNode)prevStatelessSibling).getEffectiveID();
        }
       
        addItemsList.add("position");
        addItemsList.addSimpleTypeMarker(); // simple type
        addItemsList.add(positionPropertyValue); 
        
        // relativeTo
        if (relativeToPropertyValue != null)
        {
        	addItemsList.add("relativeTo");
            addItemsList.addSimpleTypeMarker(); // simple type
        	addItemsList.add(relativeToPropertyValue); 
        }
        
        currentList.add(addItemsList.size() / 3);
        currentList.addAll(addItemsList);
        currentList.add(0); // styles
        currentList.add(0); // effects
        currentList.add(0); // events
        currentList.addNull(); // children
    }
    
    void processMXMLComponent(IMXMLComponentNode node, Context context)
    {
        // Resolve the outer document, and if it doesn't resolve to the contingent
        // definition, that means there is already an existing definition declared
        // which is an error.
        ClassDefinition componentClass = (ClassDefinition)node.getContainedClassDefinition();
        ASScope classScope = componentClass.getContainedScope();
        IDefinition outerDocument = classScope.getPropertyFromDef(
            getProject(), componentClass, IMXMLLanguageConstants.PROPERTY_OUTER_DOCUMENT, false);
        assert (outerDocument != null) : "outerDocument should never be null, as always added";
        if (!outerDocument.isContingent())
        {
            ICompilerProblem problem = new MXMLOuterDocumentAlreadyDeclaredProblem(outerDocument);
             getProblems().add(problem);
        }

        // Process the MXMLComponentNode as an MXMLFactoryNode, which it extends.
        // This leaves a ClassFactory on the stack.
        processMXMLFactory(node, context);
        
        // factory.properties = { outerDocument: this }
        currentList.add(IMXMLTypeConstants.NAME_PROPERTIES);
        currentList.addSimpleTypeMarker();
        currentList.addExplicit("{" + IMXMLLanguageConstants.PROPERTY_OUTER_DOCUMENT + ": this}");
    }
    
    void processMXMLEmbed(IMXMLEmbedNode node, Context context)
    {
    	MXMLNotImplementedProblem problem = new MXMLNotImplementedProblem(node, "MXML Embedding");
        getProblems().add(problem);
    }

    void processMXMLXML(IMXMLXMLNode node, Context context)
    {
    	MXMLNotImplementedProblem problem = new MXMLNotImplementedProblem(node, "MXML XML");
        getProblems().add(problem);
    }
    
    void processMXMLXMLList(IMXMLXMLListNode node, Context context)
    {
    	MXMLNotImplementedProblem problem = new MXMLNotImplementedProblem(node, "MXML XMLList");
        getProblems().add(problem);
    }

    /**
     * Copied from JSClassDirectiveProcessor
     * Declare a function. TODO: static vs. instance.
     */
    @Override
    void declareFunction(FunctionNode func)
    {
        func.parseFunctionBody(classScope.getProblems());

        boolean is_constructor = func.isConstructor();

        functionSemanticChecks(func);

        //  Save the constructor function until
        //  we've seen all the instance variables
        //  that might need initialization.
        if (is_constructor)
        {
            this.ctorFunction = func;
        }
        else
        {
            MethodInfo mi = classScope.getGenerator().generateFunction(func, classScope, null);
            ITraitVisitor tv;

            if (mi != null)
            {
                FunctionDefinition funcDef = func.getDefinition();
                Name funcName = funcDef.getMName(classScope.getProject());

                if (func.hasModifier(ASModifier.STATIC))
                    tv = ctraits.visitMethodTrait(functionTraitKind(func, TRAIT_Method), funcName, 0, mi);
                else
                {
                    tv = itraits.visitMethodTrait(functionTraitKind(func, TRAIT_Method), funcName, 0, mi);
                    if (funcDef.getNamespaceReference() instanceof NamespaceDefinition.IProtectedNamespaceDefinition)
                        this.iinfo.flags |= ABCConstants.CLASS_FLAG_protected;
                }

                this.classScope.processMetadata(tv, funcDef.getAllMetaTags());

                if (func.hasModifier(ASModifier.FINAL))
                    tv.visitAttribute(Trait.TRAIT_FINAL, Boolean.TRUE);
                if (func.hasModifier(ASModifier.OVERRIDE))
                    tv.visitAttribute(Trait.TRAIT_OVERRIDE, Boolean.TRUE);
            }
        }
    }

    /**
     * Copied from JSClassDirectiveProcessor
     * Declare a variable. TODO: static vs. instance.
     */
    @Override
    void declareVariable(VariableNode var)
    {
        verifyVariableModifiers(var);

        DefinitionBase varDef = var.getDefinition();
        JSGenerator jsGenerator = (JSGenerator)classScope.getGenerator();
        jsGenerator.getReducer().startDeclareVariable(varDef);

        boolean is_static = var.hasModifier(ASModifier.STATIC);
        boolean is_const = SemanticUtils.isConst(var, classScope.getProject());
        // simple initializers for public/protected vars go right on prototype.
        // the rest (all private vars), all "complex" initializers (like array) get
        // initialized in the constructor
        boolean needs_constructor_init = true;
        
        //  generateConstantValue() returns null if no constant value
        //  can be generated, and null is the correct value for "no value."
        IConstantValue constantValue =  jsGenerator.generateConstantValue(var.getAssignedValueNode(), this.classScope.getProject());

        //  initializer is null if no constant value
        //  can be generated, and null is the correct value for "no value."
        Object initializer = constantValue != null ? constantValue.getValue() : null;

        ITraitVisitor tv = declareVariable(var, varDef, is_static, is_const, initializer);

        this.classScope.processMetadata(tv, varDef.getAllMetaTags());

        //  Generate variable initializers and append them to the 
        //  proper initialization list.
        if (var.getChildCount() > 1)
        {
            //  We need to put the correct traits visitor on the class'
            //  LexicalScope; the BURM may encounter variable declarations
            //  chained onto this one, and it will need the traits visitor to declare them.

            //  Save the scope's current traits visitor (which should be null)
            //  and restore it 
            ITraitsVisitor saved_traits_visitor = this.classScope.traitsVisitor;
            assert (saved_traits_visitor == null);
            try
            {
                // the following line causes duplicate Traits.
                // JSEmitter::emitTraits works around duplicate Traits by checking against
                // a visitedTraits set.
                this.classScope.traitsVisitor = (is_static) ? ctraits : itraits;
                this.classScope.resetDebugInfo();
                InstructionList init_expression = jsGenerator.generateInstructions(var, CmcJSEmitter.__statement_NT, this.classScope);
                if (init_expression != null && !init_expression.isEmpty())
                {
                    // final JSEmitter emitter = (JSEmitter)this.classScope.getEmitter();
                    final String str = JSGeneratingReducer.instructionListToString(init_expression, true);

                    if (str.contains(" = "))
                    {
                        final String varInit = jsGenerator.getReducer().getVariableInitializer(varDef);
                        if (varInit != null && !varInit.isEmpty())
                        {
                            // set the value of the slot trait.
                            final String varName = varDef.getBaseName();
                            for (Trait t : this.classScope.traitsVisitor.getTraits())
                            {
                                final byte kind = t.getKind();
                                if (kind == TRAIT_Const || kind == TRAIT_Var)
                                {
                                	boolean is_private = false;
                                    final Name name = t.getNameAttr(Trait.TRAIT_NAME);
                                    Namespace ns = name.getSingleQualifier();
                                    if (ns.getKind() == CONSTANT_PrivateNs)
                                    	is_private = true;
                                    if (name.getBaseName().equals(varName))
                                    {
                                        t.setAttr(Trait.SLOT_VALUE, varInit);
                                        if (!is_private)
                                        	needs_constructor_init = false;
                                        break;
                                    }
                                }
                            }

                        }

                        if (is_static)
                        {
                            // see finishClassDefinition.
                            // We clear cinitInsns only if there are no side effects
                            // by initializing the static members directly.
                            // If varInit is null, or varInit is isEmpty() 
                            // then we have side effects. 
                            if (!init_expression.isEmpty())
                                registerClassInit(var);

                            cinitInsns.addAll(init_expression);
                        }
                        else if (needs_constructor_init)
                            iinitInsns.addAll(init_expression);
                    }
                }
            }
            finally
            {
                this.classScope.traitsVisitor = saved_traits_visitor;
            }
        }

        jsGenerator.getReducer().endDeclareVariable(varDef);
    }
    
    private void registerClassInit(IASNode node)
    {
        final String fullName = JSGeneratingReducer.createFullNameFromDefinition(classScope.getProject(), classDefinition);
        if (!fullName.equals(JSSharedData.JS_FRAMEWORK_NAME))
        {
            JSGenerator jsGenerator = (JSGenerator)classScope.getGenerator();
            JSSharedData.instance.registerClassInit(fullName);
            jsGenerator.getReducer().warnClassInitPerformance(node);
            jsGenerator.getReducer().setNeedsSecondPass();
        }
    }

    /**
     * Determines the Name of the event handler method for an event node.
     * This can get called to preassign the name before the method gets generated.
     */
    protected Name getEventHandlerName(IMXMLEventSpecifierNode eventNode)
    {
        // Check the map to see if a handler name
        // has already been assigned to this event node.
        Name name = eventHandlerMap.get(eventNode);
        
        // If so, return it.
        if (name != null)
            return name;
        
        // Otherwise, generate the next one in the sequence "$EH0", "$EH1", etc.
        String baseName = EVENT_HANDLER_NAME_BASE + eventHandlerCounter++;
        
        name = new Name(baseName);
        
        // Remember it in the map.
        eventHandlerMap.put(eventNode, name);
        
        return name;
    }

    static class FragmentList
    {
    	ArrayList<String> list = new ArrayList<String>();
    	
    	void add(boolean value)
    	{
    		if (value)	
    			list.add("true");
    		else
    			list.add("false");
    	}

    	void add(int value)
    	{
    		list.add(new Integer(value).toString());
    	}
    	
    	void add(long value)
    	{
    		list.add(new Long(value).toString());
    	}
    	
    	void add(double value)
    	{
    		list.add(new Double(value).toString());
    	}

    	void add(Name name)
    	{
    		StringBuilder sb = new StringBuilder();
    		String s = name.getSingleQualifier().getName();
    		if (s.length() > 0)
    		{
        		sb.append(s);
        		sb.append(".");    			
    		}
    		sb.append(name.getBaseName());
    		list.add(sb.toString());
    	}
    	
    	void add(String string)
    	{
    		list.add("'" + string + "'");
    	}
    	
    	void addExplicit(String string)
    	{
    		list.add(string);
    	}

    	void addAll(FragmentList addlist)
    	{
    		list.addAll(addlist.list);
    	}
    	
    	void addSimpleTypeMarker()
    	{
    		list.add("true");
    	}
    	
    	void addObjectTypeMarker()
    	{
    		list.add("false");
    	}
    	
    	void addArrayTypeMarker()
    	{
    		list.add("null");
    	}
    	
    	void addNull()
    	{
    		list.add("null");
    	}
    	
    	int size()
    	{
    		return list.size();
    	}
    	
    	String get(int i)
    	{
    		return list.get(i);
    	}

    	public String toString()
    	{
    		StringBuilder sb = new StringBuilder();
    		int n = list.size();
    		for (int i = 0; i < n; i++)
    		{
    			sb.append(list.get(i));
    			if (i < n - 1)
    				sb.append(", ");
    		}
    		return sb.toString();
    	}
    	
    }
}
