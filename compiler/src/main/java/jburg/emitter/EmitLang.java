package jburg.emitter;

import jburg.burg.AnnotationAccessor;
import jburg.burg.JBurgPatternMatcher;
import jburg.burg.ir.FormalParameter;
import org.stringtemplate.v4.*;

import java.io.PrintStream;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/*
 * EmitLang defines the code emitter's interface.
 */

public interface EmitLang
{
	/**
	 * Return true if based on the language and iNode class, this emitter can process the specification.
	 * @param languageName Name specifed by the 'Language' qualifier in the spec
	 * @return true if emitter handles this language (nb: on null or zero length EmitJava will trigger true)
	 */
	abstract public boolean accept(String languageName);
	
	/**
	 *  Set the i-node adapter so the emitter can generate
	 *  code to access its relevant members.
	 */
	abstract public void setInodeAdapter(jburg.burg.inode.InodeAdapter adapter);

    /**
     * Set the name of the class which will contain the BURM.
     * @param className the class name.
     */
    abstract public void setClassName(String className);
	
	/**
	 * Set the opcode type.
	 */
	abstract public void setOpcodeType(String opcode_type);

    /**
     *  @return the opcode type.
     */
	abstract public String getOpcodeType();
	
	/**
	 *  Set the i-node type.
	 */
	abstract public void setINodeType(String inode_type);

    /**
     * @return the i-node type.
     */
    abstract public String getINodeType();

    /**
     * Set the nonterminals' type.
     */
    abstract public void setNtType(String nt_type);

    /**
     * Set the annotation accessor.
     * @param accessor the annotation accessor.  May be null.
     */
    abstract public void setAnnotationAccessor(AnnotationAccessor accessor);

    /**
     * Set or reset debug mode.
     * @param debugMode when set, the emitter may generate debugging logic.
     */
    abstract public void setDebugMode(Boolean debugMode);

    /**
     * Select a template by name, with optional attributes.
     * @param name the template's name.
     * @param attrValue pairs of attrName, attrValue.
     * @return an instance of the ST.
     */
    abstract public ST getTemplate(String name, Object... attrValue);

    /**
     * Set a default attribute in the template manager.
     * @param key the attribute's name.
     * @param value the attribute's value.
     */
    abstract public void setDefaultAttribute(String key, Object value);
    
	/**
	 * Emit the file/class header information.
	 * @param packageName  the name of the package or namespace, or null if no package/namespace is to be generated.
	 * @param headerBlock  Code to be copied as-is into the header section of a Java file.
     * @param baseClassName the name of the base class, or null if no base class is to be specified.
	 * @param InterfaceNames  names of interfaces a Java class implements.
	 * @param output
	 */
	abstract public void emitHeader(String packageName, String headerBlock, String baseClassName, Vector<String> InterfaceNames, PrintStream output);
	
    /**
     * Emit static tables of subgoals.
     */
	abstract public void emitSubgoalLookupTables(int max_action, Map<Integer, Vector<JBurgPatternMatcher>> rules_by_action, PrintStream output);
	
	/**
	 * Emit verbatim code blocks that are intended to be part of the class definition.
	 * @param inclassBlocks  A Vector of strings to be copied in verbatim.
	 * @param output  the stream that's writing the generated code.
	 */
	abstract public void emitInclass(Vector<? extends Object> inclassBlocks, PrintStream output);

	/**
	 * Emit the reducer and finish the class definition.
	 * @param strClassName  the name of the class being generated.
	 * @param iNodeClass  the name of the class that represents the i-code DAG.
	 * @param subgoals  the states that this BURM may transition through.
	 * @param burm_properties  any properties (private fields and public get/set methods)
	 * @param output   the stream that's writing the generated code.
	 */
	abstract public void emitTrailer(Set<String> subgoals, Map<String, Object> burm_properties, Object default_error_handler, Map<Integer, Object> prologue_blocks, PrintStream output);

	/**
	 *  @return a code snippet that represents the code in the target language that declares
	 *  a variable and initializes it from the value stack, thus emulating a parameter declaration.
	 *  @param stackName - name of the stack.
	 *  @param paramType - the "parameter" type.
	 *  @param paramName - the "parameter's" name.
	 */
	abstract public Object genActionRoutineParameter(Object stackName, Object paramType, Object paramName);
	
	/**
	 * @return a code snippet that represents the code in the target language that will 
	 * get the top value of a stack and decrement the size of the stack 
	 * @param stackName variable name of the stack
	 * @param valueType type of the value.
	 */
	abstract public Object genPopFromStack(Object stackName, Object valueType);

	/**
	 * @return a code snippet that pushes a value onto a stack of reduced values.
	 * @param stackName the name of the stack variable.  Type of the stack is assumed to be 
	 *    reasonably generic, e.g., java.util.Stack on Java implementations, std::stack&lt;void*&gt; in C++.
	 * @param value a code snippet that generates the value to be pushed.
	 */
	abstract public Object genPushToStack(Object stackName, Object value);
	
	/**
	 * @return a snippet that gives the code to check if a reference is null
	 * @param paramName parameter to be checked
	 * @param checkForNull if true, check for a null pointer, if false check is true if not null
	 * @return object containing relevant code fragment
	 */
	abstract public Object genCheckPtr(Object paramName, boolean checkForNull);
	
	/**
	 * Generate code for direct access to an object's member
	 * @param parentName name of the parent object
	 * @param memberName name of the parameter to access
	 * @return snippet containing the code fragment to use
	 */
	abstract public Object genAccessMember(Object parentName, Object memberName);
	
	/**
	 * Generate a clip that calls a method contained within an object.
	 * @param parentName name of the parent object
	 * @param methodName name of the method to call
	 * @param params list of the paramters for the call
	 * @return the relevent code fragment
	 */
	abstract public Object genCallMethod(Object parentName, Object methodName, Object... params);
	
