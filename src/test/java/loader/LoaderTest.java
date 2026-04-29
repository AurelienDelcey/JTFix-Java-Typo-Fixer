package loader;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import shared.Failure;
import shared.PipeResult;
import shared.Success;

class LoaderTest {
	
	private static List<Path> pathList = new ArrayList<>();
	private static Map<Path, DataContext> mapTest = new HashMap<>();
	private static Map<Path, DataContext> mapTestParallel = new HashMap<>();
	private final static Path TEST_ROOT = Path.of("src/test/resources/filesTest");
	private final static Path EMPTY_10_LINES_PATH = Path.of("src/test/resources/filesTest/emptyLine10.java");
	private final static Path FIXED_328_CHAR_PATH = Path.of("src/test/resources/filesTest/fixed328Chars.java");
	private final static Path EMPTY_FILE_PATH = Path.of("src/test/resources/filesTest/empty.java");
	private final static Path FAKE_FILE_PATH = Path.of("src/test/resources/filesTest/fake.java");
	private final static List<Path> incorrectPathList = List.of(FAKE_FILE_PATH);

	@BeforeAll
	static void setUp() throws Exception {
		PipeResult<List<Path>> result = scan.FileExplorer.exploreDirectory(TEST_ROOT);
		pathList = switch(result){
		case Success<List<Path>> s -> s.payload();
		case Failure<List<Path>> f -> fail();
		};
		
		PipeResult<Map<Path, DataContext>> loadResult = loader.Loader.load(pathList);
		PipeResult<Map<Path, DataContext>> loadParallelResult = loader.Loader.loadParallel(pathList);
		
		mapTest = switch(loadResult) {
		case Success<Map<Path, DataContext>> s -> s.payload();
		case Failure<Map<Path, DataContext>> f -> fail();
		};
		
		mapTestParallel = switch(loadParallelResult) {
		case Success<Map<Path, DataContext>> s -> s.payload();
		case Failure<Map<Path, DataContext>> f -> fail();
		};
	}

	@Test
	void empty10LinesFile_shouldProduce10Chars() {
		assertEquals(10, getCharsLength(EMPTY_10_LINES_PATH));
	}

	@Test
	void empty10LinesFile_shouldProduce10LinesOffset() {
		assertEquals(10, getOffsetsLength(EMPTY_10_LINES_PATH));
	}

	@Test
	void fixed328CharsFile_shouldProduce328Chars() {
		assertEquals(328, getCharsLength(FIXED_328_CHAR_PATH));
	}
	
	@Test
	void fixed328CharsFile_shouldReturnEmptyOffsets() {
		assertEquals(0, getOffsetsLength(FIXED_328_CHAR_PATH));
	}
	
	@Test
	void emptyFile_shouldReturnEmptyCharsArray() {
		assertEquals(0, getCharsLength(EMPTY_FILE_PATH));
	}
	
	@Test
	void emptyFile_shouldReturnEmptyOffsets() {
		assertEquals(0, getOffsetsLength(EMPTY_FILE_PATH));
	}
	
	@Test
	void load_shouldReturnFailure_whenPathDoesNotExist() {
		PipeResult<Map<Path, DataContext>> loadResult = loader.Loader.load(incorrectPathList);
		
		switch(loadResult) {
		case Success<Map<Path, DataContext>> s -> fail("return Failure expected, but return Success.");
		case Failure<Map<Path, DataContext>> f -> {}
		};
	}
	
	@Test
	void empty10LinesFile_WithParallelLoading_shouldProduce10Chars() {
		assertEquals(10, getCharsLengthParallel(EMPTY_10_LINES_PATH));
	}

	@Test
	void empty10LinesFile_WithParallelLoading_shouldProduce10LinesOffset() {
		assertEquals(10, getOffsetsLengthParallel(EMPTY_10_LINES_PATH));
	}

	@Test
	void fixed328CharsFile_WithParallelLoading_shouldProduce328Chars() {
		assertEquals(328, getCharsLengthParallel(FIXED_328_CHAR_PATH));
	}
	
	@Test
	void fixed328CharsFile_WithParallelLoading_shouldReturnEmptyOffsets() {
		assertEquals(0, getOffsetsLengthParallel(FIXED_328_CHAR_PATH));
	}
	
	@Test
	void emptyFile_WithParallelLoading_shouldReturnEmptyCharsArray() {
		assertEquals(0, getCharsLengthParallel(EMPTY_FILE_PATH));
	}
	
	@Test
	void emptyFile_WithParallelLoading_shouldReturnEmptyOffsets() {
		assertEquals(0, getOffsetsLengthParallel(EMPTY_FILE_PATH));
	}
	
	@Test
	void load_WithParallelLoading_shouldReturnFailure_whenPathDoesNotExist() {
		PipeResult<Map<Path, DataContext>> loadResult = loader.Loader.loadParallel(incorrectPathList);
		
		switch(loadResult) {
		case Success<Map<Path, DataContext>> s -> fail("return Failure expected, but return Success.");
		case Failure<Map<Path, DataContext>> f -> {}
		};
	}

	private int getOffsetsLength(Path path) {
		return mapTest.get(path).linesOffsets().length;
	}

	private int getCharsLength(Path path) {
		return mapTest.get(path).fileContent().length;
	}
	
	private int getOffsetsLengthParallel(Path path) {
		return mapTestParallel.get(path).linesOffsets().length;
	}

	private int getCharsLengthParallel(Path path) {
		return mapTestParallel.get(path).fileContent().length;
	}

}
