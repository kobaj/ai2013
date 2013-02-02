package grif1252;

import grif1252.Node.NodeType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import spacewar2.actions.MoveAction;
import spacewar2.actions.SpacewarAction;
import spacewar2.clients.TeamClient;
import spacewar2.objects.Asteroid;
import spacewar2.objects.Ship;
import spacewar2.shadows.CircleShadow;
import spacewar2.shadows.Shadow;
import spacewar2.simulator.Toroidal2DPhysics;
import spacewar2.utilities.Position;

public class Project1Client extends TeamClient
{
	ArrayList<Shadow> newShadows;
	ArrayList<Shadow> oldShadows;
	HashMap<Ship, Shadow> currentShadows;
	Random random;
	
	public static int RANDOM_MOVE_RADIUS = 900;
	
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
	
	boolean once = false;
	
	@Override
	public HashMap<String, SpacewarAction> getAction(Toroidal2DPhysics space, ArrayList<Ship> ships)
	{
		HashMap<String, SpacewarAction> actions = new HashMap<String, SpacewarAction>();
		
		Toroidal2DPhysics.MAX_TRANSLATIONAL_VELOCITY = 0;
		
		// calculate our nodes and matrix
		if (!once)
		{
			// first off find our fattest ship (assuming ships can change shape)
			double min_radius = Double.MIN_VALUE;
			for (Ship ship : ships)
				if (ship.getRadius() > min_radius)
					min_radius = ship.getRadius();
			
			// calculate our matrix
			// AdjacencyMatrixGraph<Position> temp = calculateConnections(space, min_radius, calculateNodesHalfAsteroids(space, min_radius, ships.get(0)));
			AdjacencyMatrixGraph<Position> temp = calculateConnections(space, min_radius, calculateNodesRandom(space, min_radius, ships.get(0), random));
			
			// find the fastest way through it.
			
			// uncomment to draw lines
			calculateLines(space, temp, min_radius);
			
			once = true;
		}
		
		for (Ship ship : ships)
		{
			SpacewarAction current = ship.getCurrentAction();
			
			// update shadows...?
			if (current != null && current.isMovementFinished())
			{
				oldShadows.add(currentShadows.get(ship));
			}
			
			// get next ship action
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
				actions.put(ship.getId(), newAction);
				
			}
			else
			{
				// current ship action
				actions.put(ship.getId(), ship.getCurrentAction());
			}
		}
		
