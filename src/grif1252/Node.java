package grif1252;

import java.util.ArrayList;
import java.util.Formatter;

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
		{
			 System.out.println("this is parent.");
			 ArrayList<Node<T>> root =  new ArrayList<Node<T>>();
			 root.add(this);
			 return root;
		}
		
		//System.out.println("this: " + String.valueOf(this.matrix_id) + " parent: " + parent.matrix_id);
		ArrayList<Node<T>> current_path = parent.getPathToRoot();
		current_path.add(this);
		return current_path;
	}
	
	public Node<T> copy()
	{
		Node<T> returnable = new Node<T>(position.deepCopy(), matrix_id, node_type, hueristic_distance);
		returnable.parent = this.parent;
		returnable.item = this.item;
		returnable.root_to_n_distance = this.root_to_n_distance;
		
		return returnable;
	}
	
	public String toString()
	{
		String type = "regular";
		if(this.node_type == NodeType.start)
			type = "start";
		else if (this.node_type == NodeType.goal)
			type = "goal";
		
		Formatter formatter = new Formatter();
		
		String value = "Node: " + matrix_id + " has parent: " + (parent != null ? parent.matrix_id : "none")
				+ formatter.format("%n    (f, g, h): (%4d, %4d, %4d", (int)this.fn(), (int)this.root_to_n_distance, (int)this.hueristic_distance) + ") and type: " + type;
		formatter = new Formatter();
		value += formatter.format("%n    (x,y): (%4d, %4d", (int)this.position.getX(), (int)this.position.getY()) + ")";
	
		return value;
	}
}

