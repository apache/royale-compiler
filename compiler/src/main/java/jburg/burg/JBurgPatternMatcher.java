package jburg.burg;

import jburg.burg.inode.InodeAdapter;
import jburg.burg.ir.GetNthChild;
import jburg.emitter.EmitLang;

import java.util.Comparator;
import java.util.Vector;

/**
 *  JBurgPatternMatcher encodes a pattern match
 *  recognizer for a subtree.  
 */

@SuppressWarnings("nls")
public
class JBurgPatternMatcher implements Comparable<JBurgPatternMatcher>
{
	/**
	 *  The pattern's arity.
	 *  @warning only present for n-ary patterns;
     *  Fixed-arity patterns get arity from the number
     *  of sub-patterns.
	 */ 
	private Arity arity = null;

	/**
	 *  A pattern that matches an operator,
	 *  i.e. a specific node type in the
	 *  matching subtree.
	 */
	private String operator = null;


	/**
	 *  Subpatterns of this node.
	 */
	private Vector<JBurgPatternMatcher> subPatterns = new Vector<JBurgPatternMatcher>();

	/**
     *  A pattern node that matches any finite cost
	 *  for a particular goal state.  This pattern
	 *  generates a subgoal.
     */
    private String finiteCostMatch = null;
    
    /**
     * Cost factored out by the pattern-match optimizer.
     */
    String factoredCost = null;
    
    /**
     *  Path factored out by the pattern-match optimizer.
     */
    String factoredPath = null;
    
    /**
     * Parent of this node; used to traverse the tree
     * from a leaf back to the root.
     */
    private JBurgPatternMatcher parent = null;
    
    /**
     * This node's position in the parent's subpatterns vector.
     */
    Integer positionInParent = null;
    
    /**
     *  The goal state of a non-terminal parameter to a parent pattern.
     *  @see setParameterData(), which sets this when
     *    the pattern-matching BURM sees a parameter declaration.
     */
    String paramState = null;
    
    /**
     *  The name of a non-terminal parameter to a parent pattern.
     *  @see setParameterData(), which sets this when
     *    the pattern-matching BURM sees a parameter declaration.
     */
    String paramName = null;
    
    /**
     *  A top-level patter matcher may have parameterized subtrees,
     *  for example, in ADD(expr lhs, expr rhs) lhs and rhs are paramterized
     *  subtrees.  These subtrees play several parts in the computation of the
     *  locally-optimal reduction:
     *  -  They contribute to the rule's computed cost.
     *  -  The reduction's action code may refer to these elements by name.
     *  -  If the rule is of the form OP(nttype1 a [, nttype2 b]), then the rule
     *     must enforce this reduce-time goal state on its subtrees' reductions.
     */
    private Vector<JBurgPatternMatcher> parameterizedSubtrees;
    
    /**
     *  Named subtrees that are not parameterized non-terminals;
     *  record their names as a convenience so the reduction action
     *  can refer to them by name.
     */
    private Vector<JBurgPatternMatcher> namedSubtrees;

    /**
     *  The BURG sets this when it determines that this
     *  pattern is in a context where its fixed-arity
     *  checks can be elided.
     */
    private boolean elideFixedArityChecks = false;

    /**
     *  Construct a pattern matching node.
     *  @see factory methods that generate matcher nodes.
     */
	private JBurgPatternMatcher()
	{
	}

	/**
	 * Construct a matcher that matches a specific operator.
	 * @param pattern_op - the operator to match.
	 * @return a pattern matcher that matches this operator.
	 */
	public static JBurgPatternMatcher matchOperator(String pattern_op)
    {
        JBurgPatternMatcher result = new JBurgPatternMatcher();
		result.operator = pattern_op;
		return result;
    }

	/**
	 * Construct a matcher that matches a subgoal that's present
	 * at least a minimum number of times.
	 * @param minimum - the minimum number of instances to accept.
	 *   almost always 1, may be zero.
	 * @return a pattern matcher that matches the n-ary aggregation.
	 */
	public static JBurgPatternMatcher matchOperandAtLeastNTimes(int minimum)
	{
        JBurgPatternMatcher result = new JBurgPatternMatcher();
		result.arity = new Arity(minimum);
		return result;
	}

