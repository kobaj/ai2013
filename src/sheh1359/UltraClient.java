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

	HashMap<Ship, Position> stuckStart;
	HashMap<Ship,Position> stuckNow;
	HashMap<Ship,Integer> stuckCounter;
	
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
		
		stuckStart = new HashMap<Ship, Position>();
		stuckNow = new HashMap<Ship, Position>();
		stuckCounter = new HashMap<Ship, Integer>();
	}
	
	@Override
	public void shutDown() {}
	
	public Base getBase(Toroidal2DPhysics space,Ship ship){
		for (Base base : space.getBases()) {
			if (base.getTeamName().equalsIgnoreCase(ship.getTeamName())) {
				return base ;
			}
		}
		return null;
	}
	
	// Do a step, given info about the space and the ships on our team
	public HashMap<String, SpacewarAction> getAction(Toroidal2DPhysics space, ArrayList<Ship> ships) {
		counter--;
		
		// One action for every ship,  to be determined in the below loop
		HashMap<String, SpacewarAction> shipActions = new HashMap<String, SpacewarAction>();
		for (Ship ship : ships) {
			
			// make sure the ship isn't stuck by ensuring
			// that it moved atleast 10 pixels in the last
			// 80 steps.  If it is stuck, invalidate the final goal
			if(stuckCounter.get(ship) == null){
				stuckCounter.put(ship, 80);
			}
			if(stuckStart.get(ship) == null){
				stuckStart.put(ship, ship.getPosition());
			}
			stuckCounter.put(ship, stuckCounter.get(ship) - 1);
			if(stuckCounter.get(ship) == 0){
				stuckNow.put(ship,ship.getPosition());
				if(space.findShortestDistance(stuckNow.get(ship), stuckStart.get(ship)) < 10){
					finalGoal = null;
					System.out.println("I'm stuck. Trying a different goal...");
				}
				stuckStart.put(ship, ship.getPosition());
				stuckCounter.put(ship, 80);
			}
	
			// initialization stuff
			SpacewarAction current = ship.getCurrentAction();
			SpaceGrid s = new SpaceGrid(space);
			ArrayList<Position> aPaths = new ArrayList<Position>();
			Base base = getBase(space,ship);
					
		
			// determine whether a "final goal" has been reached
			if(	(finalApproach == true && (current != null && current.isMovementFinished())) || 
				(goalType.equals("money") && ship.getMoney() == 0) || 
				(goalType.equals("asteroid") && ship.getMoney() == moneyGoal)  
			){
				// set variables to activate other parts below
				finalGoal = null;
				finalApproach = false ;
				current = null;

				System.out.println("Final Goal Reached");
			}
			
			// There is no final goal. Create a new one
			if(finalGoal == null || space.isLocationFree(finalGoal, 2)){
				finalGoal = null;
				oldShadows.add(currentShadows.get(ship));

				// The ship does not have enough money to take back
				if(ship.getMoney() <= 10){
					
					// Find the asteroid so that the sum of the distances between 
					// it and the base plus it and the ship are minimized. 
					// Also make sure the amount of money is acceptable
					ArrayList<Asteroid> as = space.getAsteroids();
					Asteroid best = null ;
					for(Asteroid a : as){
						
						double  shipSum =	space.findShortestDistance(ship.getPosition(), a.getPosition());
						double baseSum =	space.findShortestDistance(base.getPosition(), a.getPosition());
						
						if(best != null ){
							double  bestSum = space.findShortestDistance(ship.getPosition(), best.getPosition());
							
							// Is the the best closest asteroid that's on our side of the base and worth money?
							if(	(shipSum <  baseSum) && (shipSum < bestSum) && a.getMoney() >= 100){
								best = a;
							// 
							}
							
						}else{
							// just take any asteroid worth money. This will only ever
							// be used of nothing else is found
							if(a.getMoney() > 0){
								best = a;
							}
						}
						
					}
					
					// set control variables
					goalType = "asteroid";
					moneyGoal = ship.getMoney() + best.getMoney();
					finalGoal = best.getPosition();
	
				// The ship has enough money to take back
				}else{
					
						// first assume we're going to take the money back
						// and set the finalGoal to the base
						goalType = "money";						
						finalGoal = base.getPosition();
						System.out.println("heading to base");
					
						// then check for asteroids that are between the
						// ship and the base. That is, asteroids where the 
						// distance between the ship and the asteroid is less 
						// than the distance between the ship and the base and
						// also the distance between the asteroid and the base 
						// is less than the distance between the ship and the base.
						// If they exist, change the finalGoal
						ArrayList<Asteroid> as = space.getAsteroids();
						Asteroid best = null ;
						for(Asteroid a : as){
							double shipToAsteroid = space.findShortestDistance(ship.getPosition(), a.getPosition());
							double shipToBase = space.findShortestDistance(ship.getPosition(), base.getPosition());
							double asteroidToBase = space.findShortestDistance(a.getPosition(), base.getPosition());
							if((shipToAsteroid < shipToBase) && (asteroidToBase < shipToBase) && a.getMoney() > 0){
								// set control variables
								goalType = "asteroid";
								moneyGoal = ship.getMoney() + a.getMoney();
								finalGoal = a.getPosition();
								System.out.println("stopping on the way");
							}
						}
						
				}
				
				// indicate the final goal
				Shadow shadow = new CircleShadow(3, getTeamColor(), finalGoal);
				newShadows.add(shadow);
				currentShadows.put(ship, shadow);
				
			}
			
			// if a movement just finished make sure finalApproach is falsified.
			// This way if we just finished a final approach we can go back to
			// finding multiple step paths with A*
			if(current != null && current.isMovementFinished()){
				finalApproach = false ;
			}
			
			// determine the next action towards the final goal
			if(	
					finalApproach == false 	&& 
					(current == null || current.isMovementFinished() || counter == 0 )
			){
				
				// restart the timeout for replanning
				counter = 20;
				
				
				// Populate a graph of positions of gridblocks
				AdjacencyListGraph<Position> graph = new AdjacencyListGraph<Position>();
				for(ArrayList<SpaceBlock> row : s.getBlocks()){
					for(SpaceBlock b : row ){
						graph.addNode(b.getPosition());
					}
				}
				
				// Connect the graph nodes containing unoccupied adjacent positions
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
				}
				
				// Perform A* to get a path to the final goal
				AStarTwo a;
				try {
					a = new AStarTwo(graph,space,s,s.getBlock(ship.getPosition()).getPosition(),finalGoal);
					aPaths = a.getPaths();
					//System.out.println(aPaths.size());
				} catch (Exception e) {
					e.printStackTrace();
				}

				
				
				// Use the path from A* to get within the nearest gridblock
				// discard the beginning nodes of the path. The first is just 
				// the start position and the second contains "noise" that 
				// causes back and forth motion as the path is updated repeatedly
				try{
					
					Position newGoal = aPaths.get(2);
					
					// make the ship go faster by extending the displacement vector to a further position
					Vector2D v = space.findShortestDistanceVector(ship.getPosition(), newGoal);
					v.multiply(10);
					int newX = (int) (v.getXValue() + ship.getPosition().getX());
					int newY = (int) (v.getYValue() + ship.getPosition().getY());
					Position multGoal = new Position (newX,newY);
					
					// set the action
					MoveAction newAction = new MoveAction(space, ship.getPosition(), multGoal);
					shipActions.put(ship.getId(), newAction);
					
					
				// We are too close to use the grid for navigation.
				// go straight to the final goal
				}catch(Exception e){
					Position newGoal = finalGoal ;
					finalApproach = true ;
					System.out.println("final approach");
					MoveAction newAction = new MoveAction(space, ship.getPosition(), newGoal);
					shipActions.put(ship.getId(), newAction);
				}

			// The current action is still valid, just reuse it
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
