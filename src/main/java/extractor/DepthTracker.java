package extractor;

public class DepthTracker {
	private int parentDepth;
	private int braceDepth;
	
	public DepthTracker() {
		this.braceDepth = 0;
		this.parentDepth = 0;
	}
	
	public void incrementDepth(char c) {
		if(c=='(') {this.parentDepth++;}
		if(c=='{') {this.braceDepth++;}
		
	}
	public void decrementDepth(char c) {
		if(c==')') {this.parentDepth--;}
		if(c=='}') {this.braceDepth--;}
	}
	 public void clearDepthTracker() {
		this.braceDepth = 0;
		this.parentDepth = 0;
	 }
	public int getParentDepth() {
		return parentDepth;
	}
	
	public int getBraceDepth() {
		return braceDepth;
	}
	
	
}
