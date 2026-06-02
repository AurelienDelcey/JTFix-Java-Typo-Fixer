package extractor.tracker;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import extractor.state.ExtractorEvent;
import extractor.state.TypeContext;

public class CommentTracker {
	
	private static final Logger log = LoggerFactory.getLogger(CommentTracker.class);
	private boolean commentSequenceStarted;
	private final static Set<TypeContext> ignored = EnumSet.of(TypeContext.IN_CHAR, 
																TypeContext.IN_STRING,
																TypeContext.IN_TEXT_BLOCK);
	
	public CommentTracker() {
		this.commentSequenceStarted = false;
	}
	
	public Optional<ExtractorEvent> trackCommentTransition(char c, TypeContext currentContext) {
		Optional<ExtractorEvent> result = Optional.empty();
		if(ignored.contains(currentContext)) {resetCommentDetection(); return result;}
		result = detectCommentOpening(c, currentContext);
		if(result.isEmpty()) {result = detectCommentClosure(c, currentContext);}
		return result;
	}

	private Optional<ExtractorEvent> detectCommentClosure(char c, TypeContext currentContext) {
		if(currentContext == TypeContext.IN_COMMENT_LINE && c == '\n') {return Optional.of(ExtractorEvent.CLOSE_CONTEXT);}
		if(currentContext == TypeContext.IN_COMMENT_BLOCK && c == '*'&& !commentSequenceStarted) {commentSequenceStarted = true; return Optional.empty();}
		if(currentContext == TypeContext.IN_COMMENT_BLOCK && c == '/'&& commentSequenceStarted) {resetCommentDetection(); return Optional.of(ExtractorEvent.CLOSE_CONTEXT);}
		if(currentContext == TypeContext.IN_COMMENT_BLOCK && c != '*'&& commentSequenceStarted) {resetCommentDetection();}
		return Optional.empty();
	}

	private Optional<ExtractorEvent> detectCommentOpening(char c, TypeContext currentContext) {
		if(currentContext == TypeContext.IN_COMMENT_LINE || currentContext == TypeContext.IN_COMMENT_BLOCK) {return Optional.empty();}
		if(c == '/'&& !commentSequenceStarted) {
			commentSequenceStarted = true;
			return Optional.empty();
		}
		if(commentSequenceStarted && c == '/') {
			resetCommentDetection();
			log.trace("[COMMENT] detect comment line context");
			return Optional.of(ExtractorEvent.OPEN_COMMENT_LINE);
		}
		if(commentSequenceStarted && c == '*') {
			resetCommentDetection();
			log.trace("[COMMENT] detect comment block context");
			return Optional.of(ExtractorEvent.OPEN_COMMENT_BLOCK);
		}
		resetCommentDetection();
		return Optional.empty();
	}

	private void resetCommentDetection() {
		commentSequenceStarted = false;
	}
	
	
	
}
