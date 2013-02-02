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
	
	public int getBlockSize(){
		return blockSize;
	}
	
	public SpaceBlock getBlock(Position p) throws Exception{
		for(ArrayList<SpaceBlock> row : blocks){
			for(SpaceBlock b : row){
				if(b.contains(p)){
					return b;
				}
			}
		}
		throw new Exception();
	}
	
	public ArrayList<SpaceBlock> getAdjacentTo(SpaceBlock b){

		ArrayList<SpaceBlock> result = new ArrayList<SpaceBlock>();
		
		for(int i=0;i<blocks.size();i++){
			for(int j=0;j<blocks.get(0).size();j++){
				if(blocks.get(i).get(j) == b){
					// this is the block. get up to 8 blocks surrounding it
					try{result.add(blocks.get(i-1).get(j-1));}catch(Exception e){}
					try{result.add(blocks.get(i-1).get(j));}catch(Exception e){}
					try{result.add(blocks.get(i-1).get(j+1));}catch(Exception e){}
					
					
					try{result.add(blocks.get(i).get(j-1));}catch(Exception e){}
					try{result.add(blocks.get(i).get(j+1));}catch(Exception e){}

					
					try{result.add(blocks.get(i+1).get(j-1));}catch(Exception e){}
					try{result.add(blocks.get(i+1).get(j));}catch(Exception e){}
					try{result.add(blocks.get(i+1).get(j+1));}catch(Exception e){}

				}
			}
		}
		return result;
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
	
	public boolean contains(Position p){
		boolean xConstraint = Math.abs(position.getX() - p.getX()) <= (size /2);
		boolean yConstraint = Math.abs(position.getY() - p.getY()) <= (size /2);
		if(xConstraint && yConstraint){
			return true;
		}else{
			return false;
		}

	}
	
	public Position getPosition(){
		return position ;
	}
	
	
}