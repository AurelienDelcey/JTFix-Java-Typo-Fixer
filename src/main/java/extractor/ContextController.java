package extractor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

public class ContextController {
	
	private final Deque<TypeContext> contextStack = new ArrayDeque<>();
	private PreparedContext preparedContext = null;
	
	private static final Set<TypeContext> explicitContext = Set.of(TypeContext.IN_CLASS,TypeContext.IN_RECORD,TypeContext.IN_INTERFACE,TypeContext.IN_ENUM,
			TypeContext.IN_IF,TypeContext.IN_FOR,TypeContext.IN_WHILE,TypeContext.IN_DO,TypeContext.IN_TRY,TypeContext.IN_SWITCH,TypeContext.IN_CATCH,
			TypeContext.IN_FINALY,TypeContext.IN_ELSE);
	
	public void pushContext(TypeContext context) {
		if(context == null) {return;}
		contextStack.push(context);
	}
	
	public boolean pushPreparedContext() {
		if(preparedContext != null) {
			contextStack.push(preparedContext.context());
			return true;
		}
		return false;
	}
	
	public boolean pushPreparedContext(TypeContext context) {
		if(context == null) {return false;}
		if(preparedContext != null && preparedContext.context() == context) {
			contextStack.push(preparedContext.context());
			return true;
		}
		return false;
	}
	
	public boolean popContext() {
		if(contextStack.pop() != null) {
			return true;
		}
		return false;
	}
	
	public boolean popIfExplicitContext() {
		if(contextStack.isEmpty()) {return false;}
		if(!explicitContext.contains(contextStack.peek())) {return false;}
		if(!contextStack.isEmpty() && contextStack.pop() != null) {
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
		case "class" -> preparedContext = new PreparedContext(TypeContext.IN_CLASS);
		case "record" -> preparedContext = new PreparedContext(TypeContext.IN_RECORD);
		case "interface" -> preparedContext = new PreparedContext(TypeContext.IN_INTERFACE);
		case "enum" -> preparedContext = new PreparedContext(TypeContext.IN_ENUM);
		case "if" -> preparedContext = new PreparedContext(TypeContext.IN_IF);
		case "for" -> preparedContext = new PreparedContext(TypeContext.IN_FOR);
		case "while" -> preparedContext = new PreparedContext(TypeContext.IN_WHILE);
		case "do" -> preparedContext = new PreparedContext(TypeContext.IN_DO);
		case "try" -> preparedContext = new PreparedContext(TypeContext.IN_TRY);
		case "catch" -> preparedContext = new PreparedContext(TypeContext.IN_CATCH);
		case "switch" -> preparedContext = new PreparedContext(TypeContext.IN_SWITCH);
		case "finaly" -> preparedContext = new PreparedContext(TypeContext.IN_FINALY);
		case "else" -> preparedContext = new PreparedContext(TypeContext.IN_ELSE);
		}
	}

	public void clear() {
		this.contextStack.clear();
		this.preparedContext = null;
	}
}
