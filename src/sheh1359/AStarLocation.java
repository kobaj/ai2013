package sheh1359;
import java.util.ArrayList;

import spacewar2.objects.Ship;
import spacewar2.utilities.Position;


public class AStarLocation{ // Eventually make a general Astar class and extend
	
	AdjacencyListGraph<Position> graph;
	Ship ship;
	SpaceGrid grid ;
	Position goal ;
	SpaceBlock start ;
	
	public AStarLocation(Ship ship,SpaceGrid grid,Position goal) throws Exception {

		// initialize fields
		this.graph = new AdjacencyListGraph<Position>();
		this.ship = ship;
		this.grid = grid ;
		this.goal = goal;
		try{
			this.start = grid.getBlock(ship.getPosition());
		}catch(Exception e){
			//This will not ever occur.
		}
		
		// populate the graph
		for(ArrayList<SpaceBlock> row : grid.getBlocks()){
			for(SpaceBlock b : row ){
				graph.addNode(b.getPosition());
				System.out.println(b.getPosition().toString());
			}
		}
		
		//set unit cost paths if not occupied
		for(Node<Position> n : graph.getNodes()){
			ArrayList<SpaceBlock> adj = grid.getAdjacentTo(grid.getBlock(n.getItem()));
			for(SpaceBlock b: adj){
				if(b.isClear()){
					graph.addPath(n.getItem(),b.getPosition() , 1);
				}
			}
		}
	}
	
	public double g(Node <Position> n) throws Exception{
		return graph.getPathCost(start.getPosition(), n.getItem());
	}
	
	public double h(Node<Position> n){
		int deltaX = (int) (n.getItem().getX() - goal.getX());
		int deltaY = (int) (n.getItem().getY() - goal.getY());
		return Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
	}
	public double f(Node<Position> n) throws Exception{
		return g(n) + h(n);
	}
	
	public boolean isGoal(SpaceBlock b){
		return b.contains(goal);
	}
	
	public ArrayList<SpaceBlock> getPaths() throws Exception{
			ArrayList<SpaceBlock> paths = new ArrayList<SpaceBlock>();
			int i = 0;
			do{
				System.out.println("Start: " + start.position.getX() + ", " + start.getPosition().getY());
				++i;
				System.out.println("adding a block hopefullyt");
					Position best = null ;
					for(Path<Position> p : graph.getNode(start.getPosition()).getPaths()){
						Node<Position> n = graph.getNode(p.getDest());
						if(isGoal(start)){
							best = goal;
						}else if(best == null || f(n) < f(graph.getNode(best))){
							best = n.getItem();
							System.out.println(best.getX() + " , " + best.getY());
						}
					}
					paths.add(grid.getBlock(best));
					start = grid.getBlock(best);
			}while(!paths.get(paths.size() - 1).contains(goal) && i < 100 );
			return paths;
	}
	
}