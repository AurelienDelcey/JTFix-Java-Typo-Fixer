package extractor;

import java.util.EnumSet;
import java.util.Set;

public class StateSnapshot {
	
	private final TypeContext currentContext;
	private final TypeContext preparedContext;
	private int braceDepth;
	private int parenDepth;
	
	private final static EnumSet<TypeContext> ignoredContext = EnumSet.of(TypeContext.IN_STRING, 
																		TypeContext.IN_CHAR, 
																		TypeContext.IN_COMMENT_LINE, 
																		TypeContext.IN_COMMENT_BLOCK,
																		TypeContext.IN_TEXT_BLOCK);
	
	private static final Set<TypeContext> controlStructureContext = Set.of(TypeContext.IN_IF,
																		TypeContext.IN_FOR,
																		TypeContext.IN_WHILE,
																		TypeContext.IN_ELSE);
	
	private final static EnumSet<TypeContext> rootContext = EnumSet.of(TypeContext.IN_CLASS, 
																		TypeContext.IN_RECORD, 
																		TypeContext.IN_INTERFACE, 
																		TypeContext.IN_ENUM);
	
	public StateSnapshot() {
		this.preparedContext = null;
		this.currentContext = null;
		this.braceDepth = 0;
		this.parenDepth = 0;
	}
	
	private StateSnapshot(TypeContext preparedContext, TypeContext currentContext, int braceDepth, int parenDepth) {
		verifyStructuralConsistency(preparedContext, currentContext, braceDepth, parenDepth);
		this.preparedContext = preparedContext;
		this.currentContext = currentContext;
		this.braceDepth = braceDepth;
		this.parenDepth = parenDepth;
	}

	private void verifyStructuralConsistency(TypeContext preparedContext, TypeContext currentContext, int braceDepth, int parenDepth) {
		if(braceDepth < 0 || parenDepth < 0) {throw new RuntimeException("negative depth");}
		if((braceDepth > 0 || parenDepth > 0) && currentContext == null) {throw new RuntimeException("no context on depth != 0");}
		if((braceDepth == 0 && parenDepth == 0 && currentContext != null) && 
				(braceDepth == 0 && currentContext != TypeContext.IN_GENERIC) &&
				(braceDepth == 0 && currentContext != TypeContext.IN_PARAMETERS_DECLARATION)) {
			throw new RuntimeException("depth = 0 but context");
			}
	}
	
	public StateSnapshot prepareContext(TypeContext context) {
		if(context == null) {throw new RuntimeException();}
		return new StateSnapshot(context, currentContext, braceDepth, parenDepth);
	}
	
	public StateSnapshot openBraceContext() {
		if(ignoredContext.contains(currentContext)) {throw new RuntimeException();}
		TypeContext context= null;
		
		
		if(currentContext == TypeContext.IN_LAMBDA) {context = TypeContext.IN_LAMBDA_BLOCK;}
		if(currentContext == TypeContext.IN_SWITCH_CASE) {context = TypeContext.IN_SWITCH_CASE_BLOCK;}
		
		if(context == null){context = TypeContext.IN_METHOD;}

		if(context == TypeContext.IN_LAMBDA_BLOCK && currentContext != TypeContext.IN_LAMBDA)  {throw new RuntimeException();}
		
		return new StateSnapshot(preparedContext, context, braceDepth +1 , parenDepth);
	}
	
	public StateSnapshot openParenContext() {
		if(ignoredContext.contains(currentContext)) {throw new RuntimeException();}
		TypeContext context= null;
		
		if(preparedContext == null && currentContext != null && rootContext.contains(currentContext)) {context = TypeContext.IN_PARAMETERS_DECLARATION;}
		if(preparedContext == TypeContext.IN_RECORD){context = TypeContext.IN_PARAMETERS_DECLARATION;}
		if(preparedContext != null && controlStructureContext.contains(preparedContext)) {context = TypeContext.IN_BOOLEAN_EXPRESSION;}
		if(currentContext == TypeContext.IN_BOOLEAN_EXPRESSION) {context = TypeContext.IN_UNCERTAIN_PAREN_IN_BOOLEAN;}
		
		if(context == null){context = TypeContext.IN_UNCERTAIN_PAREN;}
		return new StateSnapshot(preparedContext, context, braceDepth , parenDepth +1);
	}

	public StateSnapshot openSpecificContext(TypeContext context) {
		return new StateSnapshot(preparedContext, context, braceDepth, parenDepth);
	}

	public StateSnapshot openBraceSpecificContext(TypeContext context) {
		return new StateSnapshot(preparedContext, context, braceDepth+1, parenDepth);
	}

	public int getBraceDepth() {
		return braceDepth;
	}

	public int getParenDepth() {
		return parenDepth;
	}

	public TypeContext getCurrentContext() {
		return currentContext;
	}

	public TypeContext getPreparedContext() {
		return preparedContext;
	}

	@Override
	public String toString() {
		return "// prepared context = "+preparedContext +" current context = "+ currentContext ;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {return true;}
		if (!(obj instanceof StateSnapshot other)) {return false;}
		return braceDepth == other.braceDepth
				&& parenDepth == other.parenDepth
				&& currentContext == other.currentContext
				&& preparedContext == other.preparedContext;
	}
	
}