	/**
	 * Construct a matcher that matches when a subgoal has a finite
	 * cost (i.e., when the labeler found a solution).
	 * @param state - the subgoal's goal state.
	 * @return a pattern matcher that matches when the reducer
	 *   can reduce the subgoal.
	 */
	public static JBurgPatternMatcher matchFiniteCost( String state)
    {
        JBurgPatternMatcher result = new JBurgPatternMatcher();
        result.finiteCostMatch = state;
		return result;
    }


	/**
	 * @return true if the pattern matches against an operator.
	 */
	public boolean matchesOperator()
	{
	    return null != this.operator;
	}
	
	/**
	 * @return an operator-matching node's operator.
	 */
    public String getOperator()
    {
        checkPrecondition ( matchesOperator(), "Node has no operator" );
        return this.operator;
    }

    /**
     * @return The minimum number of child nodes required to match this pattern.
     */
    public int getNominalArity()
    {
        if ( hasNaryTail() )
        {
            int arity_of_fixed_operands = this.subPatterns.size() - 1;
            return arity_of_fixed_operands + getMinimumNaryChildCount();
        }
        else
        {
            return this.subPatterns.size();
        }
    }

    /**
     *  @return the minimum number of n-ary children required to match.
     */
    public int getMinimumNaryChildCount()
    {
        checkPrecondition( this.hasNaryTail(), "n-ary child count requires an n-ary pattern");
        return this.subPatterns.lastElement().arity.m_minimum;
    }

	/**
	 *  Add a child node.
	 */
	void addChild( JBurgPatternMatcher child )
	{
        checkPrecondition ( ! this.hasNaryTail(), "Cannot add a subpattern after an n-ary subpattern.");
        checkPrecondition ( null == child.parent, "Child " + child.toString() + " already has a parent.");
        
        child.positionInParent = new Integer(this.subPatterns.size());
        child.parent = this;
        this.subPatterns.add(child);
	}

    /**
     *  Assume ownership of a Vector of subpatterns.
     *  @param patterns - this node's Vector of subpatterns.
     */
    public void addAll(Vector<JBurgPatternMatcher> patterns)
    {
        for ( JBurgPatternMatcher child: patterns)
            addChild(child);
    }

	/**
	 *  @return the pattern recognizer for this node and its children.
	 *  @param emitter - the code emitter to use to generate the path.
	 *  @param stem - the path generated by the parent (or external caller).
	 */
	public Object generatePatternRecognizer( EmitLang emitter, String stem)
	{
        if ( finiteCostMatch != null || isNary() ) {
            //  Costs are checked by a specialized routine.
            return null;

        } else {
			return generatePatternAndSubpatterns(emitter, stem);
		}
	}
	
	/**
     * @param emitter - the active code emitter.
     * @param stem - the a priori expression at the root of this node's path from root.
     * @return a snippet that gets the matched node's cost.
     */
	public Object generateCost( EmitLang emitter, String stem )
    {
	    if ( factoredCost != null ) {
	        return factoredCost;

	    } else if ( finiteCostMatch != null ) {
            return generateGetFiniteCost(emitter, stem);

        } else if ( this.isNary() ) {
            return getNaryCost( emitter, stem );

        } else {
            //  the node doesn't cost anything.
            return null;
        }
    }

	/**
	 * @return true if the last sub-pattern is n-ary.
	 */
	public boolean hasNaryTail()
	{
	    return this.subPatterns.size() > 0 && this.subPatterns.lastElement().isNary();
	}

