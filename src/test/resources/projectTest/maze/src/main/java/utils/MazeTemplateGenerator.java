package utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import model.MoveSet;

public class MazeTemplateGenerator {
	
	private final int size;
	private final Random rand ;
	private final int[] cursor ;
	private final TemplateCell[][] mazeTemplate ;
	private final Deque<TemplateCell> stack;
	
	

	public MazeTemplateGenerator(int size) {
		this.stack = new ArrayDeque<>();
		this.size = size;
		this.rand = new Random();
		this.cursor = new int[]{0,0};
		mazeTemplate = new TemplateCell[this.size][this.size];
	}
	
	public TemplateCell[][] createTemplate() {
		
		Set<MoveSet> possibleMove = null;
		MoveSet nextMove = null;
		
		
		initArray();
		setCursorStart();
		stack.push(getCurrentCell());
		
		
		while (true) {
			
			getCurrentCell().setVisited();
			possibleMove = searchPossibleMoves();
			
			while (possibleMove.isEmpty()) {
				
				if (stack.isEmpty()) {
					return this.mazeTemplate;
				}
				
				TemplateCell popCell = this.stack.peek();
				
				setCursor(popCell.getxCoordinate(),popCell.getyCoordinate());
				
				possibleMove = searchPossibleMoves();
				
				if(possibleMove.isEmpty()) {
					this.stack.pop();
				}
			}
			nextMove = getRandomMove(possibleMove.toArray(new MoveSet[possibleMove.size()]));
			openWay(nextMove);
			moveCursor(nextMove);
			stack.push(getCurrentCell());
		}
	}
	
	private void openWay(MoveSet move) {
		MoveSet moveInvers = getInverseMove(move);
		this.mazeTemplate[this.cursor[0]][this.cursor[1]].removeWall(move);
		moveCursor(move);
		this.mazeTemplate[this.cursor[0]][this.cursor[1]].removeWall(moveInvers);
		moveCursor(moveInvers);
		
	}
	
	private void initArray() {
		for (int col = 0; col < this.mazeTemplate.length; col++) {
			for (int row = 0; row < this.mazeTemplate.length; row++) {
				this.mazeTemplate[col][row] = new TemplateCell(col,row);
			}
		}
		
	}
	private void setCursor(int x, int y) {
		cursor[0] = x;
		cursor[1] = y;
	}
	
	private MoveSet getInverseMove(MoveSet move) {
		return switch(move){
			case UP -> MoveSet.DOWN;
			case DOWN -> MoveSet.UP;
			case LEFT -> MoveSet.RIGHT;
			case RIGHT -> MoveSet.LEFT;
		};
	}
	
	private void moveCursor(MoveSet move) {
		switch(move) {
		case UP ->this.cursor[1] -= 1;
		case DOWN-> this.cursor[1] += 1;
		case LEFT-> this.cursor[0] -= 1;
		case RIGHT-> this.cursor[0] += 1;
		}
	}
	
	private Set<MoveSet> searchPossibleMoves() {
		Set<MoveSet>result = EnumSet.noneOf(MoveSet.class);
		for(MoveSet move:MoveSet.values()) {
			if(checkMove(move)) {
				result.add(move);
			}
		}
		return result;
	}
	
	
	
	private void setCursorStart() {
		this.cursor[0] = rand.nextInt(this.size);
		this.cursor[1] = rand.nextInt(this.size);
	}
	
	private boolean checkMove(MoveSet move) {
		return switch(move) {
			case UP -> checkUp();
			case DOWN -> checkDown();
			case RIGHT -> checkRight();
			case LEFT -> checkLeft();
		};
	}
	
	private boolean checkLeft() {
		if(cursor[0]==0) {
			return false;
		}
		return !this.mazeTemplate[cursor[0]-1][cursor[1]].isVisited();
	}
	private boolean checkRight() {
		if(cursor[0]==this.size-1) {
			return false;
		}
		return !this.mazeTemplate[cursor[0]+1][cursor[1]].isVisited();
	}
	private boolean checkUp() {
		if(cursor[1]==0) {
			return false;
		}
		return !this.mazeTemplate[cursor[0]][cursor[1]-1].isVisited();
	}
	private boolean checkDown() {
		if(cursor[1]==this.size-1) {
			return false;
		}
		return !this.mazeTemplate[cursor[0]][cursor[1]+1].isVisited();
	}
	
	private MoveSet getRandomMove(MoveSet[] moveSet) {
		if(moveSet.length==1) {return moveSet[0];}
		return moveSet[rand.nextInt(moveSet.length)];
	}
	
	private TemplateCell getCurrentCell() {
		return this.mazeTemplate[cursor[0]][cursor[1]];
	}
}
