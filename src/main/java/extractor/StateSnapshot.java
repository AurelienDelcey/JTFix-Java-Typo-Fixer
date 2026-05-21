package extractor;

import java.util.EnumSet;

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
	
	public StateSnapshot() {
		this.preparedContext = null;
		this.currentContext = null;
		this.braceDepth = 0;
		this.parenDepth = 0;
	}
	
	private StateSnapshot(TypeContext preparedContext, TypeContext currentContext, int braceDepth, int parenDepth) {
		verrifyStructuralConsistency(preparedContext, currentContext, braceDepth, parenDepth);
		this.preparedContext = preparedContext;
		this.currentContext = currentContext;
		this.braceDepth = braceDepth;
		this.parenDepth = parenDepth;
	}

	private void verrifyStructuralConsistency(TypeContext preparedContext, TypeContext currentContext, int braceDepth, int parenDepth) {
		if(braceDepth < 0 || parenDepth < 0) {throw new RuntimeException("nagative depth");}
		if((braceDepth > 0 && currentContext == null)) {throw new RuntimeException("no context on depth != 0");}
		if((braceDepth == 0 && currentContext != null) && 
				(braceDepth == 0 && currentContext != TypeContext.IN_GENERIC) &&
				(braceDepth == 0 && currentContext != TypeContext.IN_PARAMETERS_DECLARATION)) {
			throw new RuntimeException("depth = 0 but context");
			}
	}
	
	public StateSnapshot commitPreparedContext() {
		if(preparedContext == null) {throw new RuntimeException();}
		return new StateSnapshot(null, preparedContext, braceDepth +1 , parenDepth);
	}
	
	public StateSnapshot prepareContext(TypeContext context) {
		if(context == null) {throw new RuntimeException();}
		return new StateSnapshot(context, currentContext, braceDepth, parenDepth);
	}
	
	public StateSnapshot openContext(TypeContext context) {
		if(context == null) {throw new RuntimeException();}
		if(ignoredContext.contains(currentContext)) {throw new RuntimeException();}
		if(context == TypeContext.IN_LAMBDA_BLOCK && currentContext != TypeContext.IN_LAMBDA)  {throw new RuntimeException();}
		
		if(context == TypeContext.IN_PARAMETERS) {return new StateSnapshot(preparedContext, context, braceDepth , parenDepth +1);}
		if(context == TypeContext.IN_PARAMETERS_DECLARATION) {return new StateSnapshot(preparedContext, context, braceDepth , parenDepth +1);}
		if(context == TypeContext.IN_BOOLEAN) {return new StateSnapshot(preparedContext, context, braceDepth , parenDepth +1);}
		if(context == TypeContext.IN_LAMBDA_BLOCK) {return new StateSnapshot(preparedContext, context, braceDepth +1, parenDepth);}
		return new StateSnapshot(preparedContext, context, braceDepth +1 , parenDepth);
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
		
		return "prepared context = "+preparedContext +" // current context = "+ currentContext +
				" // brace depth = "+ braceDepth +" // paren depth = "+ parenDepth;
	}
	
	
}
