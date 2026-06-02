package extractor;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import extractor.valueObject.ExtractorPayload;
import loader.DataContext;
import shared.Failure;
import shared.PipeResult;
import shared.Success;

class ExtractorTest {

	private final static Path TEST_PATH = Path.of("src/test/resources/projectTest/");
	private final static Path FAIL_TEST_PATH = Path.of("src/test/resources/brokenProjectTest/");
	private static Map<Path, DataContext> mapTest;
	
	@BeforeAll
	static void setup(){
		PipeResult<List<Path>> result = scan.FileExplorer.exploreDirectory(TEST_PATH);
		List<Path> pathList = switch(result){
		case Success<List<Path>> s -> s.payload();
		case Failure<List<Path>> f -> fail();
		};
		
		PipeResult<Map<Path, DataContext>> loadResult = loader.Loader.load(pathList);
		
		mapTest = switch(loadResult) {
		case Success<Map<Path, DataContext>> s -> s.payload();
		case Failure<Map<Path, DataContext>> f -> fail();
		};
	}
	
	@Test
	void extract_ShouldTokenizeCorpusWithoutException() {
		assertDoesNotThrow(()->Extractor.extract(mapTest));	
	}
	
	@Test
	void extractParallel_ShouldTokenizeCorpusWithoutException() {
		assertDoesNotThrow(()->Extractor.extractParallel(mapTest));	
	}
	
	@Test
	void extractAndExtractParallel_ShouldReturnSameResult() {
		PipeResult<ExtractorPayload> monoResult = Extractor.extract(mapTest);
		PipeResult<ExtractorPayload> multiResult = Extractor.extractParallel(mapTest);
		
		ExtractorPayload monoPayload = switch(monoResult) {
		case Success<ExtractorPayload> s -> s.payload();
		case Failure<ExtractorPayload> f -> fail();
		};
		
		ExtractorPayload multiPayload = switch(multiResult) {
		case Success<ExtractorPayload> s -> s.payload();
		case Failure<ExtractorPayload> f -> fail();
		};
		
		assertEquals(monoPayload.knownTypes(), multiPayload.knownTypes());
		assertEquals(monoPayload.knownEnums(), multiPayload.knownEnums());
		
		assertEquals(monoPayload.tokenMap().keySet(),
					 multiPayload.tokenMap().keySet());
		
		monoPayload.tokenMap().keySet().stream()
										.forEach((i)->{
											assertIterableEquals(monoPayload.tokenMap().get(i).tokenList(), 
																multiPayload.tokenMap().get(i).tokenList());
										});
	}
	
	@Test
	void extractParallel_ShouldReturnFailure_WhenCorpusContainsBrokenFile() {
		PipeResult<List<Path>> result = scan.FileExplorer.exploreDirectory(FAIL_TEST_PATH);
		List<Path> pathList = switch(result){
		case Success<List<Path>> s -> s.payload();
		case Failure<List<Path>> f -> fail();
		};
		
		PipeResult<Map<Path, DataContext>> loadResult = loader.Loader.load(pathList);
		
		Map<Path, DataContext> mapTestWithError = switch(loadResult) {
		case Success<Map<Path, DataContext>> s -> s.payload();
		case Failure<Map<Path, DataContext>> f -> fail();
		};
		
	    PipeResult<ExtractorPayload> extractorPayload= Extractor.extractParallel(mapTestWithError);
	    
	    switch(extractorPayload) {
	        case Success<ExtractorPayload> s -> fail("Le parseur aurait dû retourner un Failure.");
	        case Failure<ExtractorPayload> f -> {}
	    }
	}

}
