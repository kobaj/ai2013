package grif1252;

import java.util.ArrayList;

public class AdjacencyMatrixGraph
{
	final public static int ROW = 0;
	final public static int COLUMN = 1;
	
	// if for some reason we see fit to not make this a square matrix
	private int rows;
	private int columns;
	
	private boolean[][] adjacency_matrix;
	
	private ArrayList<Node> nodes;
	
	public AdjacencyMatrixGraph(int node_count)
	{
		if (node_count <= 0)
			node_count = 1;
		
		this.rows = node_count;
		this.columns = node_count;
		
		adjacency_matrix = new boolean[this.rows][this.columns];
	}
	
	public void storeNodes(ArrayList<Node> nodes)
	{
		// make a handy dandy copy of the nodes
		this.nodes = new ArrayList<Node>();
		for(Node n: nodes)
			this.nodes.add(new Node(n.item, n.matrix_id));
	}
	
	public ArrayList<Node> getNodes()
	{
		return nodes;
	}
	
	public void setConnected(Node A, Node B, boolean connected)
	{
		if (A.matrix_id == B.matrix_id)
			connected = true;
		
		int[] rows_columns = fixRowColumn(A.matrix_id, B.matrix_id);
		
		adjacency_matrix[rows_columns[ROW]][rows_columns[COLUMN]] = connected;
	}
	
	public boolean getConnected(Node A, Node B)
	{
		if (A.matrix_id == B.matrix_id)
			return true;
		
		int[] rows_columns = fixRowColumn(A.matrix_id, B.matrix_id);
		
		return adjacency_matrix[rows_columns[ROW]][rows_columns[COLUMN]];
	}
	
	public static int[] fixRowColumn(int row, int column)
	{
		if (row > column)
			return new int[] { row, column };
		else
			return new int[] { column, row };
	}
	
}
