package jburg.burg.ir;

/**
 * GetNthChild carries the information necessary to build a getNthChild call,
 * and signals the code generator (via its class) that such a call is
 * to be generated.
 */
public class GetNthChild
{
    /**
     * @param memberExpression an expression to access the root inode.
     * @param indexExpression an expression that designates which child to access.
     */
    public GetNthChild(Object memberExpression, Object indexExpression)
    {
        this.memberExpression = memberExpression;
        this.indexExpression = indexExpression;
    }

    public final Object memberExpression;
    public final Object indexExpression;
}
