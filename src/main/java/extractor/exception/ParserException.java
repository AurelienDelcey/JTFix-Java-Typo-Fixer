package extractor.exception;

public class ParserException extends RuntimeException{

	private static final long serialVersionUID = 5368741894614148639L;

	public ParserException() {
		super();
	}

	public ParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParserException(String message) {
		super(message);
	}
	
	
}
