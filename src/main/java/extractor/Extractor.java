package extractor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import loader.DataContext;

public class Extractor {
	
	private static final Logger log = LoggerFactory.getLogger(Extractor.class);
	private final ContextController contextController = new ContextController();
	private final ArrowTracker arrowTracker = new ArrowTracker();
	private final TextBlockTracker textBlockTracker = new TextBlockTracker();
	private final CommentTracker commentTracker = new CommentTracker();
	private final CharTracker charTracker = new CharTracker();
	private final StringTracker stringTracker = new StringTracker();
	private final GenericTracker genericTracker = new GenericTracker();
	private final IdentifierTracker identifierTracker = new IdentifierTracker();
	private boolean inDoBlock = false;
	private boolean escape = false;
	private String filename = "";
	
	private final static EnumSet<TypeContext> ignoredContext = EnumSet.of(TypeContext.IN_STRING, 
																	TypeContext.IN_CHAR, 
																	TypeContext.IN_COMMENT_LINE, 
																	TypeContext.IN_COMMENT_BLOCK,
																	TypeContext.IN_TEXT_BLOCK);
	
	private final static EnumSet<TypeContext> rootContext = EnumSet.of(TypeContext.IN_CLASS, 
																	TypeContext.IN_RECORD, 
																	TypeContext.IN_INTERFACE, 
																	TypeContext.IN_ENUM);
	
	private static final Set<TypeContext> implicitContext = Set.of(TypeContext.IN_IF,
																TypeContext.IN_FOR,
																TypeContext.IN_WHILE,
																TypeContext.IN_ELSE);
	
	private final static Set<String> JAVA_KEY_WORD = Set.of("abstract", "assert", "boolean", "break", "byte", "case", "catch",
			"char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally",
			"float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new",
			"package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
			"synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "var", "record",
			"sealed", "permits", "non-sealed", "true", "false", "null", "Enum", "Record", "Class");
	
	public List<Token> extract(Map<Path, DataContext>pathList) {
		final List<Token> tokens = new ArrayList<>();
		pathList.keySet().stream()
						 .forEach(i->{
							 DataContext file = pathList.get(i);
							 filename=i.toString();
							 tokens.addAll(findTokens(file));
						 });
		return tokens;
	}
	
	private List<Token> findTokens(DataContext datacontext) {
		List<Token> tokens = new ArrayList<>();
		
		char[] file = datacontext.fileContent();
		for(int i=0; i<file.length; i++) {
			char currentChar = file[i];
			Optional<Token> maybeWord = handleChar(currentChar, i, file);
			maybeWord.ifPresent((word)-> handleWord(word, (token)->tokens.add(token)));
		}
		int startIndexOfLastWord = wordTracker.endOfFileCleaning();
		if (startIndexOfLastWord != -1) {handleWord(
											buildToken(startIndexOfLastWord,
													file.length,
													getFromIndex(
															startIndexOfLastWord, 
															file.length, file)), 
													(token)->tokens.add(token));}
		cleanContext();
		return tokens;
	}

	private Optional<Token> extractToken(char c, int index, char[] file) {
		if(ignoredContext.contains(contextController.getState().getCurrentContext())) {return Optional.empty();}
		Token token = null;
		int wordStart = identifierTracker.trackIdentifierBoundary(c, index);
		if (wordStart != -1) {
			String result = getFromIndex(wordStart, index, file);
			if("class".equals(result)) {
				if(c=='.' || file[wordStart-1]=='.') {result = result.toUpperCase();}
			}
			token = buildToken(index, wordStart, result);
			log.trace("[TOKEN] emit token: file = {}, token = {}", file, token);
			return Optional.of(token);
		} 
		return Optional.empty();
	}

