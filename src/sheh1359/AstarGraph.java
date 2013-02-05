package sheh1359;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import spacewar2.simulator.Toroidal2DPhysics;
import spacewar2.utilities.Position;

public class AstarGraph{

	Toroidal2DPhysics space ;
	ArrayList<AstarNode> nodes ;
	
	PriorityQueue<AstarNode> fringe;
	ArrayList<AstarNode> visited;
	
	Position goal; 
	
	public AstarGraph(Toroidal2DPhysics space,ArrayList<Position> grid, Position goal){
		
		// we're going to assume the grid is actually a grid,
		// and that the distances between all connected 
		// positions is equal either to the distance between the
		// first two, or the hypoteneuse for diagonals

		this.space = space;
		this.nodes = new ArrayList<AstarNode>();
		this.goal = goal;
		
		// find the gridSize as described above, 
		double gridSizeA = Math.abs(space.findShortestDistance(grid.get(0),grid.get(1)));
		double gridSizeB = Math.sqrt(Math.pow(gridSizeA,2)*2);
		
		//add all nodes but NO PATHS
		for(Position p : grid){
			AstarNode newNode = new AstarNode(p);
			nodes.add(newNode);
		}
		
		// add paths between nodes that are adjacent physically
		// and unoccupied unless by the goal
		for(AstarNode n : nodes){
			for(AstarNode current : nodes){
				Double diff = space.findShortestDistance(n.getPosition(),current.getPosition());
				if(diff == gridSizeA || Math.abs(diff - gridSizeB) < 0.5 ){ // very rough tolerance
					if(space.isLocationFree(current.getPosition(), (int)gridSizeB/2) || current.getPosition() == goal){
						n.addNeighbor(current);
					}
				}
			}
		}
		
	}
	
	public AstarNode getNode(Position p){
		for(AstarNode n : nodes){
			if(n.getPosition() == p){
				return n;
			}
		}
		return null;
	}
	
	public ArrayList<Position> getShortestPath(Position startPosition){
		
		
		AstarNode start = getNode(startPosition);
		AstarNode goal = getNode(this.goal);

		fringe = new PriorityQueue<AstarNode>(1,hueristicCompare);
		visited = new ArrayList<AstarNode>();
		
		fringe.add(start);
		
		while(fringe.size() > 0){	
			AstarNode current = fringe.poll();
			visited.add(current);
			
			if(current == goal){
				ArrayList<Position> outputPath = goal.getSearchPathPositions();
				Collections.reverse(outputPath);
				return outputPath;
			}
			
			for(AstarNode neighbor : current.getNeighbors()){
				if(visited.contains(neighbor)){
					continue;
				}
				
				int neighborCostFromHere = current.getSearchPathCost() + current.getNeighborCost(neighbor);
				
				if(!fringe.contains(neighbor) || neighborCostFromHere < neighbor.getSearchPathCost()){
					neighbor.setSearchParent(current);
					if(!fringe.contains(neighbor)){
						fringe.add(neighbor);
					}
				}
			}
			
		}
		
		return null; // there is no path
		
	}
	
	public Integer g(AstarNode n){
		return n.getSearchPathCost();
	}
	
	public double h(AstarNode n) {
		return space.findShortestDistance(n.getPosition(), goal);
	}

	public double f(AstarNode n){
		return g(n) + h(n);
	}
	
	public Comparator<AstarNode> hueristicCompare = new Comparator<AstarNode>(){
		public int compare(AstarNode a, AstarNode b) {
			if(f(a) < f(b) ){
				return -1 ;
			}else if(f(b) < f(a)){
				return 1 ;
			}else{
				return 0;
			}
		} 
	};
}


class AstarNode{
	
	protected Position position;
	
	protected ArrayList<AstarNode> neighbors;
	protected HashMap<AstarNode,Integer> pathCosts;
	
	protected AstarNode searchParent;
	
	// create a new node
	public AstarNode(Position position){
		this.position = position;
		this.neighbors = new ArrayList<AstarNode>() ;
		this.pathCosts = new HashMap<AstarNode,Integer>();
		this.searchParent = null;
	}
	
	// get the Position object
	public Position getPosition(){
		return position;
	}
	
	public void setSearchParent(AstarNode parent){
		this.searchParent = parent;
	}
	
	// get the node's parent in the search path
	public AstarNode getSearchParent(){
		return searchParent;
	}
	
	// add a neighbor with given cost
	public void addNeighbor(AstarNode neighbor,Integer pathCost){
		neighbors.add(neighbor);
		pathCosts.put(neighbor,pathCost);
	}
	
	// add a neighbor with unit cost
	public void addNeighbor(AstarNode neighbor){
		addNeighbor(neighbor,1);
	}
	
	// remove a neighbor 
	public void removeNeighbor(AstarNode neighbor){
		neighbors.remove(neighbor);
		pathCosts.remove(neighbor);
	}

	
	// get all neighbors
	public ArrayList<AstarNode> getNeighbors(){
		return neighbors;
	}
	
	// get cost to a particular neighbor
	public Integer getNeighborCost(AstarNode neighbor){		
		return pathCosts.get(neighbor);
	}
	
	// Traverse the search parents recursively to reconstruct the search path
	public ArrayList<AstarNode> getSearchPath(){

		// base case
		if(this.getSearchParent() == null){
			
			ArrayList<AstarNode> singleton = new ArrayList<AstarNode>();
			singleton.add(this);
			return singleton;
			
		// recursive case
		}else{
			
			ArrayList<AstarNode> path = new ArrayList<AstarNode>();
			path.add(this);
			ArrayList<AstarNode> parents = this.getSearchParent().getSearchPath();
			path.addAll(parents);
			return path;
		}
		
	}
	
	// get the cost of a search path
	public Integer getSearchPathCost(){
		Integer cost = 0 ;
		ArrayList<AstarNode> path = this.getSearchPath();
		for(int i = 0; i < path.size() - 1 ; i++ ){
			cost += path.get(i+1).getNeighborCost(path.get(i));
		}
		return cost;
	}
	
	// get the positions of a search path
	public ArrayList<Position> getSearchPathPositions(){
		ArrayList<Position> result = new ArrayList<Position>();
		ArrayList<AstarNode> path = this.getSearchPath();
		for(AstarNode n : path ){
			result.add(n.getPosition());
		}
		return result;
	}
	
}