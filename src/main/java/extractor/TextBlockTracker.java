package extractor;

import java.util.function.Consumer;

public class TextBlockTracker {
	private int counter;
	private Consumer<TypeContext> commitContext;
	private Consumer<TypeContext> popContext;
	public TextBlockTracker(Consumer<TypeContext> commitContext, Consumer<TypeContext> popContext) {
		this.counter = 0;
		this.commitContext = commitContext;
		this.popContext = popContext;
	}
	
	public void processTextBlockTracker(char c, TypeContext currentContext) {
		if(c =='"') {counter++;} else {counter = 0;}
		if(isOpenTextBlockSymbol(currentContext)) {
			counter = 0;
			commitContext.accept(TypeContext.IN_TEXT_BLOCK);
		}
		if(isClosureTextBlockSymbol(currentContext)) {
			counter = 0;
			popContext.accept(TypeContext.IN_TEXT_BLOCK);
		}
	}

	private boolean isClosureTextBlockSymbol(TypeContext currentContext) {
		return counter==3 && (currentContext == TypeContext.IN_TEXT_BLOCK);
	}

	private boolean isOpenTextBlockSymbol(TypeContext currentContext) {
		return counter==3 && (currentContext != TypeContext.IN_TEXT_BLOCK);
	}
}
