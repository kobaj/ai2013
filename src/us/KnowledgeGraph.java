package grif1252;

import java.util.ArrayList;
import spacewar2.simulator.Toroidal2DPhysics;
import spacewar2.simulator.Toroidal2DPhysics;
import spacewar2.utilities.Position;
import spacewar2.utilities.Vector2D;
import spacewar2.objects.*;

// A directed path from a to b with no properties
class Relation{
	
	// A and B - the edges this relation connects
	protected SpacewarObject a;
	protected SpacewarObject b;
	
	// make a relation between A and B if possible. This is the base class so it is never possible
	public static Relation make(SpacewarObject a, SpacewarObject b, Toroidal2DPhysics space){
		return null;
	}
	
	// constructor
	public Relation(SpacewarObject a, SpacewarObject b){
		this.a = a;
		this.b = b;
	}
	
	// setters and getters for A
	public SpacewarObject A(){
		return a;
	}
	public void A(SpacewarObject a){
		this.a = a ;
	}
	
	// setters and getters for B
	public SpacewarObject B(){
		return b;
	}
	public void B(SpacewarObject b){
		this.b = b ;
	}
	
}

// An example of a relation with some properties
class ApproachingCurrentPosition extends Relation{

	// make the relation if a will approximately reach B's location in some number of steps
	public static ApproachingCurrentPosition make(SpacewarObject a, SpacewarObject b, Toroidal2DPhysics space){

		// set constants
		int radius = 50;
		int steps = 50;
		int resolution = 1;
		
		// get velocity vector and position for A
		Vector2D v = a.getPosition().getTranslationalVelocity();
		Position futurePosition = a.getPosition();
		
		// see if A will arrive at current position of B within given number  of frames
		int i = 0;
		while(i < steps){
			
			// return relation if approximate collision found at this step
			if(space.findShortestDistance(b.getPosition(), futurePosition) < radius){
				ApproachingCurrentPosition r = new ApproachingCurrentPosition(a,b);
				r.steps(steps);
				return r;
			}
			
			// increment step and future position
			futurePosition.setX(futurePosition.getX() + (v.getXValue() * resolution));
			futurePosition.setY(futurePosition.getY() + (v.getYValue() * resolution));
			i += resolution;
			
		}
		return null ;
		
		
	}

	// the number of steps before B occupies the approximate current position of A
	protected int steps;
	public int steps(){
		return steps;
	}
	public void steps(int steps){
		this.steps = steps;
	}
	
	
	// the constructor
	public ApproachingCurrentPosition(SpacewarObject a, SpacewarObject b) {
		super(a, b);
		// compute timeBeforeThere based on position and velocity and set the local variable
		
	}


	
	
}

public class KnowledgeGraph{

	
	protected ArrayList<SpacewarObject> vertices;
	protected ArrayList<Relation> edges;
	
	public KnowledgeGraph(Toroidal2DPhysics space){
		
		edges = new ArrayList<Relation>();
		vertices = new ArrayList<SpacewarObject>();
		
		vertices.addAll(space.getAsteroids());
		vertices.addAll(space.getBases());
		vertices.addAll(space.getBeacons());
		vertices.addAll(space.getShips());
		vertices.addAll(space.getWeapons());
		
		for(SpacewarObject a : vertices){
			for(SpacewarObject b : vertices){
				if(!a.equals(b)){
					
					// Relate ships and asteroids where one is going towards the other
					Relation r = ApproachingCurrentPosition.make(a, b, space);
					if(r != null && ((a.getClass().isAssignableFrom(Asteroid.class) && b.getClass().isAssignableFrom(Ship.class)) || (b.getClass().isAssignableFrom(Asteroid.class) && a.getClass().isAssignableFrom(Ship.class)))){
						edges.add(r);
					}
				}
			}
		}
		
	}
	
	// get the list of directed relations between two SpacewarObjects
	public ArrayList<Relation> getRelations(SpacewarObject a,SpacewarObject b){
		
		ArrayList<Relation> result = new ArrayList<Relation>();
		
		for(Relation e : edges){
			if(e.A().equals(a) && e.B().equals(b)){
				result.add(e);
			}
		}
		
		return result;
	}
	
	// get the list of relations involving a particular object
	public ArrayList<Relation> getRelations(SpacewarObject a){
		
		ArrayList<Relation> result = new ArrayList<Relation>();
		
		for(Relation e : edges){
			if(e.A().equals(a) || e.B().equals(a)){
				result.add(e);
			}
		}
		
		return result;
	}
	
}
