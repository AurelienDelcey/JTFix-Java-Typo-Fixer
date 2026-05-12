package extractor;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import loader.DataContext;
import shared.PipeResult;

public class Extractor {
	
	private final Deque<TypeContext> context = new ArrayDeque<>();
	private PreparedContext preparedContext = null;
	private boolean inWord = false;
	private boolean inDoBlock = false;
	private boolean escape = false;
	private boolean firstStepLambdaSequence = false;
	private boolean secondStepLambdaSequence = false;
	private boolean followLambdaDepth = false;
	private int startIndex = -1;
	private int braceDepth = 0;
	private int parenDepth = 0;
	private int relativeLambdaParentDepth = -1;
	private char prevChar = ' ';
	
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
		if (inWord) {handleWord(getFromIndex(startIndex, file.length, file), (token)->tokens.add(token));}
		cleanContext();
		return tokens;
	}

	private void cleanContext() {
		preparedContext=null;
		startIndex = -1;
		inWord = false;
		braceDepth=0;
		parenDepth=0;
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
		if(escape) {escape = false; return Optional.empty();}
		if(c=='\\') {escape = true; return Optional.empty();}
		checkForIgnoredContexts(c);
		if(context.peek() == TypeContext.IN_CHAR || context.peek() == TypeContext.IN_STRING) {return Optional.empty();}
		checkForLambda(c);
		checkForClassKeyConsistency(c);
		updateParenDepth(c);
		updateBraceDepth(c);
		if(inWord == false && isIdentifyerStart(c)) {startWordDetection(index); return Optional.empty();}
		if(inWord == true && !isIdentifyerPart(c)){
				String word = getWord(index, file);
				prevChar = startIndex > 0 ? file[startIndex-1]:prevChar;//TODO methode perv non blank char
				stopWordDetection();
				return word != null ? Optional.of(word) : Optional.empty();
			}
		return Optional.empty();
	}
	
	private void checkForIgnoredContexts(char c) {
		if(c=='"') {
			if(context.peek() != TypeContext.IN_STRING) {context.push(TypeContext.IN_STRING);System.out.println("PUSH: String");return;}
			if(context.peek() == TypeContext.IN_STRING) {context.pop();System.out.println("POP: String ");return;}
		}
		if(c=='\'' && context.peek() != TypeContext.IN_STRING) {
			if(context.peek() != TypeContext.IN_CHAR) {context.push(TypeContext.IN_CHAR);System.out.println("PUSH: Char");return;}
			if(context.peek() == TypeContext.IN_CHAR) {context.pop();System.out.println("POP: Char ");return;}
		}
	}

	private void checkForClassKeyConsistency(char c) {
		if(preparedContext == null) {return;}
		if(preparedContext.context() == TypeContext.IN_CLASS && c=='.') {preparedContext=null;}
	}

	private void checkForLambda(char c) {
		if(c=='(' && followLambdaDepth) {relativeLambdaParentDepth++;}
		if(c==')' && followLambdaDepth) {relativeLambdaParentDepth--;}
		if(c=='\n' || c == ' ' || c == '\t' || context.peek() == TypeContext.IN_SWITCH) {return;}
		if(((c==',' || c == ';' || c == ')') && relativeLambdaParentDepth == 0) && context.peek() == TypeContext.IN_LAMBDA_STATEMENT) {followLambdaDepth = false ;System.out.println("pop : " + context.peek()); context.pop();}
		if(secondStepLambdaSequence && c =='{') {
			context.push(TypeContext.IN_LAMBDA_BLOCK); 
			System.out.println("push : " + context.peek());
			cleanLambdaSequence();
		}else if (secondStepLambdaSequence) {
			context.push(TypeContext.IN_LAMBDA_STATEMENT);
			followLambdaDepth = true;
			relativeLambdaParentDepth = 0;
			System.out.println("push : " + context.peek());
			cleanLambdaSequence();
		}
		if(firstStepLambdaSequence && c =='>') {
			secondStepLambdaSequence = true;
		}else {cleanLambdaSequence();}
		if(c=='-') {firstStepLambdaSequence = true;}
	}

	private void cleanLambdaSequence() {
		firstStepLambdaSequence = false;
		secondStepLambdaSequence = false;
	}

	private void updateParenDepth(char c) {
		if(c == '(') {parenDepth++;}
		if(c == ')') {parenDepth--;}
	}
	
	private void updateBraceDepth(char c) {
		if(c == '{') {braceDepth++; commitContext();}
		if(c == '}') {braceDepth--; popContext();}
	}
	
	private void commitContext() {
		if(braceDepth >= 1 && preparedContext == null && context.peek() != TypeContext.IN_LAMBDA_BLOCK) {
			context.push(TypeContext.IN_METHODE);
			System.out.println("push : " + context.peek());
			return;
		}
		if(preparedContext != null) {
			context.push(preparedContext.context());
			System.out.println("push : " + context.peek());
			preparedContext = null;
		}
	}
	
	private void popContext() {
		if (context.peek() != null) {
			System.out.println("pop : " + context.peek());
			context.pop();
		}
	}
	
	private String getWord(int index, char[] file) {
		if (index-startIndex > 1) {
			return getFromIndex(startIndex, index, file);
		}
		return null;
	}
	
	private void startWordDetection(int index) {
		startIndex = index;
		inWord = true;
	}
	
	private void stopWordDetection() {
		startIndex = -1;
		inWord = false;
	}

	private String getFromIndex(int startIndex, int i, char[] file) {
		return String.copyValueOf(file, startIndex, i-startIndex);
	}

	private boolean isIdentifyerPart (char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == '$';
	}
	
	private boolean isIdentifyerStart (char c) {
		return Character.isLetter(c) || c == '_' || c == '$';
	}
}
