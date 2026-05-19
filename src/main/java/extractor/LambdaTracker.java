package extractor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LambdaTracker {
	
	private static final Logger log = LoggerFactory.getLogger(LambdaTracker.class);
	private final Deque<LambdaDepth> depthTracking;
	private final Consumer<TypeContext> commitContext;
	private final Consumer<TypeContext> popContext;
	private boolean sequenceFlag;
	
	
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
			log.trace("[LAMBDA]: open lambda context for char = {}, brace = {}, parent = {}", c, braceDepth, parenDepth);
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
			log.trace("[LAMBDA]: close lambda context for char = {}, brace = {}, parent = {}", c, braceDepth, parenDepth);
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