	/**
	 * Generate a clip that compares two expressions. 
	 * @param lhs Expression to be placed on the left-hand side of the compare
	 * @param rhs Expression to be placed on the right-hand side of the compare
	 * @param bEquality set to true to compare for equality, false for non-equality
	 * @return the applicable code fragment
	 */
	abstract public Object genCmpEquality(Object lhs, Object rhs, boolean bEquality);

	/**  Manifest constant for genCmpEquality -- test for equality. */
	public static final boolean TEST_EQUALITY = true;

	/**  Manifest constant for genCmpEquality -- test for inequality. */
	public static final boolean TEST_INEQUALITY = false;
	
	/**
	 * Generate a snippet that combines 2 expressions with a logical and
	 * @param lhs Expression to be placed on the left-hand side of the and
	 * @param rhs Expression to be placed on the right-hand side of the and
	 * @return the applicable code fragment
	 */
	abstract public Object genLogicalAnd(Object lhs, Object rhs);

	/**
	 * @return a code snippet that returns true if rhs is less than lhs.
	 */
	abstract public Object genCmpLess(Object lhs, Object rhs);

	/**
     * @return a code snippet that returns true if lhs is greater than
     *   or equal to rhs.
     */
	abstract public Object genCmpGtEq(Object lhs, Object rhs);
	
	/**
	 * @param operand
	 * @return a code snippet that negates the operand.
	 */
	abstract public Object genNot(Object operand);

	/**
	 * @return a code snippet that adds two values.
	 */
	abstract public Object genAddition(Object addend1, Object addend2);

	/**
	 * @return a code snippet that defines a local variable in a method.
	 * @param type the variable's type.
	 * @param name the name of the variable.
	 * @param initializer the variable's initial value.  May be null, in which case no initialization code is generated.
	 */
	abstract public Object genLocalVar(Object type, Object name, Object initializer);

	/**
	 * @return a code snippet that defines an instance field.
	 * @param modifiers values of java.lang.reflect.Modifier that work here are PUBLIC|PRIVATE|PROTECTED, FINAL, STATIC
	 * @param type the field's type.
	 * @param name the name of the field.
	 * @param initializer the field's initial value.  May be null, in which case no initialization code is generated.
	 */
	abstract public Object genInstanceField(int modifiers, Object type, Object name, Object initializer);

	/**
	 * @return a code snippet that creates a new object.
	 * @param type the new object's class.
	 * @param parameters an array of values (code snippets) to pass to the new object's constructor.
	 */
	abstract public Object genNewObject(Object type, Object... parameters);

	/**
	 * @return a code snippet that assigns a value to a variable.
	 * @param lvar the variable into which the value is stored.
	 * @param rvalue the value to store in the variable.
	 */
	abstract public Object genAssignment(Object lvar, Object rvalue);

	/**
	 * @return a code snippet that casts a target expression to a new type.
	 * @param newType the type to cast to.
	 * @param target the expression whose value is to assume the new type.
	 */
	abstract public Object genCast(Object newType, Object target);

	/**
	 * @return a code snippet that generates a comment.
	 * @param text the comment text.
	 */
	abstract public Object genComment (Object text);

	/**
	 * @return a code snippet that gets the goal state of an i-node.
	 * @param p the i-node.
	 */
	abstract public Object genGetGoalState (Object p);

	/**
	 * @return a code snippet that gets the undecorated goal state
     * of an i-node, which can be used as a case label.
	 * @param p the i-node.
	 */
	abstract public Object genGetUndecoratedGoalState (Object p);

	/**
	 * @return a code snippet that signals a runtime error.
	 * @param diagnostic the diagnostic content associated with the signal.
	 */
	abstract public Object genThrow (Object diagnostic);

	/**
	 * @return a code snippet that declares a method.
	 * @param modifiers values of java.lang.reflect.Modifier that work are PUBLIC, PRIVATE.
	 * @param returnClass the method's return type.
	 * @param name the method's name.
	 * @param formals formal parameter declarations.
	 * @param exceptions an array of exceptions that the method may throw.
	 * @note this method may change to emit the method header.
	 */
	@SuppressWarnings("rawtypes")
	abstract public Object declareMethod(int modifiers, Object returnClass, Object name, FormalParameter[] formals, Class[] exceptions);

	/**
	 * @return a code snippet that returns a value from a method.
	 * @param value the value to return.
	 */
	abstract public Object genReturnValue(Object value);

	/**
	 * @return a code snippet that represents the target's maximum integer value, the &quot;infinite&quot; cost.
	 */
	abstract public Object genMaxIntValue();

	/**
	 * @return a code snippet that represents the target's null pointer value.
	 */
	abstract public Object genNullPointer();

	/**
	 * @return a code snippet that generates a pre-test (while) loop.
	 */
	abstract public Object genWhileLoop(Object test_condition);

	/**
	 * @return a code snippet that represents the target's generalized
	 *   container type, e.g., java.util.Vector or std::vector<void*>
	 */
	abstract public Object genNaryContainerType(Object base_type);

    /**
     *  @return a code snippet that adds two (integer) values without overflow.
     */
    abstract public Object genOverflowSafeAdd(Object expr1, Object expr2);

    /**
     * @return the type of the target's annotation object
     */
    abstract public Object getAnnotationType();

    /**
     * @return a constructor template.
     */
    abstract public ST getConstructorBody(Object decl, Object baseClass, Object... superParameters);

    /**
     * @return the name of the routine that appends to an n-ary list.
     */
    abstract public ST getNarySubtreeAdd();

    abstract public void setAllocator(Object allocator);

    abstract public void addSingletonAnnotationClass(String singletonClassName);
}
