package sheh1359;

import java.util.ArrayList;

public class AdjacencyListGraph<T>{

	ArrayList<Node<T>> nodes ;
	
	public AdjacencyListGraph(){
		nodes = new ArrayList<Node<T>>();
	}
	
	public void addNode(T item){
		Node<T> node = new Node<T>(item);
		nodes.add(node);
	}
	
	public void addPath(T source, T dest,double cost){
		
		try{
			Node<T> sourceNode = getNode(source);
			Node<T> destNode = getNode(dest); // this isn't used, but we need it to possibly throw an exception

			sourceNode.addPath(dest, cost);
		}catch(Exception e){
			System.out.println("Error: tried to add path to or from nonexistent node");
		}
	
	}
	
	public Node<T> getNode(T item) throws Exception{
		
		for(Node<T> n : nodes){
			if(n.getItem() == item){
				return n;
			}
		}

		throw new Exception();

	}
	
	public double getPathCost(T source, T dest) throws Exception{
		try{
			Node<T> sourceNode = getNode(source);
			return sourceNode.getPathCost(dest);
		}catch(Exception e){
			throw new Exception();
		}
	}
	
}

class Node<T>{

	T item;
	ArrayList<Path<T>> paths;	
	
	public Node(T item){
		this.item = item;
		paths = new ArrayList<Path<T>>();
	}
	
	public void addPath(T dest, double cost){
		paths.add(new Path<T>(dest,cost));
	}
	
	public boolean hasPathTo(T dest){
		
		for(Path<T> path : paths){
			if( path.getDest() == dest){
				return true;
			}
		}
		
		return false;
	}
	
	public double getPathCost(T dest) throws Exception{
		
		for(Path<T> path : paths){
			if( path.getDest() == dest){
				return path.getCost();
			}
		}
		
		throw new Exception();
		
	}
	
	public T getItem(){
		return item;
	}
	
	public ArrayList<Path<T>> getPaths(){
		return paths;
	}
	
}

class Path<T>{
	
	T dest ;
	double cost ;
	
	public Path(T dest, double cost){
		this.dest = dest;
		this.cost = cost;
	}
	
	public double getCost(){
		return cost;
	}
	

	public T getDest(){
		return dest;
	}
	
}