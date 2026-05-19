package extractor;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

public class CharTracker {
	private Consumer<TypeContext> commitContext;
	private Consumer<TypeContext> popContext;
	
	private final static Set<TypeContext> ignored = EnumSet.of(TypeContext.IN_STRING, 
																TypeContext.IN_COMMENT_LINE, 
																TypeContext.IN_COMMENT_BLOCK,
																TypeContext.IN_TEXT_BLOCK);
	
	public CharTracker(Consumer<TypeContext> commitContext, Consumer<TypeContext> popContext) {
		this.commitContext = commitContext;
		this.popContext = popContext;
	}
	
	public void processCharTracker(char c, TypeContext currentContext) {
		if(c=='\'' && currentContext == TypeContext.IN_CHAR) {popContext.accept(TypeContext.IN_CHAR);return;}
		if(currentContext != TypeContext.IN_CHAR && ignored.contains(currentContext)) {return;}
		if(c=='\'' && currentContext != TypeContext.IN_CHAR) {commitContext.accept(TypeContext.IN_CHAR);return;}
	}
}
