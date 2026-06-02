package extractor.tracker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import extractor.state.ExtractorEvent;
import extractor.state.TypeContext;

class StringTrackerTest {

	private StringTracker stringTracker;
	private final static TypeContext VALID = TypeContext.IN_METHOD;
	private final static TypeContext STRING = TypeContext.IN_STRING;
	private final static char[] SIX_STRING_CONTEXT = "\"\" \"''''''\" \"\" \"      \" \" \"\"\"".toCharArray();
	
	@BeforeEach
	void setup() {
		stringTracker = new StringTracker();
	}
	
	@Test
	void stringTracker_ShouldOpenAndCloseSixContext_WhenSixContextWasProcess() {
		int openCounter = 0;
		int closeCounter = 0;
		 TypeContext context = VALID;
		for(int i = 0; i < SIX_STRING_CONTEXT.length; i++) {
			Optional<ExtractorEvent> result = stringTracker.trackStringTransition(SIX_STRING_CONTEXT[i], context);
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_STRING) {
					context = STRING;
					openCounter++;
				}
				if(result.get() == ExtractorEvent.CLOSE_CONTEXT) {
					context = VALID;
					closeCounter++;
				}
			}
		}
		assertEquals(6,openCounter);
		assertEquals(6,closeCounter);
	}
	
	@ParameterizedTest
	@EnumSource(
			value = TypeContext.class,
			mode = EnumSource.Mode.INCLUDE,
			names = {"IN_COMMENT_LINE","IN_COMMENT_BLOCK","IN_TEXT_BLOCK","IN_CHAR"})
	void stringTracker_ShouldNotOpenOrCloseContext_WhenContextIsIgnored(TypeContext context) {
		int openCounter = 0;
		int closeCounter = 0;
		for(int i = 0; i < SIX_STRING_CONTEXT.length; i++) {
			Optional<ExtractorEvent> result = stringTracker.trackStringTransition(SIX_STRING_CONTEXT[i], context);
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_STRING) {
					context = STRING;
					openCounter++;
				}
				if(result.get() == ExtractorEvent.CLOSE_CONTEXT) {
					context = VALID;
					closeCounter++;
				}
			}
		}
		assertEquals(0,openCounter);
		assertEquals(0,closeCounter);
	}

}
