package grif1252;

class Node<T>
{
	public static enum NodeType { regular, start, goal};
	
	public final NodeType node_type;
	public final T item;
	public final int matrix_id;
	
	public Node(T item, int matrix_id, NodeType node_type)
	{
		this.item = item;
		this.matrix_id = matrix_id;
		this.node_type = node_type;
	}
}
