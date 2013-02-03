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
		blockSize = 64 ;
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
	
	public double getCircumRadius(){
		return Math.sqrt(Math.pow((blockSize /2),2)+Math.pow((blockSize/2),2));
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
		System.out.println("x=" + p.getX() + ", y=" + p.getY());
		System.exit(0);
		throw new Exception();
	}
	
	public ArrayList<SpaceBlock> getAdjacentTo(SpaceBlock b){
		
		ArrayList<SpaceBlock> result = new ArrayList<SpaceBlock>();
		try{
			int above = getAboveIndex(b);
			int below = getBelowIndex(b);
			int left = getLeftIndex(b);
			int right = getRightIndex(b);
			
			int currentRow = getRowIndex(b);
			int currentCol = getColIndex(b);
			
			// this is the block. get up to 8 blocks surrounding it
			try{result.add(blocks.get(below).get(left));}catch(Exception e){}
			try{result.add(blocks.get(below).get(currentCol));}catch(Exception e){}
			try{result.add(blocks.get(below).get(right));}catch(Exception e){}
			
			
			try{result.add(blocks.get(currentRow).get(left));}catch(Exception e){}
			try{result.add(blocks.get(currentRow).get(right));}catch(Exception e){}

			
			try{result.add(blocks.get(above).get(left));}catch(Exception e){}
			try{result.add(blocks.get(above).get(currentCol));}catch(Exception e){}
			try{result.add(blocks.get(above).get(right));}catch(Exception e){}
			
			if(result.size() != 8){
				
				System.out.println("Current Row: " + currentRow);
				System.out.println("Current Col: " + currentCol);

				System.out.println("above: " + above);
				System.out.println("below: " + below);
				System.out.println("left: " + left);
				System.out.println("right: " + right);

				System.out.println("numCols: " + getXCount());
				System.out.println("numRows: " + getYCount());
				System.exit(0);

				
			}
			
		}catch(Exception e){
			System.out.println("problem getting row/column indices");
			System.exit(0);
		}
		
		return result;
	}
	
	public double getDistance(SpaceBlock b, Position p){
		return space.findShortestDistanceVector(b.getPosition(), p).getMagnitude();
	}
	
	public int getXCount(){
		return width / blockSize ;
	}
	
	public int getYCount(){
		return height / blockSize ;
	}
	
	public int getRowIndex(SpaceBlock b) throws Exception{
		for(int i=0;i<blocks.size();i++){
			for(int j=0;j<blocks.get(0).size();j++){
				if(blocks.get(i).get(j) == b){	
					return i ;
				}
			}
		}
		throw new Exception();
	}
	public int getColIndex(SpaceBlock b) throws Exception{
		for(int i=0;i<blocks.size();i++){
			for(int j=0;j<blocks.get(0).size();j++){
				if(blocks.get(i).get(j) == b){	
					return j ;
				}
			}
		}
		throw new Exception();
	}
	public int getAboveIndex(SpaceBlock b) throws Exception{
		if(getRowIndex(b) == 0){
			return getYCount() - 1 ;
		}else{
			return getRowIndex(b) - 1;
		}
	}
	public int getBelowIndex(SpaceBlock b) throws Exception{
		if(getRowIndex(b) == getYCount() - 1){
			return 0 ;
		}else{
			return getRowIndex(b) + 1;
		}
	}
	public int getLeftIndex(SpaceBlock b) throws Exception{
		if(getColIndex(b) == 0){
			return getXCount() - 1 ;
		}else{
			return getColIndex(b) - 1;
		}
	}
	public int getRightIndex(SpaceBlock b) throws Exception{
		if(getColIndex(b) == getXCount() - 1){
			return 0 ;
		}else{
			return getColIndex(b) + 1;
		}
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