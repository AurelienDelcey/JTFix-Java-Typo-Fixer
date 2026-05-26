package extractor;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class GenericTracker {
	
	private final static Set<TypeContext> ignored = EnumSet.of(TypeContext.IN_CHAR, 
															TypeContext.IN_COMMENT_LINE, 
															TypeContext.IN_COMMENT_BLOCK,
															TypeContext.IN_TEXT_BLOCK,
															TypeContext.IN_STRING);
	
	public GenericTracker() {
	}
	
	public Optional<ExtractorEvent> trackGenericTransition(char c, char[] file, int index, TypeContext currentContext) {
		if(ignored.contains(currentContext)) {return Optional.empty();}
		if(c == '<' && fastForward(file, index)) {return Optional.of(ExtractorEvent.OPEN_GENERIC);}
		if(currentContext == TypeContext.IN_GENERIC && c=='>') {return Optional.of(ExtractorEvent.CLOSE_CONTEXT);}
		return Optional.empty();
	}

	private boolean fastForward(char[] file, int index) {
		for(int i = index+1; i<file.length; i++) {
			if(file[i] == '>') {return true;}
			if(file[i] == '=' || file[i] == '|' || file[i] == '&' || file[i] == ';' || file[i] == ')' || file[i] == '(' || file[i] == '{' || file[i] == '}') {return false;}
		}
		return false;
	}
}
