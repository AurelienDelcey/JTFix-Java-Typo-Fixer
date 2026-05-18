package extractor;

import java.util.function.Consumer;

public class CharTracker {
	private Consumer<TypeContext> commitContext;
	private Consumer<TypeContext> popContext;
	
	
	public CharTracker(Consumer<TypeContext> commitContext, Consumer<TypeContext> popContext) {
		this.commitContext = commitContext;
		this.popContext = popContext;
	}
	
	public void processCharTracker(char c, TypeContext currentContext) {
		if(c=='\'' && currentContext != TypeContext.IN_CHAR) {commitContext.accept(TypeContext.IN_CHAR);return;}
		if(c=='\'' && currentContext == TypeContext.IN_CHAR) {popContext.accept(TypeContext.IN_CHAR);return;}
	}
}
