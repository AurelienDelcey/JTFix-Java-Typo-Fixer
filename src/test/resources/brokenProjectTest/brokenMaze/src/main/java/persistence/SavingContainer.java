package persistence;

import model.MazeCells;
import model.Player;

public class SavingContainer {
	
	private MazeCells[][] mazeToSave;
	private Player playerToSave;
	
	public SavingContainer() {
		
	}
	
	public SavingContainer(MazeCells[][] mazeToSave, Player playerToSave) {
		this.mazeToSave = mazeToSave;
		this.playerToSave = playerToSave;
	}

	public MazeCells[][] getMazeToSave() {
		return mazeToSave;
	}

	public Player getPlayerToSave() {
		return playerToSave;
	}
	
	public void setMazeToSave(MazeCells[][] mazeToSave) {
		this.mazeToSave = mazeToSave;
	}
	
	public void setPlayerToSave(Player playerToSave) {
		this.playerToSave = playerToSave;
	}
	
}
