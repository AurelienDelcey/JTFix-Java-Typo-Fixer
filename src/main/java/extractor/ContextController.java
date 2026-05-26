package extractor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextController {
	
	private static final Logger log = LoggerFactory.getLogger(ContextController.class);
	private final Deque<StateSnapshot> states = new ArrayDeque<>();
	
	/*private static final Set<TypeContext> explicitContext = Set.of(TypeContext.IN_CLASS,TypeContext.IN_RECORD,TypeContext.IN_INTERFACE,TypeContext.IN_ENUM,
			TypeContext.IN_IF,TypeContext.IN_FOR,TypeContext.IN_WHILE,TypeContext.IN_DO,TypeContext.IN_TRY,TypeContext.IN_SWITCH,TypeContext.IN_CATCH,
			TypeContext.IN_FINALLY,TypeContext.IN_ELSE);*/
	
	private static final Set<TypeContext> implicitContext = Set.of(TypeContext.IN_IF,
																	TypeContext.IN_FOR,
																	TypeContext.IN_WHILE,
																	TypeContext.IN_ELSE);
	
	public void pushContext(TypeContext context) {
		if(context == null) {throw new RuntimeException();}
		log.debug("[CONTEXT]: Push context request: {}", context);
		StateSnapshot state = states.peek();
		if(state == null) { states.push(new StateSnapshot().openContext(context)); return;}
		states.push(state.openContext(context));
	}
	
	public boolean pushPreparedContext() {
		if(states.isEmpty()) {return false;}
		log.debug("[CONTEXT]: Push prepared context request: {}", states.peek().getPreparedContext());
		StateSnapshot state = states.pop();
		states.push(state.commitPreparedContext());
		return true;
	}
	
	public boolean isPreparedContext() {
		if(states.isEmpty()) {return false;}
		return states.peek().getPreparedContext() != null;
	}
	
	public boolean popContext() {
		if(states.isEmpty()) {return false;}
		log.debug("[CONTEXT]: Pop context request: {}", states.peek().getCurrentContext());
		states.pop();
		return true;
	}
	
	public TypeContext getPreparedContext() {
		if(states.isEmpty()) {return null;}
		return states.peek().getPreparedContext();
	}

	public void handleEvent(ExtractorEvent event) {
		TypeContext current = currentContext();
		switch(event) {
		case OPEN_ON_ARROW->resolveArrowTransition(current);
		case CLOSE_CONTEXT->{closeContext();}
		case OPEN_STRING->{pushSpecific(TypeContext.IN_STRING);}
		case OPEN_COMMENT_LINE->{pushSpecific(TypeContext.IN_COMMENT_LINE);}
		case OPEN_COMMENT_BLOCK->{pushSpecific(TypeContext.IN_COMMENT_BLOCK);}
		case OPEN_GENERIC->{pushSpecific(TypeContext.IN_GENERIC);}
		case OPEN_TEXT_BLOCK->{pushSpecific(TypeContext.IN_TEXT_BLOCK);}
		case OPEN_CHAR->{pushSpecific(TypeContext.IN_CHAR);}
		}
		throw new RuntimeException();
	}
	
	public TypeContext currentContext() {
		if(states.isEmpty()) {return null;}
		return states.peek().getCurrentContext();
	}
	
	public StateSnapshot getState() {
		return states.isEmpty() ? new StateSnapshot() : states.peek();
	}
	
	public void prepareContext(String word) {
		TypeContext newPreparedContext = switch(word) {
		case "if" ->{ yield TypeContext.IN_IF;}
		case "do" ->{ yield TypeContext.IN_DO;}
		case "for" ->{ yield TypeContext.IN_FOR;}
		case "try" ->{ yield TypeContext.IN_TRY;}
		case "else" ->{ yield TypeContext.IN_ELSE;}
		case "enum" ->{ yield TypeContext.IN_ENUM;}
		case "class" ->{ yield TypeContext.IN_CLASS;}
		case "while" ->{ yield TypeContext.IN_WHILE;}
		case "catch" ->{ yield TypeContext.IN_CATCH;}
		case "switch" ->{ yield TypeContext.IN_SWITCH;}
		case "record" ->{ yield TypeContext.IN_RECORD;}
		case "finally" ->{ yield TypeContext.IN_FINALLY;}
		case "interface" ->{ yield TypeContext.IN_INTERFACE;}
		default -> { yield null;}
		};
		if (newPreparedContext == null) {return;}
		
		StateSnapshot state = states.peek();
		state = state != null ? state : new StateSnapshot();
		
		if (!states.isEmpty()&& state.getPreparedContext() != null && implicitContext.contains(state.getPreparedContext())) {
			states.pop();
		}
		logPreparation(word);
		states.push(state.prepareContext(newPreparedContext));
	}
	
	private void logPreparation(String s) {
		log.debug("[CONTEXT]: Prepare: {}", s);
	}

	public void clear() {
		this.states.clear();
	}
	
	public String debugContextStack() {
		return states.toString();
	}
}
