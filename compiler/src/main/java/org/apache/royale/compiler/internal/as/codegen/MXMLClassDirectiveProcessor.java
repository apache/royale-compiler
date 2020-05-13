/*
 *
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

import static org.apache.royale.abc.ABCConstants.CONSTANT_Qname;
import static org.apache.royale.abc.ABCConstants.OP_applytype;
import static org.apache.royale.abc.ABCConstants.OP_call;
import static org.apache.royale.abc.ABCConstants.OP_callproperty;
import static org.apache.royale.abc.ABCConstants.OP_callpropvoid;
import static org.apache.royale.abc.ABCConstants.OP_callsupervoid;
import static org.apache.royale.abc.ABCConstants.OP_construct;
import static org.apache.royale.abc.ABCConstants.OP_constructprop;
import static org.apache.royale.abc.ABCConstants.OP_constructsuper;
import static org.apache.royale.abc.ABCConstants.OP_dup;
import static org.apache.royale.abc.ABCConstants.OP_finddef;
import static org.apache.royale.abc.ABCConstants.OP_findpropstrict;
import static org.apache.royale.abc.ABCConstants.OP_getglobalscope;
import static org.apache.royale.abc.ABCConstants.OP_getlex;
import static org.apache.royale.abc.ABCConstants.OP_getlocal0;
import static org.apache.royale.abc.ABCConstants.OP_getlocal1;
import static org.apache.royale.abc.ABCConstants.OP_getlocal2;
import static org.apache.royale.abc.ABCConstants.OP_getlocal3;
import static org.apache.royale.abc.ABCConstants.OP_getproperty;
import static org.apache.royale.abc.ABCConstants.OP_getsuper;
import static org.apache.royale.abc.ABCConstants.OP_jump;
import static org.apache.royale.abc.ABCConstants.OP_iffalse;
import static org.apache.royale.abc.ABCConstants.OP_ifne;
import static org.apache.royale.abc.ABCConstants.OP_iftrue;
import static org.apache.royale.abc.ABCConstants.OP_newarray;
import static org.apache.royale.abc.ABCConstants.OP_newfunction;
import static org.apache.royale.abc.ABCConstants.OP_newobject;
import static org.apache.royale.abc.ABCConstants.OP_not;
import static org.apache.royale.abc.ABCConstants.OP_pop;
import static org.apache.royale.abc.ABCConstants.OP_popscope;
import static org.apache.royale.abc.ABCConstants.OP_pushdouble;
import static org.apache.royale.abc.ABCConstants.OP_pushfalse;
import static org.apache.royale.abc.ABCConstants.OP_pushnull;
import static org.apache.royale.abc.ABCConstants.OP_pushscope;
import static org.apache.royale.abc.ABCConstants.OP_pushstring;
import static org.apache.royale.abc.ABCConstants.OP_pushtrue;
import static org.apache.royale.abc.ABCConstants.OP_pushuint;
import static org.apache.royale.abc.ABCConstants.OP_pushundefined;
import static org.apache.royale.abc.ABCConstants.OP_returnvalue;
import static org.apache.royale.abc.ABCConstants.OP_returnvoid;
import static org.apache.royale.abc.ABCConstants.OP_setlocal1;
import static org.apache.royale.abc.ABCConstants.OP_setlocal2;
import static org.apache.royale.abc.ABCConstants.OP_setlocal3;
import static org.apache.royale.abc.ABCConstants.OP_setproperty;
import static org.apache.royale.abc.ABCConstants.OP_swap;
import static org.apache.royale.abc.ABCConstants.TRAIT_Class;
import static org.apache.royale.abc.ABCConstants.TRAIT_Getter;
import static org.apache.royale.abc.ABCConstants.TRAIT_Method;
import static org.apache.royale.abc.ABCConstants.TRAIT_Setter;
import static org.apache.royale.abc.ABCConstants.TRAIT_Var;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.ADD_EVENT_LISTENER_CALL_OPERANDS;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.ADD_LAYER_CALL_OPERANDS;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.CREATE_XML_DOCUMENT_CALL_OPERANDS;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.CONCAT_CALL_OPERANDS;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.EXECUTE_BINDINGS_CALL_OPERANDS;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.GET_INSTANCE_CALL_OPERANDS;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.INITIALIZED_CALL_OPERANDS;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.INITIALIZE_CALL_OPERANDS;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_ARRAY;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_BOOLEAN;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_CURRENT_STATE;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_DEFAULT_FACTORY;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_DESIGN_LAYER;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_DOCUMENT;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_HANDLER_FUNCTION;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_ID;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_INITIALIZE;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_NAME;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_OBJECT;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_OVERRIDES;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_STYLE_DECLARATION;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_STYLE_MANAGER;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_TARGET;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_UNDERBAR_DOCUMENT;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_UNDERBAR_DOCUMENT_DESCRIPTOR;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_VALUE;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.NAME_VOID;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.REGISTER_EFFECTS_CALL_OPERANDS;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.SET_DOCUMENT_DESCRIPTOR_CALL_OPERANDS;
import static org.apache.royale.compiler.mxml.IMXMLTypeConstants.SET_STYLE_CALL_OPERANDS;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Label;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.abc.semantics.Trait;
import org.apache.royale.abc.visitors.IABCVisitor;
import org.apache.royale.abc.visitors.IMethodBodyVisitor;
import org.apache.royale.abc.visitors.IMethodVisitor;
import org.apache.royale.abc.visitors.ITraitVisitor;
import org.apache.royale.abc.visitors.ITraitsVisitor;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.IMetaInfo;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.definitions.IAppliedVectorDefinition;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.IReferenceMName;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.exceptions.CodegenInterruptedException;
import org.apache.royale.compiler.internal.abc.FunctionGeneratorHelper;
import org.apache.royale.compiler.internal.caches.CSSDocumentCache;
import org.apache.royale.compiler.internal.codegen.databinding.BindingInfo;
import org.apache.royale.compiler.internal.codegen.databinding.MXMLBindingDirectiveHelper;
import org.apache.royale.compiler.internal.css.codegen.CSSCompilationSession;
import org.apache.royale.compiler.internal.css.codegen.CSSReducer;
import org.apache.royale.compiler.internal.css.codegen.CSSEmitter;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.EventDefinition;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.TypeDefinitionBase;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.resourcebundles.ResourceBundleUtils;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.tree.as.VariableNode;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;
import org.apache.royale.compiler.mxml.IMXMLTypeConstants;
import org.apache.royale.compiler.problems.AccessUndefinedPropertyProblem;
import org.apache.royale.compiler.problems.CSSCodeGenProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLExecutableStatementsInScriptBlockProblem;
import org.apache.royale.compiler.problems.MXMLOuterDocumentAlreadyDeclaredProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.projects.IRoyaleProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.mxml.IMXMLArrayNode;
import org.apache.royale.compiler.tree.mxml.IMXMLBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLBooleanNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassReferenceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClearNode;
import org.apache.royale.compiler.tree.mxml.IMXMLComponentNode;
import org.apache.royale.compiler.tree.mxml.IMXMLConcatenatedDataBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDataBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDeclarationsNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDeferredInstanceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDefinitionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDesignLayerNode;
import org.apache.royale.compiler.tree.mxml.IMXMLEffectSpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLEmbedNode;
import org.apache.royale.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLExpressionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFactoryNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFunctionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLImplementsNode;
import org.apache.royale.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLIntNode;
import org.apache.royale.compiler.tree.mxml.IMXMLLibraryNode;
import org.apache.royale.compiler.tree.mxml.IMXMLMetadataNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelPropertyContainerNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelPropertyNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelRootNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNumberNode;
import org.apache.royale.compiler.tree.mxml.IMXMLObjectNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPrivateNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLRegExpNode;
import org.apache.royale.compiler.tree.mxml.IMXMLRemoteObjectMethodNode;
import org.apache.royale.compiler.tree.mxml.IMXMLReparentNode;
import org.apache.royale.compiler.tree.mxml.IMXMLRepeaterNode;
import org.apache.royale.compiler.tree.mxml.IMXMLResourceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLScriptNode;
import org.apache.royale.compiler.tree.mxml.IMXMLSingleDataBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLSpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStateNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStringNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStyleNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStyleSpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLUintNode;
import org.apache.royale.compiler.tree.mxml.IMXMLVectorNode;
import org.apache.royale.compiler.tree.mxml.IMXMLWebServiceOperationNode;
import org.apache.royale.compiler.tree.mxml.IMXMLXMLListNode;
import org.apache.royale.compiler.tree.mxml.IMXMLXMLNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.workspaces.IWorkspace;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

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
public class MXMLClassDirectiveProcessor extends ClassDirectiveProcessor
{
    /**
     * Most autogenerated names go into a special private namespace
     * which can't be accessed from ActionScript.
     * <p>
     * Exceptions:
     * <ul>
     * <li>Names of event handlers that are referenced in descriptors.
     * </ul>
     */
    private static final Namespace MXML_PRIVATE_NAMESPACE =
        new Namespace(ABCConstants.CONSTANT_PrivateNs, "MXMLPrivateNS");
    
    /**
     * A namespace set containing just the special private namespace.
     */
    private static final Nsset MXML_PRIVATE_NAMESPACE_SET =
        new Nsset(MXML_PRIVATE_NAMESPACE);
    
    /**
     * Autogenerated event handler methods are named >0, >1, etc.
     * Using short names, and using the same names in each MXML document,
     * decreases SWF size.
     * Using a character that is not legal in ActionScript identifiers
     * means that even if the event handler must be public
     * (because it is referenced in a descriptor)
     * the name will not collide with the name of a developer-written
     * method and cannot be accessed from developer code.
     */
    private static final String EVENT_HANDLER_NAME_BASE = ">";
    
    /**
     * Autogenerated vector generator methods are named >v0, >v1, etc.
     * Using short names, and using the same names in each MXML document,
     * decreases SWF size.
     * Using a character that is not legal in ActionScript identifiers
     * means that even if the event handler must be public
     * (because it is referenced in a descriptor)
     * the name will not collide with the name of a developer-written
     * method and cannot be accessed from developer code.
     */
    private static final String VECTOR_GENERATOR_NAME_BASE = ">v";

    /**
     * Autogenerated instance initialization methods are named i0, i1, etc.
     */
    private static final String INSTANCE_INITIALIZER_NAME_BASE = "i";
    
    /**
     * Autogenerated bindable overrides are named _bo0, _bo1, etc.
     */
    private static final String BINDABLE_OVERRIDE_NAME_BASE = "_bo";
    
    /**
     * constants used to generate old-style XMLNode's from the XML tag
     */
    private static final Object[] CONSTRUCT_XML_LIST_OPERANDS = new Object[] { ABCGeneratingReducer.xmlListType, 1 };
    
    /**
     * An inaccessible instance variable named <code>dd</code>
     * is generated to store the class's document descriptor,
     * which is the <code>UIComponentDescriptor</code>
     * that is the root of a tree of descriptors.
     * <p>
     * This variable is initialized in the constructor
     * and then passed to <code>setDocumentDescriptor</code>
     * in an override of the <code>initialize()</code> method.
     * <p>
     * In the old compiler, this variable was called
     * <code>_documentDescriptor_</code> and was private
     * <pre>
     * private var _documentDescriptor_ : mx.core.UIComponentDescriptor = 
     *     new mx.core.UIComponentDescriptor(...);
     * </pre>
     * but could potentially have collided with a developer variable
     * or getter/setter with the same name.
     */
    private static Name NAME_DOCUMENT_DESCRIPTOR = createMXMLPrivateName("dd");
    
    /**
     * An inaccessible instance variable named "mfi"
     * is generated to store a flag indicating whether
     * the <code>moduleFactory</code> setter has been called.
     * <p>
     * This variable is initially <code>false</code> by default,
     * and then set to <code>true</code> in the
     * <code>moduleFactory</code> setter.
     * <p>
     * In the old compiler, this variable was called
     * <code>__moduleFactoryInitialized</code> and was private
     * <pre>
     * private var __moduleFactoryInitialized:Boolean = false;
     * </pre>
     * but could potentially have collided with a developer variable
     * or getter/setter with the same name.
     */
    private static Name NAME_MODULE_FACTORY_INITIALIZED = createMXMLPrivateName("mfi");
    
    private static Name NAME_MXML_DESCRIPTOR = createMXMLPrivateName("mxmldd");
    private static Name NAME_MXML_DESCRIPTOR_GETTER = new Name("MXMLDescriptor");
    private static Name NAME_MXML_PROPERTIES = createMXMLPrivateName("mxmldp");
    private static Name NAME_MXML_STATE_DESCRIPTOR = new Name("mxmlsd");
    private static Name NAME_GENERATE_CSSSTYLEDECLARATIONS = new Name("generateCSSStyleDeclarations");

    /**
     * Wraps an unqualified name (such as ">0" for an event handler method
     * or "i0" for an instance initialzier method) into a Name
     * of type Qname with the special private MXML namespace.
     */
    private static Name createMXMLPrivateName(String baseName)
    {
        return new Name(CONSTANT_Qname, MXML_PRIVATE_NAMESPACE_SET, baseName); 
    }

    /**
     * Creates a MethodInfo describing the signature for an autogenerated
     * MXML event handler method corresponding to an MXMLEventSpecifierNode.
     * @param eventNode - an MXMLEventSpecifierNode, which is used to determine
     * the type of the 'event' parameter.
     * @param handlerName - the name for the autogenerated event handler method.
     * @return The MethodInfo specifying the signature of the event handler method.
     */
    public static MethodInfo createEventHandlerMethodInfo(ICompilerProject project,
                                                          IMXMLEventSpecifierNode eventNode,
                                                          String handlerName)
    {       
        MethodInfo mi = new MethodInfo();
        
        mi.setMethodName(handlerName);
        
        // Event handlers all have one parameter, whose type is determined by
        // the [Event] metadata that declared the event.
        // This type was stored in the EventDefinition
        // that the MXMLEventSpecifierNode refers to.
        EventDefinition eventDefinition = (EventDefinition)eventNode.getDefinition();
        Name eventTypeName = ((IReferenceMName)eventDefinition.getTypeReference()).getMName(
            project, (ASScope)eventDefinition.getContainingScope());
        Vector<Name> paramTypes = new Vector<Name>();
        paramTypes.add(eventTypeName);
        mi.setParamTypes(paramTypes);
        
        Vector<String> paramName = new Vector<String>();
        paramName.add("event");
        mi.setParamNames(paramName);
        
        //  TODO: Allow these MXML nodes to use registers.
        mi.setFlags(mi.getFlags() | ABCConstants.NEED_ACTIVATION);
        
        // Event handlers return void.
        mi.setReturnType(NAME_VOID);
        
        return mi;
    }
    
    /**
     * Creates a MethodInfo describing the signature for an autogenerated
     * vector generator method.
     * @param vectorNode - a node, which is used to determine
     * the type of the 'vector'.
     * @param handlerName - the name for the autogenerated event handler method.
     * @return The MethodInfo specifying the signature of the vector generator method.
     */
    public static MethodInfo createVectorGeneratorMethodInfo(ICompilerProject project,
                                                          IMXMLVectorNode vectorNode,
                                                          String handlerName)
    {       
        MethodInfo mi = new MethodInfo();
        
        mi.setMethodName(handlerName);
        
        ITypeDefinition type = vectorNode.getType();
        Name typeName = ((TypeDefinitionBase)type).getMName(project);
        
        Vector<Name> paramTypes = new Vector<Name>();
        paramTypes.add(IMXMLTypeConstants.NAME_ARRAY);
        mi.setParamTypes(paramTypes);
        
        Vector<String> paramName = new Vector<String>();
        paramName.add("array");
        mi.setParamNames(paramName);
        
        //  TODO: Allow these MXML nodes to use registers.
        mi.setFlags(mi.getFlags() | ABCConstants.NEED_ACTIVATION);
        
        // Event handlers return void.
        mi.setReturnType(typeName);
        
        return mi;
    }
    
    /**
     * Creates a MethodInfo describing the signature for an autogenerated
     * MXML instance initializer corresponding to an MXMLInstanceNode.
     * Instance initializers have no parameters, and their return type
     * is the type of the instance that they create.
     */
    private static MethodInfo createInstanceInitializerMethodInfo(String name, Name type)
    {
        MethodInfo mi = new MethodInfo();        
        mi.setMethodName(name);
        mi.setReturnType(type);
        return mi;
    }
    
    /**
     * Initialize the {@link MXMLClassDirectiveProcessor} and its
     * associated AET data structures.
     * @param d - the MXML document's AST.
     * @param enclosingScope - the immediately enclosing lexical scope.
     * @param emitter - the active ABC emitter.
     */
    MXMLClassDirectiveProcessor(IMXMLClassDefinitionNode classDefinitionNode,
                                LexicalScope enclosingScope, IABCVisitor emitter)
    {
        super(classDefinitionNode, (ClassDefinition)classDefinitionNode.getClassDefinition(),
              enclosingScope, emitter);
        
        this.classDefinitionNode = classDefinitionNode;
        
        globalScope = enclosingScope;
    }
    
    /**
     * The class definition node for which this processor is generating a class.
     */
    private final IMXMLClassDefinitionNode classDefinitionNode;
        
    /**
     * The AET lexical scope in which the generated class lives.
     */
    protected final LexicalScope globalScope;
    
    /**
     * number of attributes on top tag that weren't bindable
     */
    private int numElements = 0;
    
    /**
     * An incrementing counter used to create the names of the
     * auto-generated instance initializer methods.
     */
    private int instanceInitializerCounter = 0;
    
    /**
     * An incrementing counter used to create the names of the
     * auto-generated bindable overrides.
     */
    private int bindableOverrideCounter = 0;
    
    /**
     * An incrementing counter used to create the names of the
     * auto-generated event handler methods.
     */
    private int eventHandlerCounter = 0;
    
    /**
     * An incrementing counter used to create the names of the
     * auto-generated vector generator methods.
     */
    private int vectorGeneratorCounter = 0;

    /**
     * We delegate much of the work for databindings down to this guy
     */
    private final MXMLBindingDirectiveHelper bindingDirectiveHelper = new MXMLBindingDirectiveHelper(this, emitter);
    
    /**
     * Instructions to place in the class' constructor
     * after the constructsuper opcode.
     * This is currently used only for initializing
     * MXML properties, styles, and events.
     * However, this is really where AS instance vars
     * should be initialized, even though the old compiler
     * didn't do it that way.
     */
    private final InstructionList iinitAfterSuperInsns = new InstructionList();
        
    /**
     * Instructions to place in the class' constructor
     * after the constructsuper opcode.
     * This is currently used only for initializing
     * MXML properties, styles, and events for non-public
     * properties when mxml.children-as-data is true.
     */
    private final InstructionList iinitForNonPublicProperties = new InstructionList();
        
    /**
     * Instructions to place in the class' constructor
     * that represent the MXML properties.
     * This is currently used only for initializing
     * MXML properties, styles, and events on the main tag.
     */
    private final InstructionList mxmlPropertiesInsns = new InstructionList();
        
    /**
     * A Map mapping an instance node to the Name of the instance
     * initializer method (i0, i1, etc.) that creates it.
     * <P>
     * The initializer method may or may not exist at the time
     * that the initializer name is assigned to the instance node.
     * For example, when a State tag appears before
     * an state-dependent instance tag,
     * the name will get assigned and the code generated later.
     * <p>
     * This map is managed ONLY by getInstanceInitializerName().
     */
    private final Map<IMXMLInstanceNode, Name> instanceInitializerMap =
        new HashMap<IMXMLInstanceNode, Name>();
    
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
     * A Map mapping an vector type to the Name of the vector "generator"
     * method (>v0, >v1, etc.) associated with that node.
     * <p>
     * The handler method may or may not exist at the time
     * that the handler name is assigned to the event node.
     * For example, when a State tag appears before
     * an instance tag with a state-dependent event,
     * the name will get assigned and the code generated later.
     * <p>
     * This map is managed ONLY by getVectorGeneratorName().
     */
    private final Map<Name, Name> VectorGeneratorMap =
        new HashMap<Name, Name>();

    /**
     * This flag keeps track of whether there were styles
     * specified on the class definition tag.
     * <p>
     * If so, the <code>moduleFactory</code> setter will
     * be overridden to initialize these styles. 
     */
    private boolean hasStyleSpecifiers = false;
    
    /**
     * This flag keeps track of whether there were effects
     * specified on the class definition tag.
     * <p>
     * If so, the <code>moduleFactory</code> setter will
     * be overridden to initialize these effects.
     */
    private boolean hasEffectSpecifiers = false;
    
    /**
     * The unique identifier of style variables.
     * The code will generate factoryFunctions0,
     * factoryFuctions1, etc.
     * <p>
     * If so, the {@code moduleFactory} setter will
     * be overridden to initialize any styles. 
     */
    private int styleTagIndex = 0;
    
    /**
     * This keeps track of the entries in our temporary array of 
     * DeferredInstanceFromFunction objects that we CG to help with
     * State override CG.
     * 
     * Keys are Instance nodes,
     * values are the array index where the deferred instance is:
     * 
     *  deferred instance = local3[ nodeToIndexMap.get(an instance) ]
     */
    protected Map<IMXMLNode, Integer> nodeToIndexMap;
    
    protected Map<IMXMLNode, InstructionList> nodeToInstanceDescriptorMap;
    protected Map<Integer, IMXMLNode> indexToNodeMap;
    
    /**
     * This method is called by the {@code GlobalDirectiveProcessor}
     * after it constructs this {@link MXMLClassDirectiveProcessor}
     * and before it calls {@code finishClassDefinition()}.
     * <p>
     * It is therefore where all the code generation for a single MXML
     * class happens.
     * <p>
     * If during the recursive descent another class definition node
     * is found (such as within a <code>&lt;Component&gt;</code>
     * or <code>&lt;Definition&gt;</code> tag) then the
     * {@link #processMXMLClassDefinition} method) creates
     * another {@link MXMLClassDirectiveProcessor} to generate
     * the code for that class. This processes continues recursively,
     * since <code>&lt;Component&gt;</code> can be nested within
     * <code>&lt;Component&gt;</code> or <code>&lt;Definition&gt;</code>.
     */
    void processMainClassDefinitionNode(IMXMLClassDefinitionNode node)
    {
        // Create an initial Context for adding instructions
        // into the class constructor after the super() call.
        Context context = new Context(classDefinitionNode, iinitAfterSuperInsns);
        
        setupDeferredInstancesForStateDependentInstances(context);
          
        // Process child nodes. They will populate the context's various
        // secondary instruction lists (such as propertiesInstructionList,
        // stylesInstructionList, etc.)
        // This will not generate any instructions in the context's
        // main instruction list.
        traverse(node, context);
                
        if (!getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            // Set the document descriptor for the class if appropriate.
            setDocumentDescriptorForClass(node, context);
        }
        else
        {
            addVariableTrait(NAME_MXML_DESCRIPTOR, IMXMLTypeConstants.NAME_ARRAY);
            addVariableTrait(NAME_MXML_PROPERTIES, IMXMLTypeConstants.NAME_ARRAY);
            addVariableTrait(NAME_MXML_STATE_DESCRIPTOR, IMXMLTypeConstants.NAME_ARRAY);
        }
        
        // Set the document for the class.
        setDocument(node, false, context);
        
        // Generate code in the constructor to set properties,
        // then styles, then events, then effects.
        numElements = setSpecifiers(context, getProject().getTargetSettings().getMxmlChildrenAsData(), true);
        
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            overrideMXMLDescriptorGetter(node, context);
            if (numElements > 0)
                overrideMXMLPropertiesGetter(node, context, numElements);
        }

        generateBindableImpl();
        
        generateRequiredContingentDefinitions();
        
        generateStylesAndEffects(context);
    }
    
  
    /**
     * Does the following:
     *      initializes nodeToIndexMap, so that later CG can find deferred instances
     *      creates an array of deferred instances - one for each state dependent instance node
     *      
     * This is all so that when we are CGing the spark.State objects later, we can make the
     * AddItems overrides that they will need.
     * 
     * Note that we are storing our array in local3, and assuming that no other CG
     * will mess with local 3. It might be safer to use names local?
     */
    
    private void setupDeferredInstancesForStateDependentInstances(Context context)
    {
        // First round up all the the state dependent instance nodes
        List<IMXMLNode> stateDependentNodes = classDefinitionNode.getAllStateDependentNodes();
        if (stateDependentNodes==null)
            return;
    
        RoyaleProject project = getProject();
        String deferredInstanceFromFunctionClass = project.getDeferredInstanceFromFunctionClass();
        Name deferredInstanceFromFunctionName = project.getDeferredInstanceFromFunctionName();

        if (!project.getTargetSettings().getMxmlChildrenAsData() && !(classDefinition.isInstanceOf("mx.core.IStateClient2", project)))
        {
            final IResolvedQualifiersReference stateClient2Reference = ReferenceFactory.packageQualifiedReference(
                    this.getProject().getWorkspace(),
                    "mx.core.IStateClient2");
            final Name stateClient2Name = stateClient2Reference.getMName();

            IReference[] implementedInterfaces = classDefinition.getImplementedInterfaceReferences();
            IReference[] newInterfaces = null;
            Name[] newNames = null;
            if (implementedInterfaces != null)
            {
                int n = implementedInterfaces.length;
                newInterfaces = new IReference[n + 1];
                newNames = new Name[n + 1];
                for (int i = 0; i < n; i++)
                {
                    newInterfaces[i] = implementedInterfaces[i];
                    newNames[i] = iinfo.interfaceNames[i];
                }
                newInterfaces[n] = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), "mx.core.IStateClient2");
                newNames[n] = stateClient2Name;
            }
            else
            {
                newInterfaces = new IReference[1];
                newInterfaces[0] = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), "mx.core.IStateClient2");
                newNames = new Name[1];
                newNames[0] = stateClient2Name;
            }
            classDefinition.setImplementedInterfaceReferences(newInterfaces);
            iinfo.interfaceNames = newNames;
        }
        
        // now, process all the state dependent nodes
        
        int instanceNodeCounter = 0;
        IASNode anInstanceNode = null;
        for   (IMXMLNode node : stateDependentNodes)
        {
             // here we only care about instance nodes
            if (node instanceof IMXMLInstanceNode)
            {
                anInstanceNode = node;
                // Generate a map that tell for each state dependent instance node, what slot
                // it corresponds to in the array of 
                // deferredInstanceFromFunction's
                if (nodeToIndexMap==null)
                {
                    nodeToIndexMap = new HashMap<IMXMLNode, Integer>();
                    indexToNodeMap = new HashMap<Integer, IMXMLNode>();
                }
                nodeToIndexMap.put(node, instanceNodeCounter);
                indexToNodeMap.put(instanceNodeCounter, node);
                ++instanceNodeCounter;  
                
                InstructionList il;
                if (getProject().getTargetSettings().getMxmlChildrenAsData())
                {
                    if (nodeToInstanceDescriptorMap==null)      
                        nodeToInstanceDescriptorMap = new HashMap<IMXMLNode, InstructionList>();
                    il = new InstructionList();
                    nodeToInstanceDescriptorMap.put(node, il);
                    // build the initializer function by processing the node
                    Context stateContext = new Context((IMXMLInstanceNode)node, il);
                    stateContext.isContentFactory = true;
                    processNode(node, stateContext);
                    stateContext.transfer(IL.MXML_CONTENT_FACTORY);
                    stateContext.addInstruction(OP_newarray, stateContext.getCounter(IL.MXML_CONTENT_FACTORY));      
                }
                else
                {
                    context.addInstruction(OP_findpropstrict, deferredInstanceFromFunctionName);  
                    // stack: ..., DeferredInstaceFromFunction class
                    
                    // build the initializer function by processing the node
                    processNode(node, context);
                    // stack: ..., DeferredInstaceFromFunction class, initializerFunc 
                    
                    context.addInstruction(OP_constructprop, new Object[] { deferredInstanceFromFunctionName, 1});   
                    // stack: ..., DeferredInstaceFromFunction object
                }
            }
        }
        
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
            return;

        // if we didn't find any state dependent instance nodes, then leave
        if (instanceNodeCounter==0)
            return;
                
        // stack: ..., arg[0], arg[1],.., arg[n-1]
        context.addInstruction(OP_newarray, instanceNodeCounter);
        context.addInstruction(OP_setlocal3);
        
        // now local3= array of deferredInstanceFromFunctionName
        
        // make a dependency on the sdk class DeferredInstanceFromFunction
        IWorkspace workspace = project.getWorkspace();
        IResolvedQualifiersReference ref = ReferenceFactory.packageQualifiedReference(workspace, deferredInstanceFromFunctionClass);
       
        IScopedNode scopedNode = anInstanceNode.getContainingScope();
        IASScope iscope = scopedNode.getScope();
        ASScope scope = (ASScope)iscope;
        
        if (ref == null)
            assert false;
        IDefinition def = ref.resolve(project, scope, DependencyType.EXPRESSION, false);
        if (def == null)
            assert false;
    }
     
    /**
     * Traverse the children of a root node and process them.
     * @param root - the root node.  The root is not processed.
     */
    void traverse(IASNode node, Context context)
    {
        traverse(node, context, null);
    }

    /**
     * Traverse filtered children of a root node and process them with
     * {@link #processNode()}.
     * 
     * @param root The root node. The root is not processed.
     * @param context Context.
     * @param filter Children filter; Use {@code null} to traverse all children.
     */
    void traverse(IASNode node, Context context, Predicate<IASNode> filter)
    {
        for (int i = 0; i < node.getChildCount(); i++)
        {
            final IASNode child = node.getChild(i);
            
            // Skip MXML nodes that have been marked as invalid for code generation.
            if (child instanceof IMXMLNode &&
                !((IMXMLNode)child).isValidForCodeGen())
            {
                continue;
            }
            
            if (filter == null || filter.apply(child))
            {
                // process all the children, except for state dependent instances.
                // Those are created by DeferredInstance objects, and the CG
                // path is much different
                if (!isStateDependentInstance(child)) 
                    processNode(child, context);
            }
        }
    }
    
    /**
     * This override adds processing for MXML-specific node IDs.
     * A call to the supermethod handles the regular AS nodes
     * that can appear in an MXML AST.
     */
    void processNode(IASNode node, Context parentContext)
    {
        final boolean newContextRequired = isNewContextRequired(node);
        final Context childContext;
        if (newContextRequired)
            childContext = beginContext(node, parentContext);
        else
            childContext = parentContext;
        
        switch (node.getNodeID())
        {
            case MXMLBooleanID:
            {
                processMXMLBoolean((IMXMLBooleanNode)node, childContext);
                break;
            }
            case MXMLIntID:
            {
                processMXMLInt((IMXMLIntNode) node, childContext);
                break;
            }
            case MXMLUintID:
            {
                processMXMLUint((IMXMLUintNode) node, childContext);
                break;
            }
            case MXMLNumberID:
            {
                processMXMLNumber((IMXMLNumberNode) node, childContext);
                break;
            }
            case MXMLStringID:
            {
                processMXMLString((IMXMLStringNode) node, childContext);
                break;
            }
            case MXMLClassID:
            {
                processMXMLClass((IMXMLClassNode) node, childContext);
                break;
            }
            case MXMLFunctionID:
            {
                processMXMLFunction((IMXMLFunctionNode) node, childContext);
                break;
            }
            case MXMLObjectID:
            {
                processMXMLObject((IMXMLObjectNode) node, childContext);
                break;
            }
            case MXMLArrayID:
            {
                if (parentContext.isContentFactory)
                    childContext.isContentFactory = true;
                processMXMLArray((IMXMLArrayNode) node, childContext);
                break;
            }
            case MXMLVectorID:
            {
                processMXMLVector((IMXMLVectorNode) node, childContext);
                break;
            }
            case MXMLInstanceID:
            case MXMLHTTPServiceID:
            case MXMLWebServiceID:
            case MXMLRemoteObjectID:
            {
                processMXMLInstance((IMXMLInstanceNode) node, childContext);
                break;
            }
            case MXMLFactoryID:
            {
                processMXMLFactory((IMXMLFactoryNode) node, childContext);
                break;
            }
            case MXMLDeferredInstanceID:
            {
                if (parentContext.isContentFactory)
                    childContext.isContentFactory = true;
                processMXMLDeferredInstance((IMXMLDeferredInstanceNode) node, childContext);
                break;
            }
            case MXMLEventSpecifierID:
            {
                processMXMLEventSpecifier((IMXMLEventSpecifierNode) node, childContext);
                break;
            }
            case MXMLHTTPServiceRequestID:
            case MXMLPropertySpecifierID:
            {
                processMXMLPropertySpecifier((IMXMLPropertySpecifierNode) node, childContext);
                break;
            }
            case MXMLStyleSpecifierID:
            {
                processMXMLStyleSpecifier((IMXMLStyleSpecifierNode) node, childContext);
                break;
            }
            case MXMLEffectSpecifierID:
            {
                processMXMLEffectSpecifier((IMXMLEffectSpecifierNode) node, childContext);
                break;
            }
            case MXMLDeclarationsID:
            {
                processMXMLDeclarations((IMXMLDeclarationsNode) node, childContext);
                break;
            }
            case MXMLScriptID:
            {
                processMXMLScript((IMXMLScriptNode) node, childContext);
                break;
            }
            case MXMLStyleID:
            {
                processMXMLStyle((IMXMLStyleNode) node, childContext);
                break;
            }
            case MXMLMetadataID:
            {
                processMXMLMetadata((IMXMLMetadataNode) node, childContext);
                break;
            }
            case MXMLResourceID:
            {
                processMXMLResource((IMXMLResourceNode) node, childContext);
                break;
            }
            case MXMLStateID:
            {
                processMXMLState((IMXMLStateNode) node, childContext);
                break;
            }
            case MXMLDataBindingID:
            {
                processMXMLDataBinding((IMXMLSingleDataBindingNode) node, childContext);
                break;
            }
            case MXMLConcatenatedDataBindingID:
            {
                processMXMLConcatenatedDataBinding((IMXMLConcatenatedDataBindingNode) node, childContext);
                break;
            }
            case MXMLComponentID:
            {
                processMXMLComponent((IMXMLComponentNode) node, childContext);
                break;
            }
            case MXMLLibraryID:
            {
                processMXMLLibrary((IMXMLLibraryNode) node, childContext);
                break;
            }
            case MXMLDefinitionID:
            {
                processMXMLDefinition((IMXMLDefinitionNode) node, childContext);
                break;                
            }
            case MXMLClassDefinitionID:
            {
                processMXMLClassDefinition((IMXMLClassDefinitionNode) node, childContext);
                break;
            }
            case MXMLEmbedID:
            {
                processMXMLEmbed((IMXMLEmbedNode) node, childContext);
                break;
            }
            case MXMLXMLID:
            {
                processMXMLXML((IMXMLXMLNode) node, childContext);
                break;
            }
            case MXMLXMLListID:
            {
                processMXMLXMLList((IMXMLXMLListNode) node, childContext);
                break;
            }
            case MXMLModelID:
            {
                processMXMLModel((IMXMLModelNode) node, childContext);
                break;
            }
            case MXMLModelRootID:
            {
                processMXMLModelRoot((IMXMLModelRootNode) node, childContext);
                break;
            }
            case MXMLModelPropertyID:
            {
                processMXMLModelProperty((IMXMLModelPropertyNode) node, childContext);
                break;
            }
            case MXMLPrivateID:
            {
                processMXMLPrivate((IMXMLPrivateNode) node, childContext);
                break;
            }
            case MXMLWebServiceOperationID:
            {
                processMXMLWebServiceOperation((IMXMLWebServiceOperationNode)node, childContext);
                break;
            }
            case MXMLRemoteObjectMethodID:
            {
                processMXMLRemoteObjectMethod((IMXMLRemoteObjectMethodNode)node, childContext);
                break;
            }
            case MXMLRegExpID:
            {
                processMXMLRegExp((IMXMLRegExpNode)node, childContext);
                break;
            }
            case MXMLClearID:
            {
                processMXMLClear((IMXMLClearNode)node, childContext);
                break;
            }
            case MXMLDesignLayerID:
            {
                processMXMLDesignLayer((IMXMLDesignLayerNode) node, childContext);
                break;
            }
            case MXMLReparentID:
            {
                processMXMLReparent((IMXMLReparentNode)node, childContext);
                break;
            }
            case MXMLBindingID:
            {
                processMXMLBinding((IMXMLBindingNode) node, childContext);
                break;
            }
            case MXMLRepeaterID:
            {
                processMXMLRepeater((IMXMLRepeaterNode) node, childContext);
                break;
            }
            case MXMLImplementsID:
            {
                processMXMLImplements((IMXMLImplementsNode) node, childContext);
            }
            default:
            {
                super.processNode(node);
                break;
            }
        }

        if (newContextRequired)
            endContext(node, childContext, parentContext);
    }

    @Override
    void processDirective(IASNode n)
    {
        // Do nothing for MXML nodes
        if (!n.getNodeID().isMXMLNode()) {
            switch(n.getNodeID())
            {
                case UseID:
                case ImportID:
                case IncludeContainerID:
                case NamespaceID:
                    break; //don't show warning for these statements
                default :
                    ICompilerProblem problem = new MXMLExecutableStatementsInScriptBlockProblem(n);
                    getProblems().add(problem);
                    break;
            }
            super.processDirective(n);
        }
    }
    
    /**
     * This override handles inserting the instructions in iinitAfterSuperInsns
     */
    @Override
    void finishClassDefinition()
    {
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
        IMetaInfo[] metaTags = ClassDirectiveProcessor.getAllMetaTags(classDefinition);
        classScope.processMetadata(tv, metaTags);
        tv.visitEnd();
        
        // Make any vistEnd method calls
        // that were deferred.
        // callVisitEnds must be called on the same thread
        // that started code generation.  Since we don't generate
        // classes in parallel yet, we know that we are on the thread
        // that started code generation here.
        classScope.callVisitEnds();
        
        {
            // Synthesize a constructor.
            iinfo.iInit = new MethodInfo();
            MethodBodyInfo iinit = new MethodBodyInfo();
            iinit.setMethodInfo(iinfo.iInit);
            
            IMethodVisitor mv = emitter.visitMethod(iinfo.iInit);
            mv.visit();
            IMethodBodyVisitor mbv = mv.visitBody(iinit);
            
            InstructionList ctor_insns = new InstructionList();
            
            // Don't even think of removing these instructions!
            // there is lots of code we are generating that assumes that the
            // scopes and such are set up like this!!
            // In particular the data binding code may create anonymous function objects
            // in the constructor that assume "this" is already on the scope stack.
            ctor_insns.addInstruction(OP_getlocal0);
            ctor_insns.addInstruction(OP_pushscope);
            
            // iinitInsns go before the constructsuper opcode.
            ctor_insns.addAll(iinitInsns);
    
            // Call the superclass' constructor after the instance
            // init instructions; this doesn't seem like an abstractly
            // correct sequence, but it's what ASC does.
            ctor_insns.addInstruction(OP_getlocal0);
            ctor_insns.addInstruction(OP_constructsuper, 0);

            // initialize currentState to first state
            // this has to go before other iinit because
            // otherwise setCurrentState will fire off transitions
            setCurrentState(ctor_insns);
            
            if (!getProject().getTargetSettings().getMxmlChildrenAsData())
            {
                // iinitAfterSuperInsns go after the constructsuper opcode.
                ctor_insns.addAll(iinitAfterSuperInsns);
            }
            else
            {
                if (!iinitForNonPublicProperties.isEmpty())
                    ctor_insns.addAll(iinitForNonPublicProperties);
            }
            
            // call the Binding helper to get all the data binding setup code
            addBindingCodeForCtor(ctor_insns);

            if (getProject().getTargetSettings().getMxmlChildrenAsData())
            {
                // if we have state dependent instance nodes add descriptors for them
                if (indexToNodeMap!=null && indexToNodeMap.size() > 0)
                {
                    ctor_insns.addInstruction(OP_getlocal0);           
                    int numNodes = indexToNodeMap.size();
                    for (int i = 0; i < numNodes; i++)
                    {
                        IMXMLNode node = indexToNodeMap.get(Integer.valueOf(i));
                        InstructionList il = nodeToInstanceDescriptorMap.get(node);
                        ctor_insns.addAll(il);
                    }
                    ctor_insns.addInstruction(OP_newarray, numNodes);           
                    
                    ctor_insns.addInstruction(OP_setproperty, NAME_MXML_STATE_DESCRIPTOR);
                }
            }
            
            // add call to MXMLAttributes
            if (getProject().getTargetSettings().getMxmlChildrenAsData() && numElements > 0)
            {
                // generateMXMLAttributes(attributes);
                FunctionDefinition funcDef = (FunctionDefinition)SemanticUtils.findProperty(classDefinition.getContainedScope(), 
                        "generateMXMLAttributes", 
                        getProject(), false);
                if (funcDef != null)
                {
                    Name funcName = ((FunctionDefinition)funcDef).getMName(getProject());
                    ctor_insns.addInstruction(OP_getlocal0);           
                    ctor_insns.addAll(mxmlPropertiesInsns);
                    ctor_insns.addInstruction(OP_callpropvoid, new Object[] {funcName, 1 });
                }
            }

            ctor_insns.addInstruction(OP_returnvoid);
            
            mbv.visit();
            mbv.visitInstructionList(ctor_insns);
            mbv.visitEnd();
            mv.visitEnd();
        }
        
        // If the class has static variables with
        // initialization instructions, emit a class
        // init routine.
        if (!cinitInsns.isEmpty())
        {
            cinfo.cInit = new MethodInfo();
            MethodBodyInfo cinit_info = new MethodBodyInfo();
            cinit_info.setMethodInfo(cinfo.cInit);
            
            IMethodVisitor mv = emitter.visitMethod(cinfo.cInit);
            mv.visit();
            IMethodBodyVisitor mbv = mv.visitBody(cinit_info);
            InstructionList cinit_insns   = new InstructionList();
            cinit_insns.addInstruction(OP_getlocal0);
            cinit_insns.addInstruction(OP_pushscope);
            cinit_insns.addAll(cinitInsns);
            cinit_insns.addInstruction(OP_returnvoid);
            mbv.visit();
            mbv.visitInstructionList(cinit_insns);
            mbv.visitEnd();
            mv.visitEnd();
        }
        
        itraits.visitEnd();
        ctraits.visitEnd();
        
        cv.visitEnd();
    }
    
    /**
     * Creates a tree of <code>UIComponentDescriptor</code> objects
     * for a class definition node, and overrides <code>initialize</code>
     * to pass this tree to the <code>mx_internal</code>
     * <code>setDocumentDescriptor</code> method.
     */
    private void setDocumentDescriptorForClass(IMXMLClassDefinitionNode node, Context context)
    {
        if (node.needsDocumentDescriptor())
        {
            // Create an instance variable
            //   public var _documentDescriptor_:mx.core.UIComponentDescriptor;
            // to store the document descriptor before it is set into the
            // component.
            createDocumentDescriptorVariable(node);
    
            // Create a descriptor tree and set the _documentDescriptor_ var.
            // this._documentDescriptor_ = new UIComponentDescriptor(...);
            context.pushTarget();
            pushDocumentDescriptor(node, context);
            context.addInstruction(OP_setproperty, NAME_DOCUMENT_DESCRIPTOR);
            
            // Override the initialize() method to set the document descriptor
            // into the component by calling
            //   setDocumentDescriptor(_documentDescriptor_);
            overrideInitializeMethod(node);
        }
    }
    
    /**
     * Creates a tree of <code>UIComponentDescriptor</code> objects
     * for an instance node, and sets it as the instance's
     * <code>mx_internal</code> <code>_documentDescriptor</code> to it.
     */
    private void setDocumentDescriptorForInstance(IMXMLInstanceNode node, Context context)
    {
        if (node.needsDocumentDescriptor())
        {
            // temp.mx_internal::_documentDescriptor = new UIComponentDescriptor(...);
            // temp.mx_internal::_documentDescriptor.document = this;
            context.pushTarget();
            pushDocumentDescriptor(node, context);
            context.addInstruction(OP_dup);
            context.addInstruction(OP_getlocal0);
            context.addInstruction(OP_setproperty, NAME_DOCUMENT);
            context.addInstruction(OP_setproperty, NAME_UNDERBAR_DOCUMENT_DESCRIPTOR);            
        }
    }
    
    /**
     * Generates a variable
     * <pre>
     * public var _documentDescriptor_:mx.core.UIComponentDescriptor
     * </pre>
     * to store the document descriptor.
     */
    // TODO Can we put this in the special MXML private namespace?
    // If not, what happens if there is already a variable with the same name?
    private void createDocumentDescriptorVariable(IMXMLClassDefinitionNode node)
    {
        RoyaleProject project = getProject();
        Name uiComponentDescriptorName = project.getUIComponentDescriptorClassName();
        addVariableTrait(NAME_DOCUMENT_DESCRIPTOR, uiComponentDescriptorName);
    }
    
    /**
     * Creates an entire descriptor tree suitable for setting
     * as the document descriptor.
     */
    private void pushDocumentDescriptor(IMXMLClassReferenceNode node, Context context)
    {
        // Build the UIComponentDescriptor in the mainInstructionList.
        buildDescriptor(node, context);
        context.transfer(IL.DESCRIPTOR);
    }
    
    /**
     * Generates instructions in the current context's
     * {@link descriptorInstructionList} that will push a
     * <code>UIComponentDescriptor</code> for an
     * {@link IMXMLClassReferenceNode} onto the stack.
     * Most of the instructions we need are already in the
     * various helper instruction lists of the current context.
     */
    private void buildDescriptor(IMXMLClassReferenceNode node, Context context)
    {
        context.startUsing(IL.DESCRIPTOR);
        
        RoyaleProject project = getProject();
        IClassDefinition instanceClass = node.getClassReference(project);
        
        Name type = ((ClassDefinition)instanceClass).getMName(project);
        
        String id = node instanceof IMXMLInstanceNode ?
                    ((IMXMLInstanceNode)node).getEffectiveID() :
                    null;
        
        // var temp = new UIComponentDescriptor({...});
        // Note: temp is top-of-stack.
        Name uiComponentDescriptorName = project.getUIComponentDescriptorClassName();
        context.addInstruction(OP_findpropstrict, uiComponentDescriptorName);
        pushDescriptorConstructorArgument(type, id, context);
        context.addInstruction(OP_constructprop, new Object[] { uiComponentDescriptorName, 1 });
        
        context.stopUsing(IL.DESCRIPTOR, 0);
    }
    
    /**
     *  Constructs an argument Object for the descriptor constructor.
     *  <p>
     *  Suppose we are creating a descriptor for
     *  <pre>
     *  <mx:HBox id="hb1" x="100" y="100"
     *           fontFamily="Arial" fontSize="20"
     *           showEffect="Fade" hideEffect="Fade"
     *           initialize="..." click="...">
     *  </pre>
     *  The Object we build in this case would look like
     *  <pre>
     *  {
     *    type: mx.containers.HBox,
     *    id: "hb1",
     *    propertiesFactory: function():Object
     *    {
     *        return { x: 100, y: 100, childDescriptors: [...] };
     *    },
     *    stylesFactory: function():void
     *    {  
     *        this.fontFamily = "Arial";
     *        this.fontSize = 20;
     *        this.showEffect = "Fade";
     *        this.hideEffect = "Fade";
     *    },
     *    events: { initialize: "...", click: "..." },
     *    effects: [ "showEffect", "hideEffect" ]
     *  }
     *  </pre>
     *  Note: The only required key in the Object is <code>type</code>.
     *  <p>
     */
    private void pushDescriptorConstructorArgument(Name type, String id, Context context)
    {
        // The instance node has already traversed its descendants,
        // so that the propertiesInstructionList, stylesInstructionList, etc.
        // in the Context for the instance all contain various instructions that
        // we need to aggregate to create the descriptor.
        
        // Keep track of the number of key/value pairs in the Object.
        int n = 0;
        
        // type: mx.containers.HBox
        context.addInstruction(OP_pushstring, "type");
        context.addInstruction(OP_getlex, type);
        n++;
        
        // id: "hb1"
        if (id != null)
        {
            context.addInstruction(OP_pushstring, "id");
            context.addInstruction(OP_pushstring, id);
            n++;
        }
        
        // This int will store the counter for various helper
        // instruction lists that contain pieces of descriptor code.
        int counter;
        
        // Assemble the child descriptors into an array,
        // and set this array as the childDescriptors property.
        counter = context.getCounter(IL.DESCRIPTOR_CHILD_DESCRIPTORS);
        if (counter > 0)
        {
            // Add a newarray opcode to turn the m sets of
            // child-descriptor-pushing instructions into an Array.
            context.startUsing(IL.DESCRIPTOR_CHILD_DESCRIPTORS);
            context.addInstruction(OP_newarray, counter);
            context.stopUsing(IL.DESCRIPTOR_CHILD_DESCRIPTORS, 0);
            
            // Add the key/value pair
            //   childDescriptors: [...]
            // to the instructions for the descriptor's properties.
            context.startUsing(IL.DESCRIPTOR_PROPERTIES);
            context.addInstruction(OP_pushstring, "childDescriptors");
            context.transfer(IL.DESCRIPTOR_CHILD_DESCRIPTORS, IL.DESCRIPTOR_PROPERTIES);
            context.stopUsing(IL.DESCRIPTOR_PROPERTIES, 1);
        }
                
        // propertiesFactory: function():Object { return { x: 100, y: 100, childDescriptors: [ ... ] }; }
        counter = context.getCounter(IL.DESCRIPTOR_PROPERTIES);
        if (counter > 0)
        {
            // Add newobject and returnvalue opcodes to turn the m sets of
            // property key/value pairs into an Object that will be returned
            // from an anonymous function.
            context.startUsing(IL.DESCRIPTOR_PROPERTIES);
            context.addInstruction(OP_newobject, counter);
            context.addInstruction(OP_returnvalue);
            context.stopUsing(IL.DESCRIPTOR_PROPERTIES, 0);

            // Create the anonymous function.
            MethodInfo methodInfo = createNoParameterAnonymousFunction(
                NAME_OBJECT, context.get(IL.DESCRIPTOR_PROPERTIES));
            context.remove(IL.DESCRIPTOR_PROPERTIES);

            // Set this function as the value of the argument Object's propertiesFactory.
            context.addInstruction(OP_pushstring, "propertiesFactory");
            context.addInstruction(OP_newfunction, methodInfo);
            n++;
        }
        
        // Append the effects styles onto the styles.
        counter = context.getCounter(IL.DESCRIPTOR_EFFECT_STYLES);
        if (counter > 0)
        {
            context.transfer(IL.DESCRIPTOR_EFFECT_STYLES, IL.DESCRIPTOR_STYLES);
            context.incrementCounter(IL.DESCRIPTOR_STYLES, counter);
        }
        
        // stylesFactory: function():void { this.fontFamily = "Arial"; this.fontSize = 20;
        //                                  this.showEffect = "Fade"; this.hideEffect = "Fade"; },
        counter = context.getCounter(IL.DESCRIPTOR_STYLES);
        if (counter > 0)
        {
            // A a returnvoid instruction to the body of the anonymous function
            // for the stylesFactory.
            context.startUsing(IL.DESCRIPTOR_STYLES);
            context.addInstruction(OP_returnvoid);
            context.stopUsing(IL.DESCRIPTOR_STYLES, 0);
            
            // Create the anonymous function.
            MethodInfo methodInfo = createNoParameterAnonymousFunction(
                NAME_VOID, context.get(IL.DESCRIPTOR_STYLES));
            context.remove(IL.DESCRIPTOR_STYLES);
            
            // Set this function as the value of the argument Object's stylesFactory.
            context.addInstruction(OP_pushstring, "stylesFactory");
            context.addInstruction(OP_newfunction, methodInfo);
            n++;
        }
        
        // events: { initialize: "___MyApp_HBox1_initialize", click: "___MyApp_HBox1_click" }
        counter = context.getCounter(IL.DESCRIPTOR_EVENTS);
        if (counter > 0)
        {
            // Add a newobject obcode to turn the m key/value pairs
            // into an Object.
            context.startUsing(IL.DESCRIPTOR_EVENTS);
            context.addInstruction(OP_newobject, counter);
            context.stopUsing(IL.DESCRIPTOR_EVENTS, 0);
            
            // Set this Object as the value of the argument Object's 'events'.
            context.addInstruction(OP_pushstring, "events");
            context.transfer(IL.DESCRIPTOR_EVENTS);
            n++;
        }
        
        // effects: [ "showEffect", "hideEffect" ]
        counter = context.getCounter(IL.DESCRIPTOR_EFFECTS);
        if (counter > 0)
        {
            // Add a newarray obcode to turn the m effect names
            // into an Array.
            context.startUsing(IL.DESCRIPTOR_EFFECTS);
            context.addInstruction(OP_newarray, counter);
            context.stopUsing(IL.DESCRIPTOR_EFFECTS, 0);
            
            // Set this Array as the value of the argument Object's 'effects'.
            context.addInstruction(OP_pushstring, "effects");
            context.transfer(IL.DESCRIPTOR_EFFECTS);
            n++;
        }
                
        // Turn the key/value pairs (where the keys are 'type', 'id',
        // 'propertiesFactory', 'stylesFactory', 'events', and 'effects)
        // into an Object which will be the argument for the descriptor
        // constructor.
        context.addInstruction(OP_newobject, n);
    }
    
    /**
     * Transfers descriptor code from child to parent.
     * The child build its descriptor in its context's
     * {@link descriptorInstructionList}.
     * The parent needs the child's descriptor in its context's
     * {@link childrenInstructionList}.
     */
    private void transferDescriptor(IMXMLClassReferenceNode node, Context childContext, Context parentContext)
    {
        InstructionList descriptorInstructions = childContext.get(IL.DESCRIPTOR);
        childContext.remove(IL.DESCRIPTOR);
        
        // Add them to the list in which we're building up
        // the childDescriptors array for the parent node.
        parentContext.startUsing(IL.DESCRIPTOR_CHILD_DESCRIPTORS);
        parentContext.addAll(descriptorInstructions);
        parentContext.stopUsing(IL.DESCRIPTOR_CHILD_DESCRIPTORS, 1);
    }
    
    /**
     * Transfers instructions to build up an object literal as the value of
     * {@code WebService.operations} property.
     * 
     * @param node {@code IMXMLWebServiceOperationNode}
     * @param childContext Child context that contains instrcutions to push the
     * name and values of the object literal on to the stack.
     * @param parentContext Context of the {@code IMXMLWebServiceNode}.
     */
    private void transferWebServiceOperationsOrRemoteObjectMethods(IMXMLInstanceNode node, Context childContext, Context parentContext)
    {
        final InstructionList childInstructions = childContext.get(IL.WEB_SERVICE_OPERATIONS_OR_REMOTE_OBJECT_METHODS);
        childContext.remove(IL.WEB_SERVICE_OPERATIONS_OR_REMOTE_OBJECT_METHODS);
        parentContext.startUsing(IL.WEB_SERVICE_OPERATIONS_OR_REMOTE_OBJECT_METHODS);
        parentContext.addAll(childInstructions);
        parentContext.stopUsing(IL.WEB_SERVICE_OPERATIONS_OR_REMOTE_OBJECT_METHODS, 1);
    }
    
    /**
     * Creates an anonymous function with signature function():T
     * whose body is the specified list of instructions.
     */
    private MethodInfo createNoParameterAnonymousFunction(Name returnType, InstructionList instructionList)
    {
        MethodInfo mi = new MethodInfo();        
        mi.setReturnType(returnType);
        
        MethodBodyInfo methodBodyInfo = new MethodBodyInfo();
        methodBodyInfo.setMethodInfo(mi);

        IMethodVisitor methodVisitor = emitter.visitMethod(mi);
        methodVisitor.visit();        
        
        IMethodBodyVisitor methodBodyVisitor = methodVisitor.visitBody(methodBodyInfo);
        methodBodyVisitor.visit();
        methodBodyVisitor.visitInstructionList(instructionList);
        methodBodyVisitor.visitEnd();
        
        methodVisitor.visitEnd();

        return mi;
    }
    
    /**
     * Generates
     * <pre>
     * override public function initialize():void
     * {
     *    mx_internal::setDocumentDescriptor(_documentDescriptor_);
     *    super.initialize();
     * }
     * </pre>
     * to set the document descriptor into the component.
     */
    // TODO Report a problem if there is already an initialize() override in this class.
    private void overrideInitializeMethod(IMXMLClassDefinitionNode node)
    {
        String name = "initialize";
        MethodInfo methodInfo = new MethodInfo();        
        methodInfo.setMethodName(name);
        methodInfo.setReturnType(NAME_VOID);
        
        InstructionList body = new InstructionList();
        
        body.addInstruction(OP_getlocal0);
        body.addInstruction(OP_pushscope);
                    
        // mx_internal::setDocumentDescriptor(_documentDescriptor_);
        body.addInstruction(OP_getlocal0);           
        body.addInstruction(OP_getlocal0);
        body.addInstruction(OP_getproperty, NAME_DOCUMENT_DESCRIPTOR);
        body.addInstruction(OP_callpropvoid, SET_DOCUMENT_DESCRIPTOR_CALL_OPERANDS);
        
        // super.initialize();
        body.addInstruction(OP_getlocal0);
        body.addInstruction(OP_callsupervoid, INITIALIZE_CALL_OPERANDS);
          
        // return;
        body.addInstruction(OP_returnvoid);

        generateMethodBody(methodInfo, classScope, body);
        
        addMethodTrait(NAME_INITIALIZE, methodInfo, true);
    }
    
    /**
     * Generates
     * <pre>
     * override public function get MXMLProperties():Array
     * {
     *    if (!_MXMLProperties)
     *    {
     *        var arr:Array = super.mxmlProperties;
     *        var data:Array = [....];
     *        if (arr)
     *            _MXMLProperties = arr.concat(data);
     *        else
     *            _MXMLProperties = data;
     *    }
     *    return _MXMLProperties;
     * }
     * </pre>
     * to set the document descriptor into the component.
     */
    // TODO gosmith Report a problem if there is already an initialize() override in this class.
    void overrideMXMLPropertiesGetter(IMXMLClassDefinitionNode node, Context context, int numElements)
    {
        addPropertiesData(mxmlPropertiesInsns, context, numElements);
    }
    
    /**
     * Generates the array of data describing the attributes on the main tag
     */
    private void addPropertiesData(InstructionList body, Context context, int numElements)
    {
        iinitAfterSuperInsns.addInstruction(OP_newarray, numElements);      
        body.addAll(iinitAfterSuperInsns);
    }
    
    /**
     * Generates
     * <pre>
     * override public function get MXMLDescriptor():Array
     * {
     *    if (!_MXMLDescriptor)
     *    {
     *        var arr:Array = super.mxmlDescriptor;
     *        var data:Array = [....];
     *        if (arr)
     *            _MXMLDescriptor = arr.concat(data);
     *        else
     *            _MXMLDescriptor = data;
     *    }
     *    return _MXMLDescriptor;
     * }
     * </pre>
     * to set the document descriptor into the component.
     */
    // TODO gosmith Report a problem if there is already an initialize() override in this class.
    void overrideMXMLDescriptorGetter(IMXMLClassDefinitionNode node, Context context)
    {
        String name = "MXMLDescriptor";
        MethodInfo methodInfo = new MethodInfo();        
        methodInfo.setMethodName(name);
        methodInfo.setReturnType(NAME_ARRAY);
        
        InstructionList body = new InstructionList();
        
        body.addInstruction(OP_getlocal0);
        body.addInstruction(OP_pushscope);
                    
        // if (_MXMLDescriptor)
        Label label0 = new Label();
        body.addInstruction(OP_getlocal0);
        body.addInstruction(OP_getproperty, NAME_MXML_DESCRIPTOR);
        body.addInstruction(OP_not);
        body.addInstruction(OP_iffalse, label0);
        
        // arr = super.MXMLDescriptor;
        body.addInstruction(OP_findpropstrict, NAME_MXML_DESCRIPTOR_GETTER);
        body.addInstruction(OP_getsuper, NAME_MXML_DESCRIPTOR_GETTER);
        body.addInstruction(OP_setlocal1);

        // data = [...]
        addInstanceData(body, context);
        body.addInstruction(OP_setlocal2);
        
        // if (arr)
        Label label1 = new Label();
        body.addInstruction(OP_getlocal1);
        body.addInstruction(OP_iffalse, label1);
        
        // _MXMLDescriptor = arr.concat(data);
        body.addInstruction(OP_getlocal0);
        body.addInstruction(OP_getlocal1);
        body.addInstruction(OP_getlocal2);
        body.addInstruction(OP_callproperty, CONCAT_CALL_OPERANDS); 
        body.addInstruction(OP_setproperty, NAME_MXML_DESCRIPTOR);
        body.addInstruction(OP_jump, label0);
        
        // _MXMLDescriptor = data;
        body.labelNext(label1);
        body.addInstruction(OP_getlocal0);
        body.addInstruction(OP_getlocal2);
        body.addInstruction(OP_setproperty, NAME_MXML_DESCRIPTOR);
        
        // return _MXMLDescriptor;
        body.labelNext(label0);
        body.addInstruction(OP_getlocal0);
        body.addInstruction(OP_getproperty, NAME_MXML_DESCRIPTOR);
        body.addInstruction(OP_returnvalue);

        generateMethodBody(methodInfo, classScope, body);
        
        addGetter(NAME_MXML_DESCRIPTOR_GETTER, methodInfo, true);
    }
    
    /**
     * Generates the array of data describing the instances
     */
    private void addInstanceData(InstructionList body, Context context)
    {
        context.startUsing(IL.MXML_CONTENT_FACTORY);
        context.addInstruction(OP_newarray, context.getCounter(IL.MXML_CONTENT_FACTORY));      
        body.addAll(context.currentInstructionList);
        context.stopUsing(IL.MXML_CONTENT_FACTORY, 0);
    }

    /**
     * Generates code equivalent to
     * <pre>
     * target.mx_internal::_document = this;
     * </pre>
     * or
     * <pre>
     * if (!target.mx_internal::_document)
     *     target.mx_internal::_document = this;
     * </pre>
     * to set the document property on things implementing
     * IVisualElementContainer or IContainer.
     */
    // TODO Explain when 'conditionally' is true and false.
    void setDocument(IMXMLClassReferenceNode node, boolean conditionally, Context context)
    {
        if (node.isVisualElementContainer() || node.isContainer())
        {
            if (getProject().getTargetSettings().getMxmlChildrenAsData())
            {
                context.startUsing(IL.PROPERTIES);
                context.addInstruction(OP_pushstring, "document");
                context.addInstruction(OP_pushtrue);
                context.addInstruction(OP_getlocal0);
                context.stopUsing(IL.PROPERTIES, 1);
                return;
            }
 
            Label end = null;

            if (conditionally)
            {
                end = new Label();
                context.pushTarget();
                context.addInstruction(OP_getproperty, NAME_UNDERBAR_DOCUMENT);
                context.addInstruction(OP_pushnull);
                context.addInstruction(OP_ifne, end);
            }
            
            context.pushTarget();
            context.addInstruction(OP_getlocal0);
            context.addInstruction(OP_setproperty, NAME_UNDERBAR_DOCUMENT);
            
            if (conditionally)
                context.labelNext(end);
        }
    }
    
    private int setSpecifiers(Context context)
    {
        return setSpecifiers(context, false, false);
    }
    
    /**
     * Merges the helper instruction lists for properties, styles,
     * events, and effects into the main instruction list.
     */
    int setSpecifiers(Context context, Boolean addCounters, Boolean skipContentFactory)
    {        
        int numElements = 0;
        
        boolean newCodeGen = getProject().getTargetSettings().getMxmlChildrenAsData();
        
        int numProperties = context.getCounter(IL.PROPERTIES);
        if (context.hasModel)
            numProperties += 1;
        if (context.hasBeads)
            numProperties += 1;
        
        int numOperations = context.getCounter(IL.WEB_SERVICE_OPERATIONS_OR_REMOTE_OBJECT_METHODS);
        if (numOperations > 0)
        	numProperties += 1;
        if (newCodeGen && addCounters)
            context.pushNumericConstant(numProperties);
        // Adds code such as
        //   target.width = 100;
        //   target.height = 100;
        // to set properties.
        if (context.hasModel)
            context.transfer(IL.MXML_MODEL_PROPERTIES);
        context.transfer(IL.PROPERTIES);
        if (numOperations > 0)
        {
            context.addInstruction(OP_pushstring, "operations");
            context.addInstruction(OP_pushfalse);
            context.addInstruction(OP_findpropstrict, IMXMLTypeConstants.NAME_OBJECT);
            context.addInstruction(OP_getproperty, IMXMLTypeConstants.NAME_OBJECT);
            context.pushNumericConstant(numOperations);
        	context.transfer(IL.WEB_SERVICE_OPERATIONS_OR_REMOTE_OBJECT_METHODS);
            context.pushNumericConstant(0);
            context.pushNumericConstant(0);
            context.pushNumericConstant(0);
            context.addInstruction(OP_pushnull);
            context.addInstruction(OP_newarray, numOperations * 3 + 6);      
        }
        if (context.hasBeads)
            context.transfer(IL.MXML_BEAD_PROPERTIES);
        
        if (newCodeGen && addCounters)
            context.pushNumericConstant(context.getCounter(IL.STYLES));
        // Adds code such as
        //   target.setStyle("fontSize", 20);
        //   target.setStyle("fontFamily", "Arial");
        // to set styles.
        context.transfer(IL.STYLES);
        
        if (newCodeGen && addCounters)
            context.pushNumericConstant(context.getCounter(IL.EFFECT_STYLES));
        // Adds code such as
        //   target.setStyle("showEffect", "Fade");
        //   target.setStyle("hideEffect", "Fade");
        // to set effect styles.
        context.transfer(IL.EFFECT_STYLES);
        
        if (newCodeGen && addCounters)
            context.pushNumericConstant(context.getCounter(IL.EVENTS));
        // Adds code such as
        //   target.addEventListener("initialize", ...);
        //   target.addEventListener("click", ...);
        // to set events.
        context.transfer(IL.EVENTS);

        // Adds a single call such as
        //   target.registerEffects([ "showEffect", "hideEffect" ]);
        // to register effects.
        int n = context.getCounter(IL.EFFECTS);
        if (n > 0)
        {
            context.pushTarget();
            context.transfer(IL.EFFECTS);
            context.addInstruction(OP_newarray, n);
            context.addInstruction(OP_callpropvoid, REGISTER_EFFECTS_CALL_OPERANDS);
        }
        
        if (newCodeGen && addCounters)
        {
            numElements += numProperties * 3;
            numElements += context.getCounter(IL.STYLES) * 3;
            numElements += context.getCounter(IL.EFFECT_STYLES) * 3;
            numElements += context.getCounter(IL.EVENTS) * 2;
            numElements += 4;
            
            if (context.getCounter(IL.MXML_CONTENT_FACTORY) > 0 && !skipContentFactory)
            {
                InstructionList childIL;
                context.startUsing(IL.MXML_CONTENT_FACTORY);
                childIL = context.currentInstructionList;
                context.stopUsing(IL.MXML_CONTENT_FACTORY, 0);
                context.addAll(childIL);
                context.addInstruction(OP_newarray, context.getCounter(IL.MXML_CONTENT_FACTORY));      
            }
            else
                context.addInstruction(OP_pushnull);
            numElements++;
            
        }
        
        if (!newCodeGen || !addCounters)
        {
            // Adds a single call such as
            //   temp.operations = { "op1":operation1, "op2":operation2 };
            // to initialize WebService.operations property from a list of <s:operation> tags.
            final int operationsCount = context.getCounter(IL.WEB_SERVICE_OPERATIONS_OR_REMOTE_OBJECT_METHODS);
            if(operationsCount > 0)
            {
                context.pushTarget();
                context.transfer(IL.WEB_SERVICE_OPERATIONS_OR_REMOTE_OBJECT_METHODS);
                context.addInstruction(OP_newobject, operationsCount);
                context.addInstruction(OP_setproperty, IMXMLTypeConstants.NAME_OPERATIONS);
            }
        }
        return numElements;
    }

    /**
     * Generates code equivalent to
     * <pre>
     * target.initialized(this, id);
     * </pre>
     * to call the initialized() method on instances implementing IMXMLObject.
     */
    private void callInitialized(IMXMLInstanceNode node, Context context)
    {
        if (node.isMXMLObject())
        {
            String id = ((IMXMLInstanceNode)node).getID();
                
            context.pushTarget();
            context.addInstruction(OP_getlocal0);
            if (id != null)
                context.addInstruction(OP_pushstring, id);
            else
                context.addInstruction(OP_pushnull);
            context.addInstruction(OP_callpropvoid, INITIALIZED_CALL_OPERANDS);
        }
    }
    
    /**
     * Emit AET instructions for<br> {@code parent.addLayer(child);}
     * <p>
     * This method is used by {@code <fx:DesignLayer>} code generation.
     * 
     * @param parentNode DesignLayer node.
     * @param parentContext Context for reducing the DesignLayer node.
     * @param childNode Child DesignLayer node.
     */
    private void callAddLayer(
            final IMXMLDesignLayerNode parentNode,
            final Context parentContext,
            final IMXMLDesignLayerNode childNode)
    {
        assert !childNode.skipCodeGeneration() : "No-op DesignLayer tags can't be added to the parent.";
        parentContext.pushTarget();
        parentContext.addInstruction(OP_getlex, new Name(childNode.getEffectiveID()));
        parentContext.addInstruction(OP_callpropvoid, ADD_LAYER_CALL_OPERANDS);
    }

    /**
     * Emit AET instructions for<br>
     * {@code instance.designLayer = designLayerInstance;}
     * <p>
     * This method is used by {@code <fx:DesignLayer>} code generation.
     * 
     * @param designLayerNode DesignLayer node.
     * @param designLayerContext Context that contains code to initialize the
     * DesignLayer instance.
     * @param childInstanceNode Child instance node.
     */
    private void setDesignLayer(
            final IMXMLDesignLayerNode designLayerNode,
            final Context designLayerContext,
            final IMXMLInstanceNode childInstanceNode)
    {
        assert !designLayerNode.skipCodeGeneration() : "No-op DesignLayer tags shouldn't be emitted.";
        designLayerContext.pushTarget();
        designLayerContext.addInstruction(OP_getlex, new Name(childInstanceNode.getEffectiveID()));
        designLayerContext.addInstruction(OP_swap);
        designLayerContext.addInstruction(OP_setproperty, NAME_DESIGN_LAYER);
    }
    
     /**
     * If the class being generated implements <code>IStateClient</code>
     * and defines one or more states, adds instructions to set the
     * <code>currentState</code> property to the name of the initial state,
     * as in
     * <pre>
     * this.currentState = "s1";
     * </pre>
     */
    private void setCurrentState(InstructionList insns)
    {
        // Check if the class being generated implements IStateClient.
        RoyaleProject project = getProject();
        String stateClientInterface = project.getStateClientInterface();
        if (classDefinition.isInstanceOf(stateClientInterface, project))
        {
            // Check if there is an initial state.
            String initialState = classDefinitionNode.getInitialState();
            if (initialState != null)
            {    
                // Set <code>currentState</code> to the initial state.
                insns.addInstruction(OP_getlocal0);
                insns.addInstruction(OP_pushstring, initialState);
                insns.addInstruction(OP_setproperty, NAME_CURRENT_STATE);
            }
        }
    }
    
    private void addBindingCodeForCtor(InstructionList ctor_insns)
    {
        InstructionList il = bindingDirectiveHelper.getConstructorCode();
        if (il != null)
        {
            ctor_insns.addAll(il);
        }
    }
    
    /**
     * Returns the {@code RoyaleProject} for this processor.
     */
    public RoyaleProject getProject()
    {
        return (RoyaleProject)classScope.getProject();
    }
    
    /** 
     * Helper to give access to problem reporting to MXMLBindingDirectiveHelper2
     */
    public Collection<ICompilerProblem> getProblems()
    {
        return this.classScope.getProblems();
    }
    
    /**
     * Determines the Name of the instance initializer method for an instance node.
     * This can get called to preassign the name before the method gets generated.
     */
    private Name getInstanceInitializerName(IMXMLInstanceNode instanceNode)
    {
        // Check the map to see if an initializer name
        // has already been assigned to this instance node.
        Name name = instanceInitializerMap.get(instanceNode);
        
        // If so, return it.
        if (name != null)
            return name;
        
        // Otherwise, generate the next one in the sequence i0, i1, etc.
        name = createMXMLPrivateName(INSTANCE_INITIALIZER_NAME_BASE + instanceInitializerCounter++);
        
        // Remember it in the map.
        instanceInitializerMap.put(instanceNode, name);
        
        return name;
    } 
    
    /**
     * Determines the Name of the event handler method for an event node.
     * This can get called to preassign the name before the method gets generated.
     */
    public Name getEventHandlerName(IMXMLEventSpecifierNode eventNode)
    {
        // Check the map to see if a handler name
        // has already been assigned to this event node.
        Name name = eventHandlerMap.get(eventNode);
        
        // If so, return it.
        if (name != null)
            return name;
        
        // Otherwise, generate the next one in the sequence ">0", ">1", etc.
        String baseName = EVENT_HANDLER_NAME_BASE + eventHandlerCounter++;
        
        // Either make the Name public or put it in the special
        // private namespace for APIs that are autogenerated.
        name = eventNode.needsPublicHandler() ?
               new Name(baseName) :
               createMXMLPrivateName(baseName);
        
        // Remember it in the map.
        eventHandlerMap.put(eventNode, name);
        
        return name;
    }
    
    /**
     * Determines the Name of the event handler method for an event node.
     * This can get called to preassign the name before the method gets generated.
     */
    public Name getVectorGeneratorName(Name typeName)
    {
        // Check the map to see if a handler name
        // has already been assigned to this event node.
        Name name = VectorGeneratorMap.get(typeName);
        
        // If so, return it.
        if (name != null)
            return name;
        
        // Otherwise, generate the next one in the sequence ">v1", ">v1", etc.
        String baseName = VECTOR_GENERATOR_NAME_BASE + vectorGeneratorCounter++;
        
        // Either make the Name public or put it in the special
        // private namespace for APIs that are autogenerated.
        name = createMXMLPrivateName(baseName);
        
        // Remember it in the map.
        VectorGeneratorMap.put(typeName, name);
        
        return name;
    }

    /**
     * Determines whether a node is state-dependent.
     * TODO: we should move to IMXMLNode
     */
    protected boolean isStateDependent(IASNode node)
    {
        if (node instanceof IMXMLSpecifierNode)
        {
            String suffix = ((IMXMLSpecifierNode)node).getSuffix();
            return suffix != null && suffix.length() > 0;
        }
        else if (isStateDependentInstance(node))
            return true;
        return false;
    }
    
    /**
     * Determines whether the geven node is an instance node, as is state dependent
     */
    protected boolean isStateDependentInstance(IASNode node)
    {
        if (node instanceof IMXMLInstanceNode)
        {
            String[] includeIn = ((IMXMLInstanceNode)node).getIncludeIn();
            String[] excludeFrom = ((IMXMLInstanceNode)node).getExcludeFrom();
            return includeIn != null || excludeFrom != null;
        }
        return false;
    }
    
    /**
     * Adds an instance trait for a method,
     * such as an event handler or an instance initializer.
     */
    private void addMethodTrait(Name methodName, MethodInfo methodInfo, boolean isOverride)
    {
        ITraitVisitor traitVisitor =
            itraits.visitMethodTrait(TRAIT_Method, methodName, 0, methodInfo);
        
        if (isOverride)
            traitVisitor.visitAttribute(Trait.TRAIT_OVERRIDE, true);
    }
    
    /**
     * Adds an instance trait for a variable,
     * such as one created by an id attribute.
     */
    public void addVariableTrait(Name varName, Name varType)
    {
        itraits.visitSlotTrait(TRAIT_Var, varName, ITraitsVisitor.RUNTIME_SLOT, varType, LexicalScope.noInitializer);
    }

    /**
     * Adds an instance trait for a bindable variable,
     * such as one created by an id attribute.
     */
    public void addBindableVariableTrait(Name varName, Name varType, IDefinition def)
    {
        ITraitsVisitor old_tv = classScope.traitsVisitor;
        try
        {
            // Make sure the itraits is the traitsvisitor as we make the various bindable
            // definitions
            classScope.traitsVisitor = itraits;
            Binding var = classScope.getBinding(def);
            classScope.makeBindableVariable(var, varType, var.getDefinition().getAllMetaTags());
        }
        finally
        {
            // restore the old TV just in case
            classScope.traitsVisitor = old_tv;
        }
    }

    /**
     * Adds a getter method trait to the generated class.
     * 
     * @param methodName Method name.
     * @param methodInfo Method info.
     * @param isOverride True if the setter overrides a parent setter.
     */
    private void addGetter(Name methodName, MethodInfo methodInfo, boolean isOverride)
    {
        final ITraitVisitor traitVisitor =
                itraits.visitMethodTrait(TRAIT_Getter, methodName, 0, methodInfo);

        if (isOverride)
            traitVisitor.visitAttribute(Trait.TRAIT_OVERRIDE, true);
    }

    /**
     * Adds a setter method trait to the generated class.
     * 
     * @param methodName Method name.
     * @param methodInfo Method info.
     * @param isOverride True if the setter overrides a parent setter.
     */
    private void addSetter(Name methodName, MethodInfo methodInfo, boolean isOverride)
    {
        final ITraitVisitor traitVisitor =
                itraits.visitMethodTrait(TRAIT_Setter, methodName, 0, methodInfo);

        if (isOverride)
            traitVisitor.visitAttribute(Trait.TRAIT_OVERRIDE, true);
    }

    
    /**
     * A variant of the generateMethodBody() in ABCGenerator.
     * This one takes an InstructionList to use as the body.
     * The one in ABCGenerator takes a node and uses JBurg
     * to generate the instructions for that node's subtree.
     * For most MXML nodes, we generate instructions "by hand"
     * rather than using JBurg, so we mostly use this version.
     */
    private MethodInfo generateMethodBody(MethodInfo mi, LexicalScope enclosing_scope,
                                          InstructionList instructions)
    {
        IMethodVisitor mv = enclosing_scope.getEmitter().visitMethod(mi);
        mv.visit();
        
        MethodBodyInfo mbi = new MethodBodyInfo();
        mbi.setMethodInfo(mi);
        
        IMethodBodyVisitor mbv = mv.visitBody(mbi);
        mbv.visit();
        
        mbv.visitInstructionList(instructions);
        
        mbv.visitEnd();
        mv.visitEnd();
        
        return mi;
    }
    
    /**
     * Determines whether an instance node can be constructed "inline"
     * or requires its own instance initializer method.
     */
    private boolean instanceRequiresInitializerMethod(IMXMLInstanceNode instanceNode)
    {
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
            return false;
        
        // Instance nodes that are the children of a deferred instance node
        // require an instance initializer method from which a
        // DeferredInstanceFromFunction can be constructed.
        if ( instanceNode.getParent() instanceof IMXMLDeferredInstanceNode)
            return true;
        
        // State dependent nodes need one, because they will get a 
        // DeferredInstanceFromFunction created with the initializer function as a 
        // constructor argument
        if (isStateDependent(instanceNode))
            return true;
        
        return false;
    }
    
    /**
     * Determines whether the specified node should generate
     * code to be incorporated into a UIComponentDescriptor.
     */
    protected boolean generateDescriptorCode(IASNode node, Context context)
    {
        if (node instanceof IMXMLSpecifierNode &&
            ((IMXMLClassReferenceNode)node.getParent()).needsDocumentDescriptor())
        {
            return false;
        }
        
        if (node instanceof IMXMLClassReferenceNode &&
            ((IMXMLClassReferenceNode)node).needsDocumentDescriptor())
        {
            return false;
        }
            
        return context.needsDescriptor;
    }
    
    /**
     * Determines whether the specified node should generate
     * normal non-UIComponentDescriptor code.
     * <p>
     * We need to do this for every node whose parent instance
     * is not an MX container.
     */
    // TODO Explain why this is not the opposite of generateDescriptorCode().
    private boolean generateNonDescriptorCode(IASNode node, Context context)
    {
        if (node instanceof IMXMLSpecifierNode)
            node = node.getParent();
        
        // Non-visual instances always need non-descriptor code.
        if (node instanceof IMXMLInstanceNode &&
            !((IMXMLInstanceNode)node).isDeferredInstantiationUIComponent())
        {
            return true;
        }
            
        IASNode parent = node.getParent();       
        if (parent != null && parent instanceof IMXMLClassReferenceNode)
            return !((IMXMLClassReferenceNode)parent).isContainer();
        else
            return true;
    }
    
    /**
     * Check if a new child context is required to reduce {@code node}.
     * 
     * @param node AST node to be reduced.
     * @return True if a new child context is required.
     */
    private static boolean isNewContextRequired(IASNode node)
    {
        final boolean isInstanceNode = node instanceof IMXMLInstanceNode;
        final boolean isResourceNode = node instanceof IMXMLResourceNode;
        final boolean isDesignLayerNode = node instanceof IMXMLDesignLayerNode;
        final boolean isDataBindingNode = isDataBindingNode(node);
        return isInstanceNode && !isDesignLayerNode && !(isDataBindingNode || isResourceNode);
    }

    /**
     * This method is called before each processMXMLThing() method.
     * <p>
     * For instance nodes, it creates a new context because certain
     * opcodes are different when processing instance nodes than when
     * processing a document node.
     * It also starts a new InstructionList if the instance
     * requires its own instance initializer method.
     * <p>
     * For other nodes, it does nothing.
     */
    private Context beginContext(IASNode node, Context parentContext)
    {
            IMXMLInstanceNode instanceNode = (IMXMLInstanceNode)node;
            
            // try to determine if this is the children of an MX Container
            boolean isMXMLDisplayObjectChild = node.getNodeID() == ASTNodeID.MXMLInstanceID && 
                                                (node.getParent().getNodeID() == ASTNodeID.MXMLInstanceID ||
                                                    node.getParent().getNodeID() == ASTNodeID.MXMLDocumentID);
            if (isMXMLDisplayObjectChild)
            {
                // if it is, build up the children in this instruction list
                parentContext.startUsing(IL.MXML_CONTENT_FACTORY);
                parentContext.isContentFactory = true;
            }

            final Context childContext = new Context(instanceNode, parentContext.currentInstructionList);
            childContext.parentContext = parentContext;
            if (isMXMLDisplayObjectChild)
                parentContext.stopUsing(IL.MXML_CONTENT_FACTORY, 0);
            
            // Remember the Name for the instance's Class,
            // so that processMXMLInstance() can use it to construct the instance
            // and so that endNode() can use it to generate the reference variable
            // autogenerated for the id and to generate an initializer method
            // if appropriate.
            ICompilerProject project = getProject();
            IClassDefinition instanceClassReference = instanceNode.getClassReference(project);
            childContext.instanceClassName =
                instanceClassReference != null ?
                ((DefinitionBase)instanceClassReference).getMName(project) :
                null;

            // If the instance node requires an initializer method,
            // crate a new instruction list in the context.
            if (instanceRequiresInitializerMethod(instanceNode))
            {
                childContext.instanceHasOwnInitializer = true;
                childContext.currentInstructionList =
                    childContext.mainInstructionList = new InstructionList();
                childContext.addInstruction(OP_getlocal0);
                childContext.addInstruction(OP_pushscope);
            }
         
        return childContext;
    }

    /**
     * This method is called after each {@code processMXMLXXX()} method if a new
     * child context is created before {@code processMXMLXXX()}.
     * <p>
     * For instance nodes, it adds the id-generated reference variable to the
     * class and initializes it. If the instance has its own initializer method,
     * it adds that method to the class, and in the old context generates a call
     * to that initializer.
     */
    /**
     * This method is called after each processMXMLThing() method.
     * <p>
     * For instance nodes, it adds the id-generated reference variable
     * to the class and initializes it.
     * If the instance has its own initializer method,
     * it adds that method to the class, and in the old context
     * generates a call to that initializer.
     * <p>
     * For other nodes, it does nothing.
     */
    private void endContext(IASNode node, Context childContext, Context parentContext)
    {
            boolean generateDescriptorCode =
                generateDescriptorCode((IMXMLInstanceNode)node, childContext);
            boolean generateNonDescriptorCode = 
                generateNonDescriptorCode((IMXMLInstanceNode)node, childContext);
            boolean isInitializer = childContext.instanceHasOwnInitializer;
            
            if (generateDescriptorCode && !isInitializer)
                transferDescriptor((IMXMLInstanceNode)node, childContext, parentContext);

            if (node instanceof IMXMLWebServiceOperationNode ||
                node instanceof IMXMLRemoteObjectMethodNode)
            {
                transferWebServiceOperationsOrRemoteObjectMethods((IMXMLInstanceNode)node, childContext, parentContext);
            }
        
            IMXMLInstanceNode instanceNode = (IMXMLInstanceNode)childContext.node;           
            Name instanceClassName = childContext.instanceClassName;
            
            if (!getProject().getTargetSettings().getMxmlChildrenAsData() && generateNonDescriptorCode)
            {
                callInitialized(instanceNode, childContext);
                setDocument(instanceNode, true, childContext);
                setDocumentDescriptorForInstance(instanceNode, childContext);
            }
            
            String id = instanceNode.getEffectiveID();
            if (id != null)
            {
                Name idName = new Name(id);
                
                // Create a reference variable in the class whose name is the id.
                // For example, for <s:Button id="b1"/>, create
                // public var b1:spark.components.Button;
                if( instanceNode.getID() != null )
                {
                    IDefinition d = instanceNode.resolveID();
                    // Only create reference var if it isn't already declared on base class
                    // Look for a property with the same name as this function in the base class
                    // the lookup will search up the inheritance chain, so we don't have to worry about
                    // walking up the inheritance chain here.
                    ClassDefinition base = (ClassDefinition)classDefinition.resolveBaseClass(getProject());

                    if (base != null)
                    {
                        IDefinition baseDef = base.getContainedScope().getQualifiedPropertyFromDef(
                            getProject(), base, d.getBaseName(), NamespaceDefinition.getPublicNamespaceDefinition(), false);
                        if (baseDef == null)
                            addBindableVariableTrait(idName, instanceClassName, d);
                        //else
                        //    System.out.println("not adding bindable variable trait for " + d.getBaseName() + " in " + instanceClassName);
                    }
                    else
                        addBindableVariableTrait(idName, instanceClassName, d);
                }
                else
                {
                    // No ID specified, can just make a var that isn't bindable
                    addVariableTrait(idName, instanceClassName);
                }

                if (!getProject().getTargetSettings().getMxmlChildrenAsData())
                {
                    if (generateNonDescriptorCode || isStateDependentInstance(instanceNode))
                    {
                        // Set the reference variable to the new instance.
                        setIDReferenceVariable(idName, childContext);
                        
                        if (instanceAffectsBindings(instanceNode))
                        {
                            // Call executeBindings() on the new instance.
                            executeBindingsForInstance(instanceNode, childContext);
                        }
                    }
                }
            }
            
            if (getProject().getTargetSettings().getMxmlChildrenAsData())
            {
                return;
            }            
                        
            Name initializerName = null;
                       
            if (isInitializer)
            {
                initializerName = getInstanceInitializerName(instanceNode);
                MethodInfo methodInfo = createInstanceInitializerMethodInfo(
                    initializerName.getBaseName(), instanceClassName);
                addMethodTrait(initializerName, methodInfo, false);
    
                childContext.addInstruction(OP_returnvalue);
                generateMethodBody(methodInfo, classScope, childContext.currentInstructionList);
            }
                            
            // If we've made an instance initializer method, we either push it onto the stack
            // or call it to create an instance and leave the instance on the stack.
            if (isInitializer)
            {
                // This initializer (i0, i1, etc.) is a method of the class being generated,
                // so it is found or called on 'this', which is in local0.
                parentContext.addInstruction(OP_getlocal0);
                
                if ((node.getParent() instanceof IMXMLDeferredInstanceNode &&
                    !(node instanceof IMXMLClassNode)) ||
                    (isStateDependent(node)))
                {
                    // TODO: the logic above is almost, but not quite, the same as 
                    // instanceRequiresInitializerMethod(). Could we just use that,
                    // rather than duplicating the logic (sort-of)?
                    
                    // If we're making a DeferredInstanceFromFunction,
                    // leave the instance initializer on the stack.
                    // It will be the parameter for the DeferredInstanceFromFunction constructor.
                    parentContext.addInstruction(OP_getproperty, new Object[] { initializerName, 0});
                }
                else
                {
                    // Otherwise, push the instance onto the stack.
                    // This instance is created by calling the instance initializer.
                    parentContext.addInstruction(OP_callproperty, new Object[] { initializerName, 0 });
                }
            }
                     
            // If we've codegen'd an instance that isn't a value to be set
            // into something else, don't leave it (or its initializer method)
            // on the stack. The cases where this happens are:
            // 1. Instance tags that are children of a <Declarations> tag.
            // 2. Non-visual instance tags that are children
            // of a class definition tag (as allowed in MXML 2006).
            if (generateNonDescriptorCode)
            {
                IASNode parent = node.getParent();
                
                if (parent instanceof IMXMLDeclarationsNode ||
                    parent instanceof IMXMLClassDefinitionNode &&
                    ((IMXMLInstanceNode)node).isDeferredInstantiationUIComponent())
                {
                    childContext.addInstruction(OP_pop);
                }
            }
    }
    
    /**
     * For an instance like &lt;Button id="b1"&gt;, this method sets
     * the reference variable <code>b1</code> to the <code>Button</code>
     * instance by generating code such as
     * <pre>
     * this.b1 = TOS;
     * </pre>
     * <p>
     * The code assumes that the instance is TOS when the method is called,
     * and it leaves the stack unchanged.
     */
    private void setIDReferenceVariable(Name idName, Context context)
    {
        context.addInstruction(OP_dup);                   // ... instance instance
        context.addInstruction(OP_getlocal0);             // ... instance instance this
        context.addInstruction(OP_swap);                  // ... instance this instance
        context.addInstruction(OP_setproperty, idName);   // ... instance 
    }
    
    boolean isDataBound(IMXMLExpressionNode node)
    {
        IASNode n = node.getExpressionNode();
        return n == null || isDataBindingNode(n);
    }
    
    boolean isChildrenAsDataCodeGen(IMXMLExpressionNode node, Context context)
    {
        if (context.parentContext.makingSimpleArray) return false;
        if (context.parentContext.nonPublic) return false;
        
        return (getProject().getTargetSettings().getMxmlChildrenAsData() && 
                (node.getParent().getNodeID() == ASTNodeID.MXMLPropertySpecifierID ||
                 node.getParent().getNodeID() == ASTNodeID.MXMLStyleSpecifierID));
    }

    /**
     * Generates an instruction in the current context
     * to push the value of an {@code IMXMLBooleanNode}.
     * <p>
     * The opcode is either <code>pushtrue</code> or <code>pushfalse</code>,
     * as for an ActionScript <code>Boolean</code> literal.
     */
    void processMXMLBoolean(IMXMLBooleanNode booleanNode, Context context)
    {
        if (isChildrenAsDataCodeGen(booleanNode, context))
            context.addInstruction(OP_pushtrue); // simple type

        boolean value = isDataBound(booleanNode) ? false : booleanNode.getValue();       
        context.addInstruction(value ? OP_pushtrue : OP_pushfalse);
        traverse(booleanNode, context);
    }
    
    /**
     * Generates an instruction in the current context
     * to push the value of an {@code IMXMLIntNode}.
     * <p>
     * The opcode is <code>pushbyte</code>,
     * <code>pushshort</code>, or <code>pushint</code>
     * as for an ActionScript <code>int</code> literal.
     */
    void processMXMLInt(IMXMLIntNode intNode, Context context)
    {
        if (isChildrenAsDataCodeGen(intNode, context))
            context.addInstruction(OP_pushtrue); // simple type

        int value = isDataBound(intNode) ? 0 : intNode.getValue();
        context.pushNumericConstant(value);
        traverse(intNode, context);
     }
    
    /**
     * Generates an instruction in the current context
     * to push the value of an {@code IMXMLUintNode}.
     * <p>
     * The opcode is <code>pushuint</code>,
     * as for an ActionScript <code>uint</code> literal.
     */
    void processMXMLUint(IMXMLUintNode uintNode, Context context)
    {
        if (isChildrenAsDataCodeGen(uintNode, context))
            context.addInstruction(OP_pushtrue); // simple type

        long value = isDataBound(uintNode) ? 0 : uintNode.getValue();
        context.addInstruction(OP_pushuint, new Object[] { value });
        traverse(uintNode, context);
     }
    
    /**
     * Generates an instruction in the current context
     * to push the value of an {@code IMXMLNumberNode}.
     * <p>
     * The opcode is <code>pushdouble</code> opcode,
     * as for an ActionScript <code>Number</code> literal.
     */
    void processMXMLNumber(IMXMLNumberNode numberNode, Context context)
    {
        if (isChildrenAsDataCodeGen(numberNode, context))
            context.addInstruction(OP_pushtrue); // simple type

        double value = isDataBound(numberNode) ? Double.NaN : numberNode.getValue();
        context.addInstruction(OP_pushdouble, new Object[] { value });
        traverse(numberNode, context);
    }
    
    /**
     * Generates an instruction in the current context
     * to push the value of an {@code IMXMLStringNode}.
     * <p>
     * The opcode is <code>pushstring</code> or <code>pushnull</code>,
     * as for an ActionScript <code>String</code> literal.
     */
    void processMXMLString(IMXMLStringNode stringNode, Context context)
    {
        if (isChildrenAsDataCodeGen(stringNode, context))
            context.addInstruction(OP_pushtrue); // simple type

        String value = isDataBound(stringNode) ? null : stringNode.getValue();
        if (value != null)
            context.addInstruction(OP_pushstring, new Object[] { value });
        else
            context.addInstruction(OP_pushnull);
        
        traverse(stringNode, context);
    }
    
    /**
     * Generates an instruction in the current context
     * to push the value of an {@code IMXMLClassNode}.
     * <p>
     * The opcode is <code>getlex</code> or <code>pushnull</code>,
     * as for an ActionScript class reference.
     */
    void processMXMLClass(IMXMLClassNode classNode, Context context)
    {        
        // Don't call skipCodegen() because a null Class is represented
        // by the expression node being null.
        if (isDataBindingNode(classNode))
            return;
        
        if (isChildrenAsDataCodeGen(classNode, context))
            context.addInstruction(OP_pushtrue); // simple type

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
        // Don't call skipCodegen() because a null Function is represented
        // by the expression node being null.
        if (isDataBindingNode(functionNode))
            return;
        
        if (isChildrenAsDataCodeGen(functionNode, context))
            context.addInstruction(OP_pushtrue); // simple type

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
    }

    /**
     * Generates an instruction in the current context
     * to push the value of an {@code IMXMLRegExpNode}.
     *
     * If no expression is provided in the regexp node, this will construct a RegExp object
     * with no parameters
     */
    void processMXMLRegExp(IMXMLRegExpNode regexpNode, Context context)
    {
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
    }
    
    /**
     * A filter that only accepts {@link IMXMLInstanceNode}.
     */
    private static final Predicate<IASNode> MXML_INSTANCE_NODES = new Predicate<IASNode>()
    {
        @Override
        public boolean apply(IASNode node)
        {
            return node instanceof IMXMLInstanceNode;
        }
        @Override
        public boolean test(IASNode input)
        {
            return apply(input);
        }
    };

    /**
     * A filter that only accepts {@link IMXMLSpecifierNode}.
     */
    private static final Predicate<IASNode> MXML_SPECIFIER_NODES = new Predicate<IASNode>()
    {
        @Override
        public boolean apply(IASNode node)
        {
            return node instanceof IMXMLSpecifierNode;
        }
        @Override
        public boolean test(IASNode input)
        {
            return apply(input);
        }
    };
    
    /**
     * Generate instructions to initialize {@code DesignLayer} runtime
     * instances.
     * 
     * @param node {@code DesignLayer} AST node.
     * @param context Context of the parent node, because {@link #processNode()}
     * doesn't create a new context for {@link IMXMLDesignLayerNode}.
     */
    void processMXMLDesignLayer(IMXMLDesignLayerNode node, Context context)
    {
        // Hoist and emit children of the <fx:DesignLayer> tag. 
        traverse(node, context, MXML_INSTANCE_NODES);

        if (node.skipCodeGeneration())
            return;

        // Emit <fx:DesignLayer> itself.
        final Context designLayerInstanceContext = beginContext(node, context);
        emitDesignLayerInstance(node, designLayerInstanceContext);
        endContext(node, designLayerInstanceContext, context);

        if (!getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            // The DesignLayer object will not be added to the parent object, so it 
            // need to be removed from the stack.
            context.mainInstructionList.addInstruction(ABCConstants.OP_pop);
        }
    }

    /**
     * Emit instructions to initialize a {@code DesignLayer} object on the
     * stack.
     * <p>
     * Since MXML reduction traverses the AST top-down and then generate
     * instructions during bottom-up backtrack, the parent instance isn't
     * available at the time of reducing an instance node. For example: if
     * {@code DesignLayer #1} is the parent of {@code DesignLayer #2},
     * {@code DesignLayer #1} hasn't been initialized yet when we emit
     * instructions for initializing {@code DesignLayer #2}. In order to add
     * "#2" to "#1" as a child layer, it can't be done until reducing
     * {@code DesignLayer #2}. This is different from the initialization order
     * generated from the old compiler.
     * <p>
     * Similar to DesignLayer parent/child association, each child instance
     * under a {@code <fx:DesignLayer>} tag will have its {@code designLayer}
     * field set to the parent {@code <fx:DesignLayer>} tag instance. This can't
     * be done while reducing the child instance node. Instead, when reducing a
     * DesignLayer node, all its children's {@code designLayer} fields are
     * initialized altogether.
     * 
     * @param node {@code IMXMLDesignLayerNode} AST node.
     * @param context Context of the {@code DesignLayer} node.
     */
    private void emitDesignLayerInstance(IMXMLDesignLayerNode node, Context context)
    {
        boolean newCodeGen = getProject().getTargetSettings().getMxmlChildrenAsData();

        traverse(node, context, MXML_SPECIFIER_NODES);

        // Construct DesignLayer instance in the context's mainInstructionList.
        final Name instanceClassName = context.instanceClassName;
        context.addInstruction(OP_findpropstrict, instanceClassName);
        context.addInstruction(OP_constructprop, new Object[] {instanceClassName, 0});
        int numElements = setSpecifiers(context, newCodeGen, false);
        numElements++;  // for pushing the class
        if (!newCodeGen)
        {
            callInitialized(node, context);

            for (int i = 0; i < node.getChildCount(); i++)
            {
                final IASNode child = node.getChild(i);
                if (child instanceof IMXMLDesignLayerNode)
                {
                    // Call temp.addLayer(child) if there's a DesignLayer node in the direct children.
                    final IMXMLDesignLayerNode designLayerChildNode = (IMXMLDesignLayerNode)child;
                    if (!designLayerChildNode.skipCodeGeneration())
                        callAddLayer(node, context, designLayerChildNode);
                }
                else if (child instanceof IMXMLInstanceNode)
                {
                    // Set directInstanceChild.designLayer = designLayer.
                    final IMXMLInstanceNode instanceChildNode = (IMXMLInstanceNode)child;
                    setDesignLayer(node, context, instanceChildNode);
                }
            }
        }
        else
        {
            if (context.parentContext.isContentFactory)
                context.parentContext.incrementCounter(IL.MXML_CONTENT_FACTORY, numElements);
            else if (!context.parentContext.isContentFactory)
            {
                if (context.parentContext.makingArrayValues)
                    context.parentContext.numArrayValues += numElements;
                else    
                    context.addInstruction(OP_newarray, numElements); // if not in content factory, create the array now
            }   
        }
    }
    
    void processMXMLWebServiceOperation(IMXMLWebServiceOperationNode node, Context context)
    {
        processOperationOrMethod(node, context, node.getOperationName());
    }
    
    void processMXMLRemoteObjectMethod(IMXMLRemoteObjectMethodNode node, Context context)
    {
        processOperationOrMethod(node, context, node.getMethodName());
    }
    
    /**
     * Generates instructions in the current context to push the value of an
     * {@code IMXMLOperationNode}.
     */
    void processOperationOrMethod(IMXMLInstanceNode node, Context context, String name)
    {
        // If 'name' is undefined, the WebService node will report problem.
        if (!Strings.isNullOrEmpty(name))
        {
        	// build out the argument list if any
        	int n = node.getChildCount();
        	for (int i = 0; i < n; i++)
        	{
        		IASNode childNode = node.getChild(i);
        		if (childNode.getNodeID() == ASTNodeID.MXMLPropertySpecifierID)
        		{
        			IMXMLPropertySpecifierNode propNode = (IMXMLPropertySpecifierNode)childNode;
        			if (propNode.getName().equals("arguments"))
        			{
        				ArrayList<String> argList = new ArrayList<String>();
        				childNode = propNode.getChild(0); // this is an MXMLObjectNode
        				n = childNode.getChildCount();
        				for (i = 0; i < n; i++)
        				{
        					IASNode argNode = childNode.getChild(i);
        					propNode = (IMXMLPropertySpecifierNode)argNode;
        					argList.add(propNode.getName());
        				}
        				if (argList.size() > 0)
        				{
        		            context.startUsing(IL.PROPERTIES);
        		            context.addInstruction(OP_pushstring, "argumentNames");
        		            context.addInstruction(OP_pushtrue);
        		            for (String s : argList)
            		            context.addInstruction(OP_pushstring, s);
        	                context.addInstruction(OP_newarray, argList.size());      
        		            context.stopUsing(IL.PROPERTIES, 1);        					
        				}
        				break;
        			}
        		}
        	}
            context.startUsing(IL.WEB_SERVICE_OPERATIONS_OR_REMOTE_OBJECT_METHODS);
            context.addInstruction(OP_pushstring, name);
            processMXMLInstance(node, context);
            context.stopUsing(IL.WEB_SERVICE_OPERATIONS_OR_REMOTE_OBJECT_METHODS, 1);
        }
    }
    
    /**
     * Generates instructions in the current context
     * to push the value of an {@code IMXMLObjectNode}.
     * <p>
     * First the child nodes are processed;
     * they are {@code IMXMLPropertySpecifierNode} objects
     * which push the name/value pairs of the Object's properties.
     * (The values may be produced by calling instance initializers.)
     * Then this method emits the <code>newobject</code> opcode.
     * <p>
     * The result is code similar to that for the ActionScript
     * Object literal <code>{ a: 1, b: 2 }</code>,
     * where the emitted code is
     * <pre>
     * pushstring "a"
     * pushint 1
     * pushstring "b"
     * pushint 2
     * newobject {2}
     * </pre>
     */
    void processMXMLObject(IMXMLObjectNode objectNode, Context context)
    {
        boolean newCodeGen = getProject().getTargetSettings().getMxmlChildrenAsData();
        
        context.makingSimpleArray = context.parentContext.makingSimpleArray;
        
        if (newCodeGen && 
                !context.isStateDescriptor && 
                !context.parentContext.isContentFactory &&
                !context.parentContext.makingArrayValues)
            context.addInstruction(OP_pushfalse); // complex type

        if (newCodeGen && !context.makingSimpleArray)
        {
            context.addInstruction(OP_findpropstrict, IMXMLTypeConstants.NAME_OBJECT);
            context.addInstruction(OP_getproperty, IMXMLTypeConstants.NAME_OBJECT);
        }
        traverse(objectNode, context);
        
        int numElements = context.getCounter(IL.PROPERTIES);
        String id = objectNode.getEffectiveID();
        if (id != null)
        	numElements++;
        if (newCodeGen && !context.makingSimpleArray)
            context.pushNumericConstant(numElements);
        context.transfer(IL.PROPERTIES);
        if (id != null)
        {
        	if (id.startsWith("#"))
        		context.addInstruction(OP_pushstring, "_id");
        	else
        		context.addInstruction(OP_pushstring, "id");
            context.addInstruction(OP_pushtrue);
            context.addInstruction(OP_pushstring, id);
        }
        
        if (!newCodeGen || context.makingSimpleArray)
        {
            int n = objectNode.getChildCount();
            context.addInstruction(OP_newobject, n);       
        }
        else
        {
            context.pushNumericConstant(context.getCounter(IL.STYLES));
            context.pushNumericConstant(context.getCounter(IL.EFFECT_STYLES));
            context.pushNumericConstant(context.getCounter(IL.EVENTS));
            context.addInstruction(OP_pushnull); // no children
            numElements *= 3; // 3 entries per property
            numElements += 6;
            
            if (context.parentContext.isContentFactory)
                context.parentContext.incrementCounter(IL.MXML_CONTENT_FACTORY, numElements);
            else if (!context.parentContext.isContentFactory)
            {
                if (context.parentContext.makingArrayValues)
                    context.parentContext.numArrayValues += numElements;
                else    
                    context.addInstruction(OP_newarray, numElements); // if not in content factory, create the array now
            }
            
        }
    }
    
    /**
     * Generates instructions in the current context
     * to push the value of an {@code IMXMLArrayNode}.
     * <p>
     * First the child nodes are processed;
     * they are {@code IMXMLInstanceNode} objects which push their values.
     * (The values may be produced by calling instance initializers.)
     * Then this method emits the <code>newarray</code> opcode.
     * <p>
     * The result is code similar to that for the ActionScript
     * Object literal <code>[ 1, 2 ]</code>,
     * where the emitted code is
     * <pre>
     * pushint 1
     * pushint 2
     * newarray [2]
     * </pre>
     */
    void processMXMLArray(IMXMLArrayNode arrayNode, Context context)
    {
        boolean isSimple = true;
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            if (!context.isContentFactory)
            {
                if (context.parentContext.isStateDescriptor)
                    context.addInstruction(OP_pushnull); // array of descriptors
                else if (!context.parentContext.makingSimpleArray)
                {                    
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
                    context.makingSimpleArray = isSimple;
                    context.addInstruction(isSimple ? OP_pushtrue : OP_pushnull); // arrays are simple values      
                }
            }
        }

        traverse(arrayNode, context);
        
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
        
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            if (context.isContentFactory)
            {
                // pass the number of things we found up to the parent context. In spark controls
                // the array of children is buried by a layer or two
                context.parentContext.incrementCounter(IL.MXML_CONTENT_FACTORY, context.getCounter(IL.MXML_CONTENT_FACTORY));
                return;
            }
            else if (context.parentContext.isStateDescriptor)
            {
                context.addInstruction(OP_newarray, context.getCounter(IL.MXML_STATES_ARRAY));      
                return;
            }
            else if (context.makingArrayValues)
            {
                if (isSimple)
                    context.addInstruction(OP_newarray, nMax);
                else
                    context.addInstruction(OP_newarray, context.numArrayValues);      
                return;
            }
        }

        final int arraySize = getChildCountAfterFlattenDesignLayers(arrayNode) - numStateDependentChildren;
        context.addInstruction(OP_newarray, arraySize);      
    }
    
    /**
     * Recursively hoist children of a {@code <fx:DesignLayer>} tag to be
     * children of the parent of the {@code <fx:DesignLayer>} tag. Then count
     * the actual child nodes of the given {@code node}.
     * <p>
     * For example:
     * 
     * <pre>
     * DeferredInstanceNode
     *     ArrayNode
     *         InstanceNode #1
     *         InstanceNode #2
     *         DesignLayerNode
     *             InstanceNode #3
     *             DesignLayerNode
     *                 InstanceNode #4
     *         InstanceNode #5
     * </pre>
     * 
     * The "ArrayNode" has 4 children. However, the "DesignLayerNode" will be
     * flattened at code generation time so that instance #3 and instance #4
     * will be hoisted and become children of the "ArrayNode". As a result, the
     * "flattened" child count of the "ArrayNode" is 5 (instance node #1 to #5).
     * 
     * @param node AST node.
     * @return Number of actual child nodes after DesignLayer tags have been
     * flattened.
     */
    private static int getChildCountAfterFlattenDesignLayers(IASNode node)
    {
        int result = 0;
        for (int i = 0; i < node.getChildCount(); i++)
        {
            final IASNode child = node.getChild(i);
            if (child instanceof IMXMLDesignLayerNode)
            {
                result += ((IMXMLDesignLayerNode)child).getHoistedChildCount();
            }
            // IFilter out mxml script nodes which can get in here
            // via default properties.
            else if (!(child instanceof IMXMLScriptNode))
            {
                result++;
            }
        }
        return result;
    }
    
    /**
     * Generates instructions in the current context
     * to set an event handler specified by an {@code IMXMLEventSpecifierNode}.
     * <p>
     * This is accomplished by generating a call to <code>addEventListener()</code>
     * on the target.
     */
    Name generateVectorGenerator(Name typeName, IMXMLVectorNode vectorNode)
    {  
        // Event nodes (including state-dependent ones)
        // generate a new event handler method.
        // Create a MethodInfo and a method trait for the handler.
        Name name = getVectorGeneratorName(typeName);
        MethodInfo methodInfo = createVectorGeneratorMethodInfo(
            getProject(), vectorNode, name.getBaseName());
        addMethodTrait(name, methodInfo, false);
        
        ICompilerProject project = getProject();
        ASProjectScope projectScope = (ASProjectScope)project.getScope();
        IDefinition vectorDef = projectScope.findDefinitionByName(IASLanguageConstants.Vector_qname);
        Name vectorName = ((ClassDefinition)vectorDef).getMName(project);

        InstructionList generatorFunctionBody = new InstructionList(); 
        generatorFunctionBody.addInstruction(OP_getlocal0);
        generatorFunctionBody.addInstruction(OP_pushscope);
        // Synthesize the class Vector.<T>.
        generatorFunctionBody.addInstruction(OP_getlex, vectorName);
        generatorFunctionBody.addInstruction(OP_getlex, typeName);
        generatorFunctionBody.addInstruction(OP_applytype, 1);
        generatorFunctionBody.addInstruction(OP_getglobalscope);
        generatorFunctionBody.addInstruction(OP_getlocal1);
        generatorFunctionBody.addInstruction(OP_call, 1);
        generatorFunctionBody.addInstruction(OP_returnvalue);
        
        
        // now generate the function
        FunctionGeneratorHelper.generateFunction(emitter, methodInfo, generatorFunctionBody);

        return name;
    }

    /**
     * Generates instructions in the current context
     * to push the value of an {@code IMXMLVectorNode}.
     * <p>
     * First this method creates the synthetic Vector.<T> type
     * using the <code>applytype</code> opcode.
     * Next it creates an instance of that type
     * using the <code>construct</code> opcode.
     * <p>
     * Then the child nodes are processed;
     * they are {@code IMXMLInstanceNode} objects which push their values.
     * (The values may be produced by calling instance initializers.)
     * <p>
     * Finally this method sets the values as the Vector's elements,
     * one by one.
     * <p>
     * The result is code similar to that for the ActionScript
     * Vector literal <code>???</code>,
     * where the emitted code is
     * <pre>
     * ???
     * </pre>
     */
    void processMXMLVector(IMXMLVectorNode vectorNode, Context context)
    {
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
            context.addInstruction(OP_pushundefined); // vector type
        
        ICompilerProject project = getProject();
        int n = vectorNode.getChildCount();
        ITypeDefinition type = vectorNode.getType();
        if (type instanceof IAppliedVectorDefinition)
            type = ((IAppliedVectorDefinition)(vectorNode.getType())).resolveElementType(project);
        boolean fixed = vectorNode.getFixed();
                
        ASProjectScope projectScope = (ASProjectScope)project.getScope();
        IDefinition vectorDef = projectScope.findDefinitionByName(IASLanguageConstants.Vector_qname);
        Name vectorName = ((ClassDefinition)vectorDef).getMName(project);
        Name typeName = ((TypeDefinitionBase)type).getMName(project);
        Nsset nsSet = new Nsset(new Namespace(ABCConstants.CONSTANT_PackageNs));
        Name indexName = new Name(ABCConstants.CONSTANT_MultinameL, nsSet, null);
        
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            context.addInstruction(OP_getlex, typeName); // push the type so decoders have a hint
            Name vectorGenerator = generateVectorGenerator(typeName, vectorNode);
            context.addInstruction(OP_getlocal0);
            context.addInstruction(OP_getproperty, vectorGenerator);
            
            context.makingArrayValues = true;
            // Set each element of the vector.
            for (int i = 0; i < n; i++)
            {
                // Push the value of the element we're setting.
                // Note: Here we call processNode() on each child,
                // rather than calling traverse(), because we need to emit
                // code before and after each element value push.
                IMXMLInstanceNode elementNode = (IMXMLInstanceNode)vectorNode.getChild(i);
                processNode(elementNode, context);
                
            }
            // the type hint and conversion function add 2
            context.addInstruction(OP_newarray, context.numArrayValues + 2);      
            context.makingArrayValues = false;
        }
        else
        {
            // Synthesize the class Vector.<T>.
            context.addInstruction(OP_getlex, vectorName);
            context.addInstruction(OP_getlex, typeName);
            context.addInstruction(OP_applytype, 1);
            
            // Call the Vector.<T> constructor with 1 or two arguments.
            // The first is the number of elements.
            // The second is 'fixed', which defaults to false.
            context.pushNumericConstant(n);
            if (fixed)
                context.addInstruction(OP_pushtrue);
            context.addInstruction(OP_construct, fixed ? 2 : 1);
        
            // Set each element of the vector.
            for (int i = 0; i < n; i++)
            {
                // Push the vector instance whose element we're setting.
                context.addInstruction(OP_dup);
                
                // Push the index of the element we're setting.
                context.pushNumericConstant(i);
                
                // Push the value of the element we're setting.
                // Note: Here we call processNode() on each child,
                // rather than calling traverse(), because we need to emit
                // code before and after each element value push.
                IMXMLInstanceNode elementNode = (IMXMLInstanceNode)vectorNode.getChild(i);
                processNode(elementNode, context);
                
                // Set the element to the value.
                // This will pop the previous three values.
                context.addInstruction(OP_setproperty, indexName);
            }
        }
    }
    
    /**
     * Generates instructions in the current context
     * to push the value of a generic {@code IMXMLInstanceNode}.
     * <p>
     * using the <code>constructprop</code> opcode to create
     * an instance of the {@code IMXMLInstanceNode}'s class.
     * <p>
     * The properties, styles, and events of the instance are
     * set afterwards by codegen'ing the {@code IMXMLSpecifierNode} objects
     * that are the {@code IMXMLInstanceNode}'s children.
     * This may involve the creation of instance initializers
     * for the property/style values, and the creation of
     * event handlers for the event nodes.
     */
    void processMXMLInstance(IMXMLInstanceNode instanceNode, Context context)
    {       
        if (getProject().getTargetSettings().getMxmlChildrenAsData() && 
                !context.isStateDescriptor && 
                !context.parentContext.isContentFactory &&
                !context.parentContext.makingArrayValues)
            context.addInstruction(OP_pushfalse); // complex type

        traverse(instanceNode, context);
        
        if (!getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            if (generateDescriptorCode(instanceNode, context) && !context.instanceHasOwnInitializer)
            {
                // Construct a UIComponentDescriptor for this component
                // in the context's descriptorInstructionList.
                buildDescriptor(instanceNode, context);
            }
            
            if (generateNonDescriptorCode(instanceNode, context) || context.instanceHasOwnInitializer)
            {
                // Construct the new instance in the context's mainInstructionList.
                Name instanceClassName = context.instanceClassName;
                context.addInstruction(OP_findpropstrict, instanceClassName);
                context.addInstruction(OP_constructprop, new Object[] { instanceClassName, 0 });
                
                // Set its properties, styles, events, and effects.
                setSpecifiers(context);
                
                // Sets the id property if the instance
                // implements IDeferredInstantiationUIComponent.
                String id = instanceNode.getID();
                if (id != null)
                {
                    if (instanceNode.isDeferredInstantiationUIComponent())
                    {
                        context.pushTarget();
                        context.addInstruction(OP_pushstring, id);
                        context.addInstruction(OP_setproperty, NAME_ID);
                    }
                }
            }
        }
        else
        {
            int numElements = 0;
            context.addInstruction(OP_findpropstrict, context.instanceClassName);
            context.addInstruction(OP_getproperty, context.instanceClassName);
            if (context.parentContext.isContentFactory)
            {
                if (context.isStateDescriptor)
                    context.parentContext.incrementCounter(IL.MXML_STATES_ARRAY, 1);
                else
                    context.parentContext.incrementCounter(IL.MXML_CONTENT_FACTORY, 1);
            }
            else if (!context.parentContext.isContentFactory)
                numElements = 1; 
                // if not in content factory, create the array now
                // this is for an ArrayList as the dataProvider
            
            // AJH: maybe we shouldn't call setDocument at all
            if (!getProject().getTargetSettings().getMxmlChildrenAsData())
                setDocument(instanceNode, false, context);
            
            // Sets the id property if the instance
            // implements IDeferredInstantiationUIComponent.
            String id = instanceNode.getID();
            if (id != null)
            {
                context.startUsing(IL.PROPERTIES);
                
                context.addInstruction(OP_pushstring, "id");
                context.addInstruction(OP_pushtrue);
                context.addInstruction(OP_pushstring, id);
                
                context.stopUsing(IL.PROPERTIES, 1);
            }
            else
            {
                id = instanceNode.getEffectiveID();
                if (id != null)
                {
                    context.startUsing(IL.PROPERTIES);
                    
                    context.addInstruction(OP_pushstring, "_id");
                    context.addInstruction(OP_pushtrue);
                    context.addInstruction(OP_pushstring, id);
                    
                    context.stopUsing(IL.PROPERTIES, 1);
                }
            }
            // bail out now.  Other properties will be added in processMXMLState
            if (context.isStateDescriptor)
                return;
            
            numElements += setSpecifiers(context, true, false);
            if (context.parentContext.isContentFactory)
                context.parentContext.incrementCounter(IL.MXML_CONTENT_FACTORY, numElements);
            else if (!context.parentContext.isContentFactory)
            {
                if (context.parentContext.makingArrayValues)
                    context.parentContext.numArrayValues += numElements;
                else    
                    context.addInstruction(OP_newarray, numElements); // if not in content factory, create the array now
            }
        }
    }
    
    /**
     * Generates code equivalent to
     * <pre>
     * BindingManager.executeBindings(this, "b", b);
     * </pre>
     * to execute the databindings on a newly constructed instance b.
     * 
     * TODO Do this only if necessary.
     */
    private void executeBindingsForInstance(IMXMLInstanceNode instanceNode, Context context)
    {        
        if (!instanceNode.getClassDefinitionNode().getHasDataBindings())
            return;
        
        RoyaleProject project = getProject();
        
        // Get the Name for the mx.binding.BindingManager class.
        Name bindingManagerName = project.getBindingManagerClassName();
        
        // Get the id (possibly a generated one) for the instance.
        String id = instanceNode.getEffectiveID();
        if (id == null)
            return;
        
        // Call BindingManager.executeBindings(this, id, instance)
        // to execute the databindings for this instance.
        
        // Store the instance we've constructed in local1.
        context.addInstruction(OP_dup);
        context.addInstruction(OP_setlocal1);
        // Push BindingManager.
        context.addInstruction(OP_getlex, bindingManagerName);
        // Push 'this' as the first argument.
        context.addInstruction(OP_getlocal0);
        // Push the id of the instance we're constructed as the second argument.
        context.addInstruction(OP_pushstring, id);
        // Push the instance as the third argument.
        context.addInstruction(OP_getlocal1);
        // Call execute().
        context.addInstruction(OP_callpropvoid, EXECUTE_BINDINGS_CALL_OPERANDS);
    }
    
    /**
     * Generates instructions in the current context
     * to create an instance of <code>mx.core.ClassFactory</code>
     * for a property of type <code>mx.core.IFactory</code>
     * <p>
     * For example, the <code>itemRenderer</code> of a Spark <code>List</code>
     * has type <code>mx.core.IFactory</code>.
     * MXML such as
     * <pre>
     * <s:List id="myList" itemRenderer="MyRenderer"/>
     * </pre>
     * generates bytecode for
     * <pre>
     * myList.itemRenderer = new ClassFactory(MyRenderer).
     * myList.itemRenderer.properties = {outerDocument: this};
     * </pre>
     */
    void processMXMLFactory(IMXMLFactoryNode factoryNode, Context context)
    {
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
            context.addInstruction(OP_pushtrue);
        
        // Get the Name for the mx.core.ClassFactory class.
        ICompilerProject project = getProject();
        ClassDefinition classReference = (ClassDefinition)factoryNode.getClassReference(project);
        Name factoryClassName = classReference != null ? classReference.getMName(project) : null;
        
        // Push this class.
        context.addInstruction(OP_finddef, factoryClassName);
        
        // Push the class to be used as the generator,
        // by traversing the child MXMLClassNode.
        traverse(factoryNode, context);
        
        // Call new ClassFactory(generator), leaving the new instance on the stack.
        context.addInstruction(OP_constructprop, new Object[] { factoryClassName, 1 });
    }
    
    /**
     * Generates instructions in the current context
     * to create an instance of <code>mx.core.DeferredInstanceFromClass</code>
     * or <code>mx.core.DeferredInstanceFromFunction</code>
     * for a property of type <code>mx.core.IDeferredInstance</code>
     * or </code>mx.core.ITransientDeferredInstance</code>.
     */
    void processMXMLDeferredInstance(IMXMLDeferredInstanceNode deferredInstanceNode, Context context)
    {
        if (!getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            // Get the Name for the mx.core.DeferredInstanceFromClass
            // or mx.core.DeferredInstanceFromFunction class.
            ICompilerProject project = getProject();
            ClassDefinition classReference = (ClassDefinition)deferredInstanceNode.getClassReference(project);
            Name deferredInstanceClassName = classReference.getMName(project);
            
            // Push this class.
            context.addInstruction(OP_finddef, deferredInstanceClassName);
            
            // Push the class or function to be used as the generator,
            // by traversing the child MXMLClassNode or MXMLInstanceNode.
            traverse(deferredInstanceNode, context);
            
            // Call new DeferredInstanceFromXXX(generator), leaving the new instance on the stack.
            context.addInstruction(OP_constructprop, new Object[] { deferredInstanceClassName, 1 });
        }
        else
        {
            // Push the class or function to be used as the generator,
            // by traversing the child MXMLClassNode or MXMLInstanceNode.
            traverse(deferredInstanceNode, context);
            if (context.isContentFactory)
            {
                // assumption is that this number will cause the content factory to be copied but not used in a newarray
                // since it was already done in the mxmlContentFactory array processing
                context.parentContext.incrementCounter(IL.MXML_CONTENT_FACTORY, context.getCounter(IL.MXML_CONTENT_FACTORY));
                return;
            }
            
        }
    }
    
    /**
     * Generates instructions in the current context
     * to set a property specified by an {@code IMXMLPropertySpecifierNode}.
     * <p>
     * Property nodes are codegen'd differently depending on whether
     * they are properties of a plain Object tag or some other instance tag.
     * <p>
     * In the Object case, this method simply pushes the property name
     * as a String and then allows the child instance node to push the property value.
     * Finally the Object node pushes a <code>newobject</code> opcode,
     * This produces code similar to { a: 1, b: 2 }.
     * <p>
     * In the generic instance case, this method pushes the target,
     * then allows the child instance node to push the property value,
     * and then sets the property using a <code>setproperty</code> opcode.
     * This produces code similar to <code>this.width = 100</code>
     * or <code>temp.width = 100</code>.
     */
    void processMXMLPropertySpecifier(IMXMLPropertySpecifierNode propertyNode, Context context)
    {
        // State-dependent nodes are handled by processMXMLState().
        if (isStateDependent(propertyNode))
            return;
                
        String propertyName = propertyNode.getName();
        
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            if (propertyName.equals("mxmlContentFactory") || propertyName.equals("mxmlContent"))
            {
                context.startUsing(IL.MXML_CONTENT_FACTORY);
                context.isContentFactory = true;
                
                traverse(propertyNode, context, MXML_INSTANCE_NODES);
                
                context.isContentFactory = false;
                context.stopUsing(IL.MXML_CONTENT_FACTORY, 0);
                
            }
            else if (propertyName.equals("states"))
            {
                context.isStateDescriptor = true;
                
                context.startUsing(IL.PROPERTIES);
                
                context.addInstruction(OP_pushstring, propertyName);
                
                traverse(propertyNode, context);
                                
                context.stopUsing(IL.PROPERTIES, 1);
                                
                context.isStateDescriptor = false;
            }
            else if (propertyName.equals("model"))
            {
                context.hasModel = true;
                
                context.startUsing(IL.MXML_MODEL_PROPERTIES);
                
                context.addInstruction(OP_pushstring, propertyName);
                
                traverse(propertyNode, context);
                                
                context.stopUsing(IL.MXML_MODEL_PROPERTIES, 1);
                                
            }
            else if (propertyName.equals("beads"))
            {
                context.hasBeads = true;
                
                context.startUsing(IL.MXML_BEAD_PROPERTIES);
                
                context.addInstruction(OP_pushstring, propertyName);
                
                traverse(propertyNode, context, MXML_INSTANCE_NODES);
                                
                context.stopUsing(IL.MXML_BEAD_PROPERTIES, 1);
                                
            }
            else
            {
                if (!isDataboundProp(propertyNode))
                {
                    IDefinition propDef = propertyNode.getDefinition();
                    if (propDef == null && propertyNode.getParent() instanceof IMXMLObjectNode)
                    {
                        context.startUsing(IL.PROPERTIES);
                        
                        context.addInstruction(OP_pushstring, propertyName);
                        
                        traverse(propertyNode, context);
                        
                        context.stopUsing(IL.PROPERTIES, 1);                        
                    }
                    else if (propDef == null)
                    {
                    	getProblems().add(new AccessUndefinedPropertyProblem(propertyNode, propertyName));
                    }
                    else if (propDef.isPublic())
                    {
                        context.startUsing(IL.PROPERTIES);
                        
                        context.addInstruction(OP_pushstring, propertyName);
                        
                        traverse(propertyNode, context);
                        
                        context.stopUsing(IL.PROPERTIES, 1);
                    }
                    else
                    {
                        Context tempContext = new Context(classDefinitionNode, iinitForNonPublicProperties);
                        tempContext.nonPublic = true;
                        
                        // Push the object on which the property is to be set.
                        tempContext.pushTarget();
                        
                        // Push the property value.
                        // Do this by codegen'ing sole child, which is an IMXMLInstanceNode.
                        traverse(propertyNode, tempContext);
                        
                        Name n = ((DefinitionBase)propDef).getMName(getProject());
                        tempContext.addInstruction(OP_setproperty, n);
                    }
                }
                else
                {
                    IMXMLInstanceNode instanceNode = propertyNode.getInstanceNode();
                    if (instanceNode instanceof IMXMLSingleDataBindingNode)
                        processMXMLDataBinding((IMXMLSingleDataBindingNode)instanceNode, context);
                    else if (instanceNode instanceof IMXMLConcatenatedDataBindingNode)
                        processMXMLConcatenatedDataBinding((IMXMLConcatenatedDataBindingNode)instanceNode, context);
                }
            }
            return;
        }
        
        boolean isDb = isDataboundProp(propertyNode);

        if (generateDescriptorCode(propertyNode, context))
        {
            if (!isDb)
            {
                context.startUsing(IL.DESCRIPTOR_PROPERTIES);
                
                context.addInstruction(OP_pushstring, propertyName);
                
                traverse(propertyNode, context);
                
                context.stopUsing(IL.DESCRIPTOR_PROPERTIES, 1);
            }
            else
            {
                IMXMLInstanceNode instanceNode = propertyNode.getInstanceNode();
                if (instanceNode instanceof IMXMLSingleDataBindingNode)
                    processMXMLDataBinding((IMXMLSingleDataBindingNode)instanceNode, context);
                else if (instanceNode instanceof IMXMLConcatenatedDataBindingNode)
                    processMXMLConcatenatedDataBinding((IMXMLConcatenatedDataBindingNode)instanceNode, context);
            }
        }
        
        if (generateNonDescriptorCode(propertyNode, context))
        {
            context.startUsing(IL.PROPERTIES);
            
            if (propertyNode.getParent().getNodeID() == ASTNodeID.MXMLObjectID)
            {
                // TODO This case presuambly also needs
                // some logic involving isDb.
                
                // Push the property name.
                context.addInstruction(OP_pushstring, propertyName);
                
                // Push the property value.
                // Do this by codegen'ing sole child, which is an IMXMLInstanceNode.
                traverse(propertyNode, context);
            }
            else
            {
                // Push the object on which the property is to be set.
                if (!isDb)
                    context.pushTarget();
                
                // Push the property value.
                // Do this by codegen'ing sole child, which is an IMXMLInstanceNode.
                traverse(propertyNode, context);
                
                // Set the property.
                // unless it's a databinding, then the property is set indiretly
               if (!isDb)
               {
                   IDefinition def = propertyNode.getDefinition();
                   Name n = ((DefinitionBase)def).getMName(getProject());
                   context.addInstruction(OP_setproperty, n);
               }
            }
            
            context.stopUsing(IL.PROPERTIES, 1);
        }
    }
    
    /**
     * Is a give node a "databinding node"?
     */
    public static boolean isDataBindingNode(IASNode node)
    {
        return node instanceof IMXMLDataBindingNode;
    }
    
    // check to see if any attributes are databound.  Child instances don't count.
    // Child instances come through this method as well.
    protected static boolean instanceAffectsBindings(IMXMLInstanceNode instanceNode)
    {
        int numChildren = instanceNode.getChildCount();
        for (int i = 0; i < numChildren; i++)
        {
            final IASNode child = instanceNode.getChild(i);
            if (child instanceof IMXMLPropertySpecifierNode)
            {
                IMXMLPropertySpecifierNode propertyNode = (IMXMLPropertySpecifierNode)child;
                if (isDataBindingNode(propertyNode.getInstanceNode()))
                    return true;
            }
        }
        return false;
    }
    
    protected static boolean isDataboundProp(IMXMLPropertySpecifierNode propertyNode)
    {
        boolean ret = propertyNode.getChildCount() > 0 && isDataBindingNode(propertyNode.getInstanceNode());
        
        // Sanity check that we based our conclusion about databinding on the correct node.
        // (code assumes only one child if databinding)
        int n = propertyNode.getChildCount();
        for (int i = 0; i < n; i++)
        {
            boolean db = isDataBindingNode(propertyNode.getChild(i));
            assert db == ret;
        }
        
        return ret;
    }
    
    /**
     * Generates instructions code like
     * <pre>
     * this.fontFamily = "Arial";
     * </pre>
     * or
     * </pre>
     * this.showEffect = "Fade";
     * </pre>
     * that specifies styles or effect styles
     * in the <code>styleFactory</code> of an <code>UIComponentDescriptor</code>
     * or the <code>defaultFactory</code> of a <code>CSSStyleDeclaration</code>.
     */
    private void setFactoryStyle(IMXMLStyleSpecifierNode styleNode, Context context)
    {
        assert !isDataBindingNode(styleNode.getInstanceNode());   

        String styleName = styleNode.getName();
        
        // Push 'this'.
        context.addInstruction(OP_getlocal0);
        
        // Push the second argument: the value of the style.
        // Do this by codegen'ing sole child, which is an IMXMLInstanceNode.
        traverse(styleNode, context);
        
        // Set the style key/value on 'this', leaving nothing on the stack.
        context.addInstruction(OP_setproperty, new Name(styleName));
    }
    
    /**
     * Generates instructions in the current context
     * to set a style specified by an {@code IMXMLStyleSpecifierNode}.
     * <p>
     * This is accomplished by generating a call too <code>setStyle()</code>
     * on the target instance.
     */
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
            context.startUsing(IL.MODULE_FACTORY_STYLES);
            context.makingSimpleArray = true;
            
            setFactoryStyle(styleNode, context);
            
            context.makingSimpleArray = false;
            context.stopUsing(IL.MODULE_FACTORY_STYLES, 1);
            
            hasStyleSpecifiers = true;
        }
        
        else if (generateDescriptorCode(styleNode, context))
        {
            if (!getProject().getTargetSettings().getMxmlChildrenAsData())
            {
                context.startUsing(IL.DESCRIPTOR_STYLES);
                
                setFactoryStyle(styleNode, context);
                
                context.stopUsing(IL.DESCRIPTOR_STYLES, 1);
            }
            else
            {
                context.startUsing(IL.STYLES);

                String styleName = styleNode.getName();

                // Push the first argument: the name of the style.
                context.addInstruction(OP_pushstring, styleName);
                
                // Push the second argument: the value of the style.
                // Do this by codegen'ing sole child, which is an IMXMLInstanceNode.
                traverse(styleNode, context);
                                
                context.stopUsing(IL.STYLES, 1);

            }
        }
        
        else if (generateNonDescriptorCode(styleNode, context))
        {
            String styleName = styleNode.getName();
            
            context.startUsing(IL.STYLES);

            if (!getProject().getTargetSettings().getMxmlChildrenAsData())
            {
                // Push the object on which we'll call setStyle().
                context.pushTarget();
                
                // Push the first argument: the name of the style.
                context.addInstruction(OP_pushstring, styleName);
                
                // Push the second argument: the value of the style.
                // Do this by codegen'ing sole child, which is an IMXMLInstanceNode.
                traverse(styleNode, context);
                
                // Call setStyle() with these two arguments
                // and pop off off the return value,
                // which is 'undefined' since the return type is void.
                context.addInstruction(OP_callpropvoid, SET_STYLE_CALL_OPERANDS);
            }
            else
            {
                // Push the first argument: the name of the style.
                context.addInstruction(OP_pushstring, styleName);
                
                // Push the second argument: the value of the style.
                // Do this by codegen'ing sole child, which is an IMXMLInstanceNode.
                traverse(styleNode, context);
                                
            }
            context.stopUsing(IL.STYLES, 1);
        }
    }
    
    /**
     * Generates instructions in the current context
     * to set an effect specified by an {@code IMXMLEffectSpecifierNode}.
     */
    void processMXMLEffectSpecifier(IMXMLEffectSpecifierNode effectNode, Context context)
    {
        // State-dependent nodes are handled by processMXMLState().
        if (isStateDependent(effectNode))
            return;
        
        // Data bound styles don't need this processing either
        IMXMLInstanceNode value = effectNode.getInstanceNode();
        if (isDataBindingNode(value))
        {
            return;
        }

        String effectName = effectNode.getName();

        // Effect specifiers on the class definition node
        // generate code in the moduleFactory setter.
        if (effectNode.getParent() instanceof IMXMLClassDefinitionNode)
        {
            context.startUsing(IL.MODULE_FACTORY_EFFECT_STYLES);
            
            setFactoryStyle(effectNode, context);
            
            context.stopUsing(IL.MODULE_FACTORY_EFFECT_STYLES, 1);
            
            context.startUsing(IL.MODULE_FACTORY_EFFECTS);
            
            // Push the effect name, as part of building up an array like
            // [ "showEffect", "hideEffect" ].
            context.addInstruction(OP_pushstring, effectName);           
            
            context.stopUsing(IL.MODULE_FACTORY_EFFECTS, 1);

            hasEffectSpecifiers = true;            
        }

        else if (generateDescriptorCode(effectNode, context))
        {
            context.startUsing(IL.DESCRIPTOR_EFFECT_STYLES);
            
            setFactoryStyle(effectNode, context);
            
            context.stopUsing(IL.DESCRIPTOR_EFFECT_STYLES, 1);
            
            context.startUsing(IL.DESCRIPTOR_EFFECTS);
            
            // Push the effect name, as part of building up an array like
            // [ "showEffect", "hideEffect" ].
            context.addInstruction(OP_pushstring, effectName);
            
            context.stopUsing(IL.DESCRIPTOR_EFFECTS, 1);
        }
        
        if (generateNonDescriptorCode(effectNode, context))
        {
            context.startUsing(IL.EFFECT_STYLES);
            
            // Push the object on which we'll call setStyle().
            context.pushTarget();
            
            // Push the first argument: the name of the style.
            context.addInstruction(OP_pushstring, effectName);
            
            // Push the second argument: the value of the style.
            // Do this by codegen'ing sole child, which is an IMXMLInstanceNode.
            traverse(effectNode, context);
            
            // Call setStyle() with these two arguments
            // and pop off off the return value,
            // which is 'undefined' since the return type is void.
            context.addInstruction(OP_callpropvoid, SET_STYLE_CALL_OPERANDS);

            context.stopUsing(IL.EFFECT_STYLES, 1);
            
            context.startUsing(IL.EFFECTS);
            
            // Push the effect name, as part of building up an array like
            // [ "showEffect", "hideEffect" ] to eventually pass to
            // registerEffects().
            context.addInstruction(OP_pushstring, effectName);
            
            context.stopUsing(IL.EFFECTS, 1);
        }
    }
    
    /**
     * Generates instructions in the current context
     * to set an event handler specified by an {@code IMXMLEventSpecifierNode}.
     * <p>
     * This is accomplished by generating a call to <code>addEventListener()</code>
     * on the target.
     */
    void processMXMLEventSpecifier(IMXMLEventSpecifierNode eventNode, Context context)
    {  
        // Event nodes (including state-dependent ones)
        // generate a new event handler method.
        // Create a MethodInfo and a method trait for the handler.
        Name name = getEventHandlerName(eventNode);
        MethodInfo methodInfo = createEventHandlerMethodInfo(
            getProject(), eventNode, name.getBaseName());
        addMethodTrait(name, methodInfo, false);
        
        // Use ABCGenerator to codegen the handler body from the
        // ActionScript nodes that are the children of the event node.
        classScope.getGenerator().generateMethodBodyForFunction(
            methodInfo, eventNode, classScope, null);

        // Otherwise, state-dependent nodes are handled by processMXMLState().
        if (isStateDependent(eventNode))
            return;
        
        String eventName = eventNode.getName();
        Name eventHandler = getEventHandlerName(eventNode);

        if (generateDescriptorCode(eventNode, context))
        {
            if (getProject().getTargetSettings().getMxmlChildrenAsData())
            {
                context.startUsing(IL.EVENTS);

                // Push the first argument: the name of the event (e.g., "click").
                context.addInstruction(OP_pushstring, eventName);
                
                // Push the second argument: the handler reference (e.g., >0).
                context.addInstruction(OP_getlocal0);
                context.addInstruction(OP_getproperty, eventHandler);                

                context.stopUsing(IL.EVENTS, 1);
            }
            else
            {
                context.startUsing(IL.DESCRIPTOR_EVENTS);
                
                context.addInstruction(OP_pushstring, eventName);
                
                context.addInstruction(OP_pushstring, eventHandler.getBaseName());
                
                context.stopUsing(IL.DESCRIPTOR_EVENTS, 1);
            }
        }
        
        if (generateNonDescriptorCode(eventNode, context))
        {
            context.startUsing(IL.EVENTS);

            if (getProject().getTargetSettings().getMxmlChildrenAsData())
            {
                // Push the first argument: the name of the event (e.g., "click").
                context.addInstruction(OP_pushstring, eventName);
                
                // Push the second argument: the handler reference (e.g., >0).
                context.addInstruction(OP_getlocal0);
                context.addInstruction(OP_getproperty, eventHandler);                
            }
            else
            {
                // Push the object on which we'll call addEventListener().
                context.pushTarget();
                
                // Push the first argument: the name of the event (e.g., "click").
                context.addInstruction(OP_pushstring, eventName);
                
                // Push the second argument: the handler reference (e.g., >0).
                context.addInstruction(OP_getlocal0);
                context.addInstruction(OP_getproperty, eventHandler);
                
                // Call addEventListener() with these two arguments
                // and pop off the return value,
                // which is 'undefined' since the return type is void.
                context.addInstruction(OP_callpropvoid, ADD_EVENT_LISTENER_CALL_OPERANDS);
            }    
            context.stopUsing(IL.EVENTS, 1);
        }
    }
    
    void processMXMLDeclarations(IMXMLDeclarationsNode declarationsNode, Context context)
    {
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            context.startUsing(IL.MXML_CONTENT_FACTORY);
            context.isContentFactory = true;
            
            traverse(declarationsNode, context);
            
            context.isContentFactory = false;
            context.stopUsing(IL.MXML_CONTENT_FACTORY, 0);
        }
        else
        {
            // The <Declarations> tag itself generates no code,
            // but we have to traverse the instance nodes that are its children
            // and generate code for them.
            traverse(declarationsNode, context);
        }
    }
    
   void processMXMLScript(IMXMLScriptNode scriptNode, Context context)
    {
        // Traverse the ActionScript nodes that are direct children of the MXMLScriptNode.
        // VariableNodes, which represent var and const declarations inside the <Script>,
        // get codegen'd by the inherited declareVariable() method on ClassDirectiveProcessor.
        // FunctionNodes, which represent method declarations inside the <Script>,
        // get codegen'd by the inherited declareFunction() method on ClassDirectiveProcessor.
        traverse(scriptNode, context);
    }
    
    void processMXMLMetadata(IMXMLMetadataNode metadataNode, Context context)
    {
        // Nothing to do.
        // The metadata inside a <Metadata> tag was set onto the class definition
        // when the file scope for the MXML file was created by MXMLScopeBuilder.
        // Then finishClassDefinition() of this class will process it into ABC.
    }
    
    /**
     * Process an MXML Resource compiler directive node
     * 
     * @param node node represents Resource compiler directive in MXML
     */
    void processMXMLResource(IMXMLResourceNode node, Context context)
    {
        ITypeDefinition type = node.getType();
        
        RoyaleProject project = getProject();

        try
        {
            String bundleName = node.getBundleName();
            String key = node.getKey();
            if(bundleName != null && key != null)
            {
                //compilation unit of the file that has the compiler directive
                final ICompilationUnit refCompUnit = project.getScope(
                ).getCompilationUnitForScope(((NodeBase)node).getASScope());
                assert refCompUnit != null;

                ResourceBundleUtils.resolveDependencies(bundleName, refCompUnit, project, node, getProblems());
            }
        }
        catch(InterruptedException ie)
        {
            throw new CodegenInterruptedException(ie);
        }
        
        //Based on the type of the identifier which its value is set with this compiler
        //directive, we are figuring out which method we need to call on ResourceManager instance.
        String methodName = null;        
        if (type.isInstanceOf((ITypeDefinition)project.getBuiltinType(
                BuiltinType.STRING), project))
        {
            methodName = "getString";
        }
        else if (type.isInstanceOf((ITypeDefinition)project.getBuiltinType(
                BuiltinType.BOOLEAN), project))
        {
            methodName = "getBoolean";
        }
        else if (type.isInstanceOf((ITypeDefinition)project.getBuiltinType(
                BuiltinType.NUMBER), project))
        {
            methodName = "getNumber";
        }
        else if (type.isInstanceOf((ITypeDefinition)project.getBuiltinType(
                BuiltinType.INT), project))
        {
            methodName = "getInt";
        }
        else if (type.isInstanceOf(project.getBuiltinType(
                BuiltinType.UINT).resolveType(project), project))
        {
            methodName = "getUint";
        }
        else if (type.isInstanceOf((ITypeDefinition)project.getBuiltinType(
                BuiltinType.CLASS), project))
        {
            methodName = "getClass";
        }
        else if (type.isInstanceOf((ITypeDefinition)project.getBuiltinType(
                BuiltinType.ARRAY), project))
        {
            methodName = "getStringArray";
        }
        else
        {
            methodName = "getObject";
        }
        
        Name resourceManagerName = project.getResourceManagerClassName();
        
        // Call the method to get the value, such as
        //   ResourceManager.getInstance().getString("bundle", "key")
        // for String type.
        context.addInstruction(ABCConstants.OP_getlex, resourceManagerName);
        context.addInstruction(ABCConstants.OP_callproperty, GET_INSTANCE_CALL_OPERANDS);
        context.addInstruction(ABCConstants.OP_pushstring, node.getBundleName());
        context.addInstruction(ABCConstants.OP_pushstring, node.getKey());
        context.addInstruction(ABCConstants.OP_callproperty, new Object[] { new Name(methodName), 2 }); 
    }
    
    void processMXMLStyle(IMXMLStyleNode styleNode, Context context)
    {
        // Ignore semanticProblems. They should have been collected during the semantic analysis phase already.
        final Collection<ICompilerProblem> problems = new HashSet<ICompilerProblem>();
        final ICSSDocument css = styleNode.getCSSDocument(problems);
        if (css == CSSDocumentCache.EMPTY_CSS_DOCUMENT)
            return;
        
        final IRoyaleProject royaleProject = (IRoyaleProject)getProject();

        final CSSCompilationSession session = styleNode.getFileNode().getCSSCompilationSession();
        if (session == null)
            return;

        final CSSReducer reducer = new CSSReducer(royaleProject, css, this.emitter, session, false, styleTagIndex);
        final CSSEmitter emitter = new CSSEmitter(reducer);
        try
        {
            emitter.burm(css);
        }
        catch (Exception e)
        {
            problems.add(new CSSCodeGenProblem(e));
        }

        getProblems().addAll(problems);
        if (styleTagIndex == 0) // don't duplicate traits if there's a second style block
            reducer.visitClassTraits(ctraits);
        cinitInsns.addAll(reducer.getClassInitializationInstructions());
        styleTagIndex++;
    }
    
    /**
     * Generates instructions in the current context
     * to create an instance of a State (in MXML 2009 or later).
     * <p>
     * The 'overrides' property of the instance is automatically set
     * based on the instances, properties, styles, and events
     * that depend on the state.
     */
    @SuppressWarnings("incomplete-switch")
	void processMXMLState(IMXMLStateNode stateNode, Context context)
    {
        int numElements = 1;
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            context.isStateDescriptor = true;
        }
        
        // First process the State node as an instance node,
        // so that properties (name, stateGroups, basedOn) get set
        // and event handlers (enterState, etc.) get set.
        processMXMLInstance(stateNode, context);
        
        // Init the name property of the state (it's not a normal property specifier nodes
        // TODO: should we make this a property node?
        String name = stateNode.getStateName();
        if (name != null)
        {
            if (!getProject().getTargetSettings().getMxmlChildrenAsData())
            {
                context.addInstruction(OP_dup);
                context.addInstruction(OP_pushstring, name);
                context.addInstruction(OP_setproperty, NAME_NAME);
            }
            else
            {
                context.startUsing(IL.PROPERTIES);
                context.addInstruction(OP_pushstring, "name");
                context.addInstruction(OP_pushtrue);
                context.addInstruction(OP_pushstring, name);
                context.stopUsing(IL.PROPERTIES, 1);
            }
        }
        
        // In MXML 2009 and later, a state's 'overrides' property is implicitly
        // determined by the nodes that are dependent on this state.
        // We use these nodes to autogenerate runtime IOverride objects
        // and set them as the value of the 'overrides' property.
        IMXMLClassDefinitionNode classDefinitionNode = stateNode.getClassDefinitionNode();
        List<IMXMLNode> nodes = classDefinitionNode.getNodesDependentOnState(stateNode.getStateName());
        if (nodes != null)
        {
            if (!getProject().getTargetSettings().getMxmlChildrenAsData())
            {
                // Push the State instance on which we'll set the 'overrides' property.
                context.addInstruction(OP_dup);
            }
            else
            {
                // Set this Array as the value of the 'overrides' property of the State object.
                context.startUsing(IL.PROPERTIES);
                context.addInstruction(OP_pushstring, "overrides");
                context.addInstruction(OP_pushnull);  // complex array
            }
           
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
            
            if (!getProject().getTargetSettings().getMxmlChildrenAsData())
            {
                // Create the Array of IOverrides.
                context.addInstruction(OP_newarray, nodes.size());
                
                // Set this Array as the value of the 'overrides' property of the State object.
                context.addInstruction(OP_setproperty, NAME_OVERRIDES);
            }
            else
            {
                // Create the Array of IOverrides.
                context.addInstruction(OP_newarray, context.getCounter(IL.MXML_OVERRIDE_PROPERTIES));
                context.stopUsing(IL.PROPERTIES, 1);
                
                numElements += setSpecifiers(context, true, false);
                context.parentContext.incrementCounter(IL.MXML_STATES_ARRAY, numElements);
            }
        }
        else
        {
            if (getProject().getTargetSettings().getMxmlChildrenAsData())
            {
                numElements += setSpecifiers(context, true, false);
                context.parentContext.incrementCounter(IL.MXML_STATES_ARRAY, numElements);
            }
        }
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            context.isStateDescriptor = false;
        }
    }
    
    /**
     * Generates instructions in the current context
     * to create an instance of mx.states.SetProperty
     * with its <code>target</code>, <code>name</code>,
     * and <code>value</code> properties set.
     */
    void processPropertyOverride(IMXMLPropertySpecifierNode propertyNode, Context context)
    {
        RoyaleProject project = getProject();
        Name propertyOverride = project.getPropertyOverrideClassName();
        processPropertyOrStyleOverride(propertyOverride, propertyNode, context);
    }
    
    /**
     * Generates instructions in the current context
     * to create an instance of mx.states.SetStyle
     * with its <code>target</code>, <code>name</code>,
     * and <code>value</code> properties set.
     */
    void processStyleOverride(IMXMLStyleSpecifierNode styleNode, Context context)
    {
        RoyaleProject project = getProject();
        Name styleOverride = project.getStyleOverrideClassName();
        processPropertyOrStyleOverride(styleOverride, styleNode, context);
    }
    
    void processPropertyOrStyleOverride(Name overrideName, IMXMLPropertySpecifierNode propertyOrStyleNode, Context context)
    {
        IASNode parentNode = propertyOrStyleNode.getParent();
        String id = parentNode instanceof IMXMLInstanceNode ?
                    ((IMXMLInstanceNode)parentNode).getEffectiveID() :
                    "";
        
        String name = propertyOrStyleNode.getName();        
        
        IMXMLInstanceNode propertyOrStyleValueNode = propertyOrStyleNode.getInstanceNode();
        
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            // Construct the SetProperty or SetStyle object.
            context.addInstruction(OP_findpropstrict, overrideName);
            context.addInstruction(OP_getproperty, overrideName);
            
            boolean valueIsDataBound = isDataBindingNode(propertyOrStyleNode.getChild(0));
            context.pushNumericConstant(3);
            // Set its 'target' property to the id of the object
            // whose property or style this override will set.
            context.addInstruction(OP_pushstring, "target");
            context.addInstruction(OP_pushtrue);
            if (id.length() == 0)
                context.addInstruction(OP_pushnull);
            else
                context.addInstruction(OP_pushstring, id);

            // Set its 'name' property to the name of the property or style.
            context.addInstruction(OP_pushstring, "name");
            context.addInstruction(OP_pushtrue);
            context.addInstruction(OP_pushstring, name);

            if (!valueIsDataBound)
            {
                // Set its 'value' property to the value of the property or style.
                context.addInstruction(OP_pushstring, "value");
                processNode(propertyOrStyleValueNode, context); // push value
            }
            else
            {
                String overrideID = BINDABLE_OVERRIDE_NAME_BASE + Integer.toString(bindableOverrideCounter++);
                context.addInstruction(OP_pushstring, "id");
                context.addInstruction(OP_pushtrue);
                context.addInstruction(OP_pushstring, overrideID);
                addVariableTrait(new Name(overrideID), overrideName);
                BindingInfo bi = bindingDirectiveHelper.visitNode((IMXMLDataBindingNode)propertyOrStyleNode.getChild(0));
                bi.setDestinationString(overrideID + ".value");
            }
            
            context.pushNumericConstant(0); // styles
            context.pushNumericConstant(0); // effects
            context.pushNumericConstant(0); // events
            context.addInstruction(OP_pushnull);
            context.incrementCounter(IL.MXML_OVERRIDE_PROPERTIES, 15);      

        }
        else
        {
            // Construct the SetProperty or SetStyle object.
            context.addInstruction(OP_findpropstrict, overrideName);
            context.addInstruction(OP_constructprop, new Object[] { overrideName, 0 });
            
            // Set its 'target' property to the id of the object
            // whose property or style this override will set.
            context.addInstruction(OP_dup);
            if (id.length() == 0)
                context.addInstruction(OP_pushnull);
            else
                context.addInstruction(OP_pushstring, id);
            context.addInstruction(OP_setproperty, NAME_TARGET);
    
            // Set its 'name' property to the name of the property or style.
            context.addInstruction(OP_dup);
            context.addInstruction(OP_pushstring, name);
            context.addInstruction(OP_setproperty, NAME_NAME);
    
            // Set its 'value' property to the value of the property or style.
            context.addInstruction(OP_dup);
            boolean valueIsDataBound = isDataBindingNode(propertyOrStyleNode.getChild(0));
            if (!valueIsDataBound)
                processNode(propertyOrStyleValueNode, context); // push value
            else
                context.addInstruction(OP_pushundefined);
            context.addInstruction(OP_setproperty, NAME_VALUE);
        }    
        // TODO Handle valueFactory when we implement support for IDeferredInstance
    }
    
    /**
     * Generates instructions in the current context
     * to create an instance of mx.states.SetEventHandler
     * with its <code>target</code>, <code>name</code>,
     * and <code>handlerFunction</code> properties set.
     */
    void processEventOverride(IMXMLEventSpecifierNode eventNode, Context context)
    {
        RoyaleProject project = getProject();
        Name eventOverride = project.getEventOverrideClassName();
        
        IASNode parentNode = eventNode.getParent();
        String id = parentNode instanceof IMXMLInstanceNode ?
                    ((IMXMLInstanceNode)parentNode).getEffectiveID() :
                    "";
        
        String name = eventNode.getName();
        
        Name eventHandler = getEventHandlerName(eventNode);

        if (getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            // Construct the SetProperty or SetStyle object.
            context.addInstruction(OP_findpropstrict, eventOverride);
            context.addInstruction(OP_getproperty, eventOverride);
            
            boolean valueIsDataBound = isDataBindingNode(eventNode.getChild(0));
            context.pushNumericConstant(valueIsDataBound ? 2 : 3);
            // Set its 'target' property to the id of the object
            // whose property or style this override will set.
            context.addInstruction(OP_pushstring, "target");
            context.addInstruction(OP_pushtrue);
            if (id.length() == 0)
                context.addInstruction(OP_pushnull);
            else
                context.addInstruction(OP_pushstring, id);

            // Set its 'name' property to the name of the property or style.
            context.addInstruction(OP_pushstring, "name");
            context.addInstruction(OP_pushtrue);
            context.addInstruction(OP_pushstring, name);

            if (!valueIsDataBound)
            {
                // Set its 'value' property to the value of the property or style.
                context.addInstruction(OP_pushstring, "handlerFunction");
                context.addInstruction(OP_pushtrue);
                context.addInstruction(OP_getlocal0);
                context.addInstruction(OP_getproperty, eventHandler);
            }
            
            context.pushNumericConstant(0); // styles
            context.pushNumericConstant(0); // effects
            context.pushNumericConstant(0); // events
            context.addInstruction(OP_pushnull);
            context.incrementCounter(IL.MXML_OVERRIDE_PROPERTIES, valueIsDataBound ? 12 : 15);                  
        }
        else
        {
            // Construct the SetEventHandler object.
            context.addInstruction(OP_findpropstrict, eventOverride);
            context.addInstruction(OP_constructprop, new Object[] { eventOverride, 0 });
            
            // Set its 'target' property to the id of the object
            // whose event this override will set.
            context.addInstruction(OP_dup);
            context.addInstruction(OP_pushstring, id);
            context.addInstruction(OP_setproperty, NAME_TARGET);
    
            // Set its 'name' property to the name of the event.
            context.addInstruction(OP_dup);
            context.addInstruction(OP_pushstring, name);
            context.addInstruction(OP_setproperty, NAME_NAME);
    
            // Set its 'handlerFunction' property to the autogenerated event handler.
            context.addInstruction(OP_dup);
            context.addInstruction(OP_getlocal0);
            context.addInstruction(OP_getproperty, eventHandler);
            context.addInstruction(OP_setproperty, NAME_HANDLER_FUNCTION);
        }
    }
    
    /**
     * Generates instructions in the current context
     * to create an instance of mx.states.AddItems...
     * 
     * Assumes lookup table is still in local3
     */

    void processInstanceOverride(IMXMLInstanceNode instanceNode, Context context)
    {
        RoyaleProject project = getProject();
        Name instanceOverrideName = project.getInstanceOverrideClassName();
        
        assert nodeToIndexMap != null;
        
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            context.addInstruction(OP_findpropstrict, instanceOverrideName);
            context.addInstruction(OP_getproperty, instanceOverrideName);
        }
        else
        {
            // create the AddItems object
            context.addInstruction(OP_findpropstrict, instanceOverrideName);
            context.addInstruction(OP_constructprop, new Object[] {instanceOverrideName, 0});
            // stack: AddItems
        }
        
        // Now set properties on it!
        
        //----------------------------------------------------------------------
        // First property: set itemsFactory to the deferredInstanceFunction we created earlier
        Integer index = nodeToIndexMap.get(instanceNode);
        assert index != null;
        
        InstructionList addItemsIL = new InstructionList();
        int addItemsCounter = 0;
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            
            addItemsIL.addInstruction(OP_pushstring, "itemsDescriptorIndex");
            addItemsIL.addInstruction(OP_pushtrue);  // the value is an array of descriptor data that will be parsed later
            addItemsIL.pushNumericConstant(index);     // stack: ..., addItems, addItems, instanceFuncs[], index
            addItemsCounter++;
        }
        else
        {
            context.addInstruction(OP_dup);         // stack: ..., addItems, addItems
            context.addInstruction(OP_getlocal3);   // stack: ..., addItems, addItems, instanceFuncs[]
            context.pushNumericConstant(index);     // stack: ..., addItems, addItems, instanceFuncs[], index
            context.addInstruction(OP_getproperty,  IMXMLTypeConstants.NAME_ARRAYINDEXPROP); 
                                                    // stack: ..., addItems, addItems, instanceFunction
           
            context.addInstruction(OP_setproperty, new Name("itemsFactory"));
                                                    // stack: ..., addItems
        }
        
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
               
               
               if (getProject().getTargetSettings().getMxmlChildrenAsData())
               {
                   addItemsIL.addInstruction(OP_pushstring, "destination");
                   addItemsIL.addInstruction(OP_pushtrue); // simple type
                   addItemsIL.addInstruction(OP_pushstring, parentId); 
                   addItemsIL.addInstruction(OP_pushstring, "propertyName");
                   addItemsIL.addInstruction(OP_pushtrue); // simple type
                   addItemsIL.addInstruction(OP_pushstring, propName); 
                   addItemsCounter += 2;
               }
               else
               {
                   context.addInstruction(OP_dup);         // stack: ..., addItems, addItems
                   context.addInstruction(OP_pushstring, parentId); 
                   context.addInstruction(OP_setproperty, new Name("destination"));
                                                           // stack: ..., addItems
                   context.addInstruction(OP_dup);         // stack: ..., addItems, addItems
                   context.addInstruction(OP_pushstring, propName); 
                   context.addInstruction(OP_setproperty, new Name("propertyName"));
                                                           // stack: ..., addItems
               }
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
            if (sib instanceof IMXMLInstanceNode)
            {
               
                // stop looking for previous nodes when we find ourself
                if (sib == instanceNode)
                    break;
    
                if (!isStateDependent(sib))
                {
                    prevStatelessSibling = sib;
                }
            }
        }
        
        if (prevStatelessSibling == null) {
            positionPropertyValue = "first";        // TODO: these should be named constants
        }
        else {
            positionPropertyValue = "after";
            relativeToPropertyValue = ((IMXMLInstanceNode)prevStatelessSibling).getEffectiveID();
        }
       
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            // position
            addItemsIL.addInstruction(OP_pushstring, "position");
            addItemsIL.addInstruction(OP_pushtrue);
            addItemsIL.addInstruction(OP_pushstring, positionPropertyValue); 
            addItemsCounter++;
        }
        else
        {
            // position
            context.addInstruction(OP_dup);        
            context.addInstruction(OP_pushstring, positionPropertyValue); 
            context.addInstruction(OP_setproperty, new Name("position"));
            
        }
        
        // relativeTo
        if (relativeToPropertyValue != null)
        {
            if (getProject().getTargetSettings().getMxmlChildrenAsData())
            {
                // position
                addItemsIL.addInstruction(OP_pushstring, "relativeTo");
                addItemsIL.addInstruction(OP_pushtrue);
                addItemsIL.addInstruction(OP_pushstring, relativeToPropertyValue); 
                addItemsCounter++;
            }
            else
            {
                context.addInstruction(OP_dup);        
                context.addInstruction(OP_pushstring, relativeToPropertyValue); 
                context.addInstruction(OP_setproperty, new Name("relativeTo"));
            }
        }
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
        {
            context.pushNumericConstant(addItemsCounter);
            context.addAll(addItemsIL);
            context.pushNumericConstant(0); // styles
            context.pushNumericConstant(0); // effects
            context.pushNumericConstant(0); // events
            context.addInstruction(OP_pushnull); // children
            // we add 6: one for the addItems class def, and one each for the count of properties, styles, effect, children
            context.incrementCounter(IL.MXML_OVERRIDE_PROPERTIES, addItemsCounter * 3 + 6);      
        }
    }
     
    void processMXMLDataBinding(IMXMLSingleDataBindingNode node, Context context)
    {
        bindingDirectiveHelper.visitNode(node);
    }
    
    void processMXMLConcatenatedDataBinding(IMXMLConcatenatedDataBindingNode node, Context context)
    {
        bindingDirectiveHelper.visitNode(node);
    }

    void processMXMLBinding(IMXMLBindingNode node, Context context)
    {
        bindingDirectiveHelper.visitNode(node);
    }

    void processMXMLRepeater(IMXMLRepeaterNode node, Context context)
    {
        // not yet implemented
    }

    void processMXMLImplements(IMXMLImplementsNode node, Context context)
    {
        // don't do anything it was processed elsewhere
    }

    void processMXMLComponent(IMXMLComponentNode node, Context context)
    {
        boolean inDecl = false;
        if (getProject().getTargetSettings().getMxmlChildrenAsData() &&
                node.getParent() instanceof IMXMLDeclarationsNode)
        {
            inDecl = true;
            context = context.parentContext;    // up-level to parent context's properties list
            context.startUsing(IL.PROPERTIES);
            String id = node.getID();
            if (id == null)
            	id = node.getClassName();
            assert (id != null) : "id should never be null, as always added";
        	context.addInstruction(OP_pushstring, id);
        }
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
        context.addInstruction(OP_dup);
        context.addInstruction(OP_pushstring, IMXMLLanguageConstants.PROPERTY_OUTER_DOCUMENT);
        context.addInstruction(OP_getlocal0);
        context.addInstruction(OP_newobject, 1);
        context.addInstruction(OP_setproperty, IMXMLTypeConstants.NAME_PROPERTIES);
        
        if (inDecl)
            context.stopUsing(IL.PROPERTIES, 1);
    }

    void processMXMLLibrary(IMXMLLibraryNode node, Context context)
    {
        traverse(node, context);
    }

    void processMXMLDefinition(IMXMLDefinitionNode node, Context context)
    {
        traverse(node, context);
    }

    protected void processMXMLClassDefinition(IMXMLClassDefinitionNode node, Context context)
    {
        // Create the <Component> or <Definition> class.
        MXMLClassDirectiveProcessor dp = new MXMLClassDirectiveProcessor(node, globalScope, emitter);
        dp.processMainClassDefinitionNode(node);
        dp.finishClassDefinition();
        
        // Leave a reference to the class on the stack.
        ClassDefinition classDefinition =
            (ClassDefinition)((IMXMLClassDefinitionNode)node).getClassDefinition();
        ICompilerProject project = getProject();
        Name className = classDefinition.getMName(project);
        context.addInstruction(OP_getlex, className);
    }
    
    void processMXMLEmbed(IMXMLEmbedNode node, Context context)
    {
        // push a reference to the asset class on the stack
        ICompilerProject project = getProject();
        ClassDefinition classDefinition = (ClassDefinition)node.getClassReference(project);
        Name className = classDefinition != null ? classDefinition.getMName(project) : null;
        if (getProject().getTargetSettings().getMxmlChildrenAsData())
            context.addInstruction(OP_pushtrue);

        context.addInstruction(OP_getlex, className);        
    }

    void processMXMLXML(IMXMLXMLNode node, Context context)
    {
        String xmlString = node.getXMLString();
        if (xmlString == null)
        {
            if (!getProject().getTargetSettings().getMxmlChildrenAsData())
            	context.addInstruction(OP_pushnull);
        }
        else if (node.getXMLType() == IMXMLXMLNode.XML_TYPE.E4X)
        {
            // new XML(xmlString);
            context.addInstruction(OP_findpropstrict, ABCGeneratingReducer.xmlType);
            context.addInstruction(OP_pushstring, xmlString);
            context.addInstruction(OP_constructprop, new Object[] { ABCGeneratingReducer.xmlType, 1 });
         }
        else if (node.getXMLType() == IMXMLXMLNode.XML_TYPE.OLDXML)
        {
            // mx.utils.XMLUtil.createXMLDocument(xmlString).firstChild
            RoyaleProject royaleProject = (RoyaleProject)getProject();
            context.addInstruction(OP_getlex, royaleProject.getXMLUtilClassName());
            context.addInstruction(OP_pushstring, node.getXMLString());
            context.addInstruction(OP_callproperty, CREATE_XML_DOCUMENT_CALL_OPERANDS);
            context.addInstruction(OP_getproperty, new Name("firstChild"));
        }

        // Traverse the children - these will be any MXMLBindingNode that were created for
        // databinding expressions inside the XML
        traverse(node, context);
    }
    
    void processMXMLXMLList(IMXMLXMLListNode node, Context context)
    {
        context.addInstruction(OP_findpropstrict, ABCGeneratingReducer.xmlListType);
        context.addInstruction(OP_pushstring, node.getXMLString());
        context.addInstruction(OP_constructprop, CONSTRUCT_XML_LIST_OPERANDS);
        
        // Traverse the children - these will be any MXMLBindingNode that were created for
        // databinding expressions inside the XML
        traverse(node, context);
    }
    
    /**
     * Generates instructions for an {@link IMXMLModelNode}.
     * <p>
     * The instructions leave an ObjectProxy on the stack.
     */
    void processMXMLModel(IMXMLModelNode node, Context context)
    {
        // Create an ObjectProxy instance. Even an empty Model tag creates one.
        pushModelClass(context);
        
        // Set properties on the ObjectProxy.
        traverse(node, context);
    }
    
    /**
     * Pushes an instance of <code>mx.utils.ObjectProxy</code>, which represents
     * an MXML Model, and is also the value of the Model's non-leaf properties.
     */
    private void pushModelClass(Context context)
    {
        RoyaleProject project = getProject();
        Name modelClassName = project.getModelClassName();
        
        // Push a new ObjectProxy.
        context.addInstruction(OP_findpropstrict, modelClassName);
        context.addInstruction(OP_constructprop, new Object[] { modelClassName, 0 });                
    }
    
    /**
     * Generates instructions for an {@link IMXMLModelRootNode}.
     * <p>
     * The root node by itself doesn't actually generate anything,
     * but its {@link IMXMLModelPropertyNode} children cause properties
     * to be set on the ObjectProxy created by the parent {@link IMXMLModelNode}.
     * <p>
     * The stack will be unaffected.
     */
    void processMXMLModelRoot(IMXMLModelRootNode node, Context context)
    {
        // We can't just traverse the child nodes since multiple child nodes
        // (which might not even be adjacent) set a property to an Array value.
        setModelProperties(node, context);
    }
    
    /**
     * Generates instructions that cause properties to be set on the target ObjectProxy.
     * 
     * The stack will be unaffected.
     */
    private void setModelProperties(IMXMLModelPropertyContainerNode node, Context context)
    {
        // Set its properties. Node that multiple child property nodes with the
        // same name, and not-necessarily adjacent, create an array-valued property.
        // We have to use processNode() rather than traverse()
        // because the child nodes can't be get code-gen'd in child order.
        for (String propertyName : node.getPropertyNames())
        {
            context.pushTarget();
            pushModelPropertyValue(node, propertyName, context);
            context.addInstruction(OP_setproperty, new Name(propertyName));
        }
    }
    
    /**
     * Generates instructions to push a property value for a Model
     * (possibly an Array value) onto the stack.
     */
    private void pushModelPropertyValue(IMXMLModelPropertyContainerNode node, String propertyName, Context context)
    {
        IMXMLModelPropertyNode[] childNodes = node.getPropertyNodes(propertyName);
        int n = childNodes.length;
        if (n > 1)
        {
            for (IMXMLModelPropertyNode childNode : childNodes)
            {
                processMXMLModelProperty(childNode, context);
            }
            context.addInstruction(OP_newarray, n);
        }
        else if (n == 1)
        {
            processMXMLModelProperty(childNodes[0], context);
        }
    }
    
    /**
     * Generates instructions for an {@link IMXMLModelPropertyNode}.
     * <p>
     * For a leaf property, the instructions will set that property
     * onto the ObjectProxy on the top of the stack.
     * <p>
     * For a non-leaf property, the instructions will create another
     * ObjectProxy and set that as the property value.
     * <p>
     * The stack will be unaffected.
     */
    void processMXMLModelProperty(IMXMLModelPropertyNode node, Context context)
    {
       if (node.hasLeafValue())
        {
            if (isDataBindingNode(node.getInstanceNode()))
            {
                // If the model propery is a data binding, we need to use null
                // as the initial value, then traverse to find the binding and
                // evaluate it.
                context.addInstruction(OP_pushnull);
                traverse(node, context);                

            }
            else
            {
                // For the non-binding case,
                // Push the property value represented by the child instance node.
                traverse(node, context);                
            }
        }
        else
        {
            // Push an ObjectProxy instance as the property value.
            pushModelClass(context);
            
            // Set its properties.
            setModelProperties(node, context);
        }
    }
    
    void processMXMLPrivate(IMXMLPrivateNode node, Context context)
    {
        // The <fx:Private> tag is represented in the tree by an IMXMLPrivateNode
        // but it doesn't generate any code.
    }
    
    void processMXMLClear(IMXMLClearNode node, Context context)
    {
        // TODO
    }
    
    void processMXMLReparent(IMXMLReparentNode node, Context context)
    {
        // TODO
    }
    
    private void generateStylesAndEffects(Context context)
    {
        if (hasStyleSpecifiers || hasEffectSpecifiers || styleTagIndex > 0)
        {
            // We can only override the setter for moduleFactory
            // if the class implements mx.core.IFlexModule.
            RoyaleProject project = getProject();
            String royaleModuleInterface = project.getFlexModuleInterface();
            if (classDefinition.isInstanceOf(royaleModuleInterface, project))
            {
                addVariableTrait(NAME_MODULE_FACTORY_INITIALIZED, NAME_BOOLEAN);
                
                overrideModuleFactorySetter(context);
            }
        }
    }
    
    /**
     * Override the <code>moduleFactory</code> setter in the generated class.
     * This override function is used to inject code into the style manager
     * and the component's own <code>CSSStyelDeclaration</code>.
     * We need this function if there are Style tags
     * or style specifiers on the class definition tag.
     * <pre>
     * override public function set moduleFactory(factory:IFlexModuleFactory):void
     * {
     *     super.moduleFactory = factory;
     *     
     *     // Do the style initialization only the first time this setter is called.
     *     if (mfi)
     *         return;
     *     mfi = true;
     *
     *     // Initialize this component's styleDeclaration.defaultFactory.
     *     // This part is generated only if there are style specifiers
     *     // on the class definition tag.
     *     if (!this.styleDeclaration)
     *         this.styleDeclaration = new CSSStyleDeclaration(null, this.styleManager);
     *     this.styleDeclaration.defaultFactory = function():void
     *     {
     *         this.fontSize = 12;
     *         this.fontFamily = "Arial";
     *     };
     *
     *     // Initialize the StyleManager.
     *     // This part is generated only if there are <Style> tags
     *     // within the class definition tag.
     *     flex.compiler.support.generateCSSStyleDeclarationsForComponents(
     *         styleManager, factoryFunctions, dataArray);
     *         
     *     styleManager.initProtoChainRoots();
     * }
     * </pre>
     */
    private void overrideModuleFactorySetter(Context context)
    {
        final Name moduleFactoryName = new Name("moduleFactory");
        final IResolvedQualifiersReference styleManagerReference = ReferenceFactory.packageQualifiedReference(
                this.getProject().getWorkspace(),
                "mx.styles.StyleManagerImpl");
        final Name styleManagerReferenceName = styleManagerReference.getMName();

        final MethodInfo methodInfo = new MethodInfo();
        methodInfo.setMethodName("moduleFactory");
        methodInfo.setReturnType(NAME_VOID);
        methodInfo.setParamNames(ImmutableList.<String> of("factory"));

        final Vector<Name> paramTypes = new Vector<Name>();
        final Name royaleModuleFactoryTypeName = new Name(
                new Namespace(ABCConstants.CONSTANT_PackageNs, "mx.core"),
                "IFlexModuleFactory");
        paramTypes.add(royaleModuleFactoryTypeName);
        methodInfo.setParamTypes(paramTypes);
        
        final InstructionList methodInstructions = new InstructionList();
        
        // super.moduleFactory = factory;
        methodInstructions.addInstruction(ABCConstants.OP_getlocal0);
        methodInstructions.addInstruction(ABCConstants.OP_getlocal1);
        methodInstructions.addInstruction(ABCConstants.OP_setsuper, moduleFactoryName);
        
        // if (mfi)
        //     return;
        Label label1 = new Label();
        methodInstructions.addInstruction(OP_getlocal0);
        methodInstructions.addInstruction(OP_getproperty, NAME_MODULE_FACTORY_INITIALIZED);
        methodInstructions.addInstruction(OP_iffalse, label1);
        
        methodInstructions.addInstruction(OP_returnvoid);
        methodInstructions.labelNext(label1);

        // mfi = true;
        methodInstructions.addInstruction(OP_getlocal0);
        methodInstructions.addInstruction(OP_pushtrue);
        methodInstructions.addInstruction(OP_setproperty, NAME_MODULE_FACTORY_INITIALIZED);
        
        if (hasStyleSpecifiers || hasEffectSpecifiers)
        {
            RoyaleProject project = this.getProject();
            Name cssStyleDeclarationName = project.getCSSStyleDeclarationClassName();
            
            // Create an anonymous function from the style and effect-style specifiers
            // for the class definition tag. It will be set as the value of
            // styleDeclaration.defaultFactory.
            MethodInfo styleDeclarationDefaultFactory = createStyleDeclarationDefaultFactory(context);
            
            // if (this.styleDeclaration == null)
            //     this.styleDeclaration = new CSSStyleDeclaration(null, this.styleManager);
            Label label2 = new Label();
            methodInstructions.addInstruction(OP_getlocal0);
            methodInstructions.addInstruction(OP_getproperty, NAME_STYLE_DECLARATION);
            methodInstructions.addInstruction(OP_iftrue, label2);
    
            methodInstructions.addInstruction(OP_getlocal0);
            methodInstructions.addInstruction(OP_findpropstrict, cssStyleDeclarationName);
            methodInstructions.addInstruction(OP_pushnull);
            methodInstructions.addInstruction(OP_getlocal0);
            methodInstructions.addInstruction(OP_getproperty, NAME_STYLE_MANAGER);
            methodInstructions.addInstruction(OP_constructprop, new Object[] { cssStyleDeclarationName, 2} );
            methodInstructions.addInstruction(OP_setproperty, NAME_STYLE_DECLARATION);
            methodInstructions.labelNext(label2);
    
            // this.styleDeclaration.defaultFactory = <anonymous function>
            methodInstructions.addInstruction(OP_getlocal0);
            methodInstructions.addInstruction(OP_getproperty, NAME_STYLE_DECLARATION);
            methodInstructions.addInstruction(OP_newfunction, styleDeclarationDefaultFactory);
            methodInstructions.addInstruction(OP_setproperty, NAME_DEFAULT_FACTORY);
        }
        
        if (hasEffectSpecifiers)
        {
            // this.registerEffects([ ... ]);
            methodInstructions.addInstruction(OP_getlocal0);
            methodInstructions.addAll(context.get(IL.MODULE_FACTORY_EFFECTS));
            methodInstructions.addInstruction(OP_newarray, context.getCounter(IL.MODULE_FACTORY_EFFECTS));
            methodInstructions.addInstruction(OP_callpropvoid, REGISTER_EFFECTS_CALL_OPERANDS);
        }
        
        if (styleTagIndex > 0)
        {
            // generateCSSStyleDeclarationsForComponents(super.styleManager, factoryFunctions, data);
            methodInstructions.addInstruction(ABCConstants.OP_getlex, styleManagerReferenceName);
            methodInstructions.addInstruction(ABCConstants.OP_getlocal0);
            methodInstructions.addInstruction(ABCConstants.OP_getsuper, NAME_STYLE_MANAGER);
            methodInstructions.addInstruction(ABCConstants.OP_getlex, CSSReducer.NAME_FACTORY_FUNCTIONS);
            methodInstructions.addInstruction(ABCConstants.OP_getlex, CSSReducer.NAME_DATA_ARRAY); 
            methodInstructions.addInstruction(ABCConstants.OP_callproperty, new Object[] { NAME_GENERATE_CSSSTYLEDECLARATIONS, 3 });
            if (styleTagIndex > 1)
            {
                for (int i = 1; i < styleTagIndex; i++)
                {
                    methodInstructions.addInstruction(ABCConstants.OP_getlex, styleManagerReferenceName);
                    methodInstructions.addInstruction(ABCConstants.OP_getlocal0);
                    methodInstructions.addInstruction(ABCConstants.OP_getsuper, NAME_STYLE_MANAGER);
                    methodInstructions.addInstruction(ABCConstants.OP_getlex, new Name(CSSReducer.FACTORY_FUNCTIONS + Integer.toString(i)));
                    methodInstructions.addInstruction(ABCConstants.OP_getlex, new Name(CSSReducer.NAME_DATA_ARRAY + Integer.toString(i))); 
                    methodInstructions.addInstruction(ABCConstants.OP_callproperty, new Object[] { NAME_GENERATE_CSSSTYLEDECLARATIONS, 3 });                    
                }
            }
        }
        
        // styleManager.initProtoChainRoots();
        methodInstructions.addInstruction(ABCConstants.OP_getlocal0);
        methodInstructions.addInstruction(ABCConstants.OP_getsuper, NAME_STYLE_MANAGER);
        methodInstructions.addInstruction(ABCConstants.OP_callpropvoid, new Object[] {new Name("initProtoChainRoots"), 0 }); 
        methodInstructions.addInstruction(ABCConstants.OP_returnvoid);
        
        generateMethodBody(methodInfo, classScope, methodInstructions);

        addSetter(moduleFactoryName, methodInfo, true);
    }
    
    /**
     * Creates an anonymous factory function for the styles and effect styles
     * on the class definition node. It first sets the styles
     * and then the effect styles, as in
     * <pre>
     * function():void
     * {
     *     this.fontSize = 12;
     *     this.fontFamily = "Arial";
     *     
     *     this.showEffect = "Fade";
     *     this.hideEffect = "Fade";
     * }
     * </pre>
     */
    private MethodInfo createStyleDeclarationDefaultFactory(Context context)
    {
        InstructionList body = new InstructionList();
        
        body.addAll(context.get(IL.MODULE_FACTORY_STYLES));
        body.addAll(context.get(IL.MODULE_FACTORY_EFFECT_STYLES));
        body.addInstruction(OP_returnvoid);
        
        return createNoParameterAnonymousFunction(NAME_VOID, body);
    }
    
    /**
     * The purpose of this Context class is to keep
     * track of whether we are emitting instructions
     * for a class definition node or an instance node,
     * and which instruction list the instructions
     * are being emitted into.
     */
    static class Context
    {
        /**
         * Constructs the context for an MXML document node.
         */
        private Context(IMXMLClassDefinitionNode classDefinitionNode, InstructionList instructionList)
        {
            this(classDefinitionNode, instructionList, OP_getlocal0);
        }
        
        /**
         * Constructs the context for an MXML instance node.
         */
        private Context(IMXMLInstanceNode instanceNode, InstructionList instructionList)
        {
            this(instanceNode, instructionList, OP_dup);
        }
        
        private Context(IMXMLClassReferenceNode node, InstructionList instructionList, int pushTargetOpcode)
        {
            this.node = node;
            currentInstructionList = mainInstructionList = instructionList;
            this.pushTargetOpcode = pushTargetOpcode;
            needsDescriptor = node.needsDescriptor();
        }
        
        /**
         * The class reference node that created this Context.
         */
        private final IMXMLClassReferenceNode node;
        
        /**
         * The main instruction list for the Context.
         * If we are inlining an instance, this may
         * be shared with the Context for an ancestor node.
         */
        private InstructionList mainInstructionList;
        
        /**
         * The current instruction list of this context,
         * to which instructions are added when you call
         * {@link #addInstruction()} in {@link MXMLClassDirectiveProcessor}.
         * This might be the main instruction list
         * for the constructor or instance initializer method,
         * or one of the helper instruction lists that accumulate
         * instructions just for properties, styles, events,
         * effects, or child descriptors.
         */
        private InstructionList currentInstructionList;
        
        /**
         * A flag indicating whether the class reference node
         * needs to codegen a for a non-public property
         */
        private boolean nonPublic = false;
        
       /**
         * A flag indicating whether the class reference node
         * needs to codegen a UIComponentDescriptor.
         */
        private boolean needsDescriptor = false;
        
        /**
         * This map contains helper instruction lists
         * accessed via methods like startUsing(), transfer(), etc.
         * <p>
         * These helper lists are used to segregate instructions
         * for setting properties, setting styles, registering
         * events, and registering effects.
         * <p>
         * One reason that each node cannot simply emit into the
         * same list is that for a tag such as
         * <pre>
         * &lt;s:Button label="OK" initialize="..."
         *           fontSize="12" showEffect="Fade"
         *           width="100" click="..."
         *           fontFamily="Arial" hideEffect="Fade"/&gt;
         * </pre>
         * the old compiler emitted grouped code like
         * <pre>
         * temp.label = "OK";
         * temp.width = 100;
         * temp.setStyle("fontSize", 12);
         * temp.setStyle("fontFamily", "Arial");
         * temp.setStyle("showEffect", "Fade");
         * temp.setStyle("hideEffect", "Fade");
         * temp.addEventListener("initialize", ...);
         * temp.addEventListener("click", ...);
         * temp.registerEffects([ "showEffect", "hideEffect" ]);
         * </pre>
         * rather than in node order
         * <pre>
         * temp.label = "OK";
         * temp.addEventListener("initialize", ...);
         * temp.setStyle("fontSize", 12);
         * temp.setStyle("showEffect", "Fade");
         * temp.registerEffect("showEffect");
         * temp.width = 100;
         * temp.addEventListener("click", ...);
         * temp.setStyle("fontFamily", "Arial");
         * temp.setStyle("hideEffect", "Fade");
         * temp.registerEffect("hideEffect");
         * </pre>
         * Another reason is that in a UIComponentDescriptor
         * the properties, events, styles, and effects
         * are similarly grouped with each other.
         */
        private Map<IL, InstructionList> instructionListMap;
        
        /**
         * This stack is maintained by startUsing() / stopUsing()
         * and keeps track of which instructions list we'll return
         * to when we're done with one we're temporarily using.
         * Sometimes we need to nest using helper lists, such as
         * when we're building the descriptor but have to add
         * instructions to the descriptor properties first.
         */
        private Deque<InstructionList> instructionListDeque;
        
        /**
         * This map keeps track of how many properties, events,
         * styles, and effects we've generated.
         */
        private Map<IL, Integer> counterMap;
        
        /**
         * This Name is used in instance contexts to keep track
         * of the instance's type.
         */
        Name instanceClassName;
        
        /**
         * This flag used in instance contexts to keep track
         * of whether the instance has it own initializer method.
         */
        private boolean instanceHasOwnInitializer;
        
        /**
         * This flag used in instance contexts to keep track
         * of whether we are processing the mxmlContentFactory.
         */
        boolean isContentFactory;
        
        /**
         * This flag used in instance contexts to keep track
         * of whether we saw the model property.
         */
        boolean hasModel;
        
        /**
         * This flag used in instance contexts to keep track
         * of whether we saw the beads property
         */
        boolean hasBeads;
        
        /**
         * This flag is true when setting styles
         * in a style factory or making other
         * simple arrays that don't have instances
         * as values.
         */
        boolean makingSimpleArray;

        /**
         * This flag is true when setting values
         * in an array (other than contextFactory)
         */
        boolean makingArrayValues;

        /**
         * number of elements in array when makingArrayValues
         */
        int numArrayValues = 0;
        
        /**
         * This flag used in instance contexts to keep track
         * of whether we are processing a state.
         */
        boolean isStateDescriptor;
        
        /**
         * reference to parent context
         */
        Context parentContext;
        
        /**
         * The opcode that pushes the target object
         * on which properties/styles/event are to be set.
         * <p>
         * In a document context, code such as
         * <pre>
         *   this.width = 100;
         * </pre>
         * to set properties/styles/events is emitted
         * into the class constructor.
         * Since 'this' is always in local 0,
         * the relevant opcode is OP_getlocal0.
         * <p>
         * In an instance context, code such as
         * <pre>
         *   temp = new Button();
         *   temp.width = 100;
         * </pre>
         * to set properties/styles/events is emitted
         * into either an instance initializer method
         * for this instance, into an instance initializer
         * method for another instance, or into the class
         * constructor.
         * In each case the relevant opcode is OP_dup
         * because the temp object is at the top of the stack.
         */
        protected int pushTargetOpcode;
        
        /**
         * Pushes the object whose property, style, or event we want to set
         * onto the current instruction list.
         */
        private void pushTarget()
        {
            addInstruction(pushTargetOpcode);
        }
        
        /**
         * Adds an instruction to the current instruction list.
         */
        private void addInstruction(int opcode)
        {
            currentInstructionList.addInstruction(opcode);
        }
        
        /**
         * Adds an instruction to the current instruction list.
         */
        private void addInstruction(int opcode, int immed)
        {
            currentInstructionList.addInstruction(opcode, immed);
        }
        
        /**
         * Adds an instruction to the current instruction list.
         */
        private void addInstruction(int opcode, Object operand)
        {
            currentInstructionList.addInstruction(opcode, operand);
        }
        
        /**
         * Adds an instruction to the current instruction list.
         */
        private void addInstruction(int opcode, Object[] operands)
        {
            currentInstructionList.addInstruction(opcode, operands);
        }
        
        /**
         * Adds an entire helper instruction list to the current instruction list.
         */
        private void addAll(InstructionList sourceInstructionList)
        {
            if (sourceInstructionList != null)
                currentInstructionList.addAll(sourceInstructionList);
        }
        
        /**
         * Labels the next instruction in the current instruction list.
         */
        private void labelNext(Label label)
        {
            currentInstructionList.labelNext(label);
        }
        
        private void pushNumericConstant(long value)
        {
            currentInstructionList.pushNumericConstant(value);
        }
        
         /**
         * Returns a helper instruction list, lazily creating it if it doesn't exist.
         */
        private InstructionList get(IL whichList)
        {
            if (instructionListMap == null)
                instructionListMap = new EnumMap<IL, InstructionList>(IL.class);
            
            InstructionList instructionList = instructionListMap.get(whichList);
            
            if (instructionList == null)
            {
                instructionList = new InstructionList();
                instructionListMap.put(whichList, instructionList);
            }
            
            return instructionList;
        }
        
        /**
         * Makes a specified helper instruction list the current one
         * on which addInstruction() etc. will operate.
         */
        void startUsing(IL whichList)
        {
            InstructionList instructionListToUse = get(whichList);
            
            if (instructionListDeque == null)
                instructionListDeque = new ArrayDeque<InstructionList>();
            
            instructionListDeque.push(currentInstructionList);
            currentInstructionList = instructionListToUse;
        }
        
        /**
         * Returns to the previously current instruction list
         * (which might be another helper list or the main list).
         * Also increments the counter associated with the old list,
         * for keeping track of how many propertie, etc.
         */
        private void stopUsing(IL whichList, int delta)
        {
            // Check that stopUsing(FOO) is paired with startUsing(FOO).
            assert(get(whichList) == currentInstructionList);
            
            currentInstructionList = instructionListDeque.pop();
            incrementCounter(whichList, delta);
        }
        
        /**
         * Transfers the instructions from one helper list to another
         * and then frees the source list.
          */
        private void transfer(IL source, IL destination)
        {
            InstructionList sourceInstructionList = get(source);
            InstructionList destinationInstructionList = get(destination);
            
            destinationInstructionList.addAll(sourceInstructionList);
                        
            remove(source);
        }
        
        /**
         * Transfers the instructions from a helper list to the
         * current list and then frees the source list.
         */
        private void transfer(IL source)
        {
            InstructionList sourceInstructionList = get(source);
            
            currentInstructionList.addAll(sourceInstructionList);
            
            remove(source);
        }
             
        /**
         * Removes and frees a helper list.
         */
        private void remove(IL whichList)
        {
            instructionListMap.remove(whichList);
        }
        
        /**
         * Gets a counter associated with a helper listk,
         * for keeping track of how many properites, etc. exist.
         */
        private int getCounter(IL whichList)
        {
            if (counterMap == null)
                counterMap = new EnumMap<IL, Integer>(IL.class);
            
            Integer counter = counterMap.get(whichList);
            return counter != null ? counter.intValue() : 0;
        }
        
        /**
         * Increments a counter associated with a helper list,
         * for keeping track of how many properties, etc. exist.
         */
        private void incrementCounter(IL whichList, int amount)
        {
            int n = getCounter(whichList);
            counterMap.put(whichList, n + amount);
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            
            // Always show the main instruction list.
            sb.append("MAIN:\n");
            sb.append(mainInstructionList.toString());
                        
            // Show any helper instruction lists that exist,
            // in the order they are listed in the IL enum.
            if (instructionListMap != null)
            {
                for (IL whichList : IL.values())
                {
                    if (instructionListMap.containsKey(whichList))
                    {
                        sb.append('\n');
                        sb.append(whichList.name());
                        sb.append('\n');
                        
                        sb.append(get(whichList).toString());
                    }
                }
            }
            
            return sb.toString();
        }
        
        /**
         * For debugging only.
         * Generates a call to trace() in the current instruction list.
         */
        private void callTrace(String message)
        {
            Name trace = new Name("trace");
            
            addInstruction(OP_getlocal0);
            addInstruction(OP_pushscope);
            
            addInstruction(OP_findpropstrict, trace);
            addInstruction(OP_pushstring, message);
            addInstruction(OP_callpropvoid, new Object[] { trace, 1 });
            
            addInstruction(OP_popscope);
        }
        
        /**
         * For debugging only.
         * @param message
         */
        @SuppressWarnings("unused")
        private void trace(String message)
        {
            // Turn tracing on/off by uncommenting/commenting the following line.
            callTrace(message);
        }
    }
        
    /**
     * This enumeration provides access to 12 lazily-created helper instruction lists.
     * Each one has a specific codgen purpose as described below.
     */
    static enum IL
    {
        /**
         * A helper instruction list for the instructions
         * that set properties for a class reference node.
         * <p>
         * Typical code is
         * <pre>
         * target.label = "OK";
         * target.width = 100;
         * </pre>
         * or
         * <pre>
         * getlocal0 / dup
         * pushstring "OK"
         * setproperty label
         * 
         * getlocal0 / dup
         * pushbyte 100
         * setproperty width
         * </pre>
         * Here the target is <code>this</code> for properties
         * of a class definition node or top-of-stack
         * for properties of an instance node.
         * <p>
         * If the property value is not a primitive value such as
         * <code>"OK"</code>, then instead of a simple instruction
         * like <code>pushstring</code> we generate either an inline
         * sequence of instructions that push the value or a call
         * to an initializer method that pushes it.
         */
        PROPERTIES,
        
        /**
         * A helper instruction list for the instructions
         * that register event listeners for a class reference node.
         * <p>
         * Typical code is
         * <pre>
         * target.addEventListener("initialize", >0);
         * target.addEventListener("click", >1);
         * </pre>
         * or
         * <pre>
         * getlocal0 / dup
         * pushstring "initialize"
         * getlocal0
         * getproperty >0
         * callpropvoid addEventListener(2)
         * 
         * getlocal0 / dup
         * pushstring "click"
         * getlocal0
         * getproperty h1
         * callpropvoid addEventListener(2)
         * </pre>
         * Here the target is <code>this</code> for events
         * of a class definition node or top-of-stack
         * for events of an instance node.
         * <p>
         * The event handler (>0, >1, etc.) is an inaccessible
         * method in a special private MXML namespace.
         */
        EVENTS,
        
        /**
         * A helper instruction list for the instructions
         * that set styles for a class reference node.
         * <p>
         * Typical code is
         * <pre>
         * target.setStyle("fontSize", 12);
         * target.setStyle("fontFamily", "Arial");
         * </pre>
         * or
         * <pre>
         * getlocal0 / dup
         * pushstring "fontSize"
         * pushbyte 12
         * callpropvoid setStyle(2)
         * 
         * getlocal0 / dup
         * pushstring "fontFamily"
         * pushstring "Arial"
         * callpropvoid setStyle(2)
         * </pre>
         * Here the target is <code>this</code> for styles
         * of a class definition node or top-of-stack
         * for styles of an instance node.
         * <p>
         * If the style value is not a primitive value such as
         * <code>"Arial"</code>, then instead of a simple instruction
         * like <code>pushstring</code> we generate either an inline
         * sequence of instructions that push the value or a call
         * to an initializer method that pushes it.
         */
        STYLES,
        
        /**
         * A helper instruction list for the instructions
         * that set effect styles for a class reference node.
         * <p>
         * Typical code is
         * <pre>
         * target.setStyle("showEffect", "Fade");
         * target.setStyle("hideEffect", "Fade");
         * </pre>
         * or
         * <pre>
         * getlocal0 / dup
         * pushstring "showEffect"
         * pushstring "Fade"
         * callpropvoid setStyle(2)
         * 
         * getlocal0 / dup
         * pushstring "hideEffect"
         * pushstring "Fade"
         * callpropvoid setStyle(2)
         * </pre>
         * Here the target is <code>this</code> for effect styles
         * of a class definition node or top-of-stack
         * for effect styles of an instance node.
         * <p>
         * If the style value is not a primitive value such as
         * <code>"Fade"</code>, then instead of a simple instruction
         * like <code>pushstring</code> we generate either an inline
         * sequence of instructions that push the value or a call
         * to an initializer method that pushes it.
         */
        EFFECT_STYLES,
        
        /**
         * A helper instruction list for the instructions
         * that register effects for a class reference node.
         * <p>
         * Typical code that will ultimate be generated is
         * the single call
         * <pre>
         * target.registerEffects([ "showEffect", "hideEffect" ]);
         * </pre>
         * but this list is used only to accumulates the effect
         * names from each effect node, so the instructions look like
         * <pre>
         * pushstring "showEffect"
         * 
         * pushstring "hideEffect"
         * </pre>
         * Here the target is <code>this</code> for effects
         * of a class definition node or top-of-stack
         * for effect styles of an instance node.
         */
        EFFECTS,
        
        /**
         * A helper instruction list for the instructions
         * that create a complete UIComponentDescriptor
         * (with its nested child descriptors) for a class reference node.
         * This is built up from DESCRIPTOR_PROPERTIES, DESCRIPTOR_EVENTS, etc.
         */
        DESCRIPTOR,
        
        /**
         * A helper instruction list for the instructions
         * that specify properties for a descriptor.
         * <p>
         * Typical code is key/value pairs that contribute to an Object
         * returned by the descriptor's <code>propertiesFactory</code>.
         * <pre>
         * label: "OK";
         * width: 100;
         * </pre>
         * or
         * <pre>
         * pushstring "OK"
         * pushstring "label"
         * 
         * pushbyte 100
         * pushstring "width"
         * </pre>
         * <p>
         * If the property value is not a primitive value such as
         * <code>"OK"</code>, then instead of a simple instruction
         * like <code>pushstring</code> we generate either an inline
         * sequence of instructions that push the value or a call
         * to an initializer method that pushes it.
         */
        DESCRIPTOR_PROPERTIES,
        
        /**
         * A helper instruction list for the instructions
         * that specify events for a descriptor.
         * <p>
         * Typical code is key/value pairs that contribute to
         * the descriptor's <code>events</code> Object.
         * <pre>
         * initialize: ">0";
         * click: ">1"
         * </pre>
         * or
         * <pre>
         * pushstring "initialize"
         * pushstring ">0"
         * 
         * pushstring "click"
         * pushstring ">1"
         * </pre>
         */
        DESCRIPTOR_EVENTS,
        
        /**
         * A helper instruction list for the instructions
         * that specify styles for a descriptor.
         * <p>
         * Typical code is assignment statements within
         * the descriptor's <code>stylesFactory</code> function.
         * <pre>
         * this.fontSize = 12;
         * this.fontFamily = "Arial"
         * </pre>
         * or
         * <pre>
         * getlocal0
         * pushbyte 12
         * setproperty fontSize
         * 
         * getlocal0
         * pushstring "Arial"
         * setproperty fontFamily
         * </pre>
         */
        DESCRIPTOR_STYLES,
        
        /**
         * A helper instruction list for the instructions
         * that specify effect styles for a descriptor.
         * <p>
         * Typical code is assignment statements within
         * the descriptor's <code>stylesFactory</code> function.
         * <pre>
         * this.showEffect = "Fade";
         * this.hideEffect = "Fade"
         * </pre>
         * or
         * <pre>
         * getlocal0
         * pushstring "Fade"
         * setproperty showEffect
         * 
         * getlocal0
         * pushstring "Fade"
         * setproperty hideEffect
         * </pre>
         */
        DESCRIPTOR_EFFECT_STYLES,
        
        /**
         * A helper instruction list for the instructions
         * that specify effects for a descriptor.
         * <p>
         * Typical code is array elements for
         * the descriptor's <code>effects</code> property.
         * <pre>
         * "showEffect",
         * "hideEffect"
         * </pre>
         * or
         * <pre>
         * pushstring "showEffect"
         * 
         * pushstring "hideEffect"
         * </pre>
         */
        DESCRIPTOR_EFFECTS,
        
        /**
         * A helper instruction list for the instructions
         * that specify child descriptors for a descriptor.
         * <p>
         * The code consists of instruction to create
         * multiple child descriptors, which will get
         * made into an Array for the value of the
         * descriptor's <code>childDescriptors</code> proeprty.
         * <pre>
         * new UIComponentDescriptor(...),
         * new UIComponentDescriptor(...)
         * </pre>
         */
        DESCRIPTOR_CHILD_DESCRIPTORS,
        
        /**
         * A helper instruction list for the instructions
         * that specify styles inside the setter
         * for the class's <code>moduleFactory</code> property.
         * These are generated from the class definition node's
         * style specifiers.
         * <p>
         * Typical code is assignment statements that will go into
         * the component's <code>styleDeclaration.defaultFactory</code>
         * function:
         * <pre>
         * this.fontSize = 12;
         * this.fontFamily = "Arial"
         * </pre>
         * or
         * <pre>
         * getlocal0
         * pushbyte 12
         * setproperty fontSize
         * 
         * getlocal0
         * pushstring "Arial"
         * setproperty fontFamily
         * </pre>
         */
        MODULE_FACTORY_STYLES,
        
        /**
         * A helper instruction list for the instructions
         * that specify effect styles inside the setter
         * for the class's <code>moduleFactory</code> property.
         * These are generated from the class definition node's
         * effect specifiers.
         * <p>
         * Typical code is assignment statements that will go into
         * the component's <code>styleDeclaration.defaultFactory</code>
         * function:
         * <pre>
         * this.showEffect = "Fade";
         * this.hideEffect = "Fade"
         * </pre>
         * or
         * <pre>
         * getlocal0
         * pushstring "Fade"
         * setproperty showEffect
         * 
         * getlocal0
         * pushstring "Fade"
         * setproperty hideEffect
         * </pre>
         */
        MODULE_FACTORY_EFFECT_STYLES,
        
        /**
         * A helper instruction list for the instructions
         * that register effects inside the setter
         * for the class's <code>moduleFactory</code> property.
         * These are generated from the class definition node's
         * effect specifiers.
         * <p>
         * Typical code is array elements that will go into an Array
         * passed to <code>registerEffects()</code:
         * <pre>
         * "showEffect",
         * "hideEffect"
         * </pre>
         * or
         * <pre>
         * pushstring "showEffect"
         * 
         * pushstring "hideEffect"
         * </pre>
         */
        MODULE_FACTORY_EFFECTS,

        /**
         * Instructions to construct an object literal for {@code operations}
         * property on a {@code WebService} instance.
         */
        WEB_SERVICE_OPERATIONS_OR_REMOTE_OBJECT_METHODS,
        
        /**
         * Instructions to construct the children of an MXML tag.
         */
        MXML_CONTENT_FACTORY,
        
        /**
         * Instructions to construct the children of an MXML states array.
         */
        MXML_STATES_ARRAY,
        
        /**
         * Instructions to construct the children of an state tag.
         */
        MXML_OVERRIDE_PROPERTIES,
        
        /**
         * Instructions to construct the children of an AddItems tag.
         */
        MXML_ADD_ITEMS_PROPERTIES,
        
        /**
         * Instructions to construct the children of an AddItems tag.
         */
        MXML_MODEL_PROPERTIES,
        
        /**
         * Instructions to construct the beads property of an AddItems tag.
         */
        MXML_BEAD_PROPERTIES;
    }
}
