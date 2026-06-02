package extractor.tracker;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import extractor.state.ExtractorEvent;
import extractor.state.TypeContext;

public class ArrowTracker {
	
	private static final Logger log = LoggerFactory.getLogger(ArrowTracker.class);
	private boolean dashDetected;
	
	private final static Set<TypeContext> ignored = EnumSet.of(TypeContext.IN_CHAR, 
															TypeContext.IN_COMMENT_LINE, 
															TypeContext.IN_COMMENT_BLOCK,
															TypeContext.IN_TEXT_BLOCK,
															TypeContext.IN_STRING);
	
	
	public ArrowTracker() {
		this.dashDetected = false;
	}
	
	public Optional<ExtractorEvent> trackArrowTransition(char c, TypeContext currentContext) {
		if(ignored.contains(currentContext)) {return Optional.empty();}
		return  detectArrowTransition(c);
	}

	private Optional<ExtractorEvent> detectArrowTransition(char c) {
		if(c == '-' && !dashDetected) {dashDetected = true; return Optional.empty();}
		if(dashDetected && c == '>') {
			log.trace("[ARROW]: open arrow context for char = {}", c);
			resetArrowDetection();
			return Optional.of(ExtractorEvent.OPEN_ON_ARROW);
		}
		resetArrowDetection();
		return Optional.empty();
	}
	
	private void resetArrowDetection() {
		this.dashDetected = false;

	}
	
}