	/**
     * @param emitter - the active code emitter.
	 * @param stem - the a priori expression at the root of this node's path from root.
     * @return a snippet that compares the candidate node to this matcher's constraints.
     */
	private Object generatePatternAndSubpatterns( EmitLang emitter, String stem)
	{
        checkPrecondition(operator != null, "Pattern node must have an operator");

        Object result_pattern = null;
        
        if ( this.hasNaryTail() ) {

            if ( this.getNominalArity() > 0 ) {
                result_pattern = emitter.genCmpGtEq(
                        emitter.genCallMethod( generatePathToRoot(emitter, stem), "getArity"),
                        Integer.toString(getNominalArity())
                    );
            }
        } else {
            result_pattern = generateFixedArityCheck(emitter, stem);
        }
        
        //  The check against opcode is redundant at the root;
        //  that's just been tested as the switch expression.
        if ( !this.isRoot() ) {
            result_pattern =
                emitter.genLogicalAnd(
                    result_pattern, 
                    generateOpCheck(emitter, stem)
            );
        }
        
		//  AND in each pattern-matching child's recognizer.
		//  Note: this could result in an arbitrarily deep
        //  parenthetical structure, but in practice patterns
        //  aren't nested very deeply.
		for ( JBurgPatternMatcher currentChild: this.subPatterns ) {
            result_pattern = emitter.genLogicalAnd(result_pattern, currentChild.generatePatternRecognizer(emitter, stem));
		}

		return result_pattern;
	}

	/**
     * @param emitter - the active code emitter.
     * @param stem - the a priori expression at the root of this node's path from root.
     * @return a snippet that calls the candidate node's getNaryCost() routine.
     */
	private Object getNaryCost(EmitLang emitter, String stem)
	{
		checkPrecondition(isNary(), "getNaryCost called on fixed-arity pattern");

		return emitter.genCallMethod( 
			null,
			"getNaryCost", 
            generatePathToRoot(emitter, stem), 
            emitter.genGetGoalState(getSubgoal()), 
            this.positionInParent.toString()
		);
	}


	/**
	 * Generate a code snippet to access this pattern's node.
	 * @param emitter - the active code emitter.
	 * @param stem - the a priori name of the root node.
	 * @return the path from the root node to this node.
	 */
	public Object generatePathToRoot(EmitLang emitter, String stem)
	{
	    Object result;
	    
	    if ( this.factoredPath != null ) {
	        result = this.factoredPath;
        
        } else {

            //  Build the path iteratively.
            result = stem;

            for ( PathElement idx: generateAccessPath() ) {
	           result = emitter.genCallMethod(
	                   result, 
	                   "getNthChild", 
                       Integer.toString(idx.index));
            }
	    }
	    
	    return result;
	}
	
	/**
	 *  Generate the sequence of getNthChild calls necessary to access
	 *  the node that matched this pattern.
	 *  @return a Vector getNthChild indexes.
	 */
	public Vector<PathElement> generateAccessPath()
	{
	    if ( null == this.parent )
        {
            return new Vector<PathElement>();
        }
        else if ( this.isNary() )
        {
            checkPrecondition(this.parent != null, "N-ary node with no parent");
            return this.parent.generateAccessPath();
        }
        else
        {
            Vector<PathElement> path = parent.generateAccessPath();
            PathElement own_element = new PathElement(this.operator, this.paramState, this.positionInParent);
            path.add(own_element);
            return path;
        }
	}

	/**
	 * Get the subgoal (non-terminal) state this pattern produces.
	 * @return the subgoal state.
	 */
	public String getSubgoal()
	{
	    if ( this.isNary() )
	    {
    		checkPrecondition(this.paramState != null, "n-ary goal has no parameter state");
    		return this.paramState;
	    }
	    else
	    {
	        checkPrecondition(finiteCostMatch != null, "pattern has no subgoal");
	        return this.finiteCostMatch;
	    }
	}

	/**
	 * Is this an n-ary pattern?
	 * @return true if this is an n-ary pattern.
	 */
	public boolean isNary()
	{
		return this.arity != null;
	}

    /**
     * Wrap an isNary() call with standard naming conventions
     * so the StringTemplate renderer can find it.
     * @return true if this is a n-ary pattern.
     */
    public Object getIsNary()
    {
        return Boolean.valueOf(isNary());
    }
	
