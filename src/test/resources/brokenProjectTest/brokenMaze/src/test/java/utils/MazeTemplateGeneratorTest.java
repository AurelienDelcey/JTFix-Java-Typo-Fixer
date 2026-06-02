package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.MoveSet;

public class MazeTemplateGeneratorTest {
	private MazeTemplateGenerator generator;
    private TemplateCell[][] maze;

    @BeforeEach
    void setUp() {
        generator = new MazeTemplateGenerator(5);
        maze = generator.createTemplate();
    }

    @Test
    void shouldGenerateCorrectGridSize() {
    	assertEquals(5,maze.length);
    	assertEquals(5,maze[0].length);
    }

    @Test
    void shouldMarkAllCellsAsVisited() { 
    	for (int i = 0; i < maze.length; i++) {
			for (int j = 0; j < maze[0].length; j++) {
				assertTrue(maze[i][j].isVisited());
			}
		}
    }

    @Test
    void eachCellShouldHaveAtLeastOneOpenWall() {
    	for (int i = 0; i < maze.length; i++) {
			for (int j = 0; j < maze[0].length; j++) {
				boolean result = maze[i][j].countWalls()<4;
				assertTrue(result);
			}
		}
    }

    @Test
    void wallsShouldBeConsistentBetweenAdjacentCells() {
    	for (int i = 0; i < maze.length; i++) {
			for (int j = 0; j < maze[0].length; j++) {
				boolean testResult = adjacentCellContainTheOtherSideOfTheWall(i, j, maze[i][j].getWalls());
				assertTrue(testResult);
			}
		}
    }
    
    private boolean adjacentCellContainTheOtherSideOfTheWall(int x, int y, EnumSet<MoveSet> cellWalls) {
    	
    	for(MoveSet e:cellWalls) {
    		switch(e){
        	case UP -> {
        		if(y>0) {
        			if(!maze[x][y-1].getWalls().contains(MoveSet.DOWN)) {
        				return false;
        			}
        		}
        	}
        	case DOWN -> {
        		if(y<maze[0].length-1) {
        			if(!maze[x][y+1].getWalls().contains(MoveSet.UP)) {
        				return false;
        			}
        		}
        	}
        	case LEFT ->{
        		if(x>0) {
        			if(!maze[x-1][y].getWalls().contains(MoveSet.RIGHT)) {
        				return false;
        			}
        		}
        	}
        	case RIGHT ->{
        		if(x<maze.length-1) {
        			if(!maze[x+1][y].getWalls().contains(MoveSet.LEFT)) {
        				return false;
        			}
        		}
        	}
        	};
        	
    	}
    	return true;
    }
}
