package extractor;

public class WordTracker {
	private int startIndex;
	
	public WordTracker() {
		this.startIndex = -1;
	}
	
	public int processWordTracker(char c, int index) {
		if(!isIdentifierPart(c) && inWord()) {
			int result = startIndex;
			startIndex = -1;
			return result;
		}
		if(isIdentifierPart(c) && inWord()) {return -1;}
		if(isIdentifierStart(c) && !inWord()) {
			this.startIndex = index;
			return -1;
		}
		return -1;
	}
	
	public int endOfFileCleaning() {
		return this.startIndex;
	}
	
	private boolean isIdentifierPart (char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == '$';
	}
	
	private boolean isIdentifierStart (char c) {
		return Character.isLetter(c) || c == '_' || c == '$';
	}
	
	private boolean inWord() {
		return this.startIndex > -1;
	}
}
