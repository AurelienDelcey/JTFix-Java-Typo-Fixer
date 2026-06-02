package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import model.MazeCells;
import model.MazeCellsState;

public class MazeFactoryTest {
	
	private MazeFactory factory;
	
	@Test
	void shouldThrowException_whenSizeIsEven() {
		assertThrows(IllegalArgumentException.class, ()->new MazeFactory(10));
	}
	
	@Test
	void fshouldCreateMazeWithCorrectSize() {
		this.factory = new MazeFactory(11);
		MazeCells[][] maze = this.factory.create();
		assertEquals(maze.length,11);
		assertEquals(maze[0].length,11);
	}
	
	@Test
	void shouldContainGoalCell() {
		this.factory = new MazeFactory(11);
		MazeCells[][] maze = this.factory.create();
		assertEquals(MazeCellsState.GOAL,maze[1][1].getState());
	}
	
	@Test
	void shouldContainStartCell() {
		this.factory = new MazeFactory(11);
		MazeCells[][] maze = this.factory.create();
		assertEquals(MazeCellsState.START,maze[maze.length-2][maze[0].length-2].getState());
	}
}
