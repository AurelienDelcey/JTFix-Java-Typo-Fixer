package loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import shared.Failure;
import shared.PipeResult;
import shared.Success;

public class Loader {
	
	private final static int INITIAL_ARRAY_SIZE = 150;
	private final static double GROWTH_FACTOR = 1.5;
	
	public static PipeResult<Map<Path, DataContext>> load(List<Path> pathList){
		Map<Path, DataContext> result = new HashMap<>();
		for(Path path : pathList) {
			try {
				LoadingResult loadedFile = loadOne(path);
				result.put(loadedFile.path(), loadedFile.context());
			} catch (IOException e) {
				return new Failure<>("loader failed: " + path + " // " + e.getMessage());
			}
		}
		return new Success<>(result);
	}
	
	public static PipeResult<Map<Path, DataContext>> loadParallel(List<Path> pathList){
		int workers = Runtime.getRuntime().availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(workers);
		List<Future<LoadingResult>> resultList = new ArrayList<>();
		Map<Path, DataContext> result = new HashMap<>();
		for(Path path : pathList) {
			Future<LoadingResult> loadedFile = exec.submit(() -> loadOne(path));
			resultList.add(loadedFile);
		}
		exec.shutdown();
		for(Future<LoadingResult> loadedFile : resultList) {
			try {
				LoadingResult file = loadedFile.get();
				result.put(file.path(), file.context());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return new Failure<>("loader failed: " + e.getMessage());
			} catch (ExecutionException e) {
				return new Failure<>("loader failed: " + e.getMessage());
			}
		}
		return new Success<>(result);
	}

	private static LoadingResult loadOne(Path path) throws IOException {
		try(BufferedReader reader = Files.newBufferedReader(path)){
			
			char[] fileContent = new char[INITIAL_ARRAY_SIZE];
			int[] linesOffsets = new int[INITIAL_ARRAY_SIZE];
			int lineCounter = 0;
			int charCounter = 0;
			
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
			
			fileContent = Arrays.copyOf(fileContent, charCounter);
			linesOffsets = Arrays.copyOf(linesOffsets, lineCounter);
			return new LoadingResult(path, new DataContext(fileContent, linesOffsets));
		}
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
