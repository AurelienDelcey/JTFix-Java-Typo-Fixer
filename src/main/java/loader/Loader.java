package loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
				
				char[] text = new char[INITIAL_ARRAY_SIZE];
				int[] lineOffset = new int[INITIAL_ARRAY_SIZE];
				int lineCounter = 0;
				
				for(int i=0; i>=0; i++) {
					int code = reader.read();
					if(code == -1) {
						break;
					}
					char current = (char) code;
					if(current == '\n') {
						lineOffset[lineCounter] = i;
						lineCounter++;
					}
					text[i] = current;
					
					if(i == text.length-1) {
						text = charArrayGrowth(text);
					}
					
					if(lineCounter == lineOffset.length-1) {
						lineOffset = intArrayGrowth(lineOffset);
					}
				}
				result.put(path, new DataContext(text,lineOffset));
			} catch (IOException e) {
				return new Failure<>("fail!");
			}
		}
		return new Success<>(result);
	}

	private static char[] charArrayGrowth(char[] initialArray) {
		int newSize = (int) (initialArray.length*GROWTH_FACTOR);
		char[] newArray = new char[newSize];
		for(int i=0; i<initialArray.length; i++) {
			newArray[i] = initialArray[i];
		}
		return newArray;
	}
	
	private static int[] intArrayGrowth(int[] initialArray) {
		int newSize = (int) (initialArray.length*GROWTH_FACTOR);
		int[] newArray = new int[newSize];
		for(int i=0; i<initialArray.length; i++) {
			newArray[i] = initialArray[i];
		}
		return newArray;
	}
	
}
