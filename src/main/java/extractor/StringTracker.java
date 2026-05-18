package extractor;

import java.util.function.Consumer;

public class StringTracker {
	private Consumer<TypeContext> commitContext;
	private Consumer<TypeContext> popContext;
	
	
	public StringTracker(Consumer<TypeContext> commitContext, Consumer<TypeContext> popContext) {
		this.commitContext = commitContext;
		this.popContext = popContext;
	}
	
	public void processStringTracker(char c, TypeContext currentContext) {
		if(c=='"' && currentContext != TypeContext.IN_STRING) {commitContext.accept(TypeContext.IN_STRING);return;}
		if(c=='"' && currentContext == TypeContext.IN_STRING) {popContext.accept(TypeContext.IN_STRING);return;}
	}
}
