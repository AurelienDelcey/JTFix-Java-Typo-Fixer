package extractor;

import java.util.function.Consumer;

public class CommentTracker {
	
	private Consumer<TypeContext> commitContext;
	private Consumer<TypeContext> popContext;
	private boolean sequenceFlag;
	
	public CommentTracker(Consumer<TypeContext> commitContext, Consumer<TypeContext> popContext) {
		this.commitContext = commitContext;
		this.popContext = popContext;
		this.sequenceFlag = false;
	}
	
	public void processCommentTracker(char c, TypeContext currentContext) {
		checkForOpenContext(c, currentContext);
		checkForContextClosure(c, currentContext);
	}

	private void checkForContextClosure(char c, TypeContext currentContext) {
		if(currentContext == TypeContext.IN_COMMENT_LINE && c == '\n') {popContext.accept(currentContext); return;}
		if(currentContext == TypeContext.IN_COMMENT_BLOCK && c == '*'&& !sequenceFlag) {sequenceFlag = true; return;}
		if(currentContext == TypeContext.IN_COMMENT_BLOCK && c == '/'&& sequenceFlag) {sequenceFlag = false; popContext.accept(currentContext); return;}
		sequenceFlag = false;
	}

	private void checkForOpenContext(char c, TypeContext currentContext) {
		if(currentContext == TypeContext.IN_COMMENT_LINE || currentContext == TypeContext.IN_COMMENT_BLOCK) {return;}
		if(c == '/'&& !sequenceFlag) {sequenceFlag = true; return;}
		if(sequenceFlag && c == '/') {sequenceFlag = false; commitContext.accept(TypeContext.IN_COMMENT_LINE); return;}
		if(sequenceFlag && c == '*') {sequenceFlag = false; commitContext.accept(TypeContext.IN_COMMENT_BLOCK); return;}
		sequenceFlag = false;
	}
	
	
}
