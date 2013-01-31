package grif1252;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import spacewar2.actions.MoveAction;
import spacewar2.actions.SpacewarAction;
import spacewar2.clients.TeamClient;
import spacewar2.objects.Ship;
import spacewar2.shadows.CircleShadow;
import spacewar2.shadows.Shadow;
import spacewar2.simulator.Toroidal2DPhysics;
import spacewar2.utilities.Position;

public class RandomTeamClient extends TeamClient
{
	ArrayList<Shadow> newShadows;
	ArrayList<Shadow> oldShadows;
	HashMap<Ship, Shadow> currentShadows;
	Random random;
	boolean fired = false;
	
	public static int RANDOM_MOVE_RADIUS = 200;
	public static double SHOOT_PROBABILITY = 0.2;
	
	@Override
	public void initialize()
	{
		newShadows = new ArrayList<Shadow>();
		oldShadows = new ArrayList<Shadow>();
		currentShadows = new HashMap<Ship, Shadow>();
		random = new Random();
	}
	
	@Override
	public void shutDown()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public HashMap<String, SpacewarAction> getAction(Toroidal2DPhysics space, ArrayList<Ship> ships)
	{
		HashMap<String, SpacewarAction> randomActions = new HashMap<String, SpacewarAction>();
		
		for (Ship ship : ships)
		{
			SpacewarAction current = ship.getCurrentAction();
			
			if (current != null && current.isMovementFinished())
			{
				oldShadows.add(currentShadows.get(ship));
			}
			
			if (current == null || current.isMovementFinished())
			{
				Position currentPosition = ship.getPosition();
				Position newGoal = space.getRandomFreeLocationInRegion(random, Ship.SHIP_RADIUS, (int) currentPosition.getX(), (int) currentPosition.getY(), RANDOM_MOVE_RADIUS);
				MoveAction newAction = null;
				newAction = new MoveAction(space, currentPosition, newGoal);
				// System.out.println("Ship is at " + currentPosition + " and goal is " + newGoal);
				Shadow shadow = new CircleShadow(3, getTeamColor(), newGoal);
				newShadows.add(shadow);
				currentShadows.put(ship, shadow);
				// Vector2D shortVec = space.findShortestDistanceVector(currentPosition, newGoal);
				// LineShadow lineShadow = new LineShadow(currentPosition, newGoal, shortVec);
				// newShadows.add(lineShadow);
				randomActions.put(ship.getId(), newAction);
			}
			else
			{
				randomActions.put(ship.getId(), ship.getCurrentAction());
			}
			
			if (current != null && random.nextDouble() < SHOOT_PROBABILITY)
			{
				System.out.println("Firing!");
				current.setWeapon(ship.getNewBullet());
			}
			// if (current != null && !fired) {
			// System.out.println("Ship is at " + ship.getPosition());
			// Bullet bullet = ship.getNewBullet();
			// System.out.println("Bullet is at " + bullet.getPosition());
			// current.setWeapon(bullet);
			// fired = true;
			// }
			
		}
		
		return randomActions;
		
	}
	
	@Override
	public void endAction(Toroidal2DPhysics space, ArrayList<Ship> ships)
	{
		newShadows.clear();
	}
	
	@Override
	public ArrayList<Shadow> getNewShadows()
	{
		return newShadows;
	}
	
	@Override
	public ArrayList<Shadow> getOldShadows()
	{
		if (oldShadows.size() > 0)
		{
			ArrayList<Shadow> shadows = new ArrayList<Shadow>(oldShadows);
			oldShadows.clear();
			return shadows;
		}
		else
		{
			return null;
		}
	}
	
}
