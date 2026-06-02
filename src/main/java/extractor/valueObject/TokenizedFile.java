package extractor.valueObject;

import java.nio.file.Path;
import java.util.List;

import loader.DataContext;

public record TokenizedFile(List<Token> tokenList,
							DataContext dataContext,
							Path path) {

}