	private void processCharacter(char c, int index, char[] file){
		StateSnapshot contextSnapshot = contextController.getState();
		
		//escape sequence
		if(escape) {escape = false; return Optional.empty();}
		if(c=='\\') {escape = true; return Optional.empty();}
		
		commentTracker.trackCommentTransition(c, contextSnapshot.getCurrentContext()).ifPresent((i)->contextController.handleEvent(i));
		textBlockTracker.trackTextBlockTransition(c, contextSnapshot.getCurrentContext()).ifPresent((i)->contextController.handleEvent(i));
		stringTracker.trackStringTransition(c, contextSnapshot.getCurrentContext()).ifPresent((i)->contextController.handleEvent(i));
		charTracker.trackCharacter(c, contextSnapshot.getCurrentContext()).ifPresent((i)->contextController.handleEvent(i));
		
		//ignored cases support (order is important)
		if(c==';' && contextSnapshot.getPreparedContext() != null && implicitContext.contains(contextSnapshot.getPreparedContext())){
			context.consumePreparedContext();
		}
		
		arrowTracker.trackArrowTransition(c, contextController.getState().getCurrentContext()).ifPresent((i)->contextController.handleEvent(i));
		genericTracker.trackGenericTransition(c, file, index, contextController.currentContext()).ifPresent((i)->contextController.handleEvent(i));
		
		//update the depth increment first
		depthTracker.incrementDepth(c);
		
		//short-circuiting specific contexts
		if(c == '{' && contextSnapshot.getCurrentContext() == TypeContext.IN_LAMBDA) {
			context.pushContext(TypeContext.IN_LAMBDA_BLOCK);
			log.trace("[PIPE]: Commit lambda block context: char = {}, index = {}, file = {}", c, index, filename);
			return emptyOrToken(token, contextSnapshot, index);
		}
		if(c == '{' && !context.isPreparedContext() && rootContext.contains(contextSnapshot.getCurrentContext())) {
			context.pushContext(TypeContext.IN_METHODE);
			return emptyOrToken(token, contextSnapshot, index);
		}
		
		//then analyze specifics contexts
		lambdaTracker.processLambdaTracking(c, depthTracker.getBraceDepth(), depthTracker.getParentDepth(), context.currentContext());
		if(contextSnapshot.getCurrentContext() != context.currentContext()) {depthTracker.decrementDepth(c);return emptyOrToken(token, contextSnapshot, index);}
		
		genericTracker.processGenericTracker(c, file, index, context.currentContext());
		
		//Update the context stack if a context is prepared; otherwise, fall back to IN_METHODE
		if(c=='{') {
			if(!context.isPreparedContext()) {
				log.trace("[PIPE]: fallback in methode context: char = {}, index = {}, file = {}", c, index, filename);
				context.pushContext(TypeContext.IN_METHODE);
			}else {
				context.pushPreparedContext();
			}
			return emptyOrToken(token, contextSnapshot, index);
		}
		if(c=='}') {
			if(!context.popContext()) {
				log.debug("[PIPE]: Try to pop empty context : file = {} index = {}", filename, index);
			}
		}
		
		depthTracker.decrementDepth(c);
		
		//Implicit end-of-life binding of IN_LAMBDA_BLOCK and IN_LAMBDA
		/*if(contextSnapshot.getCurrentContext() == TypeContext.IN_LAMBDA_BLOCK && context.currentContext() == TypeContext.IN_LAMBDA) {
			log.trace("[PIPE]: Implicit lambda block closure: char = {}, index = {}, file = {}", c, index, filename);
			lambdaTracker.processLambdaTracking(c, depthTracker.getBraceDepth(), depthTracker.getParentDepth(), context.currentContext());
		}*/
		
		return emptyOrToken(token, contextSnapshot, index);
	}

	private Token buildToken(int index, int wordStart, String result) {
		return new Token(filename, result, wordStart, 
				index, depthTracker.getBraceDepth(), 
				depthTracker.getParentDepth(), context.currentContext());
	}

	private Optional<Token> emptyOrToken(Token result, StateSnapshot snapshot, int index) {
		if(snapshot.getCurrentContext() != context.currentContext()) {
			log.debug("[TRANSITON]: {} ===> {} // file: {} // index: {}", snapshot, context.currentContext(),filename, index);
		}
		if(result!=null) {
			log.trace("[TOKEN]: Emit token: {} // file: {} // index: {}", result, filename, index);
		}
		return result == null ? Optional.empty() : Optional.of(result);
	}

	private void handleWord(Token token, Consumer<Token> add) {
		if("do".equals(token.name())) {inDoBlock = true;}
		if("while".equals(token.name()) && inDoBlock) {inDoBlock = false;return;}
		if (JAVA_KEY_WORD.contains(token.name())) {context.prepareContext(token.name());return;}
		add.accept(token);
	}

	private void cleanContext() {
		log.debug("[EOF] {}: Context stack state: {}, Depth State: brace: {} : paren: {}", filename, context.debugContextStack(), depthTracker.getBraceDepth(), depthTracker.getParentDepth());
		depthTracker.clearDepthTracker();
		context.clear();
	}

	private String getFromIndex(int startIndex, int endIndex, char[] file) {
		return String.copyValueOf(file, startIndex, endIndex-startIndex);
	}
}
