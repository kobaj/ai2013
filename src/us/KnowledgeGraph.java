package grif1252;

import java.util.ArrayList;
import spacewar2.simulator.Toroidal2DPhysics;
import spacewar2.simulator.Toroidal2DPhysics;
import spacewar2.utilities.Position;
import spacewar2.objects.*;

// A directed path from a to b with no properties
class Relation{
	
	// A and B - the edges this relation connects
	protected SpacewarObject a;
	protected SpacewarObject b;
	
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
