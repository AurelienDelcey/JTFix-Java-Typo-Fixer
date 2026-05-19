package extractor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextController {
	
	private static final Logger log = LoggerFactory.getLogger(ContextController.class);
	private final Deque<TypeContext> contextStack = new ArrayDeque<>();
	private final Deque<PreparedContext> preparedContext = new ArrayDeque<>();
	
	private static final Set<TypeContext> explicitContext = Set.of(TypeContext.IN_CLASS,TypeContext.IN_RECORD,TypeContext.IN_INTERFACE,TypeContext.IN_ENUM,
			TypeContext.IN_IF,TypeContext.IN_FOR,TypeContext.IN_WHILE,TypeContext.IN_DO,TypeContext.IN_TRY,TypeContext.IN_SWITCH,TypeContext.IN_CATCH,
			TypeContext.IN_FINALLY,TypeContext.IN_ELSE);
	
	public void pushContext(TypeContext context) {
		if(context == null) {return;}
		log.debug("[CONTEXT]: Push context request: {}", context);
		contextStack.push(context);
	}
	
	public boolean pushPreparedContext() {
		if(!preparedContext.isEmpty()) {
			log.debug("[CONTEXT]: Commit prepared context request: {}", preparedContext.peek());
			contextStack.push(preparedContext.pop().context());
			return true;
		}
		return false;
	}
	
	public boolean pushPreparedContext(TypeContext context) {
		if(context == null) {return false;}
		if(!preparedContext.isEmpty() && preparedContext.peek().context() == context) {
			log.debug("[CONTEXT]: Commit prepared context request: {}", preparedContext.peek());
			contextStack.push(preparedContext.pop().context());
			return true;
		}
		return false;
	}
	
	public boolean isPreparedContext() {
		return !preparedContext.isEmpty();
	}
	
	public boolean popContext() {
		if(contextStack.isEmpty()) {return false;}
		log.debug("[CONTEXT]: Pop context request: {}", contextStack.peek());
		contextStack.pop();
		return true;
	}
	
	public void consumePreparedContext() {
		if(preparedContext.isEmpty()) {return;}
		log.debug("[CONTEXT]: Consume prepared context request: {}", preparedContext.peek());
		preparedContext.pop();
	}
	
	public TypeContext getPreparedContext() {
		if(preparedContext.isEmpty()) {return null;}
		return preparedContext.peek().context();
	}
	
	public boolean popIfExplicitContext() {
		if(contextStack.isEmpty()) {return false;}
		if(!explicitContext.contains(contextStack.peek())) {return false;}
		log.debug("[CONTEXT]: Pop explicit context request: {}", contextStack.peek());
		if(contextStack.pop() != null) {
			return true;
		}
		return false;
	}
	
	public boolean popContext(TypeContext context) {
		if(context == null) {return false;}
		if(contextStack.peek() != context) {
			return false;
		}
		if(!contextStack.isEmpty() && contextStack.pop() != null) {
			return true;
		}
		return false;
	}
	
	public TypeContext currentContext() {
		return contextStack.peek();
	}
	
	public void prepareContext(String word) {
		switch(word) {
		case "class" ->{ logPreparation(word); preparedContext.push(new PreparedContext(TypeContext.IN_CLASS));}
		case "record" ->{ logPreparation(word); preparedContext.push(new PreparedContext(TypeContext.IN_RECORD));}
		case "interface" ->{ logPreparation(word); preparedContext.push(new PreparedContext(TypeContext.IN_INTERFACE));}
		case "enum" ->{ logPreparation(word); preparedContext.push(new PreparedContext(TypeContext.IN_ENUM));}
		case "if" ->{ logPreparation(word); preparedContext.push(new PreparedContext(TypeContext.IN_IF));}
		case "for" ->{ logPreparation(word); preparedContext.push(new PreparedContext(TypeContext.IN_FOR));}
		case "while" ->{ logPreparation(word); preparedContext.push(new PreparedContext(TypeContext.IN_WHILE));}
		case "do" ->{ logPreparation(word); preparedContext.push(new PreparedContext(TypeContext.IN_DO));}
		case "try" ->{ logPreparation(word); preparedContext.push(new PreparedContext(TypeContext.IN_TRY));}
		case "catch" ->{ logPreparation(word); preparedContext.push(new PreparedContext(TypeContext.IN_CATCH));}
		case "switch" ->{ logPreparation(word); preparedContext.push(new PreparedContext(TypeContext.IN_SWITCH));}
		case "finaly" ->{ logPreparation(word); preparedContext.push(new PreparedContext(TypeContext.IN_FINALLY));}
		case "else" ->{ logPreparation(word); preparedContext.push(new PreparedContext(TypeContext.IN_ELSE));}
		}
	}
	
	private void logPreparation(String s) {
		log.debug("[CONTEXT]: Prepare: {}", s);
	}

	public void clear() {
		this.contextStack.clear();
		this.preparedContext.clear();;
	}
	
	public String debugContextStack() {
		return contextStack.toString();
	}
}
