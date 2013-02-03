package sheh1359;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import spacewar2.actions.MoveAction;
import spacewar2.actions.MoveToObjectAction;
import spacewar2.actions.SpacewarAction;
import spacewar2.actions.SpacewarActionException;
import spacewar2.clients.TeamClient;
import spacewar2.objects.Asteroid;
import spacewar2.objects.Base;
import spacewar2.objects.Bullet;
import spacewar2.objects.Ship;
import spacewar2.shadows.CircleShadow;
import spacewar2.shadows.LineShadow;
import spacewar2.shadows.Shadow;
import spacewar2.simulator.Toroidal2DPhysics;
import spacewar2.utilities.Position;
import spacewar2.utilities.Vector2D;

public class UltraClient extends TeamClient {

	ArrayList<Shadow> newShadows;
	ArrayList<Shadow> oldShadows;
	HashMap<Ship, Shadow> currentShadows;
	int counter = 20;
	Position finalGoal ;
	boolean finalApproach ;
	String goalType ;
	int moneyGoal;
	
	public void initialize() {
		newShadows = new ArrayList<Shadow>();
		oldShadows = new ArrayList<Shadow>();
		currentShadows = new HashMap<Ship,Shadow>();
		finalGoal = null ;
		finalApproach = false ;
		goalType = "none";
		moneyGoal = 0;
	}
	
	@Override
	public void shutDown() {
		// TODO Auto-generated method stub

	}
	
	// Do a step, given info about the space and the ships on our team
	public HashMap<String, SpacewarAction> getAction(Toroidal2DPhysics space, ArrayList<Ship> ships) {
		counter--;

		// One action for every ship,  to be determined in the below loop
		HashMap<String, SpacewarAction> shipActions = new HashMap<String, SpacewarAction>();
		for (Ship ship : ships) {
			SpacewarAction current = ship.getCurrentAction();
			SpaceGrid s = new SpaceGrid(space);
			ArrayList<Position> aPaths = new ArrayList<Position>();
					
			if((finalApproach == true && (current != null && current.isMovementFinished())) || (goalType.equals("money") && ship.getMoney() == 0) || (goalType.equals("asteroid") && ship.getMoney() == moneyGoal)  ){
				finalGoal = null;
				finalApproach = false ;
				System.out.println("Final Goal Reached");
				current = null;
				shipActions.clear();
			}
			
			if(finalGoal == null){
				if(ship.getMoney() <= 50){
					ArrayList<Asteroid> as = space.getAsteroids();
					Asteroid closest = null ;
					for(Asteroid a : as){
						if(closest == null || space.findShortestDistance(ship.getPosition(), a.getPosition()) <  space.findShortestDistance(ship.getPosition(), closest.getPosition())){
							if(a.getMoney() > 10){
								closest = a;
							}
						}
					}
					goalType = "asteroid";
					moneyGoal = ship.getMoney() + closest.getMoney();
					finalGoal = closest.getPosition();
	
				}else{
						goalType = "money";
						for (Base base : space.getBases()) {
							if (base.getTeamName().equalsIgnoreCase(ship.getTeamName())) {
								finalGoal = base.getPosition();
								System.out.println("heading to base");
								break;
							}
						}
				
				}
				
			}
			System.out.println(finalGoal);
			
			
			
		
			
			if(current != null && current.isMovementFinished()){
				finalApproach = false ;
			}
			
			// dtermine the next action
			if ( finalApproach == false && (current == null || current.isMovementFinished() || counter == 0 )) {
				counter = 20;
				shipActions.clear();
				current = null;
				oldShadows.add(currentShadows.get(ship));
				
				try{
					AdjacencyListGraph<Position> graph = new AdjacencyListGraph<Position>();
					// populate the graph
					for(ArrayList<SpaceBlock> row : s.getBlocks()){
						for(SpaceBlock b : row ){
							graph.addNode(b.getPosition());
						}
					}
					try{
						//set unit cost paths if not occupied and adjacent
						for(Node<Position> n : graph.getNodes()){
							ArrayList<SpaceBlock> adj = s.getAdjacentTo(s.getBlock(n.getItem()));
							for(SpaceBlock b: adj){
								if(b.isClear() || b.contains(finalGoal)){
									graph.addPath(n.getItem(),b.getPosition() , 1);
								}
							}
						}
					}catch(Exception e){
						System.out.println("cant get block");
						System.exit(0);
					}
					
					AStarTwo a = new AStarTwo(graph,space,s,s.getBlock(ship.getPosition()).getPosition(),finalGoal);
	
					aPaths = a.getPaths();
					try{
						Position newGoal = aPaths.get(2);
						
						// make the ship go faster by extending the vector to a further position
						Vector2D v = space.findShortestDistanceVector(ship.getPosition(), newGoal);
						v.multiply(5);
						
						int newX = (int) (v.getXValue() + ship.getPosition().getX());
						int newY = (int) (v.getYValue() + ship.getPosition().getY());
						Position multGoal = new Position (newX,newY);
						System.out.println("final: " + finalGoal);
						System.out.println("new: " + newGoal);
						System.out.println("path length:" + aPaths.size());
						MoveAction newAction = new MoveAction(space, ship.getPosition(), multGoal);
						shipActions.put(ship.getId(), newAction);
					}catch(Exception e){
						Position newGoal = finalGoal ;
						finalApproach = true ;
						System.out.println("final approach");
						MoveAction newAction = new MoveAction(space, ship.getPosition(), newGoal);
						shipActions.put(ship.getId(), newAction);
					}
					
					
					for(Position p : aPaths){
						System.out.println(p);
					}
					

					a = null;
					aPaths = null;
					

				}catch(Exception e){
					System.out.println("error");
				}


			} else {
				shipActions.put(ship.getId(), ship.getCurrentAction());
			}
			
		}
	
		return shipActions;
	
	}


	@Override
	public void endAction(Toroidal2DPhysics space, ArrayList<Ship> ships) {
		newShadows.clear();
	}

	@Override
	public ArrayList<Shadow> getNewShadows() {
		return newShadows;
	}

	@Override
	public ArrayList<Shadow> getOldShadows() {
		if (oldShadows.size() > 0) {
			ArrayList<Shadow> shadows = new ArrayList<Shadow>(oldShadows);
			oldShadows.clear();
			return shadows;
		} else {
			return null;
		}
	}


}
