package model;



public class MazeCells {
	
	private int xCoordonate;
	private int yCoordonate;
	private MazeCellsState state;
	
	public MazeCells() {
		
	}
	public MazeCells(int xCoordonate, int yCoordonate) {
		this.xCoordonate = xCoordonate;
		this.yCoordonate = yCoordonate;
		this.state = MazeCellsState.EMPTY;
	}

	public MazeCellsState getState() {
		return state;
	}

	public void setState(MazeCellsState state) {
		this.state = state;
	}

	public int getxCoordonate() {
		return xCoordonate;
	}

	public int getyCoordonate() {
		return yCoordonate;
	}
	public void setxCoordonate(int xCoordonate) {
		this.xCoordonate = xCoordonate;
	}
	public void setyCoordonate(int yCoordonate) {
		this.yCoordonate = yCoordonate;
	}
	
	
}
