package grif1252;

import spacewar2.actions.SpacewarAction;
import spacewar2.objects.Ship;
import spacewar2.simulator.Toroidal2DPhysics;
import spacewar2.utilities.Movement;
import spacewar2.utilities.Position;
import spacewar2.utilities.Vector2D;

public class ConstantMoveAction extends SpacewarAction
{
	
	private Position start;
	private Position destination;
	
	private boolean finished = false;
	
	public ConstantMoveAction(Position start, Position destination)
	{
		this.start = start;
		this.destination = destination;
	}
	
	@Override
	public Movement getMovement(Toroidal2DPhysics space, Ship ship)
	{
		Movement movement = new Movement();
		
		if (isMovementFinished())
			return movement;
		
		movement.setTranslationalAcceleration(new Vector2D(destination.getX() - ship.getPosition().getX(), -destination.getY() + ship.getPosition().getY()));
		
		if(space.findShortestDistance(ship.getPosition(), destination) < 5)
			finished = true;
		
		return movement;
	}
	
	@Override
	public boolean isMovementFinished()
	{
		return finished;
	}
	
}
