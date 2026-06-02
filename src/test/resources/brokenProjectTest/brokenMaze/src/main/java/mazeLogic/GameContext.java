package mazeLogic;

import javafx.beans.property.SimpleObjectProperty;
import model.MazeCells;
import model.Player;
import utils.MazeFactory;

public class GameContext {
	
	private SimpleObjectProperty<GameState> globalState;
	private MazeCells[][] maze;
	private Player player;
	private MazeFactory factory;

	public GameContext() {
		this.globalState = new SimpleObjectProperty<GameState>(GameState.IN_GAME);
		this.factory = new MazeFactory(21);
		this.maze = factory.create();
		this.player = new Player("player",0,0);
		setPlayerOnStart();
	}
	
	public GameContext(MazeCells[][] maze, Player player) {
		this.globalState = new SimpleObjectProperty<GameState>(GameState.IN_GAME);
		this.maze = maze;
		this.player = player;
	}

	public MazeCells[][] getMaze() {
		return maze;
	}

	public Player getPlayer() {
		return player;
	}
	
	public SimpleObjectProperty<GameState> getStateProperty() {
		return this.globalState;
	}

	public GameState getGlobalState() {
		return this.globalState.getValue();
	}
	
	public void reset() {
		this.maze = factory.create();
		this.player = new Player("player",0,0);
		setPlayerOnStart();
		setGlobalState(GameState.IN_GAME);
	}

	public void setGlobalState(GameState globalState) {
		this.globalState.setValue(globalState);
	}
	private void setPlayerOnStart() {
		this.player.setxCoordonate(this.maze.length-2);
		this.player.setyCoordonate(this.maze[0].length-2);
	}
	
}
