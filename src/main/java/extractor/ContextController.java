package extractor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

public class ContextController {
	
	private final Deque<TypeContext> contextStack = new ArrayDeque<>();
	private final Deque<PreparedContext> preparedContext = new ArrayDeque<>();
	
	private static final Set<TypeContext> explicitContext = Set.of(TypeContext.IN_CLASS,TypeContext.IN_RECORD,TypeContext.IN_INTERFACE,TypeContext.IN_ENUM,
			TypeContext.IN_IF,TypeContext.IN_FOR,TypeContext.IN_WHILE,TypeContext.IN_DO,TypeContext.IN_TRY,TypeContext.IN_SWITCH,TypeContext.IN_CATCH,
			TypeContext.IN_FINALLY,TypeContext.IN_ELSE);
	
	public void pushContext(TypeContext context) {
		if(context == null) {return;}
		contextStack.push(context);
	}
	
	public boolean pushPreparedContext() {
		if(!preparedContext.isEmpty()) {
			contextStack.push(preparedContext.pop().context());
			return true;
		}
		return false;
	}
	
	public boolean pushPreparedContext(TypeContext context) {
		if(context == null) {return false;}
		if(!preparedContext.isEmpty() && preparedContext.peek().context() == context) {
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
		contextStack.pop();
		return true;
	}
	
	public void consumePreparedContext() {
		if(preparedContext.isEmpty()) {return;}
		preparedContext.pop();
	}
	
	public TypeContext getPreparedContext() {
		if(preparedContext.isEmpty()) {return null;}
		return preparedContext.peek().context();
	}
	
	public boolean popIfExplicitContext() {
		if(contextStack.isEmpty()) {return false;}
		if(!explicitContext.contains(contextStack.peek())) {return false;}
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
		case "class" -> preparedContext.push(new PreparedContext(TypeContext.IN_CLASS));
		case "record" -> preparedContext.push(new PreparedContext(TypeContext.IN_RECORD));
		case "interface" -> preparedContext.push(new PreparedContext(TypeContext.IN_INTERFACE));
		case "enum" -> preparedContext.push(new PreparedContext(TypeContext.IN_ENUM));
		case "if" -> preparedContext.push(new PreparedContext(TypeContext.IN_IF));
		case "for" -> preparedContext.push(new PreparedContext(TypeContext.IN_FOR));
		case "while" -> preparedContext.push(new PreparedContext(TypeContext.IN_WHILE));
		case "do" -> preparedContext.push(new PreparedContext(TypeContext.IN_DO));
		case "try" -> preparedContext.push(new PreparedContext(TypeContext.IN_TRY));
		case "catch" -> preparedContext.push(new PreparedContext(TypeContext.IN_CATCH));
		case "switch" -> preparedContext.push(new PreparedContext(TypeContext.IN_SWITCH));
		case "finaly" -> preparedContext.push(new PreparedContext(TypeContext.IN_FINALLY));
		case "else" -> preparedContext.push(new PreparedContext(TypeContext.IN_ELSE));
		}
	}

	public void clear() {
		this.contextStack.clear();
		this.preparedContext.clear();;
	}
}