	/**
	 * @return true if this pattern or any subpattern is n-ary.
	 */
	public boolean hasNaryness()
	{
        boolean result = isNary();
        
        if ( this.subPatterns != null )
        {
            for ( JBurgPatternMatcher subpattern: this.subPatterns )
                result |= subpattern.hasNaryness();
        }
        return result;
	}

	/**
     * @param emitter - the active code emitter.
     * @param stem - the a priori expression at the root of this node's path from root.
     * @return a snippet that compares the candidate node's arity to this matcher's fixed arity,
     *   or null if the arity has been checked here as a compiler-compile time constant.
     */
    private Object generateFixedArityCheck( EmitLang emitter, String stem)
    {
        if ( this.elideFixedArityChecks ) {
            return null;
        } else {
            //  This calls the JBurgAnnotation's getArity() method,
            //  indirectly calling the i-node's getArity implementation
            //  Thus it's hardcoded.
                
            return emitter.genCmpEquality(
                emitter.genCallMethod(generatePathToRoot(emitter, stem), "getArity"),
                Integer.toString(getNominalArity()),
                EmitLang.TEST_EQUALITY
            );
        }
    }

    /**
     * @param emitter - the active code emitter.
     * @param stem - the a priori expression at the root of this node's path from root.
     * @return a snippet that compares the candidate node's operator to this matcher's specified operator.
     */
    private Object generateOpCheck( EmitLang codeEmitter, String stem )
    {
        return codeEmitter.genCmpEquality(
            codeEmitter.genCallMethod( generatePathToRoot(codeEmitter, stem), "getOperator"),
			operator,
			EmitLang.TEST_EQUALITY
		);
    }

    /**
     * @param emitter - the active code emitter.
     * @param stem - the a priori expression at the root of this node's path from root.
     * @return a call to the matched node's getCost() method.
     */
    private Object generateGetFiniteCost(EmitLang emitter, String stem)
    {
        return emitter.genCallMethod( 
            generatePathToRoot(emitter, stem), 
            "getCost", 
            emitter.genGetGoalState(finiteCostMatch)
        );
    }
    
    /**
     *  The BURM sees a parameterized subtree to a parent pattern, e.g.,
     *  <xmp>operand = NON_TERMINAL_PARAMETER( simple_identifier state, simple_identifier paramName )</xmp> 
     *  @param param_state - the non-terminal state the subtree must derive.
     *  @param param_name - the name to give to the reduced subtree result.
     */
    void setParameterData(String param_state, String param_name)
    {
        this.paramState = param_state;
        this.paramName  = param_name;
    }
    
    /**
     *  Get the name this parameterized subtree is to be given 
     *  in the parent pattern matcher's reduction action. 
     *  @return the name of this parameter.
     */
    public String getParameterName()
    {
        checkPrecondition(hasParameterName(), "no parameter name");
        return this.paramName;
    }
    
    /**
     *  Does this pattern matcher have a parameter name?
     *  @return true => the pattern is a parameter type matcher with a name.
     */
    public boolean hasParameterName()
    {
        return this.paramName != null;
    }
    
    /**
     *  Get a pattern matcher's parameterized subtrees, e.g. 
     *  lhs and rhs in the pattern ADD(expr lhs, expr rhs).
     *  @return this matcher's vector of subtrees.
     *  @warn only top-level pattern matchers have these.
     */
    public Vector<JBurgPatternMatcher> getParameterizedSubtrees()
    {
        checkPrecondition (this.parameterizedSubtrees != null, "no parameterized subtrees");
        return this.parameterizedSubtrees;
    }
    
    /**
     *  Set this pattern matcher's parameterized subtrees; the BURM harvests
     *  these subtrees from the pattern matcher's AST and the BURM's caller
     *  transfers them to the top-level pattern matcher.
     *  @param subtrees - the subtrees identified by the BURM.
     */
    public void setParameterizedSubtrees(Vector<JBurgPatternMatcher> subtrees)
    {
        this.parameterizedSubtrees = subtrees;
    }
    
