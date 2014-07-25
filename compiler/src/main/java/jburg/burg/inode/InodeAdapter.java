package jburg.burg.inode;

/**
 *  INodeAdapter defines the interface the BURG uses to
 *  write code that interacts with the input intermediate
 *  representation, a.k.a. "i-nodes."
 */
public interface InodeAdapter
{
	/**
	 *  @return true if the adapter can handle this type of inode.
	 */
	public boolean accept(String inodeClassName);

	/**
	 *  @return an expression that computes an inode's number of children.
	 */
	public Object genGetArity(Object stem, jburg.emitter.EmitLang emitter);

	/**
	 *  @return an expression that fetches a child node at the specified index.
	 */
	public Object genGetNthChild(Object stem, Object index, jburg.emitter.EmitLang emitter);

	/**
	 * @return an expression that fetches the i-node's operator code
	 *   for pattern matching.
	 */
	public Object genGetOperator(Object node, jburg.emitter.EmitLang emitter);
}
