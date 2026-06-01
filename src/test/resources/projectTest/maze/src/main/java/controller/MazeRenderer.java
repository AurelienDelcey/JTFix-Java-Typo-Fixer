package controller;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import mazeLogic.GameContext;
import model.MazeCells;
import model.MazeCellsState;
import model.Player;

public class MazeRenderer {
	
	private static final Color WALL_COLOR = Color.CHOCOLATE;
	private static final Color PATH_COLOR = Color.BEIGE;
	private static final Color PLAYER_COLOR = Color.DARKGOLDENROD;
	private static final int CELL_SIZE = 20;
	private static final int MARGIN = 5;
	private final GameContext context;
	private final Canvas[][] renderCanvas;
	private int previousPositionX;
	private int previousPositionY;
	
	public MazeRenderer(GameContext context) {
		this.context = context;
		MazeCells[][] maze = this.context.getMaze();
		this.renderCanvas = new Canvas[maze.length][maze[0].length];
		this.previousPositionX = 0;
		this.previousPositionY = 0;
	}

	public void renderMaze(GridPane mazeLayout) {
		MazeCells[][] maze = this.context.getMaze();
		for (int i = 0; i < maze.length; i++) {
			for (int j = 0; j < maze[i].length; j++) {
				Canvas mazePart= new Canvas(CELL_SIZE, CELL_SIZE) ;
				GraphicsContext gc= mazePart.getGraphicsContext2D();
				if(maze[i][j].getState()== MazeCellsState.WALL) {
					gc.setFill(WALL_COLOR);
				}else {
					gc.setFill(PATH_COLOR);
				}
				gc.fillRect(0,0,CELL_SIZE,CELL_SIZE);
				
				drawStartAndGoal(i, j, gc);
				mazeLayout.add(mazePart,i,j);
				this.renderCanvas[i][j] = mazePart;
			}
		}
		drawPlayer();
	}
	
	public void renderPlayerMove() {
		renderPlayerCell();
		restorePreviousCell();
	}

	public void storePreviousPlayerPosition() {
		Player player = this.context.getPlayer();
		this.previousPositionX = player.getxCoordonate();
		this.previousPositionY = player.getyCoordonate();
	}

	private void renderPlayerCell() {
		Player player = this.context.getPlayer();
		Canvas canvas = this.renderCanvas[player.getxCoordonate()][player.getyCoordonate()];
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, CELL_SIZE, CELL_SIZE);
		gc.setFill(PATH_COLOR);
		gc.fillRect(0, 0, CELL_SIZE, CELL_SIZE);
		drawPlayer();
		
	}

	private void restorePreviousCell() {
		Canvas canvas = this.renderCanvas[this.previousPositionX][this.previousPositionY];
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, CELL_SIZE, CELL_SIZE);
		gc.setFill(PATH_COLOR);
		gc.fillRect(0, 0, CELL_SIZE, CELL_SIZE);
		drawStartAndGoal(this.previousPositionX,this.previousPositionY,gc);
	}
	
	private void drawStartAndGoal(int x, int y, GraphicsContext gc) {
		MazeCells[][] maze = this.context.getMaze();
		switch(maze[x][y].getState()){
			case GOAL ->{
				gc.setFill(Color.BLUE);
				gc.fillOval(MARGIN/2, MARGIN/2, CELL_SIZE-MARGIN, CELL_SIZE-MARGIN);
			}
			case START ->{
				gc.setFill(Color.GREEN);
				gc.fillOval(MARGIN/2, MARGIN/2, CELL_SIZE-MARGIN, CELL_SIZE-MARGIN);	
			}
		default -> {}
		};
	}

	private void drawPlayer() {
		Player player = this.context.getPlayer();
		Canvas canvas = this.renderCanvas[player.getxCoordonate()][player.getyCoordonate()];
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(PLAYER_COLOR);
		gc.fillOval(MARGIN/2, MARGIN/2, CELL_SIZE-MARGIN, CELL_SIZE-MARGIN);
	}
}
