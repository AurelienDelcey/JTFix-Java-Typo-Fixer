package extractor;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommentTracker {
	
	private static final Logger log = LoggerFactory.getLogger(CommentTracker.class);
	private Consumer<TypeContext> commitContext;
	private Consumer<TypeContext> popContext;
	private boolean sequenceFlag;
	private final static Set<TypeContext> ignored = EnumSet.of(TypeContext.IN_CHAR, 
																TypeContext.IN_STRING,
																TypeContext.IN_TEXT_BLOCK);
	
	public CommentTracker(Consumer<TypeContext> commitContext, Consumer<TypeContext> popContext) {
		this.commitContext = commitContext;
		this.popContext = popContext;
		this.sequenceFlag = false;
	}
	
	public void processCommentTracker(char c, TypeContext currentContext) {
		if(ignored.contains(currentContext)) {sequenceFlag = false; return;}
		if(currentContext == TypeContext.IN_COMMENT_LINE || currentContext == TypeContext.IN_COMMENT_BLOCK) {
			checkForContextClosure(c, currentContext);
		} else {
			checkForOpenContext(c, currentContext);
		}
	}

	private void checkForContextClosure(char c, TypeContext currentContext) {
		if(currentContext == TypeContext.IN_COMMENT_LINE && c == '\n') {popContext.accept(currentContext); return;}
		if(currentContext == TypeContext.IN_COMMENT_BLOCK && c == '*'&& !sequenceFlag) {sequenceFlag = true; return;}
		if(currentContext == TypeContext.IN_COMMENT_BLOCK && c == '/'&& sequenceFlag) {sequenceFlag = false; popContext.accept(currentContext); return;}
		sequenceFlag = false;
	}

	private void checkForOpenContext(char c, TypeContext currentContext) {
		if(currentContext == TypeContext.IN_COMMENT_LINE || currentContext == TypeContext.IN_COMMENT_BLOCK) {return;}
		if(c == '/'&& !sequenceFlag) {
			sequenceFlag = true; 
			log.trace("[COMMENT][FLAG] flag trigger: state = {} char = {}, context = {}", sequenceFlag, c, currentContext);
			return;
		}
		if(sequenceFlag && c == '/') {
			sequenceFlag = false; 
			log.trace("[COMMENT][FLAG] Comment line trigger: state = {} char = {}, context = {}", sequenceFlag, c, currentContext);
			commitContext.accept(TypeContext.IN_COMMENT_LINE); 
			return;
		}
		if(sequenceFlag && c == '*') {
			sequenceFlag = false;
			log.trace("[COMMENT][FLAG] Comment block trigger: state = {} char = {}, context = {}", sequenceFlag, c, currentContext);
			commitContext.accept(TypeContext.IN_COMMENT_BLOCK); 
			return;
		}
		sequenceFlag = false;
	}
	
	
}
