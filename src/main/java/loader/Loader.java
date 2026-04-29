package loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shared.Failure;
import shared.PipeResult;
import shared.Success;

public class Loader {
	
	private final static int INITIAL_ARRAY_SIZE = 150;
	private final static double GROWTH_FACTOR = 1.5;
	
	public static PipeResult<Map<Path, DataContext>> load(List<Path> pathList){
		Map<Path, DataContext> result = new HashMap<>();
		for(Path path : pathList) {
			try(BufferedReader reader = Files.newBufferedReader(path)){
				
				char[] fileContent = new char[INITIAL_ARRAY_SIZE];
				int[] linesOffsets = new int[INITIAL_ARRAY_SIZE];
				int lineCounter = 0;
				
				for(int i=0; i>=0; i++) {
					int charCode = reader.read();
					if(isEndOfFile(charCode)) {
						break;
					}
					char current = (char) charCode;
					
					if(isEndOfLine(current)) {
						linesOffsets[lineCounter] = i;
						lineCounter++;
					}
					
					fileContent[i] = current;
					charCounter++;
					
					if(i == fileContent.length-1) {
						fileContent = resizeCharArray(fileContent);
					}
					
					if(lineCounter == linesOffsets.length-1) {
						linesOffsets = resizeIntArray(linesOffsets);
					}
				}
				result.put(path, new DataContext(fileContent,linesOffsets));
				
			} catch (IOException e) {
				return new Failure<>("fail!");
			}
		}
		return new Success<>(result);
	}

	private static char[] resizeCharArray(char[] initialArray) {
		int newSize = (int) (initialArray.length*GROWTH_FACTOR);
		return Arrays.copyOf(initialArray, newSize);
	}
	
	private static int[] resizeIntArray(int[] initialArray) {
		int newSize = (int) (initialArray.length*GROWTH_FACTOR);
		return Arrays.copyOf(initialArray, newSize);
	}

	private static boolean isEndOfFile(int code) {
		return code == -1;
	}

	private static boolean isEndOfLine(char current) {
		return current == '\n';
	}
	
}
