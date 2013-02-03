package sheh1359;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import spacewar2.actions.MoveAction;
import spacewar2.actions.SpacewarAction;
import spacewar2.actions.SpacewarActionException;
import spacewar2.clients.TeamClient;
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

	public void initialize() {
		newShadows = new ArrayList<Shadow>();
		oldShadows = new ArrayList<Shadow>();
		currentShadows = new HashMap<Ship,Shadow>();
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
			
			// dtermine the next action
			if (ship.getCurrentAction() == null || (current.isMovementFinished() && aPaths.size() == 0)) {
				oldShadows.add(currentShadows.get(ship));
				
				
				Position currentPosition = ship.getPosition();

				try{
					AdjacencyListGraph<Position> graph = new AdjacencyListGraph<Position>();
					// populate the graph
					for(ArrayList<SpaceBlock> row : s.getBlocks()){
						for(SpaceBlock b : row ){
							graph.addNode(b.getPosition());
						}
					}
					
					//set unit cost paths if not occupied
					for(Node<Position> n : graph.getNodes()){
						ArrayList<SpaceBlock> adj = s.getAdjacentTo(s.getBlock(n.getItem()));
						for(SpaceBlock b: adj){
							if(b.isClear()){
								graph.addPath(n.getItem(),b.getPosition() , 1);
							}
						}
					}

					
					AStarTwo a = new AStarTwo(graph,space,s,s.getBlock(ship.getPosition()).getPosition(),new Position(500,500));
					aPaths = a.getPaths();
					
					Position newGoal = aPaths.get(0);
					MoveAction newAction = new MoveAction(space, currentPosition, newGoal);
					shipActions.put(ship.getId(), newAction);

					System.out.println("path length:" + aPaths.size());
					for(Position p : aPaths){
						Shadow shadow = new CircleShadow(3, getTeamColor(), p);
						newShadows.add(shadow);
						currentShadows.put(ship, shadow);
						System.out.println(p);
					}
					
					

				}catch(Exception e){
					System.out.println("error");
				}

			// We are currently carrying out an action, use it again
			}else if (current.isMovementFinished() || aPaths.size() != 0){
				aPaths.remove(0);
				Position newGoal = aPaths.get(0);
				MoveAction newAction = new MoveAction(space,ship.getPosition(), newGoal);
				shipActions.put(ship.getId(), newAction);
				
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
