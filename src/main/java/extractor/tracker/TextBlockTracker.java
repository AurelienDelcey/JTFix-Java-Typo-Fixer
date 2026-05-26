package extractor.tracker;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import extractor.state.ExtractorEvent;
import extractor.state.TypeContext;

public class TextBlockTracker {
	private int counter;
	
	private final static Set<TypeContext> ignored = EnumSet.of(TypeContext.IN_CHAR, 
															TypeContext.IN_COMMENT_LINE, 
															TypeContext.IN_COMMENT_BLOCK);
	
	public TextBlockTracker() {
		this.counter = 0;
	}
	
	public Optional<ExtractorEvent> trackTextBlockTransition(char c, TypeContext currentContext) {
		if(ignored.contains(currentContext)) {resetCounter();return Optional.empty();}
		if(c =='"') {counter++;} else {resetCounter();return Optional.empty();}
		if(isTextBlockOpening(currentContext)) {
			resetCounter();
			return Optional.of(ExtractorEvent.OPEN_TEXT_BLOCK);
		}
		if(isTextBlockClosing(currentContext)) {
			resetCounter();
			return Optional.of(ExtractorEvent.CLOSE_CONTEXT);
		}
		return Optional.empty();
	}

	private void resetCounter() {
		counter = 0;
	}

	private boolean isTextBlockClosing(TypeContext currentContext) {
		return counter==3 && (currentContext == TypeContext.IN_TEXT_BLOCK);
	}

	private boolean isTextBlockOpening(TypeContext currentContext) {
		return counter==3 && (currentContext != TypeContext.IN_TEXT_BLOCK);
	}
}
