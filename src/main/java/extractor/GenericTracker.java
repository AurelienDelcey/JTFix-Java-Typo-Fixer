package extractor;

import java.util.function.Consumer;

public class GenericTracker {
	private Consumer<TypeContext> commitContext;
	private Consumer<TypeContext> popContext;
	
	public GenericTracker(Consumer<TypeContext> commitContext, Consumer<TypeContext> popContext) {
		this.commitContext = commitContext;
		this.popContext = popContext;
	}
	
	public void processGenericTracker(char c, char[] file, int index, TypeContext currentContext) {
		if(c == '<' && fastForward(file, index)) {commitContext.accept(TypeContext.IN_GENERIC);}
		if(currentContext == TypeContext.IN_GENERIC && c=='>') {popContext.accept(currentContext);}
	}

	private boolean fastForward(char[] file, int index) {
		for(int i = index+1; i<file.length; i++) {
			if(file[i] == '>') {return true;}
			if(file[i] == '=' || file[i] == '|' || file[i] == '&' || file[i] == ';' || file[i] == ')' || file[i] == '(') {return false;}
		}
		return false;
	}
}
