package extractor;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

public class StringTracker {
	private Consumer<TypeContext> commitContext;
	private Consumer<TypeContext> popContext;
	private final static Set<TypeContext> ignored = EnumSet.of(TypeContext.IN_CHAR, 
																TypeContext.IN_COMMENT_LINE, 
																TypeContext.IN_COMMENT_BLOCK,
																TypeContext.IN_TEXT_BLOCK);
	
	
	public StringTracker(Consumer<TypeContext> commitContext, Consumer<TypeContext> popContext) {
		this.commitContext = commitContext;
		this.popContext = popContext;
	}
	
	public void processStringTracker(char c, TypeContext currentContext) {
		if(c=='"' && currentContext == TypeContext.IN_STRING) {popContext.accept(TypeContext.IN_STRING);return;}
		if(currentContext != TypeContext.IN_STRING && ignored.contains(currentContext)) {return;}
		if(c=='"') {commitContext.accept(TypeContext.IN_STRING);return;}
	}
}
