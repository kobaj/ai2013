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
	
	public double h(Node<Position> n) throws Exception{
		return grid.getDistance(grid.getBlock(n.getItem()), goal);
	}
	public double f(Node<Position> n) throws Exception{
		if(g(n) != 1){
			System.out.println("g != 1");
			System.exit(0);
		}
		System.out.println("f(n) = " + g(n) + " + " +  h(n));
		return g(n) + h(n);
	}
	
	public boolean isGoal(SpaceBlock b){
		return b.contains(goal);
	}
	
	public ArrayList<SpaceBlock> getPaths()  {
			ArrayList<SpaceBlock> paths = new ArrayList<SpaceBlock>();
			do{
					Position best = null ;
					try{
						System.out.println("num possible positions: " + graph.getNode(start.getPosition()).getPaths().size());
						for(Path<Position> p : graph.getNode(start.getPosition()).getPaths()){
							Node<Position> n = graph.getNode(p.getDest());
							System.out.println("distance: " + f(n));
							if(isGoal(start)){
								best = goal;
							}else if(best == null || f(n) < f(graph.getNode(best))){
								
								if(!paths.contains(grid.getBlock(n.getItem()))){
									best = n.getItem();
								}
								
							}
						}
						
					}catch(Exception e){
						System.out.println("failed the for loop");
					}
					
					try{
						System.out.println(best == null);
						start = grid.getBlock(best);
					}catch(Exception e){
						System.out.println("couldnt change start");
						System.out.println(e);
						System.exit(0);
					}
					
					try{
							paths.add(grid.getBlock(best));
					}catch(Exception e){
						System.out.println("couldnt add path");
						System.exit(0);
					}

			}while(!paths.get(paths.size() - 1).contains(goal));
			return paths;
	}
	
}