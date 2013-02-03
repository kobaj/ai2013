package grif1252;

import grif1252.Node.NodeType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Random;

import spacewar2.actions.MoveAction;
import spacewar2.actions.SpacewarAction;
import spacewar2.clients.TeamClient;
import spacewar2.objects.Asteroid;
import spacewar2.objects.Base;
import spacewar2.objects.Beacon;
import spacewar2.objects.Ship;
import spacewar2.objects.SpacewarObject;
import spacewar2.shadows.CircleShadow;
import spacewar2.shadows.LineShadow;
import spacewar2.shadows.Shadow;
import spacewar2.simulator.Toroidal2DPhysics;
import spacewar2.utilities.Position;
import spacewar2.utilities.Vector2D;

public class Project1Client extends TeamClient
{
	HashMap<Ship, String> ship_goals;
	HashMap<Ship, Position> local_goals;
	
	HashMap<String, ArrayList<Shadow>> managedShadows;
	ArrayList<Shadow> newShadows;
	ArrayList<Shadow> oldShadows;
	
	final public static int SUBGOAL_DISTANCE = 30;
	
	final public static int MONEY_RETURN = 500;
	final public static int BEACON_GET = 200;
	
	final public static int RANDOM_LOCATION = 200;
	public static int MAX_RANDOM_NODES = 10;
	
	final public static int MAX_ITERATIONS = 20;
	int current_iterations = 0;
	
	final public static double X_RES = 1024;
	final public static double Y_RES = 768;
	final public static double RES = 12.0;
	
	final public static int MAX_NUM_NODE_CONNECTIONS = 4;
	
	@Override
	public void initialize()
	{
		ship_goals = new HashMap<Ship, String>();
		local_goals = new HashMap<Ship, Position>();
		
		managedShadows = new HashMap<String, ArrayList<Shadow>>();
		newShadows = new ArrayList<Shadow>();
		oldShadows = new ArrayList<Shadow>();
	}
	
	@Override
	public void shutDown()
	{
		// TODO Auto-generated method stub
		
	}
	
