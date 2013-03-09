package grif1252;

import java.util.ArrayList;
import spacewar2.simulator.Toroidal2DPhysics;
import spacewar2.simulator.Toroidal2DPhysics;
import spacewar2.utilities.Position;
import spacewar2.objects.*;

// A directed path from a to b with no properties
abstract class Relation{
	
	// A and B - the edges this relation connects
	protected SpacewarObject a;
	protected SpacewarObject b;
	
	public abstract Relation makeRelation(SpacewarObject a, SpacewarObject b);
	
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

	public static ApproachingCurrentPosition makeRelation(SpacewarObject a, SpacewarObject b){
		// if the conditions are right, return a relation between a and b with the appropriate
		// properties by calling the constructor on a and b. otherwise return null.
		
		// obviously this is wrong. Would it be possible to just make the constructor so that 
		// it checks the input and throws an exception if there is no such relation?
		
	}

	// the number of steps before B occupies the approximate current position of A
	protected double timeBeforeThere;
	
	// the constructor
	public ApproachingCurrentPosition(SpacewarObject a, SpacewarObject b) {
		super(a, b);
		// compute timeBeforeThere based on position and velocity and set the local variable
		
	}


	
	
}

public class KnowledgeGraph{

	
	ArrayList<SpacewarObject> vertices;
	ArrayList<Relation> edges;
	
	public KnowledgeGraph(Toroidal2DPhysics space){
		// fill ine vertices and edges
	}
	
	// get the list of relations between two SpacewarObjects
	public ArrayList<Relation> getRelations(SpacewarObject a,SpacewarObject b){
		
		ArrayList<Relation> result = new ArrayList<Relation>();
		
		for(Relation e : edges){
			if(e.A().equals(a) && e.B().equals(b)){
				result.add(e);
			}
		}
		
		return edges;
	}
	
}