    /**
     * @return non-parameterized subtrees that were given names,
     *   mostly embedded terminal nodes.
     */
    public Vector<JBurgPatternMatcher> getNamedSubtrees()
    {
        return this.namedSubtrees;
    }
    
    /**
     * Set a top-level pattern matcher's list of named subtrees.
     * @param named_subtrees - named subtrees identified by the BURM.
     */
    public void setNamedSubtrees(Vector<JBurgPatternMatcher> named_subtrees)
    {
        this.namedSubtrees = named_subtrees;
    }

    /**
     * Panic if the precondition isn't satisfied.
     * @param pre_condition - the condition that must be true.
     * @param diagnostic - detail message for the panic exception
     * @throws IllegalStateException if panic ensues.
     */
    private void checkPrecondition(boolean pre_condition, String diagnostic)
    {
        if ( !pre_condition )
        {
            System.err.println("!checkPrecondition(" + toString() + "): " + diagnostic );
            throw new IllegalStateException(diagnostic);
        }
    }
    
    
    @Override
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        toStringHelper(buffer,0);
        return buffer.toString();
    }

    /**
     * Traverse a tree of pattern matchers and build
     * a string representation.
     * @param buffer - the StringBuffer used to build the result
     * @param level - recursion level, used to indent lines.
     */
	private void toStringHelper(StringBuffer buffer, int level)
    {
	    for ( int i = 0; i < level; i++ )
	        buffer.append("  ");
	    
	    if ( this.operator != null )
            buffer.append("opcode:" + this.operator + " ");
	    
	    if ( this.arity != null )
	       buffer.append("Arity: " + this.arity.m_minimum + ":" + this.arity.m_maximum + " ");
    
	    if ( finiteCostMatch != null )
	        buffer.append("cost:" + finiteCostMatch + " ");
	    
        for ( JBurgPatternMatcher matcher: this.subPatterns )
        {
            buffer.append("\n");
            matcher.toStringHelper(buffer, level+1);
        }
        
        buffer.append("\n");

    }

	/**
	 *  The minimum and maximum number of subtrees an n-ary pattern may match.
	 */
    private static class Arity
	{
		int m_minimum;
		int m_maximum;

		Arity(int minimum)
		{
			m_minimum = minimum;
			m_maximum = Integer.MAX_VALUE;
		}
	}
    
    public static Comparator<JBurgPatternMatcher> getPatternComparator(final EmitLang e)
    {
        return  new Comparator<JBurgPatternMatcher>()
        {
            EmitLang emitter;
            {
                this.emitter = e;
            }
            public int compare(JBurgPatternMatcher arg0, JBurgPatternMatcher arg1)
            {
                String path0 = arg0.generatePathToRoot(this.emitter, "node").toString();
                String path1 = arg1.generatePathToRoot(this.emitter, "node").toString();

                if (path0.compareTo(path1) != 0)
                    return path0.compareTo(path1);
                
                if ( arg0.arity != null && arg1.arity != null ) {
                    if ( arg0.arity.m_maximum - arg1.arity.m_maximum != 0 )
                        return arg0.arity.m_maximum - arg1.arity.m_maximum;
                    else if ( arg0.arity.m_minimum - arg1.arity.m_minimum != 0 )
                        return arg0.arity.m_minimum - arg1.arity.m_minimum;

                } else if ( arg0.arity != null ) {
                    return 1;

                } else if ( arg1.arity != null ) {
                    return -1;
                }

                if ( arg0.matchesOperator() && arg1.matchesOperator() ) {
                    if ( arg0.operator.compareTo(arg1.operator) != 0 )
                        return arg0.operator.compareTo(arg1.operator);

                } else if ( arg0.matchesOperator() ) {
                    return 1;

                } else if ( arg1.matchesOperator() ) {
                    return -1;
                }


                if ( arg0.subPatterns.size() != arg1.subPatterns.size() )
                    return arg0.subPatterns.size() - arg1.subPatterns.size();

                for ( int i = 0; i < arg0.subPatterns.size(); i++ ) {

                    int sub_result = compare(arg0.subPatterns.elementAt(i), arg1.subPatterns.elementAt(i));
                    if ( sub_result != 0 )
                        return sub_result;
                }

                return 0;
            }
        };
    }
    
    public static Comparator<JBurgPatternMatcher> getCostComparator(final EmitLang e)
    {
        return new Comparator<JBurgPatternMatcher>()
        {
            EmitLang emitter;
            {
                this.emitter = e;
            }
            
            public int compare(JBurgPatternMatcher arg0, JBurgPatternMatcher arg1)
            {
                if ( arg0.finiteCostMatch != null && arg1.finiteCostMatch != null )
                {
                    if ( arg0.finiteCostMatch.compareTo(arg1.finiteCostMatch) != 0 )
                        return arg0.finiteCostMatch.compareTo(arg1.finiteCostMatch);
                }
                else
                    throw new IllegalArgumentException("Cost comparator called on non-cost pattern");
                
                String path0 = arg0.generatePathToRoot(this.emitter, "node").toString();
                String path1 = arg1.generatePathToRoot(this.emitter, "node").toString();

                return path0.compareTo(path1);
            }
        };
    }


    /**
     *  @return the minimum number of nodes this pattern requires for a match.
     *  @pre the pattern must be n-ary.
     */
    public int getMinimumArity()
    {
        return this.arity.m_minimum;
    }


    /**
     *  @return this pattern's position in its parent.
     */
    public int getPositionInParent()
    {
        return this.positionInParent;
    }

    /**
     * Generate a path to this pattern recognizer's node in the tree being parsed,
     * i.e., a path that can be used at reduce time.  This is used to find named
     * terminals, or other parts of the subtree being reduced.
     * @param emitter - the language-specific emitter in use.
     * @param stem - the path to this subtree.
     * @param adapter - the i-node specific adapter in use.
     * @return the path to this subtree.
     */
    public Object generateReduceTimePath(EmitLang emitter, String stem, InodeAdapter adapter)
    {
        Object result = stem;
        
        for ( PathElement idx: generateAccessPath() ) {

            if ( adapter != null ) {
                result = new GetNthChild(result, Integer.toString(idx.index));

            } else {
                result = emitter.genAccessMember(
                   result,
                   emitter.genCallMethod(
                           null, 
                           "getNthChild", 
                           Integer.toString(idx.index)) );
            }
        }
        
        return result;
    }
    
    /**
     *  Information about one element of a path to a subtree.
     */
    public static class PathElement implements Comparable<PathElement>
    {
        /**
         * The operator of this subtree.  Null if the subtree represents a nonterminal.
         */
        public final String operator;
        
        /**
         * This subtree's nonterminal state.  Null if the subtree represents a pattern match.
         * One of operator or nonterminal must be set or the subtree is invalid.
         */
        public final String nonterminal;

        /**
         * This subtree's index in its parent.
         */
        public final int index;
        
        private PathElement(String operator, String nonterminal, int index)
        {
            this.operator = operator;
            this.nonterminal = nonterminal;
            this.index = index;
        }
        
        /**
         * @return true if this PathElement represents a pattern match.
         */
        public boolean isPatternMatch()
        {
            return this.operator != null;
        }

        /**
         * Compare PathElements based on their position in their parent.
         * {@inheritDoc}
         */
        @Override
        public int compareTo(PathElement other)
        {
            return this.index - other.index;
        }
    }

    /**
     * Compare two JBurgPatternMatchers.  Semantics of comparison are:
     * <ol>
     * <li> Shorter path to root is lesser
     * <li> For each position in access path, if not equal then lesser position in parent is lesser
     * </ol>
     * @param other - the 
     * @return
     */
    @Override
    public int compareTo(JBurgPatternMatcher other)
    {
        Vector<PathElement> this_path  = this.generateAccessPath();
        Vector<PathElement> other_path = other.generateAccessPath();
        
        int result = this_path.size() - other_path.size();
        
        for ( int i = 0; i < this_path.size() && result == 0; i++ )
            result = this_path.elementAt(i).compareTo(other_path.elementAt(i));

        return result;
    }
    
    /**
     *  Aggregate all the paths in this pattern matcher by a prototype path.
     *  @param aggregated_matchers - the aggregated matchers.
     */
    void aggregatePaths(Multimap<JBurgPatternMatcher,JBurgPatternMatcher> aggregated_matchers)
    {
        aggregated_matchers.addToSet(this,this);
        if ( this.subPatterns != null )
            for (JBurgPatternMatcher kid: this.subPatterns)
                kid.aggregatePaths(aggregated_matchers);
    }

    /**
     *  Find all (potentially) common subexpressions in a pattern matcher's path.
     *  @param factors - a map from a prototype pattern matcher to the collection
     *  of pattern matchers with that match that path prefix.
     */
    void findFactors(Multimap<JBurgPatternMatcher, JBurgPatternMatcher> factors)
    {
        if (  isRoot() || parentIsRoot() || isNary() )
        {
            //  The first two levels of the tree are a priori present so
            //  they don't need to be factored, and n-ary patterns are
            //  leaf nodes with their own arity checking so they don't
            //  need to be factored, either.
        }
        else
        {
            factors.addToSet(this,this);
        }

        if ( this.subPatterns != null )
            for (JBurgPatternMatcher kid: this.subPatterns)
                kid.findFactors(factors);
    }

    /**
     * Is this node at the root of a pattern?
     * @return true if this node is the root of a pattern.
     */
    boolean isRoot()
    {
        return this.parent == null;
    }

    /**
     *  Is this node's parent the root of a pattern?
     *  @return true if this node's parent is the root of a pattern.
     */
    boolean parentIsRoot()
    {
        return this.parent != null && this.parent.isRoot();
    }


    /**
     * Generate the initializer expression for a factored path variable.
     * @param codeEmitter - the active code emitter.
     * @return the correct initializer.
     */
    String generateFactoredReference(EmitLang codeEmitter)
    {
        Object result;

        if ( this.factoredPath != null )
        {
            result = this.factoredPath;
        }
        else if ( this.isRoot() )
        {
            result = "this";
        }
        else if ( this.parent.isRoot() )
        {
            result = codeEmitter.genCallMethod("this", "getNthChild", this.positionInParent);
        }
        else
        {
            Object parent = this.parent.generateFactoredReference(codeEmitter);

            result = codeEmitter.getTemplate(
                "factoredReferenceGuard",
                "parent",           parent,
                "positionInParent", this.positionInParent
            );
        }

        return result.toString();
    }

    /**
     *  The BURG has determined that this pattern (which is a root) 
     *  will only be matched in a context where its arity is already known.
     *  @param elideFixedArityChecks - new setting for this.elideFixedArityChecks.
     */
    public void setFixedArityContext(boolean elideFixedArityChecks)
    {
        checkPrecondition( !this.hasNaryTail(), "Can't set fixed arity context on a n-ary pattern.");
        checkPrecondition( this.isRoot(), "Can't set fixed arity context on a non-root pattern.");
        this.elideFixedArityChecks = elideFixedArityChecks;
    }

    /**
     *  Does this pattern use the given factored variable?
     *  @param factoredVariable - the factored variable of interest.
     *  @return true if this pattern uses the factored variable.
     */
    public boolean usesFactoredVariable(String factoredVariable)
    {
        if ( this.factoredPath != null && factoredVariable.contains(this.factoredPath) )
        {
            return true;
        }
        else
        {
            for ( JBurgPatternMatcher subpattern: this.subPatterns )
                if ( subpattern.usesFactoredVariable(factoredVariable) )
                    return true;
        }

        return false;
    }
}
