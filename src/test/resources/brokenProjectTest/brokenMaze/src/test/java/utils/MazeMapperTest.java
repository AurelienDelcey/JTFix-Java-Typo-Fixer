package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import model.MazeCells;
import model.MazeCellsState;
import model.MoveSet;

public class MazeMapperTest {
	
	private MazeFactory factory;
	private MazeMapper mapperToTest;
	
	@Test
	void shouldGenerateMazeWithBorderWalls() {
		this.factory = new MazeFactory(11);
		MazeCells[][] maze = this.factory.create();
		for (int i = 0; i < maze.length; i++) {
			for (int j = 0; j < maze[0].length; j++) {
				if(i==0 || i == maze.length-1 || j==0 || j==maze[0].length-1) {
					assertEquals(MazeCellsState.WALL,maze[i][j].getState());
				}
			}
		}
	}
	
	@Test
	void intersectionCellsShouldAlwaysBeWalls() {
		this.factory = new MazeFactory(11);
		MazeCells[][] maze = this.factory.create();
		for (int i = 0; i < maze.length; i++) {
			for (int j = 0; j < maze[0].length; j++) {
				if(i%2==0 && j%2==0) {
					assertEquals(MazeCellsState.WALL,maze[i][j].getState());
				}
			}
		}
	}
	
	@Test
	void shouldNotPlaceWallsOnPathCells() {
		this.factory = new MazeFactory(11);
		MazeCells[][] maze = this.factory.create();
		for (int i = 0; i < maze.length; i++) {
			for (int j = 0; j < maze[0].length; j++) {
				if(i%2==1 && j%2==1) {
					assertNotEquals(MazeCellsState.WALL,maze[i][j].getState());
				}
			}
		}
	}
	
	@Test
	void shouldGenerateVerticalWalls_whenTemplateHasOnlyVerticalOpenings() {
		this.mapperToTest = new MazeMapper();
		MazeCells[][] mazeTest = this.mapperToTest.mapMaze(createTemplateWithVerticalWall());
		for (int i = 0; i < mazeTest.length; i++) {
			for (int j = 0; j < mazeTest[0].length; j++) {
				if(i%2==0) {
					assertEquals(MazeCellsState.WALL,mazeTest[i][j].getState());
				}else if(j>0 && j<mazeTest[1].length-1) {
					assertNotEquals(MazeCellsState.WALL,mazeTest[i][j].getState());
				}
			}
		}
	}
	
	@Test
	void shouldGenerateHorizontalWalls_whenTemplateHasOnlyHorizontalOpenings() {
		this.mapperToTest = new MazeMapper();
		MazeCells[][] mazeTest = this.mapperToTest.mapMaze(createTemplateWithHorizontalWall());
		for (int i = 0; i < mazeTest.length; i++) {
			for (int j = 0; j < mazeTest[0].length; j++) {
				if(j%2==0) {
					assertEquals(MazeCellsState.WALL,mazeTest[i][j].getState());
				}else if(i>0 && i<mazeTest.length-1) {
					assertNotEquals(MazeCellsState.WALL,mazeTest[i][j].getState());
				}
			}
		}
	}
	
	private TemplateCell[][] createTemplateWithHorizontalWall(){
		TemplateCell[][] HorizontalTemplate = new TemplateCell[6][6];
		for (int i = 0; i < HorizontalTemplate.length; i++) {
			for (int j = 0; j < HorizontalTemplate.length; j++) {
				HorizontalTemplate[i][j]= new TemplateCell(i,j);
				HorizontalTemplate[i][j].removeWall(MoveSet.LEFT);
				HorizontalTemplate[i][j].removeWall(MoveSet.RIGHT);
			}
		}
		return HorizontalTemplate;
	}
	
	private TemplateCell[][] createTemplateWithVerticalWall(){
		TemplateCell[][] VerticalTemplate = new TemplateCell[6][6];
		for (int i = 0; i < VerticalTemplate.length; i++) {
			for (int j = 0; j < VerticalTemplate.length; j++) {
				VerticalTemplate[i][j] = new TemplateCell(i,j);
				VerticalTemplate[i][j].removeWall(MoveSet.UP);
				VerticalTemplate[i][j].removeWall(MoveSet.DOWN);
			}
		}
		return VerticalTemplate;
	}
}
