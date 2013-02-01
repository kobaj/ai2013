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
	
		// One action for every ship,  to be determined in the below loop
		HashMap<String, SpacewarAction> shipActions = new HashMap<String, SpacewarAction>();
		for (Ship ship : ships) {
			
			SpacewarAction current = ship.getCurrentAction();
			
			// What is this ?
			if (current != null && current.isMovementFinished()) {
				oldShadows.add(currentShadows.get(ship));
			}
			
			// We are not currently carrying out an action, make a new one
			if (current == null || current.isMovementFinished()) {

				
				Position currentPosition = ship.getPosition();

				// Just go diagonally
				Position newGoal = new Position(currentPosition.getX() + 200, currentPosition.getY() +200);

				// All boilerplate ?
				MoveAction newAction = null;
				newAction = new MoveAction(space, currentPosition, newGoal);
				Shadow shadow = new CircleShadow(3, getTeamColor(), newGoal);
				newShadows.add(shadow);
				currentShadows.put(ship, shadow);
				shipActions.put(ship.getId(), newAction);

			// We are currently carrying out an action, use it again
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