	private void switchShadows()
	{
		oldShadows = new ArrayList<Shadow>(newShadows);
		newShadows.clear();
		
		Iterator<Entry<String, ArrayList<Shadow>>> it = managedShadows.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry<String, ArrayList<Shadow>> pairs = (Map.Entry<String, ArrayList<Shadow>>) it.next();
			
			ArrayList<Shadow> shadows = pairs.getValue();
			
			for (Shadow s : shadows)
				newShadows.add(s);
		}
	}
	
	@Override
	public HashMap<String, SpacewarAction> getAction(Toroidal2DPhysics space, ArrayList<Ship> ships)
	{
		// switch shadows
		switchShadows();
		
		HashMap<String, SpacewarAction> actions = new HashMap<String, SpacewarAction>();
		Toroidal2DPhysics local_space = space.deepClone();
		
		current_iterations--;
		
		for (Ship ship : ships)
		{
			SpacewarAction current = ship.getCurrentAction();
			
			// see if the map contains our goal anymore
			String arc_goal = ship_goals.get(ship);
			boolean goal_exists = false;
			if (arc_goal != null)
			{
				for (Asteroid as : local_space.getAsteroids())
					if (as.getPosition().toString().equals(arc_goal))
					{
						goal_exists = true;
					}
				
				for (Beacon be : local_space.getBeacons())
					if (be.getPosition().toString().equals(arc_goal))
					{
						goal_exists = true;
					}
			}
			
			// we're close enough. dont have to be perfect.
			Position sub_goal = local_goals.get(ship);
			if (sub_goal != null)
				if (local_space.findShortestDistance(ship.getPosition(), sub_goal) < this.SUBGOAL_DISTANCE)
				{
					// System.out.println("short circuited distance!");
					goal_exists = false;
				}
			
			// get next ship action
			if (current == null || current.isMovementFinished() || current_iterations <= 0 || !goal_exists)
			{
				current_iterations = MAX_ITERATIONS;
				
				ArrayList<Node<Position>> nodes; // all nodes
				AdjacencyMatrixGraph<Position> matrix_graph; // all connections between all nodes
				ArrayList<Node<Position>> fast_path; // fastest path through nodes
				
				int i = 0;
				while (true)
				{
					// calculate our matrix
					 nodes = calculateNodesHalfAsteroids(local_space, ship.getRadius(), ship);
					// nodes = calculateNodesRandom(local_space, ship.getRadius(), ship, random);
					//nodes = calculateNodesGrid(local_space, X_RES / RES, Y_RES / RES, ship);
					
					// make all connections
					matrix_graph = calculateSetConnections(local_space, ship.getRadius(), nodes, false, MAX_NUM_NODE_CONNECTIONS);
					// matrix_graph = calculateAllConnections(local_space, ship.getRadius(), nodes, false);
					
					// store our goal
					if (Asteroid.class.isAssignableFrom(matrix_graph.getNodes().get(0).item.getClass()))
						ship_goals.put(ship, Asteroid.class.cast(matrix_graph.getNodes().get(0).item).getPosition().toString());
					
					// find the fastest way through it
					fast_path = AStar(space, matrix_graph, matrix_graph.getNodes().get(1), false);
					
					if (SpacewarObject.class.isAssignableFrom(matrix_graph.getNodes().get(0).item.getClass()))
						local_space.removeObject((SpacewarObject) matrix_graph.getNodes().get(0).item);
					
					// move on
					i++;
					if (i > 10 || fast_path != null)
						break;
					else
					{
						// System.out.println("No path found, retrying: " + i);
						MAX_RANDOM_NODES++;
					}
				}
				
				// uncomment to draw lines
				ArrayList<Shadow> node_shadows = new ArrayList<Shadow>();
				// for(Node<Position> n: nodes)
				// drawNodesConnections(space, n, n, ship.getRadius(), node_shadows); // draw all nodes
				// drawLines(space, matrix_graph, 0, node_shadows); // draw all the lines connecting all nodes
				drawSolution(space, fast_path, ship.getRadius(), node_shadows); // draw the shortest path
				managedShadows.put(ship.getId() + "sources", node_shadows);
				
				// make the goals
				Position currentPosition = ship.getPosition();
				Position newGoal = (fast_path != null && fast_path.get(1) != null ? fast_path.get(1).position : space.getRandomFreeLocation(random, ship.getRadius())); // get next movement
				local_goals.put(ship, newGoal);
				
				// SpacewarAction newAction = new ConstantMoveAction(currentPosition, newGoal);//MoveAction(space, currentPosition, newGoal);
				SpacewarAction newAction = new MoveAction(space, currentPosition, newGoal);
				
				// System.out.println("Ship is at " + currentPosition + " and goal is " + newGoal);
				Shadow shadow = new CircleShadow(3, getTeamColor(), newGoal);
				ArrayList<Shadow> test = new ArrayList<Shadow>();
				test.add(shadow);
				managedShadows.put(ship.getId() + "destination", test);
				
				// finally
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
	
	private void drawSolution(Toroidal2DPhysics space, ArrayList<Node<Position>> nodes, double min_radius, ArrayList<Shadow> node_shadows)
	{
		if (nodes == null)
			return;
		
		int i = 0;
		for (Node<Position> n : nodes)
		{
			if (n.parent != null)
				drawNodesConnections(space, n.parent, n, min_radius, node_shadows);
			
			i++;
		}
	}
	
	private void drawNodesConnections(Toroidal2DPhysics space, Node<Position> a, Node<Position> b, double min_radius, ArrayList<Shadow> node_shadows)
	{
		node_shadows.add(new CircleShadow(1, Color.orange, a.position));
		
		if (min_radius > 0)
		{
			double divisors = Math.ceil(space.findShortestDistance(a.position, b.position) / (min_radius * 2));
			for (int j = 0; j < divisors; j++)
			{
				double next_x = lerp(0, divisors, j, a.position.getX(), b.position.getX());
				double next_y = lerp(0, divisors, j, a.position.getY(), b.position.getY());
				
				node_shadows.add(new CircleShadow((int) (min_radius / 5.0), getTeamColor(), new Position(next_x, next_y)));
			}
		}
		else
		{
			node_shadows.add(new LineShadow(b.position, a.position, new Vector2D(a.position.getX() - b.position.getX(), a.position.getY() - b.position.getY())));
		}
	}
	
	private void drawLines(Toroidal2DPhysics space, AdjacencyMatrixGraph<Position> temp, double min_radius, ArrayList<Shadow> node_shadows)
	{
		ArrayList<Node<Position>> nodes = temp.getNodes();
		
		ArrayList<Node<Position>> visited_nodes = new ArrayList<Node<Position>>();
		
		int e = 0;
		
		for (Node<Position> n1 : nodes)
		{
			for (Node<Position> n2 : nodes)
			{
				if (!n1.equals(n2) && !visited_nodes.contains(n2) && temp.getConnected(n1, n2))
				{
					drawNodesConnections(space, n1, n2, min_radius, node_shadows);
					e++;
				}
			}
			visited_nodes.add(n1);
		}
		
	}
	
	private ArrayList<Node<Position>> AStar(Toroidal2DPhysics space, AdjacencyMatrixGraph<Position> graph, Node<Position> start, boolean output)
	{
		// have to stick the graph into a tree starting from start
		
		ArrayList<Node<Position>> closed_visited = new ArrayList<Node<Position>>();
		closed_visited.add(start);
		PriorityQueue<Node<Position>> fringe = new PriorityQueue<Node<Position>>(10, new NodeComparator<Position>());
		
		if (output)
			System.out.println("starting at: " + start.toString());
		
		ArrayList<Node<Position>> children = graph.getChildren(start);
		for (Node<Position> child : children)
		{
			child.root_to_n_distance = space.findShortestDistance(start.position, child.position);
			fringe.add(child.copy());
			if (output)
				System.out.println("child: " + child.toString());
		}
		
		while (true)
		{
			if (output)
				System.out.println("doing a loop");
			
			if (fringe.isEmpty())
				return null;
			
			Node<Position> next = fringe.poll();
			
			if (output)
				System.out.println("next is at: " + next.toString());
			
			if (next.node_type == NodeType.goal)
			{
				if (output)
					System.out.println("found the goal: " + next.toString());
				return next.getPathToRoot();
			}
			else
			{
				closed_visited.add(next.copy());
				ArrayList<Node<Position>> sub_children = graph.getChildren(next);
				for (Node<Position> child : sub_children)
				{
					child.root_to_n_distance = next.root_to_n_distance + space.findShortestDistance(child.position, next.position);
					
					if (output)
						System.out.println("child: " + child.toString());
					
					boolean inserted = false;
					
					// or visited
					for (Node<Position> p : closed_visited)
					{
						if (p.matrix_id == child.matrix_id)
						{
							if (output)
								System.out.println("already visited : " + child.toString());
							inserted = true;
							break;
						}
					}
					
					// already there
					if (!inserted)
						for (Node<Position> p : fringe)
							if (p.matrix_id == child.matrix_id)
							{
								if (output)
									System.out.println("already fringed : " + child.toString() + (new Formatter()).format("%n  previous child was: ") + p.toString());
								
								if (p.fn() > child.fn())
								{
									p.root_to_n_distance = child.root_to_n_distance;
									p.parent = next;
									
									if (output)
										System.out.println("  this child is better : " + child.toString());
									
								}
								inserted = true;
								break;
							}
					
					// add to fringe
					if (!inserted)
						fringe.add(child);
				}
			}
		}
	}
	
	private ArrayList<Node<Position>> calculateNodesGrid(Toroidal2DPhysics space, double x_adder, double y_adder, Ship ship)
	{
		ArrayList<Node<Position>> nodes = new ArrayList<Node<Position>>();
		
		Position goal = addStartAndGoal(space, ship, nodes);
		
		int e = 2;
		for (int i = 0; i < X_RES; i += x_adder)
			for (int j = 0; j < Y_RES; j += y_adder)
			{
				Position position = new Position(i, j);
				
				// find the distance to player
				// dont add it if the player is really close
				if(space.findShortestDistance(position, ship.getPosition()) > SUBGOAL_DISTANCE)
					nodes.add(new Node<Position>(position, e, NodeType.regular, space.findShortestDistance(position, goal)));
				
				e++;
			}
		
		return nodes;
	}
	
	private ArrayList<Node<Position>> calculateNodesRandom(Toroidal2DPhysics space, double min_distance, Ship ship, Random random)
	{
		ArrayList<Node<Position>> nodes = new ArrayList<Node<Position>>();
		
		Position goal = addStartAndGoal(space, ship, nodes);
		
		for (int i = 2; i < MAX_RANDOM_NODES; i++)
		{
			Position open = space.getRandomFreeLocation(random, (int) min_distance);
			nodes.add(new Node<Position>(open, i, NodeType.regular, space.findShortestDistance(open, goal)));
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
						Node<Position> potential_location = new Node<Position>(this_position, i, NodeType.regular, space.findShortestDistance(this_position, goal));
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
		// we add two nodes, one for start, one for destination
		
		int i = 0;
		
		// this is our intelligent search system
		SpacewarObject goal_max_asteroid = getMaxAsteroid(space);
		SpacewarObject goal_close_asteroid = getClosestAsteroid(space, ship);
		SpacewarObject base = getMyBase(space, ship);
		SpacewarObject goal_close_beacon = getClosestBeacon(space, ship);
		
		HashMap<Double, SpacewarObject> relations = new HashMap<Double, SpacewarObject>();
		PriorityQueue<Double> intelligent_select = new PriorityQueue<Double>(4);
		if (goal_close_asteroid != null)
		{
			double distance = space.findShortestDistance(goal_close_asteroid.getPosition(), ship.getPosition());
			intelligent_select.add(distance);
			relations.put(distance, goal_close_asteroid);
		}
		if (base != null)
		{
			double distance = space.findShortestDistance(base.getPosition(), ship.getPosition());
			intelligent_select.add(distance);
			relations.put(distance, base);
		}
		if (goal_close_beacon != null)
		{
			double distance = space.findShortestDistance(goal_close_beacon.getPosition(), ship.getPosition());
			intelligent_select.add(distance);
			relations.put(distance, goal_close_beacon);
		}
		if (goal_max_asteroid != null)
		{
			double distance = space.findShortestDistance(goal_max_asteroid.getPosition(), ship.getPosition());
			intelligent_select.add(distance);
			relations.put(distance, goal_max_asteroid);
		}
		
		SpacewarObject goal = null;
		
		while (!intelligent_select.isEmpty() || goal == null)
		{
			double closest_goal = 0;
			try
			{
				closest_goal = intelligent_select.poll();
			}
			catch (NullPointerException e)
			{
				break;
			}
			
			SpacewarObject closest_object = relations.get(closest_goal);
			
			// return if we have some money
			if (Base.class.isAssignableFrom(closest_object.getClass()) && ship.getMoney() > this.MONEY_RETURN)
			{
				// System.out.println("picking the base");
				goal = closest_object;
			}
			else if (Beacon.class.isAssignableFrom(closest_object.getClass()) && ship.getEnergy() < this.BEACON_GET)
			{
				// System.out.println("picking the beacon");
				goal = closest_object;
			}
			else if (Asteroid.class.isAssignableFrom(closest_object.getClass()))
			{
				// System.out.println("picking the asteroid " + closest_goal + " money: " + ((Asteroid) closest_object).getMoney());
				goal = closest_object;
			}
		}
		
		// and just a catch all
		if (goal == null)
			goal = new Beacon(space.getRandomFreeLocationInRegion(random, ship.getRadius(), (int) ship.getPosition().getX(), (int) ship.getPosition().getY(), RANDOM_LOCATION), random.nextInt());
		
		Node<Position> goal_node = new Node<Position>(goal.getPosition(), i, NodeType.goal, 0);
		goal_node.item = goal;
		nodes.add(goal_node);
		i++;
		
		// next
		Node<Position> start_node = new Node<Position>(ship.getPosition(), i, NodeType.start, space.findShortestDistance(ship.getPosition(), goal.getPosition()));
		start_node.item = ship;
		nodes.add(start_node);
		i++;
		
		// remove from space so we can do collision detection
		space.removeObject(ship);
		space.removeObject(goal);
		
		return goal.getPosition();
	}
	
	private AdjacencyMatrixGraph<Position> calculateSetConnections(Toroidal2DPhysics space, double min_distance, ArrayList<Node<Position>> nodes, boolean output, int connections)
	{
		if (output)
		{
			System.out.println("******************************************************");
			System.out.println("*               Calculating Connections              *");
			System.out.println("******************************************************");
		}
		
		// remove beacons as they are actually good
		Toroidal2DPhysics local_space = space.deepClone();
		local_space.getBases().clear();
		local_space.getBeacons().clear();
		
		ArrayList<Node<Position>> visited_nodes = new ArrayList<Node<Position>>();
		
		// now we have our nodes, lets see which ones touch
		AdjacencyMatrixGraph<Position> my_graph = new AdjacencyMatrixGraph<Position>(nodes.size() + 1);
		my_graph.storeNodes(nodes);
		
		// walk through the nodes and find out which ones can touch
		for (Node<Position> n1 : nodes)
		{
			// before immediately walking over ALL nodes
			// we can only search for the x closest amount
			// and walk through them much more efficiently.
			PriorityQueue<Double> distances = new PriorityQueue<Double>(nodes.size());
			HashMap<Double, Node<Position>> distance_relations = new HashMap<Double, Node<Position>>();
			
			int y = 0;
			for (Node<Position> n2 : nodes)
				if (n1.matrix_id != n2.matrix_id && !visited_nodes.contains(n2))
				{
					double distance = local_space.findShortestDistance(n1.position, n2.position);
					distances.add(distance);
					distance_relations.put(distance, n2);
					y++;
				}
			
			if (connections > y)
				connections = y;
			
			double[] count_distances = new double[connections];
			for (int i = 0; i < connections; i++)
				count_distances[i] = distances.poll();
			
			// now walk through the closest amount q we have found.
			for (int q = 0; q < connections; q++)
			{
				Node<Position> n2 = distance_relations.get(count_distances[q]);
				
				if (n1.matrix_id != n2.matrix_id && !visited_nodes.contains(n2))
				{
					double distance = local_space.findShortestDistance(n1.position, n2.position);
					
					if (output)
					{
						System.out.println("Node A: " + n1.matrix_id);
						System.out.println("Node B: " + n2.matrix_id);
						System.out.println("Distance: " + distance);
					}
					
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
						
						if (!local_space.isLocationFree(new Position(next_x, next_y), (int) (min_distance * 2.0)))
						{
							if (output)
								System.out.println("                                                                  Collision");
							
							collision = true;
							break;
						}
					}
					
					// set if we can go between these nodes
					if (!collision)
					{
						my_graph.setConnected(n1, n2, distance);
						
						if (output)
							System.out.println("                                                                  Stored: " + distance);
					}
				}
			}
			
			visited_nodes.add(n1);
		}
		
		return my_graph;
	}
	
	private AdjacencyMatrixGraph<Position> calculateAllConnections(Toroidal2DPhysics space, double min_distance, ArrayList<Node<Position>> nodes, boolean output)
	{
		return calculateSetConnections(space, min_distance, nodes, output, 1000);
	}
	
	private Base getMyBase(Toroidal2DPhysics space, Ship ship)
	{
		for (Base base : space.getBases())
			if (base.getTeamName().equalsIgnoreCase(ship.getTeamName()))
				return base;
		
		return null;
	}
	
	private Asteroid getClosestAsteroid(Toroidal2DPhysics space, Ship ship)
	{
		Asteroid close = null;
		double close_distance = Double.MAX_VALUE;
		for (Asteroid as : space.getAsteroids())
			if (as.isMineable())
			{
				double local_distance = space.findShortestDistance(ship.getPosition(), as.getPosition());
				if (close == null || local_distance < close_distance)
				{
					close_distance = local_distance;
					close = as;
				}
			}
		
		return close;
	}
	
	private Asteroid getMaxAsteroid(Toroidal2DPhysics space)
	{
		ArrayList<Asteroid> asteroids = space.getAsteroids();
		double max = Double.MIN_VALUE;
		Asteroid max_asteroid = null;
		for (Asteroid a : asteroids)
			if (a.getMoney() > max && a.isMineable())
			{
				max = a.getMoney();
				max_asteroid = a;
			}
		
		return max_asteroid;
	}
	
	private Beacon getClosestBeacon(Toroidal2DPhysics space, Ship ship)
	{
		ArrayList<Beacon> beacons = space.getBeacons();
		
		double min = Double.MAX_VALUE;
		Beacon min_beacon = null;
		for (Beacon b : beacons)
		{
			double distance = space.findShortestDistance(b.getPosition(), ship.getPosition());
			if (distance < min)
			{
				min_beacon = b;
				min = distance;
			}
		}
		
		return min_beacon;
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
		// newShadows.clear();
	}
	
	@Override
	public ArrayList<Shadow> getNewShadows()
	{
		ArrayList<Shadow> shadows = new ArrayList<Shadow>(newShadows);
		return shadows;
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
