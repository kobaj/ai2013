package grif1252;

import grif1252.Node.NodeType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;

import javax.swing.tree.DefaultMutableTreeNode;

import spacewar2.actions.MoveAction;
import spacewar2.actions.SpacewarAction;
import spacewar2.clients.TeamClient;
import spacewar2.objects.Asteroid;
import spacewar2.objects.Ship;
import spacewar2.shadows.CircleShadow;
import spacewar2.shadows.LineShadow;
import spacewar2.shadows.Shadow;
import spacewar2.simulator.Toroidal2DPhysics;
import spacewar2.utilities.Position;
import spacewar2.utilities.Vector2D;

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
			
			Toroidal2DPhysics local_space = space.deepClone();
			// calculate our matrix
			// AdjacencyMatrixGraph<Position> temp = calculateConnections(space, min_radius, calculateNodesHalfAsteroids(space, min_radius, ships.get(0)));
			AdjacencyMatrixGraph<Position> temp = calculateConnections(local_space, min_radius, calculateNodesRandom(local_space, min_radius, ships.get(0), random));
			
			// find the fastest way through it.
			ArrayList<Node<Position>> test = AStar(space, temp, temp.getNodes().get(0));
			
			// uncomment to draw lines
			//drawLines(space, temp, min_radius);
			drawSolution(space, test);
			
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
	
	private void drawSolution(Toroidal2DPhysics space, ArrayList<Node<Position>> nodes)
	{
		for (Node<Position> n : nodes)
		{
			newShadows.add(new CircleShadow(1, Color.orange, n.position));
			if(n.child != null)
			newShadows.add(new LineShadow(n.position, n.child.position, new Vector2D(n.position.getX() - n.child.position.getX(), n.child.position.getY() - n.position.getY())));
		}
	}
	
	private void drawLines(Toroidal2DPhysics space, AdjacencyMatrixGraph<Position> temp, double min_radius)
	{
		ArrayList<Node<Position>> nodes = temp.getNodes();
		
		for (Node<Position> n : nodes)
			newShadows.add(new CircleShadow(1, Color.orange, n.position));
		
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
						double divisors = Math.ceil(space.findShortestDistance(n1.position, n2.position) / (min_radius * 2));
						for (int j = 0; j < divisors; j++)
						{
							double next_x = lerp(0, divisors, j, n1.position.getX(), n2.position.getX());
							double next_y = lerp(0, divisors, j, n1.position.getY(), n2.position.getY());
							
							// newShadows.add(new CircleShadow((int) min_radius, getTeamColor(), new Position(next_x, next_y)));
						}
						
						newShadows.add(new LineShadow(n1.position, n2.position, new Vector2D(n2.position.getX() - n1.position.getX(), n2.position.getY() - n1.position.getY())));
						
						i++;
					}
					else
						break_me = true;
			}
			visited_nodes.add(n1);
		}
		
	}
	
	private ArrayList<Node<Position>> AStar(Toroidal2DPhysics space, AdjacencyMatrixGraph<Position> graph, Node<Position> start)
	{
		//have to stick the graph into a tree starting from start
		
		ArrayList<Node<Position>> closed_visited = new ArrayList<Node<Position>>();
		PriorityQueue<Node<Position>> fringe = new PriorityQueue<Node<Position>>(10, new NodeComparator());

		ArrayList<Node<Position>> children = graph.getChildren(start);
		for(Node<Position> child: children)
		{
			child.root_to_n_distance = space.findShortestDistance(start.position, child.position);
			fringe.add(child);
		}
		
		while(true)
		{
			if(fringe.isEmpty())
				return null;
			
			Node<Position> next = fringe.poll();
			if(next.node_type == NodeType.goal)
			{
			
				return next.getPathToRoot();
			}
			else
			{
				closed_visited.add(next);
				ArrayList<Node<Position>> sub_children = graph.getChildren(next);
				for(Node<Position> child: sub_children)
				{
					child.root_to_n_distance = next.root_to_n_distance + space.findShortestDistance(child.position, next.position);
					
					boolean inserted = false;
					
					//already there
					for(Node<Position> p: fringe)
					{
						if(p.matrix_id == child.matrix_id)
						{
							if(p.fn() > child.fn())
							{
								p.root_to_n_distance = child.root_to_n_distance;
								p.parent = next;
								next.child = p;
							
							}
							inserted = true;
							break;
						}
					}
					
					for(Node<Position> p: closed_visited)
					{
						if(p.matrix_id == child.matrix_id)
							inserted = true;
					}
					
					//add to fringe
					if(!inserted)
						fringe.add(child);
				}
			}
		}
	}
	
	private ArrayList<Node<Position>> calculateNodesRandom(Toroidal2DPhysics space, double min_distance, Ship ship, Random random)
	{
		ArrayList<Node<Position>> nodes = new ArrayList<Node<Position>>();
		
		// add about 400 random spots
		
		Position goal = addStartAndGoal(space, ship, nodes);
		
		int i = 2;
		for (; i < 40; i++)
		{
			Position open = space.getRandomFreeLocation(random, (int) min_distance);
			nodes.add(new Node<Position>(open, i, NodeType.regular, space.findShortestDistance(open, goal) / 2.0));
		}
		
		return nodes;
	}
	
	private ArrayList<Node<Position>> calculateNodesHalfAsteroids(Toroidal2DPhysics space, double min_distance, Ship ship)
	{
		ArrayList<Asteroid> asteroids = space.getAsteroids();
		ArrayList<Node<Position>> nodes = new ArrayList<Node<Position>>();
		
		ArrayList<Asteroid> visited_asteroids = new ArrayList<Asteroid>();
		
		Position goal = addStartAndGoal(space, ship, nodes);
		
		int i = 2;
		for (Asteroid a1 : asteroids)
		{
			for (Asteroid a2 : asteroids)
				if (!a1.equals(a2) && !visited_asteroids.contains(a2))
				{
					// not the same, find the distance between them
					double distance = space.findShortestDistance(a1.getPosition(), a2.getPosition()) - 2.0 * (a1.getRadius() + a2.getRadius());
					if (distance > min_distance)
					{
						// we can place a node
						Position this_position = get_half_way_point(a1.getPosition(), a2.getPosition());
						Node<Position> potential_location = new Node<Position>(this_position, i, NodeType.regular, space.findShortestDistance(this_position, goal) / 2.0);
						nodes.add(potential_location);
						i++;
					}
				}
			
			visited_asteroids.add(a1);
		}
		
		return nodes;
	}
	
	private Position addStartAndGoal(Toroidal2DPhysics space, Ship ship, ArrayList<Node<Position>> nodes)
	{
		int i = 0;
		
		// add one more node for source (the ship) and another for destination (shoot for the max asteroid at the moment)
		Asteroid goal = getMaxAsteroid(space);
		if (goal != null)
		{
			Node<Position> goal_node = new Node<Position>(goal.getPosition(), i, NodeType.goal, 0);
			goal_node.item = goal;
			nodes.add(goal_node);
			i++;
			
			space.removeObject(goal);
		}
		else
			goal = new Asteroid(new Position(0, 0), false, 1, false);
		
		Node<Position> start_node = new Node<Position>(ship.getPosition(), i, NodeType.start, space.findShortestDistance(ship.getPosition(), goal.getPosition()) / 2.0);
		start_node.item = ship;
		nodes.add(start_node);
		i++;
		
		// remove from space
		space.removeObject(ship);
		
		return goal.getPosition();
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
					double distance = space.findShortestDistance(n1.position, n2.position);
					
					// see if there is anything between n1 and n2 by lerping from
					// n1 to n2, checking positions inbetween.
					// ideally this will be replaced with a much better circle in
					// rectangle collision check one day.
					int divisors = (int) Math.ceil(distance / min_distance * 2.0);
					boolean collision = false;
					
					for (int j = 0; j < divisors; j++)
					{
						double next_x = lerp(0, divisors, j, n1.position.getX(), n2.position.getX());
						double next_y = lerp(0, divisors, j, n1.position.getY(), n2.position.getY());
						
						if (!space.isLocationFree(new Position(next_x, next_y), (int) (min_distance * 2.0)))
						{
							collision = true;
							break;
						}
					}
					
					// set if we can go between these nodes
					if (!collision)
						my_graph.setConnected(n1, n2, space.findShortestDistance(n1.position, n2.position));
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
