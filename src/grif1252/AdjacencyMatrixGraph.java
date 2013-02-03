package grif1252;

import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;

public class AdjacencyMatrixGraph<T>
{
	final public static int ROW = 0;
	final public static int COLUMN = 1;
	
	final private int node_count;
	
	final private double[][] adjacency_matrix;
	
	private ArrayList<Node<T>> nodes;
	
	public AdjacencyMatrixGraph(int node_count)
	{
		if (node_count <= 0)
			node_count = 1;
		
		this.node_count = node_count;
		
		adjacency_matrix = new double[node_count][node_count];
	}
	
	public void storeNodes(ArrayList<Node<T>> nodes)
	{
		// make a handy dandy copy of the nodes
		this.nodes = new ArrayList<Node<T>>();
		for (Node<T> n : nodes)
			this.nodes.add(n.copy());
	}
	
	public ArrayList<Node<T>> getNodes()
	{
		ArrayList<Node<T>> all_nodes = new ArrayList<Node<T>>();
		for(Node<T> n: nodes)
			all_nodes.add(n.copy());
		return all_nodes;
	}
	
	public void setConnected(Node<T> A, Node<T> B, double distance)
	{
		if (A.matrix_id == B.matrix_id)
			distance = 0;
		
		int[] rows_columns = fixRowColumn(A.matrix_id, B.matrix_id);
		
		adjacency_matrix[rows_columns[ROW]][rows_columns[COLUMN]] = distance;
	}
	
	public boolean getConnected(Node<T> A, Node<T> B)
	{
		if (A.matrix_id == B.matrix_id)
			return true;
		
		int[] rows_columns = fixRowColumn(A.matrix_id, B.matrix_id);
		
		return (adjacency_matrix[rows_columns[ROW]][rows_columns[COLUMN]] > 0.0);
	}
	
	// this is not recursive. just one level deep
	public ArrayList<Node<T>> getChildren(Node<T> parent)
	{
		ArrayList<Node<T>> children = new ArrayList<Node<T>>();
		
		for (Node<T> n : nodes)
			if (parent.matrix_id != n.matrix_id)
			{
				if (getConnected(parent, n))
				{
					n = n.copy();
					n.parent = parent;
					children.add(n);
				}
			}
		
		return children;
	}
	
	
	/*
	 * public void buildTree(DefaultMutableTreeNode top, Node<T> parent) { ArrayList<Node<T>> visited = new ArrayList<Node<T>>(); visited.add(parent);
	 * 
	 * while (true) { ArrayList<Node<T>> children = getChildren(parent); for (Node<T> child : children) { if (!visited.contains(child)) { top.add(new DefaultMutableTreeNode(child));
	 * visited.add(child); } } } }
	 */
	
	private int[] fixRowColumn(int row, int column)
	{
		if (row > column)
			return new int[] { row, column };
		else
			return new int[] { column, row };
	}
	
}
