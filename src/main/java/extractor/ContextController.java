package extractor;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextController {
	
	private static final Logger log = LoggerFactory.getLogger(ContextController.class);
	private final StateStack states = new StateStack();
	
	private static final Set<TypeContext> implicitContext = Set.of(TypeContext.IN_IF,
																	TypeContext.IN_FOR,
																	TypeContext.IN_WHILE,
																	TypeContext.IN_ELSE);
	
	public void openBraceContext() {
		if(states.isEmpty()) {states.push(new StateSnapshot().openBraceContext());return;}
		StateSnapshot state = states.peek();
		if(state.getPreparedContext() != null) {openPreparedContext(); return;}
		states.push(state.openBraceContext());
	}
	
	public void openParenContext() {
		if(states.isEmpty()) {states.push(new StateSnapshot().openParenContext());return;}
		states.push(states.peek().openParenContext());
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
		
		log.debug("[PREPARE] prepare context: {}", newPreparedContext);
		
		StateSnapshot state = states.peek();
		state = state != null ? state : new StateSnapshot();
		
		if (shouldReplaceImplicitPreparedContext(newPreparedContext, state)) {
			log.debug("[PREPARE] replace implicit context: {} -> {}", state, newPreparedContext);
			states.pop();
			}
		
		states.push(state.prepareContext(newPreparedContext));
	}

	private boolean shouldReplaceImplicitPreparedContext(TypeContext newPreparedContext, StateSnapshot state) {
		return !states.isEmpty()&& state.getPreparedContext() != null && implicitContext.contains(state.getPreparedContext()) &&
				implicitContext.contains(newPreparedContext);
	}

	public boolean closeContext() {
		if(states.isEmpty()) {return false;}
		StateSnapshot state = states.pop();
		if(state.getCurrentContext() == TypeContext.IN_LAMBDA_BLOCK) {
			if(states.peek().getCurrentContext() == TypeContext.IN_LAMBDA) {
				log.debug("[CLOSE] collapse lambda context");
				states.pop();
			}
		}
		if(state.getCurrentContext() == TypeContext.IN_SWITCH_CASE_BLOCK) {
			if(states.peek().getCurrentContext() == TypeContext.IN_SWITCH_CASE) {
				log.debug("[CLOSE] collapse switch case context");
				states.pop();
			}
		}
		
		if(states.isEmpty()) {return true;}
		if(state.getCurrentContext() == TypeContext.IN_UNCERTAIN_PAREN) {
			if(states.peek().getCurrentContext() == TypeContext.IN_LAMBDA) {
				log.debug("[CLOSE] collapse lambda context");
				states.pop();
			}
		}
		if(state.getCurrentContext() == TypeContext.IN_UNCERTAIN_PAREN) {
			if(states.peek().getCurrentContext() == TypeContext.IN_SWITCH_CASE) {
				log.debug("[CLOSE] collapse switch case context");
				states.pop();
			}
		}
		return true;
	}

	public void clear() {
		this.states.clear();
	}

	public TypeContext currentContext() {
		if(states.isEmpty()) {return null;}
		return states.peek().getCurrentContext();
	}

	public StateSnapshot getState() {
		return states.isEmpty() ? new StateSnapshot() : states.peek();
	}

	public String debugContextStack() {
		return states.debugView().toString();
	}

	private void pushSpecific(TypeContext context) {
		StateSnapshot state = states.peek();
		if(state == null) { states.push(new StateSnapshot().openSpecificContext(context)); return;}
		states.push(state.openSpecificContext(context));
	}

	private void openPreparedContext() {
		StateSnapshot state = states.pop();
		TypeContext preparedContext = state.getPreparedContext();
		log.debug("[PREPARE] commit prepared context: {}", preparedContext);
		if(states.isEmpty()) {states.push(new StateSnapshot().openBraceSpecificContext(preparedContext));return;}
		states.push(states.peek().openBraceSpecificContext(preparedContext));
		return;
	}

	private void resolveArrowTransition(TypeContext current) {
		if(current == null) {throw new RuntimeException();}
		if(current != TypeContext.IN_SWITCH) {log.debug("[ARROW] resolve lambda transition");pushSpecific(TypeContext.IN_LAMBDA);return;}
		log.debug("[ARROW] resolve switch transition");
		pushSpecific(TypeContext.IN_SWITCH_CASE);
	}
}
