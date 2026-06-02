package extractor.exception;

public class StructuralInconsistencyException extends ParserException{

	private static final long serialVersionUID = -1639719672615216090L;

	public StructuralInconsistencyException() {
		super();
	}

	public StructuralInconsistencyException(String message, Throwable cause) {
		super(message, cause);
	}

	public StructuralInconsistencyException(String message) {
		super(message);
	}
	
	
}
