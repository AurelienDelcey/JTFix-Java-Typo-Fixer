package loader;

import java.util.Arrays;

public record DataContext(char[] fileContent, int[] linesOffsets) {
	
	public DataContext(char[] fileContent, int[] linesOffsets){
		this.linesOffsets = Arrays.copyOf(linesOffsets, linesOffsets.length);
		this.fileContent = Arrays.copyOf(fileContent, fileContent.length);
	}
	@Override
	public char[] fileContent() {
		return Arrays.copyOf(fileContent, fileContent.length);
	}
	
	@Override
	public int[] linesOffsets() {
		return Arrays.copyOf(linesOffsets, linesOffsets.length);
	}
}
