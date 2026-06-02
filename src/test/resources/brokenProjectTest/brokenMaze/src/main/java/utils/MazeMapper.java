package utils;

import model.MazeCells;
import model.MazeCellsState;
import model.MoveSet;

public class MazeMapper {
	
	

	public MazeCells[][] mapMaze(TemplateCell[][] template){
		
		MazeCells[][] finalMaze = new MazeCells[(template.length*2)+1][(template[0].length*2)+1];
		finalMaze = initMaze(finalMaze);
		
		for (int i = 0; i < template.length; i++) {
			for (int j = 0; j < template[0].length; j++) {
				int col = (i*2)+1;
				int row = (j*2)+1;
				
				if(template[i][j].hasWall(MoveSet.UP)) {
					drawUp(finalMaze[col][row],finalMaze);
				}
				if(template[i][j].hasWall(MoveSet.DOWN)) {
					drawDown(finalMaze[col][row],finalMaze);
				}
				if(template[i][j].hasWall(MoveSet.LEFT)) {
					drawLeft(finalMaze[col][row],finalMaze);
				}
				if(template[i][j].hasWall(MoveSet.RIGHT)) {
					drawRight(finalMaze[col][row],finalMaze);
				}
			}
		}
		finalizeMaze(finalMaze);
		initStartAndGoal(finalMaze);
		return finalMaze;
	}
	
	private void initStartAndGoal(MazeCells[][] finalMaze) {
		finalMaze[1][1].setState(MazeCellsState.GOAL);
		finalMaze[finalMaze.length-2][finalMaze[0].length-2].setState(MazeCellsState.START);
	}
	
	private void finalizeMaze(MazeCells[][] finalMaze) {
		for (int col = 0; col < finalMaze.length; col++) {
			for (int row = 0; row < finalMaze.length; row++) {
				if(col==0 || col==finalMaze[0].length-1 || row==0 || row==finalMaze.length-1 ) {
					finalMaze[col][row].setState(MazeCellsState.WALL);
					continue;
				}
				if(col%2==0 && row%2==0) {
					finalMaze[col][row].setState(MazeCellsState.WALL);
					continue;
				}
			}
		}
	}
	private void drawUp(MazeCells cell, MazeCells[][] finalMaze) {
		finalMaze[cell.getxCoordonate()][cell.getyCoordonate()-1].setState(MazeCellsState.WALL);
	}
	private void drawLeft(MazeCells cell, MazeCells[][] finalMaze) {
		finalMaze[cell.getxCoordonate()-1][cell.getyCoordonate()].setState(MazeCellsState.WALL);
	}
	private void drawDown(MazeCells cell, MazeCells[][] finalMaze) {
		finalMaze[cell.getxCoordonate()][cell.getyCoordonate()+1].setState(MazeCellsState.WALL);
	}
	private void drawRight(MazeCells cell, MazeCells[][] finalMaze) {
		finalMaze[cell.getxCoordonate()+1][cell.getyCoordonate()].setState(MazeCellsState.WALL);
	}
	
	private MazeCells[][] initMaze(MazeCells[][] maze){
		for (int col = 0; col < maze.length; col++) {
			for (int row = 0; row < maze[0].length; row++) {
				maze[col][row] = new MazeCells(col,row);
			}
		}
		return maze;
	}

}
