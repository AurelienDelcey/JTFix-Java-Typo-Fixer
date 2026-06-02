package mazeLogic;

import model.MazeCellsState;
import model.MoveSet;
import model.Player;

import model.MazeCells;

public class Rules {
	
	private final GameContext context;
	
	
	public Rules(GameContext context) {
		this.context = context;
	}
	
	public boolean applyMove(MoveSet move) {
		if(movementAuthorization(move)) {
			movePlayer(move);
			this.context.setGlobalState(checkVictory());
			return true;
		}
		return false;
	}
	
	private GameState checkVictory() {
		Player player = this.context.getPlayer();
		MazeCells[][] maze = this.context.getMaze();
		return switch(maze[player.getxCoordonate()][player.getyCoordonate()].getState()) {
			case EMPTY -> GameState.IN_GAME;
			case START -> GameState.IN_GAME;
			case GOAL -> GameState.VICTORY;
			default -> GameState.IN_GAME;
		};
	}
	
	private void movePlayer(MoveSet move) {
		Player player = this.context.getPlayer();
		switch(move){
			case UP -> player.setyCoordonate(player.getyCoordonate()-1);
			case DOWN -> player.setyCoordonate(player.getyCoordonate()+1);
			case LEFT -> player.setxCoordonate(player.getxCoordonate()-1);
			case RIGHT -> player.setxCoordonate(player.getxCoordonate()+1);
		};
	}
	
	private boolean movementAuthorization(MoveSet move) {
		Player player = this.context.getPlayer();
		MazeCells[][] maze = this.context.getMaze();
		if(!player.getMoveSet().contains(move)) {
			return false;
		}
		return switch(move){
			case UP -> maze[player.getxCoordonate()][player.getyCoordonate()-1].getState() == MazeCellsState.WALL?false:true;
			case DOWN -> maze[player.getxCoordonate()][player.getyCoordonate()+1].getState() == MazeCellsState.WALL?false:true;
			case LEFT -> maze[player.getxCoordonate()-1][player.getyCoordonate()].getState() == MazeCellsState.WALL?false:true;
			case RIGHT -> maze[player.getxCoordonate()+1][player.getyCoordonate()].getState() == MazeCellsState.WALL?false:true;
		};
	}
}
