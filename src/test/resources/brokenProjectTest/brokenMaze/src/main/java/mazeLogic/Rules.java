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
	
}
