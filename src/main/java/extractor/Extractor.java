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
	private int startIndex = -1;
	private int braceDepth = 0;
	private int parenDepth = 0;
	
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
		return tokens;
	}
	
	private void handleWord(String word, Consumer<String> add) {
		if (JAVA_KEY_WORD.contains(word)) {prepareContext(word);return;}
		add.accept(word);
	}
	
	private void prepareContext(String word) {
		if(Objects.equals("class", word)) {preparedContext = new PreparedContext(TypeContext.IN_CLASS);}
	}
	
	private Optional<String> handleChar(char c, int index, char[] file){
		if(inWord == false && isIdentifyerStart(c)) {startWordDetection(index); return Optional.empty();}
		if(inWord == true && !isIdentifyerPart(c)){
				String word = getWord(index, file);
				stopWordDetection();
				return word != null ? Optional.of(word) : Optional.empty();
			}
		updateParenDepth(c);
		updateBraceDepth(c);
		return Optional.empty();
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
