package extractor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import extractor.state.ContextController;
import extractor.state.StateSnapshot;
import extractor.state.TypeContext;
import extractor.tracker.ArrowTracker;
import extractor.tracker.CharTracker;
import extractor.tracker.CommentTracker;
import extractor.tracker.GenericTracker;
import extractor.tracker.IdentifierTracker;
import extractor.tracker.StringTracker;
import extractor.tracker.TextBlockTracker;
import extractor.valueObject.FileExtraction;
import extractor.valueObject.Token;
import extractor.valueObject.TokenizedFile;
import loader.DataContext;

public class SingleFileExtractor {
	
	private static final Logger log = LoggerFactory.getLogger(SingleFileExtractor.class);
	private final ContextController contextController = new ContextController();
	private final ArrowTracker arrowTracker = new ArrowTracker();
	private final TextBlockTracker textBlockTracker = new TextBlockTracker();
	private final CommentTracker commentTracker = new CommentTracker();
	private final CharTracker charTracker = new CharTracker();
	private final StringTracker stringTracker = new StringTracker();
	private final GenericTracker genericTracker = new GenericTracker();
	private final IdentifierTracker identifierTracker = new IdentifierTracker();
	private final Set<String> knownTypes = new HashSet<>();
	private final Set<String> knownEnums = new HashSet<>();
	private boolean inDoBlock = false;
	private boolean escape = false;
	private String filename = "";
	private int[] offsets = null;
	
	private final static EnumSet<TypeContext> ignoredContext = EnumSet.of(TypeContext.IN_STRING, 
																		TypeContext.IN_CHAR, 
																		TypeContext.IN_COMMENT_LINE, 
																		TypeContext.IN_COMMENT_BLOCK,
																		TypeContext.IN_TEXT_BLOCK);
	
	private static final Set<TypeContext> closableWithoutBraceContext = Set.of(TypeContext.IN_IF,
																			TypeContext.IN_FOR,
																			TypeContext.IN_WHILE,
																			TypeContext.IN_ELSE);
	
	private final static EnumSet<TypeContext> rootContext = EnumSet.of(TypeContext.IN_CLASS, 
																	TypeContext.IN_RECORD, 
																	TypeContext.IN_INTERFACE, 
																	TypeContext.IN_ENUM);
	
	private final static Set<String> JAVA_KEY_WORDS = Set.of("abstract", "assert", "boolean", "break", "byte", "case", "catch",
			"char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally",
			"float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new",
			"package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
			"synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "var", "record",
			"sealed", "permits", "non-sealed", "true", "false", "null");
	
	FileExtraction tokenizeFile(Path path, DataContext datacontext) {
		List<Token> tokens = new ArrayList<>();
		char[] file = datacontext.fileContent();
		filename = path.getFileName().toString();
		offsets = datacontext.linesOffsets();
		
		for(int i=0; i<file.length; i++) {
			char currentChar = file[i];
			Optional<Token> maybeToken = extractToken(currentChar, i, file);
			maybeToken.ifPresent((word)-> handleWord(word, file, (token)->tokens.add(token)));
			processCharacter(currentChar, i, file);
		}
		
		int startIndexOfLastWord = identifierTracker.flushPendingIdentifier();
		if (startIndexOfLastWord != -1) {handleEofWord(tokens, file, startIndexOfLastWord);}
		
		finalizeFileParsing();
		return new FileExtraction(new TokenizedFile(tokens, datacontext, path), knownTypes, knownEnums);
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
				knownEnums.add(token.name());
			}
			knownTypes.add(token.name());
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

	private boolean hasStateTransitioned(StateSnapshot contextSnapshot) {
		return !contextSnapshot.equals(contextController.getState());
	}

	private Token buildToken(int index, int wordStart, String result) {
		return new Token(filename, result, wordStart, 
				index, contextController.getState());
	}

	private int findLine(int index, int[] offsets) {
		int result = 0;
		if(index > offsets[offsets.length-1]) {return offsets.length;}
		
		result = Arrays.binarySearch(offsets, index);
		
		return result < 0 ? -(result)-1:result;
	}

	private void finalizeFileParsing() {
		log.debug("[EOF] {}: Context stack state = {}, Depth: brace = {} / paren = {}", filename, contextController.debugContextStack(), contextController.getState().getBraceDepth(), contextController.getState().getParenDepth());
		contextController.clear();
	}

	private String getFromIndex(int startIndex, int endIndex, char[] file) {
		return String.copyValueOf(file, startIndex, endIndex-startIndex);
	}

	private boolean isIgnoredContext(StateSnapshot contextSnapshot) {
		return contextController.getState().getCurrentContext() != null && 
				ignoredContext.contains(contextController.getState().getCurrentContext()) ||
				contextSnapshot.getCurrentContext() != null && 
				ignoredContext.contains(contextSnapshot.getCurrentContext());
	}
}
