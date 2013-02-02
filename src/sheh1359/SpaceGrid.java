package sheh1359;

import java.util.ArrayList;

import spacewar2.simulator.Toroidal2DPhysics;
import spacewar2.utilities.Position;

public class SpaceGrid{

	ArrayList<ArrayList<SpaceBlock>> blocks;
	Toroidal2DPhysics space;
	int width;
	int height;
	int blockSize;
	
	public SpaceGrid(Toroidal2DPhysics space){
		width = 1024 ;
		height = 768 ;
		blockSize = 32 ;
		this.space = space ;
		blocks = new ArrayList<ArrayList<SpaceBlock>>();
		
		int y = 0 ;
		while(y < height){
			int x = 0;
			blocks.add(new ArrayList<SpaceBlock>());
			while(x < width){
				Position p = new Position(x + (blockSize / 2),(y + blockSize / 2));
				SpaceBlock block = new SpaceBlock(space,p,blockSize);
				blocks.get(blocks.size() - 1).add(block);
				x += blockSize ;
			}
			y += blockSize ;
		}
	}
	
	public ArrayList<ArrayList<SpaceBlock>> getBlocks(){
		return blocks ;
	}
	
}


class SpaceBlock{
	
	Toroidal2DPhysics space;
	Position position ;
	int size;
	
	public SpaceBlock(Toroidal2DPhysics space, Position position, int size){
		this.position = position ;
		this.size = size ;
		this.space = space ;
	}
	
	public double getCircumRadius(){
		return Math.sqrt(Math.pow((size /2),2)+Math.pow((size/2),2));
	}
	
	public boolean isClear(){
		int radius = (int) getCircumRadius();
		Position p = position ;
		
		return space.isLocationFree(p, radius);
	}
	
	public Position getPosition(){
		return position ;
	}
	
}