package extractor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public class LambdaTracker {
	
	private Deque<LambdaDepth> depthTracking;
	private boolean sequenceFlag;
	private Consumer<TypeContext> commitContext;
	private Consumer<TypeContext> popContext;
	
	
	public LambdaTracker(Consumer<TypeContext> commitContext, Consumer<TypeContext> popContext) {
		this.depthTracking = new ArrayDeque<LambdaDepth>();
		this.sequenceFlag = false;
		this.commitContext = commitContext;
		this.popContext = popContext;
	}
	
	public void processLambdaTracking(char c, int braceDepth, int parenDepth, TypeContext currentContext) {
		if(currentContext==TypeContext.IN_SWITCH) {return;}
		if(currentContext==TypeContext.IN_LAMBDA) { checkForEndContext(c, braceDepth, parenDepth);}
		checkForOpenContext(c, braceDepth, parenDepth);
	}

	private void checkForOpenContext(char c, int braceDepth, int parenDepth) {
		if(c == '-' && !sequenceFlag) {sequenceFlag = true; return;}
		if(sequenceFlag && c == '>') {
			depthTracking.push(new LambdaDepth(braceDepth, parenDepth));
			commitContext.accept(TypeContext.IN_LAMBDA);
			cleanFlag();
			return;
		}
		cleanFlag();
	}

	private void checkForEndContext(char c, int braceDepth, int parenDepth) {
		if(!isTrackedContext()) {return;}
		if(!isRelativeDepthZero(braceDepth, parenDepth)) {return;}
		if(c == '}' || c == ')' || c == ';' || c == ',') {
			depthTracking.pop();
			popContext.accept(null);
		}
	}

	private boolean isRelativeDepthZero(int braceDepth, int parenDepth) {
		return braceDepth == depthTracking.peek().braceDepth() && parenDepth == depthTracking.peek().parenDepth();
	}

	private boolean isTrackedContext() {
		return !depthTracking.isEmpty();
	}

	private void cleanFlag() {
		this.sequenceFlag = false;

	}
	
}
