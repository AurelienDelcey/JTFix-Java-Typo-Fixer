package extractor.tracker;

public class IdentifierTracker {
	private int startIndex;
	
	public IdentifierTracker() {
		this.startIndex = -1;
	}
	
	public int trackIdentifierBoundary(char c, int index) {
		if(!Character.isJavaIdentifierPart(c) && inIdentifier()) {
			int result = startIndex;
			startIndex = -1;
			return result;
		}
		if(Character.isJavaIdentifierPart(c) && inIdentifier()) {return -1;}
		if(Character.isJavaIdentifierStart(c) && !inIdentifier()) {
			this.startIndex = index;
			return -1;
		}
		return -1;
	}
	
	public int flushPendingIdentifier() {
		return this.startIndex;
	}
	
	private boolean inIdentifier() {
		return this.startIndex > -1;
	}
}
