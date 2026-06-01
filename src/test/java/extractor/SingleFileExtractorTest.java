package extractor;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import extractor.state.TypeContext;
import extractor.valueObject.FileExtraction;
import loader.DataContext;
import shared.Failure;
import shared.PipeResult;
import shared.Success;

import static extractor.state.TypeContext.*;

class SingleFileExtractorTest {
	
	private final static Path TEST_PATH = Path.of("src/test/resources/filesTest/singleFileExtractorTest.java");
	private SingleFileExtractor extractor;
	private static Map<Path, DataContext> mapTest;
	
	private final static List<String> expectedTokenNames = List.of(
		    /*"package",*/ "truc", "test",
		    /*"import",*/ "java", "util", "Map",
		    /*"import",*/ "java", "util", "List",
		    /*"public", "class",*/ "TestParsing", "T", /*"extends",*/ "Map", "String", "List", "T", "java", "io", "Serializable",
		    "String", "$str1",
		    /*"char",*/ "_c1",
		    "String", "$str2",
		    "String", "TEXT_BLOCK",
		    /*"public",*/ "U", /*"boolean",*/ "testMethod", /*"int",*/ "a", /*"int",*/ "b",
		    /*"int",*/ "_$$", /*"int",*/ "__", "_$$",
		    /*"boolean",*/ "trap1", "a", "_$$", "__", "b", "java", "util", "Collections", "String", "emptyList", /*"null",*/
		    /*"boolean",*/ "trap2", "a", "b",
		    /*"return",*/ "trap1", "trap2",
		    "java", "util", "function", "Function", "Integer", "Integer", "lambda", "Integer", "i",
		    /*"int",*/ "x",
		    /*"while",*/ "x",
		    "i",
		    /*"return", "switch",*/ "i",
		    /*"case",*/ "i",
		    /*"default", "yield",*/ "i"
		);
	private final static List<TypeContext> expectedContext = Arrays.asList(
		    /*"package",*/ null, null,
		    /*"import",*/ null, null, null,
		    /*"import",*/ null, null, null,
		    /*"public", "class",*/ null, IN_GENERIC, /*"extends",*/ IN_GENERIC, IN_GENERIC, IN_GENERIC, IN_GENERIC, IN_GENERIC, IN_GENERIC, IN_GENERIC,
		    IN_CLASS, IN_CLASS,
		    /*"char",*/ IN_CLASS,
		    IN_CLASS, IN_CLASS,
		    IN_CLASS, IN_CLASS,
		    /*"public",*/ IN_GENERIC, /*"boolean",*/ IN_CLASS, /*"int",*/ IN_PARAMETERS_DECLARATION, /*"int",*/ IN_PARAMETERS_DECLARATION,
		    /*"int",*/ IN_METHOD, /*"int",*/ IN_METHOD, IN_METHOD,
		    /*"boolean",*/ IN_METHOD, IN_METHOD, IN_METHOD, IN_METHOD, IN_METHOD, IN_METHOD, IN_METHOD, IN_METHOD, IN_GENERIC, IN_METHOD, /*"null",*/
		    /*"boolean",*/ IN_METHOD, IN_UNCERTAIN_PAREN, IN_UNCERTAIN_PAREN,
		    /*"return",*/ IN_METHOD, IN_METHOD,
		    IN_CLASS, IN_CLASS, IN_CLASS, IN_CLASS, IN_GENERIC, IN_GENERIC, IN_CLASS, IN_PARAMETERS_DECLARATION, IN_PARAMETERS_DECLARATION,
		    /*"int",*/ IN_LAMBDA_BLOCK,
		    /*"while",*/ IN_BOOLEAN_EXPRESSION,
		    IN_WHILE,
		    /*"return", "switch",*/ IN_BOOLEAN_EXPRESSION,
		    /*"case",*/ IN_SWITCH_CASE,
		    /*"default", "yield",*/ IN_SWITCH_CASE_BLOCK
		);
	
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
	
	@BeforeEach
	void beforeEachSetup(){
		extractor = new SingleFileExtractor();
	}
	
	@Test
	void singleFileExtractor_ShouldNotThrowException_WhenTokenizeFile() {
		extractor.tokenizeFile(TEST_PATH, mapTest.get(TEST_PATH));
	}
	
	@Test
	void singleFileExtractor_ShouldExtractCorrectToken_WhenTokenizeFile() {
		FileExtraction result = extractor.tokenizeFile(TEST_PATH, mapTest.get(TEST_PATH));
		List<String>tokenNameList =result.tokens().tokenList().stream()
									.map(i->i.name())
									.toList();
		assertIterableEquals(expectedTokenNames, tokenNameList, "La liste des tokens ne correspond pas");
	}
	
	@Test
	void singleFileExtractor_ShouldExtractCorrectContext_WhenTokenizeFile() {
		FileExtraction result = extractor.tokenizeFile(TEST_PATH, mapTest.get(TEST_PATH));
		List<TypeContext>tokencontextList =result.tokens().tokenList().stream()
									.map(i->i.context().getCurrentContext())
									.toList();
		assertIterableEquals(expectedContext, tokencontextList, "La liste des context ne correspond pas");
	}

}
