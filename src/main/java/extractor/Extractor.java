package extractor;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import loader.DataContext;
import shared.PipeResult;

public class Extractor {
	
	private final static Set<String> JAVA_KEY_WORD = Set.of("abstract", "assert", "boolean", "break", "byte", "case", "catch",
			"char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally",
			"float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new",
			"package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
			"synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "var", "record",
			"sealed", "permits", "non-sealed", "true", "false", "null", "Enum", "Record", "Class");
	
	public static List<String> extract(Map<Path, DataContext>pathList) {
		final List<String> words = new ArrayList<>();
		pathList.keySet().stream()
						 .forEach(i->{
							 DataContext file = pathList.get(i);
							 words.addAll(findWords(file));
						 });
		return words;
	}
	
	private static List<String> findWords(DataContext file) {
		List<String> words = new ArrayList<>();
		PreparedContext preparedContext = null;
		Deque<TypeContext> context = new ArrayDeque<>();
		boolean inWord = false;
		int startIndex = 0;
		int braceDepth = 0;
		int parenDepth = 0;
		char[] data = file.fileContent();
		for(int i=0; i<data.length; i++) {
			char currentChar = data[i];
			if(currentChar == '{') {
				braceDepth++;
					if(preparedContext != null) {
						context.push(preparedContext.context());
						preparedContext = null;
					}
				}
			if(currentChar == '}') {
				braceDepth--;
				if (context.peek() != null) {
					context.pop();
				}
				}
			if(currentChar == '(') {parenDepth++;}
			if(currentChar == ')') {parenDepth--;}
			if(inWord == false && isIdentifyerStart(currentChar)) {
				startIndex = i;
				inWord = true;
			}
			if(inWord == true && !isIdentifyerPart(currentChar)) {
				if (i-startIndex > 1) {
					String word = getFromIndex(startIndex, i, data);
					if (!JAVA_KEY_WORD.contains(word)) {
						words.add(word);
					}else {
						if(Objects.equals("class", word)) {
							preparedContext = new PreparedContext(TypeContext.IN_CLASS);
						}
					}
				}
				startIndex = 0;
				inWord = false;
			}
		}
		if (inWord) {
		    words.add(getFromIndex(startIndex, data.length, data));
		}
		return words;
	}

	private static String getFromIndex(int startIndex, int i, char[] data) {
		return String.copyValueOf(data, startIndex, i-startIndex);
	}

	private static boolean isIdentifyerPart (char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == '$';
	}
	
	private static boolean isIdentifyerStart (char c) {
		return Character.isLetter(c) || c == '_' || c == '$';
	}
}
