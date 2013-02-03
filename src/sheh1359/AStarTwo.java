package sheh1359;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

import spacewar2.objects.Ship;
import spacewar2.simulator.Toroidal2DPhysics;
import spacewar2.utilities.Position;
import java.util.Comparator;

public class AStarTwo{
	PriorityQueue<AstarNode> fringe ;
	AdjacencyListGraph<Position> graph ;
	ArrayList<Position> visited ;
	Position start ;
	Position goal ;
	Toroidal2DPhysics space;
	SpaceGrid grid;
	
	
	class HeuristicComparator implements Comparator<AstarNode>{
		@Override
		public int compare(AstarNode a, AstarNode b){
			if(f(a) < f(b) ){
				return -1 ;
			}else if(f(b) < f(a)){
				return 1 ;
			}else{
				return 0;
			}
		}
	}
	
	public AStarTwo(AdjacencyListGraph graph,Toroidal2DPhysics space, SpaceGrid grid, Position start, Position goal) {

		// initialize fields
		this.graph = graph;
		this.start = start;
		this.goal = goal;
		this.space = space;
		this.grid = grid;
        Comparator<AstarNode> comparator = new HeuristicComparator();
		fringe = new PriorityQueue<AstarNode>(8,comparator);
		visited = new ArrayList<Position>();

		
	}
	
	public double g(AstarNode n){
		return getPathFromStart(n).size() ;
	}
	
	public double h(AstarNode n) {
		return space.findShortestDistance(n.getPosition(), goal);
	}
	public double f(AstarNode n){
		return g(n) + h(n);
	}
	
	public boolean isGoal(Position p){
		return (space.findShortestDistance(p, goal) < grid.getCircumRadius());
	}
	
	public ArrayList<Position> getPaths()  {

		AstarNode head = new AstarNode(start,null);
		visited.add(head.getPosition());
		
		// add all nodes connected to the start node to the fringe
		// this populates fringe but not children of head
		for(Node<Position> node : graph.getNodes()){
			Position n = node.getItem();
			try {
				if(graph.getNode(start).hasDirectPathTo(n)){
					fringe.add(new AstarNode(n,head));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		while(true){
			if(fringe.isEmpty()){
				return null;
			}
			
			AstarNode next = fringe.poll();
			head.addChild(next);
			head = next;
			
			if (isGoal(next.getPosition())){
				return getPathFromStart(head); // path to next
			}else{
				visited.add(next.getPosition());
				
				for(Node<Position> node : graph.getNodes()){
					Position n = node.getItem();
					try {
						if(graph.getNode(next.getPosition()).hasDirectPathTo(n)){
							if(visited.contains(n)){
								// nothing
							}else if(fringe.contains(n)){
								// change value
								// TODO
							}else{
								fringe.add(new AstarNode(n,head));
							}
							
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			
		}
		
	}
	
	
	public ArrayList<Position> getPathFromStart(AstarNode node){
		
		ArrayList<Position> result = new ArrayList<Position>();
		result.add(node.getPosition());
		
		AstarNode n = node ;
		while(n.getParent() != null){
			result.add(n.getParent().getPosition());
			n = n.getParent();
		}
		
		Collections.reverse(result);
		return result;
	}
	
}

class AstarNode{
	Position position;
	AstarNode parent ;
	ArrayList<AstarNode> children;
	
	public AstarNode(Position position, AstarNode parent){
		this.position = position;
		this.parent = parent ;
		children = new ArrayList<AstarNode>();
	}
	public Position getPosition(){
		return position ;
	}
	public AstarNode getParent(){
		return parent ;
	}
	public void addChild(AstarNode child){
		children.add(child);
	}
}


