package extractor.tracker;

public class IdentifierTracker {
	private int startIndex;
	
	public IdentifierTracker() {
		this.startIndex = -1;
	}
	
	public int trackIdentifierBoundary(char c, int index) {
		if(!isIdentifierPart(c) && inIdentifier()) {
			int result = startIndex;
			startIndex = -1;
			return result;
		}
		if(isIdentifierPart(c) && inIdentifier()) {return -1;}
		if(isIdentifierStart(c) && !inIdentifier()) {
			this.startIndex = index;
			return -1;
		}
		return -1;
	}
	
	public int flushPendingIdentifier() {
		return this.startIndex;
	}
	
	private boolean isIdentifierPart (char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == '$';
	}
	
	private boolean isIdentifierStart (char c) {
		return Character.isLetter(c) || c == '_' || c == '$';
	}
	
	private boolean inIdentifier() {
		return this.startIndex > -1;
	}
}
