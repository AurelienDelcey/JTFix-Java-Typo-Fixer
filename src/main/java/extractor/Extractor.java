package extractor;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import loader.DataContext;

public class Extractor {
	
	private final Deque<TypeContext> context = new ArrayDeque<>();
	private final LambdaTracker lambdaTracker = new LambdaTracker((i)->context.push(i), (i)->context.pop());
	private final TextBlockTracker textBlockTracker = new TextBlockTracker((i)->context.push(i), (i)->context.pop());
	private final CommentTracker commentTracker = new CommentTracker((i)->context.push(i), (i)->context.pop());
	private final CharTracker charTracker = new CharTracker((i)->context.push(i), (i)->context.pop());
	private final StringTracker stringTracker = new StringTracker((i)->context.push(i), (i)->context.pop());
	private final GenericTracker genericTracker = new GenericTracker((i)->context.push(i), (i)->context.pop());
	private final DepthTracker depthTracker = new DepthTracker();
	private final WordTracker wordTracker = new WordTracker();
	private PreparedContext preparedContext = null;
	private String filename = "";
	private boolean inDoBlock = false;
	private boolean escape = false;
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

	private void cleanContext() {
		preparedContext=null;
		depthTracker.clearDepthTracker();
		if(context.peek()!=null) {System.out.println("ERROR: "+context.peek() +" // "+ filename+ " //"+depthTracker.getBraceDepth());}
		context.clear();
	}
	
	private void handleWord(String word, Consumer<String> add) {
		if("do".equals(word)) {inDoBlock = true;}
		if("while".equals(word) && inDoBlock) {inDoBlock = false;return;}
		if("class".equals(word) && prevChar == '.') {prevChar = ' ';return;}
		if (JAVA_KEY_WORD.contains(word)) {prepareContext(word);return;}
		add.accept(word);
	}
	
	private void prepareContext(String word) {
		switch(word) {
		case "class" -> preparedContext = new PreparedContext(TypeContext.IN_CLASS);
		case "record" -> preparedContext = new PreparedContext(TypeContext.IN_RECORD);
		case "interface" -> preparedContext = new PreparedContext(TypeContext.IN_INTERFACE);
		case "enum" -> preparedContext = new PreparedContext(TypeContext.IN_ENUM);
		case "if" -> preparedContext = new PreparedContext(TypeContext.IN_IF);
		case "for" -> preparedContext = new PreparedContext(TypeContext.IN_FOR);
		case "while" -> preparedContext = new PreparedContext(TypeContext.IN_WHILE);
		case "do" -> preparedContext = new PreparedContext(TypeContext.IN_DO);
		case "try" -> preparedContext = new PreparedContext(TypeContext.IN_TRY);
		case "catch" -> preparedContext = new PreparedContext(TypeContext.IN_CATCH);
		case "switch" -> preparedContext = new PreparedContext(TypeContext.IN_SWITCH);
		case "finaly" -> preparedContext = new PreparedContext(TypeContext.IN_FINALY);
		case "else" -> preparedContext = new PreparedContext(TypeContext.IN_ELSE);
		}
	}
	
	private Optional<String> handleChar(char c, int index, char[] file){
		TypeContext firstContextOfStep = context.peek();
		if(escape) {escape = false; return Optional.empty();}
		if(c=='\\') {escape = true; return Optional.empty();}
		
		commentTracker.processCommentTracker(c, context.peek());
		if(firstContextOfStep == TypeContext.IN_COMMENT_BLOCK || context.peek() == TypeContext.IN_COMMENT_LINE) {return Optional.empty();}
		textBlockTracker.processTextBlockTracker(c, context.peek());
		if(firstContextOfStep == TypeContext.IN_TEXT_BLOCK) {return Optional.empty();}
		
		stringTracker.processStringTracker(c, context.peek());
		if(firstContextOfStep == TypeContext.IN_STRING) {return Optional.empty();}
		
		charTracker.processCharTracker(c, context.peek());
		if(firstContextOfStep == TypeContext.IN_CHAR) {return Optional.empty();}
		
		depthTracker.updateDepth(c);
		lambdaTracker.processLambdaTracking(c, depthTracker.getBraceDepth(), depthTracker.getParentDepth(), context.peek());
		genericTracker.processGenericTracker(c, file, index, context.peek());
		checkForClassKeyConsistency(c);
		manageContext(c);
		int wordStart = wordTracker.processWordTracker(c, index);
		if(wordStart != -1) {return Optional.of(getFromIndex(wordStart, index, file));}
		return Optional.empty();
	}
	
	private void manageContext(char c) {
		if(c=='{') {commitContext();return;}
		if(c=='}') {popContext();return;}
	}

	private void checkForClassKeyConsistency(char c) {
		if(preparedContext == null) {return;}
		if(preparedContext.context() == TypeContext.IN_CLASS && c=='.') {preparedContext=null;}
	}
	
	private void commitContext() {
		if(depthTracker.getBraceDepth() >= 1 && preparedContext == null && context.peek() != TypeContext.IN_LAMBDA) {
			context.push(TypeContext.IN_METHODE);
			return;
		}
		if(preparedContext != null) {
			context.push(preparedContext.context());
			preparedContext = null;
		}
	}
	
	private void popContext() {
		if (context.peek() != null) {
			context.pop();
		}
	}

	private String getFromIndex(int startIndex, int endIndex, char[] file) {
		return String.copyValueOf(file, startIndex, endIndex-startIndex);
	}
}
