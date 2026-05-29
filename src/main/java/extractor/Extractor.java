package extractor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import extractor.exception.ParserException;
import loader.DataContext;
import shared.Failure;
import shared.PipeResult;
import shared.Success;

public class Extractor {
	
	public static PipeResult<ExtractorPayload> extract(Map<Path,DataContext> mapData){
		List<FileExtraction> extractionResult = null;
		try {
			extractionResult = mapData.keySet().stream()
											   .map((i)->extractOne(mapData, i))
											   .toList();
		} catch (ParserException e) {
			return new Failure<>(e.getMessage());
		}
		return new Success<>(mergeResult(extractionResult));
	}

	private Optional<Token> extractToken(char c, int index, char[] file) {
		if(ignoredContext.contains(contextController.getState().getCurrentContext())) {return Optional.empty();}
		Token token = null;
		int wordStart = identifierTracker.trackIdentifierBoundary(c, index);
		if (wordStart != -1) {
			String result = getFromIndex(wordStart, index, file);
			token = buildToken(index, wordStart, result);
			log.trace("[TOKEN] emit token: file = {}, token = {}", file, token);
			return Optional.of(token);
		} 
		return Optional.empty();
	}

	private void processCharacter(char c, int index, char[] file){
		StateSnapshot contextSnapshot = contextController.getState();
		
		if(escape) {escape = false; return;}
		if(c=='\\') {escape = true; return;}
		
		commentTracker.trackCommentTransition(c, contextSnapshot.getCurrentContext()).ifPresent((i)->contextController.handleEvent(i));
		textBlockTracker.trackTextBlockTransition(c, contextSnapshot.getCurrentContext()).ifPresent((i)->contextController.handleEvent(i));
		stringTracker.trackStringTransition(c, contextSnapshot.getCurrentContext()).ifPresent((i)->contextController.handleEvent(i));
		charTracker.trackCharacter(c, contextSnapshot.getCurrentContext()).ifPresent((i)->contextController.handleEvent(i));
		
		if(isIgnoredContext(contextSnapshot)){logTransition(index, contextSnapshot);return;}
		
		arrowTracker.trackArrowTransition(c, contextController.getState().getCurrentContext()).ifPresent((i)->contextController.handleEvent(i));
		genericTracker.trackGenericTransition(c, file, index, contextController.currentContext()).ifPresent((i)->contextController.handleEvent(i));
		
		closeBraceLessContexts(c, contextSnapshot);
		applyStructuralTransition(c, index);
		
		logTransition(index, contextSnapshot);
		return;
	}

	private void handleWord(Token token, char[] file, Consumer<Token> add) {
		if("class".equals(token.name())) {
			if((token.endIndex()<file.length && file[token.endIndex()]=='.') || 
					(token.startIndex() > 0 && file[token.startIndex()-1]=='.')) {
				return;
				}
		}
		if("do".equals(token.name())) {inDoBlock = true;}
		if("while".equals(token.name()) && inDoBlock) {inDoBlock = false;return;}
		if (JAVA_KEY_WORDS.contains(token.name())) {contextController.prepareContext(token.name());return;}
		if (!JAVA_KEY_WORDS.contains(token.name()) && rootContext.contains(contextController.getState().getPreparedContext())) {
			if(contextController.getState().getPreparedContext()==TypeContext.IN_ENUM) {
				knowEnums.add(token.name());
			}
			knowTypes.add(token.name());
		}
		add.accept(token);
	}

	private void handleEofWord(List<Token> tokens, char[] file, int startIndexOfLastWord) {
		handleWord(buildToken(startIndexOfLastWord,
							file.length,getFromIndex(startIndexOfLastWord, 
													file.length, 
													file)),
							file,
							(token)->tokens.add(token));
	}

	private void logTransition(int index, StateSnapshot contextSnapshot) {
		if(hasStateTransitioned(contextSnapshot)) {
			log.debug("[TRANSITION]: {} ===> {} // file: {} // index: {} // line :{}", contextSnapshot, contextController.getState().getCurrentContext(),filename, index, findLine(index,offsets));
		}
	}

	private void closeBraceLessContexts(char c, StateSnapshot contextSnapshot) {
		if(contextController.getState().getPreparedContext() == TypeContext.IN_FOR) {return;}
		if(c==';' && contextController.getState().getPreparedContext() != null && closableWithoutBraceContext.contains(contextController.getState().getPreparedContext())){
			contextController.closeContext();
		}
	}

	private void applyStructuralTransition(char c, int index) {
		if(c=='{') {
			contextController.openBraceContext();
		}else if (c=='('){
			contextController.openParenContext();
		}else if(c=='}' || c==')') {
			if(!contextController.closeContext()) {
				log.debug("[CONTEXT]: Try to pop empty context : file = {} index = {}", filename, index);
			}
		}
	}

	private static FileExtraction extractOne(Map<Path, DataContext> mapData, Path path) {
		SingleFileExtractor extractorAgent = new SingleFileExtractor();
		 DataContext file = mapData.get(path);
		 return extractorAgent.tokenizeFile(path, file);
	}
	
	private static ExtractorPayload mergeResult( List<FileExtraction> extractionResult ) {
		Set<String> types = new HashSet<>();
		Set<String> enums = new HashSet<>();
		
		for (FileExtraction rawResult : extractionResult) {
            types.addAll(rawResult.knownTypes());
            enums.addAll(rawResult.knownEnums());
        }
		Map<Path, TokenizedFile> tokenMap = extractionResult.stream()
																.map(i->i.tokens())
																.collect(Collectors.toMap(i->i.path(), i->i));
		return new ExtractorPayload(tokenMap, types, enums);
	}

}
