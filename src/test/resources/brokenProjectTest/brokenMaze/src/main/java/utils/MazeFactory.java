package utils;

import model.MazeCells;

public class MazeFactory {
	
	private int size;
	private MazeTemplateGenerator mazeGen;
	private MazeMapper mapper;
	
	
	
	public MazeFactory(int size) throws IllegalArgumentException{
		if(size%2 == 0 ) {
			throw new IllegalArgumentException(
					"Invalid maze size: must be odd to preserve structure"
					);
		}
		this.size = size;
		this.mazeGen = new MazeTemplateGenerator((this.size-1)/2);
		this.mapper = new MazeMapper();
	}

	public MazeCells[][] create(){
		TemplateCell[][] template = mazeGen.createTemplate();
		MazeCells[][] finalMaze = mapper.mapMaze(template);
		return finalMaze;
	}
}
