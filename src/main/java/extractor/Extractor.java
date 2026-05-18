package extractor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import loader.DataContext;

public class Extractor {
	
	private final ContextController context = new ContextController();
	private final LambdaTracker lambdaTracker = new LambdaTracker((i)->context.pushContext(i), (i)->context.popContext());
	private final TextBlockTracker textBlockTracker = new TextBlockTracker((i)->context.pushContext(i), (i)->context.popContext());
	private final CommentTracker commentTracker = new CommentTracker((i)->context.pushContext(i), (i)->context.popContext());
	private final CharTracker charTracker = new CharTracker((i)->context.pushContext(i), (i)->context.popContext());
	private final StringTracker stringTracker = new StringTracker((i)->context.pushContext(i), (i)->context.popContext());
	private final GenericTracker genericTracker = new GenericTracker((i)->context.pushContext(i), (i)->context.popContext());
	private final DepthTracker depthTracker = new DepthTracker();
	private final WordTracker wordTracker = new WordTracker();
	private boolean inDoBlock = false;
	private boolean escape = false;
	private String filename = "";
	private char prevChar = ' ';
	
	private final EnumSet<TypeContext> ignoredContext = EnumSet.of(TypeContext.IN_STRING, 
																	TypeContext.IN_CHAR, 
																	TypeContext.IN_COMMENT_LINE, 
																	TypeContext.IN_COMMENT_BLOCK,
																	TypeContext.IN_TEXT_BLOCK);
	
	private final static Set<String> JAVA_KEY_WORD = Set.of("abstract", "assert", "boolean", "break", "byte", "case", "catch",
			"char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally",
			"float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new",
			"package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
			"synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "var", "record",
			"sealed", "permits", "non-sealed", "true", "false", "null", "Enum", "Record", "Class");
	
	public List<String> extract(Map<Path, DataContext>pathList) {
		final List<String> tokens = new ArrayList<>();
		pathList.keySet().stream()
						 .forEach(i->{
							 DataContext file = pathList.get(i);
							 filename=i.toString();
							 tokens.addAll(findTokens(file));
						 });
		return tokens;
	}
	
	private List<String> findTokens(DataContext datacontext) {
		List<String> tokens = new ArrayList<>();
		
		char[] file = datacontext.fileContent();
		for(int i=0; i<file.length; i++) {
			char currentChar = file[i];
			Optional<String> maybeWord = handleChar(currentChar, i, file);
			maybeWord.ifPresent((word)-> handleWord(word, (token)->tokens.add(token)));
		}
		int startIndexOfLastWord = wordTracker.endOfFileCleaning();
		if (startIndexOfLastWord != -1) {handleWord(getFromIndex(startIndexOfLastWord, file.length, file), (token)->tokens.add(token));}
		cleanContext();
		return tokens;
	}

	private Optional<String> handleChar(char c, int index, char[] file){
		TypeContext contextSnapshot = context.currentContext();
		String result = null;
		if(escape) {escape = false; return Optional.empty();}
		if(c=='\\') {escape = true; return Optional.empty();}
		
		
		if (!ignoredContext.contains(contextSnapshot)) {
			int wordStart = wordTracker.processWordTracker(c, index);
			if (wordStart != -1) {
				result = getFromIndex(wordStart, index, file);
			} 
		}
		
		commentTracker.processCommentTracker(c, context.currentContext());
		if(contextSnapshot == TypeContext.IN_COMMENT_BLOCK || context.currentContext() == TypeContext.IN_COMMENT_LINE) {return result == null ? Optional.empty() : Optional.of(result);}
		
		textBlockTracker.processTextBlockTracker(c, context.currentContext());
		if(contextSnapshot == TypeContext.IN_TEXT_BLOCK) {return result == null ? Optional.empty() : Optional.of(result);}
		
		stringTracker.processStringTracker(c, context.currentContext());
		if(contextSnapshot == TypeContext.IN_STRING) {return result == null ? Optional.empty() : Optional.of(result);}
		
		charTracker.processCharTracker(c, context.currentContext());
		if(contextSnapshot == TypeContext.IN_CHAR) {return result == null ? Optional.empty() : Optional.of(result);}
		
		
		depthTracker.updateDepth(c);
		if(c=='{') {context.pushPreparedContext();}
		if(c=='}') {context.popIfExplicitContext();}
		
		lambdaTracker.processLambdaTracking(c, depthTracker.getBraceDepth(), depthTracker.getParentDepth(), context.currentContext());
		if(contextSnapshot != context.currentContext()) {return result == null ? Optional.empty() : Optional.of(result);}
		genericTracker.processGenericTracker(c, file, index, context.currentContext());
		//checkForClassKeyConsistency(c);
		return result == null ? Optional.empty() : Optional.of(result);
	}

	private void handleWord(String word, Consumer<String> add) {
		if("do".equals(word)) {inDoBlock = true;}
		if("while".equals(word) && inDoBlock) {inDoBlock = false;return;}
		if("class".equals(word) && prevChar == '.') {prevChar = ' ';return;}
		if (JAVA_KEY_WORD.contains(word)) {context.prepareContext(word);return;}
		add.accept(word);
	}

	private void cleanContext() {
		depthTracker.clearDepthTracker();
		if(context.currentContext()!=null) {System.out.println("ERROR: "+context.currentContext() +" // "+ filename+ " //"+depthTracker.getBraceDepth());}
		context.clear();
	}

	private String getFromIndex(int startIndex, int endIndex, char[] file) {
		return String.copyValueOf(file, startIndex, endIndex-startIndex);
	}
}
