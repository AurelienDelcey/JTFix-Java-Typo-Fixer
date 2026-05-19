package extractor;

import java.util.function.Consumer;

public class SwitchTracker {
	private Consumer<TypeContext> commitContext;
	private Consumer<TypeContext> popContext;
	private int depth;
	
	public SwitchTracker(Consumer<TypeContext> commitContext, Consumer<TypeContext> popContext) {
		this.commitContext = commitContext;
		this.popContext = popContext;
		this.depth = -1;
	}
	
	public boolean processSwitchTracker(char c, int currentDepth, TypeContext preparedContext, TypeContext currentContext) {
		if(c== '{' && preparedContext == TypeContext.IN_SWITCH) {
			commitContext.accept(TypeContext.IN_SWITCH);
			depth = currentDepth;
			return true;
			}
		if(c == '}' && currentDepth == depth && currentContext == TypeContext.IN_SWITCH) {
			popContext.accept(TypeContext.IN_SWITCH);
			depth = -1;
			return true;
		}
		return false;
	}
}