		return actions;
	}
	
	private void calculateLines(Toroidal2DPhysics space, AdjacencyMatrixGraph<Position> temp, double min_radius)
	{
		ArrayList<Node<Position>> nodes = temp.getNodes();
		
		for (Node<Position> n : nodes)
			newShadows.add(new CircleShadow(1, Color.orange, n.item));
		
		int i = 0;
		int max = 1000000;
		ArrayList<Node<Position>> visited_nodes = new ArrayList<Node<Position>>();
		
		boolean break_me = false;
		for (Node<Position> n1 : nodes)
		{
			if (break_me)
				break;
			
			for (Node<Position> n2 : nodes)
			{
				if (break_me)
					break;
				
				if (!n1.equals(n2) && !visited_nodes.contains(n2) && temp.getConnected(n1, n2))
					if (i < max)
					{
						// stupid way
						double divisors = Math.ceil(space.findShortestDistance(n1.item, n2.item) / min_radius);
						for (int j = 0; j < divisors; j++)
						{
							double next_x = lerp(0, divisors, j, n1.item.getX(), n2.item.getX());
							double next_y = lerp(0, divisors, j, n1.item.getY(), n2.item.getY());
							
							newShadows.add(new CircleShadow((int) min_radius, getTeamColor(), new Position(next_x, next_y)));
						}
						
						// newShadows.add(new LineShadow(n1.item, n2.item, new Vector2D(n2.item.getX() - n1.item.getX(), n2.item.getY() - n1.item.getY())));
						
						i++;
					}
					else
						break_me = true;
			}
			visited_nodes.add(n1);
		}
		
	}
	
	private ArrayList<Node<Position>> calculateNodesRandom(Toroidal2DPhysics space, double min_distance, Ship ship, Random random)
	{
		ArrayList<Node<Position>> nodes = new ArrayList<Node<Position>>();
		
		// add about 400 random spots
		
		int i = 0;
		for (; i < 10; i++)
		{
			Position open = space.getRandomFreeLocation(random, (int) min_distance);
			nodes.add(new Node<Position>(open, i, NodeType.regular));
		}
		
		// add one more node for source (the ship) and another for destination (shoot for the max asteroid at the moment)
		nodes.add(new Node<Position>(ship.getPosition(), i, NodeType.start));
		i++;
		Asteroid goal = getMaxAsteroid(space);
		if (goal != null)
		{
			nodes.add(new Node<Position>(goal.getPosition(), i, NodeType.goal));
			i++;
		}
		
		return nodes;
	}
	
	private ArrayList<Node<Position>> calculateNodesHalfAsteroids(Toroidal2DPhysics space, double min_distance, Ship ship)
	{
		ArrayList<Asteroid> asteroids = space.getAsteroids();
		ArrayList<Node<Position>> nodes = new ArrayList<Node<Position>>();
		
		ArrayList<Asteroid> visited_asteroids = new ArrayList<Asteroid>();
		
		int i = 0;
		for (Asteroid a1 : asteroids)
		{
			for (Asteroid a2 : asteroids)
				if (!a1.equals(a2) && !visited_asteroids.contains(a2))
				{
					// not the same, find the distance between them
					double distance = space.findShortestDistance(a1.getPosition(), a2.getPosition()) - (a1.getRadius() + a2.getRadius());
					if (distance > min_distance)
					{
						// we can place a node
						Node<Position> potential_location = new Node<Position>(get_half_way_point(a1.getPosition(), a2.getPosition()), i, NodeType.regular);
						nodes.add(potential_location);
						i++;
					}
				}
			
			visited_asteroids.add(a1);
		}
		
		// add one more node for source (the ship) and another for destination (shoot for the max asteroid at the moment)
		nodes.add(new Node<Position>(ship.getPosition(), i, NodeType.start));
		i++;
		Asteroid goal = getMaxAsteroid(space);
		if (goal != null)
		{
			nodes.add(new Node<Position>(goal.getPosition(), i, NodeType.goal));
			i++;
		}
		
		return nodes;
	}
	
	private Asteroid getMaxAsteroid(Toroidal2DPhysics space)
	{
		ArrayList<Asteroid> asteroids = space.getAsteroids();
		double max = Integer.MIN_VALUE;
		Asteroid max_asteroid = null;
		for (Asteroid a : asteroids)
			if (a.getMoney() > max && a.isMineable())
			{
				max = a.getMoney();
				max_asteroid = a;
			}
		
		// hope max_asteroid is not null...
		return max_asteroid;
	}
	
	private AdjacencyMatrixGraph<Position> calculateConnections(Toroidal2DPhysics space, double min_distance, ArrayList<Node<Position>> nodes)
	{
		ArrayList<Node<Position>> visited_nodes = new ArrayList<Node<Position>>();
		
		// now we have our nodes, lets see which ones touch
		AdjacencyMatrixGraph<Position> my_graph = new AdjacencyMatrixGraph<Position>(nodes.size());
		my_graph.storeNodes(nodes);
		
		// walk through the nodes and find out which ones can touch
		for (Node<Position> n1 : nodes)
		{
			for (Node<Position> n2 : nodes)
			{
				if (n1.matrix_id != n2.matrix_id && !visited_nodes.contains(n2))
				{
					double distance = space.findShortestDistance(n1.item, n2.item);
					
					// see if there is anything between n1 and n2 by lerping from
					// n1 to n2, checking positions inbetween.
					// ideally this will be replaced with a much better circle in 
					// rectangle collision check one day.
					int divisors = (int) Math.ceil(distance / min_distance);
					boolean collision = false;
					
					int max = divisors;
					int start = 0;
					
					// special care for start ship and end goal
					if (n1.node_type == NodeType.start)
					{
						start += 3;
						if (start > max)
							start = max;
					}
					if (n1.node_type == NodeType.goal)
					{
						max -= 3;
						if (max < 0)
							max = 0;
					}
					
					for (int j = start; j < max; j++)
					{
						double next_x = lerp(0, divisors, j, n1.item.getX(), n2.item.getX());
						double next_y = lerp(0, divisors, j, n1.item.getY(), n2.item.getY());
						
						if (!space.isLocationFree(new Position(next_x, next_y), (int) (min_distance * 2.0)))
						{
							collision = true;
							break;
						}
					}
					
					// set if we can go between these nodes
					my_graph.setConnected(n1, n2, !collision);
				}
			}
			
			visited_nodes.add(n1);
		}
		
		return my_graph;
	}
	
	private double lerp(double min_x, double max_x, double between, double min_y, double max_y)
	{
		double numerator1 = min_y * (between - max_x);
		double denominator1 = (min_x - max_x);
		double numerator2 = max_y * (between - min_x);
		double denominator2 = (max_x - min_x);
		
		double final_value = numerator1 / denominator1 + numerator2 / denominator2;
		
		return final_value;
	}
	
	private Position get_half_way_point(Position a, Position b)
	{
		return new Position(((a.getX() + b.getX()) / 2.0), ((a.getY() + b.getY()) / 2.0));
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
