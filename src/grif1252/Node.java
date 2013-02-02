package grif1252;

import java.util.ArrayList;

import spacewar2.utilities.Position;

class Node<T>
{
	public static enum NodeType { regular, start, goal};
	
	public final NodeType node_type;
	public final Position position;
	public final int matrix_id;
	public final double hueristic_distance;
	
	public Object item;
	public double root_to_n_distance = 0;
	public Node<T> parent;
	public Node<T> child;
	
	public Node(Position position, int matrix_id, NodeType node_type, double hueristic_distance)
	{
		this.position = position;
		this.matrix_id = matrix_id;
		this.node_type = node_type;
		this.hueristic_distance = hueristic_distance;
	}
	
	public double fn()
	{
		return hueristic_distance + root_to_n_distance;
	}
	
	public ArrayList<Node<T>> getPathToRoot()
	{
		if(parent == null)
			return new ArrayList<Node<T>>();
		
		ArrayList<Node<T>> current_path = parent.getPathToRoot();
		current_path.add(this);
		return current_path;
	}
}

