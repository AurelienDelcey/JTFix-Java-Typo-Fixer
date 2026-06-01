package mazeLogic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.MazeCells;
import model.MazeCellsState;
import model.MoveSet;
import model.Player;

class RulesTest {
	
	private Player player;
	private GameContext context ;
	private Rules rules;
	MazeCells[][] maze;

	@BeforeEach
	void setUp() throws Exception {
		this.player = new Player("player",1,1);

		
	}

	@Test
	void applyMove_shouldMovePlayerVertically_whenCellIsEmpty() {
		this.maze= buildTinyEmptyMaze();
		this.context = new GameContext(maze,player);
		this.rules = new Rules(this.context);
		boolean result = rules.applyMove(MoveSet.DOWN);
		assertTrue(result);
		assertEquals(1,player.getxCoordonate());
		assertEquals(2,player.getyCoordonate());
		result = rules.applyMove(MoveSet.UP);
		assertTrue(result);
		assertEquals(1,player.getxCoordonate());
		assertEquals(1,player.getyCoordonate());
	}
	
	@Test
	void applyMove_shouldMovePlayerHorizontally_whenCellIsEmpty() {
		this.maze= buildTinyEmptyMaze();
		this.context = new GameContext(maze,player);
		this.rules = new Rules(this.context);
		boolean result = rules.applyMove(MoveSet.RIGHT);
		assertTrue(result);
		assertEquals(2,player.getxCoordonate());
		assertEquals(1,player.getyCoordonate());
		result = rules.applyMove(MoveSet.LEFT);
		assertTrue(result);
		assertEquals(1,player.getxCoordonate());
		assertEquals(1,player.getyCoordonate());
	}
	
	@Test
	void applyMove_shouldNotMovePlayerVertically_whenCellIsWall() {
		this.maze= buildTinyMazeWithWallsAround();
		this.context = new GameContext(maze,player);
		this.rules = new Rules(this.context);
		boolean result = rules.applyMove(MoveSet.DOWN);
		assertFalse(result);
		assertEquals(1,player.getxCoordonate());
		assertEquals(1,player.getyCoordonate());
		result = rules.applyMove(MoveSet.UP);
		assertFalse(result);
		assertEquals(1,player.getxCoordonate());
		assertEquals(1,player.getyCoordonate());
	}
	
	@Test
	void applyMove_shouldNotMovePlayerHorizontally_whenCellIsWall() {
		this.maze= buildTinyMazeWithWallsAround();
		this.context = new GameContext(maze,player);
		this.rules = new Rules(this.context);
		boolean result = rules.applyMove(MoveSet.LEFT);
		assertFalse(result);
		assertEquals(1,player.getxCoordonate());
		assertEquals(1,player.getyCoordonate());
		result = rules.applyMove(MoveSet.RIGHT);
		assertFalse(result);
		assertEquals(1,player.getxCoordonate());
		assertEquals(1,player.getyCoordonate());
	}
	
	@Test
	void applyMove_shouldSetGameStateToVictory_whenCellIsGoal() {
		this.maze= buildTinyEmptyMaze();
		maze[2][1].setState(MazeCellsState.GOAL);
		this.context = new GameContext(maze,player);
		this.rules = new Rules(this.context);
		boolean result = rules.applyMove(MoveSet.RIGHT);
		assertTrue(result);
		assertEquals(GameState.VICTORY,this.context.getGlobalState());
	}
	
	private MazeCells[][] buildTinyEmptyMaze(){
		return new MazeCells[][]{
			{new MazeCells(0,0), new MazeCells(1,0), new MazeCells(2,0)},
			{new MazeCells(0,1), new MazeCells(1,1), new MazeCells(2,1)},
			{new MazeCells(0,2), new MazeCells(1,2), new MazeCells(2,2)}
		};
	}
	
	private MazeCells[][] buildTinyMazeWithWallsAround(){
		MazeCells[][] result= buildTinyEmptyMaze();
		result[0][0].setState(MazeCellsState.WALL);
		result[1][0].setState(MazeCellsState.WALL);
		result[2][0].setState(MazeCellsState.WALL);
		result[0][1].setState(MazeCellsState.WALL);
		result[2][1].setState(MazeCellsState.WALL);
		result[0][2].setState(MazeCellsState.WALL);
		result[1][2].setState(MazeCellsState.WALL);
		result[2][2].setState(MazeCellsState.WALL);
		return result;
	}

}
