package extractor;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class StringTracker {
	private final static Set<TypeContext> ignored = EnumSet.of(TypeContext.IN_CHAR, 
																TypeContext.IN_COMMENT_LINE, 
																TypeContext.IN_COMMENT_BLOCK,
																TypeContext.IN_TEXT_BLOCK);
	
	public Optional<ExtractorEvent> trackStringTransition(char c, TypeContext currentContext) {
		if(ignored.contains(currentContext)) {return Optional.empty();}
		if(c=='"' && currentContext == TypeContext.IN_STRING) {return Optional.of(ExtractorEvent.CLOSE_CONTEXT);}
		if(c=='"') {return Optional.of(ExtractorEvent.OPEN_STRING);}
		return Optional.empty();
	}
}
