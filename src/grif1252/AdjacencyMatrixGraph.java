package grif1252;

import java.util.ArrayList;

public class AdjacencyMatrixGraph<T>
{
	final public static int ROW = 0;
	final public static int COLUMN = 1;
	
	// if for some reason we see fit to not make this a square matrix
	private int rows;
	private int columns;
	
	private boolean[][] adjacency_matrix;
	
	private ArrayList<Node<T>> nodes;
	
	public AdjacencyMatrixGraph(int node_count)
	{
		if (node_count <= 0)
			node_count = 1;
		
		this.rows = node_count;
		this.columns = node_count;
		
		adjacency_matrix = new boolean[this.rows][this.columns];
	}
	
	public void storeNodes(ArrayList<Node<T>> nodes)
	{
		// make a handy dandy copy of the nodes
		this.nodes = new ArrayList<Node<T>>();
		for (Node<T> n : nodes)
			this.nodes.add(new Node<T>(n.item, n.matrix_id, n.node_type));
	}
	
	public ArrayList<Node<T>> getNodes()
	{
		return nodes;
	}
	
	public void setConnected(Node<T> A, Node<T> B, boolean connected)
	{
		if (A.matrix_id == B.matrix_id)
			connected = true;
		
		int[] rows_columns = fixRowColumn(A.matrix_id, B.matrix_id);
		
		adjacency_matrix[rows_columns[ROW]][rows_columns[COLUMN]] = connected;
	}
	
	public boolean getConnected(Node<T> A, Node<T> B)
	{
		if (A.matrix_id == B.matrix_id)
			return true;
		
		int[] rows_columns = fixRowColumn(A.matrix_id, B.matrix_id);
		
		return adjacency_matrix[rows_columns[ROW]][rows_columns[COLUMN]];
	}
	
	private int[] fixRowColumn(int row, int column)
	{
		if (row > column)
			return new int[] { row, column };
		else
			return new int[] { column, row };
	}
	
}
