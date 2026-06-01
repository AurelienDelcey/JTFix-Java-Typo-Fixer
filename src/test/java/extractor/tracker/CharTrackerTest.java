package extractor.tracker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import extractor.state.ExtractorEvent;
import extractor.state.TypeContext;

class CharTrackerTest {

	private CharTracker charTracker;
	private final static TypeContext VALID = TypeContext.IN_METHOD;
	private final static TypeContext CHAR = TypeContext.IN_CHAR;
	private final static char[] SIX_CHAR_CONTEXT = "'v''\"' ' ' '.' '!' ''".toCharArray();
	
	@BeforeEach
	void setup() {
		charTracker = new CharTracker();
	}
	
	@Test
	void charTracker_ShouldOpenAndCloseSixContext_WhenSixContextWasProcess() {
		int openCounter = 0;
		int closeCounter = 0;
		 TypeContext context = VALID;
		for(int i = 0; i < SIX_CHAR_CONTEXT.length; i++) {
			Optional<ExtractorEvent> result = charTracker.trackCharacter(SIX_CHAR_CONTEXT[i], context);
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_CHAR) {
					context = CHAR;
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
			names = {"IN_COMMENT_LINE","IN_COMMENT_BLOCK","IN_TEXT_BLOCK","IN_STRING"})
	void charTracker_ShouldNotOpenOrCloseContext_WhenContextIsIgnored(TypeContext context) {
		int openCounter = 0;
		int closeCounter = 0;
		for(int i = 0; i < SIX_CHAR_CONTEXT.length; i++) {
			Optional<ExtractorEvent> result = charTracker.trackCharacter(SIX_CHAR_CONTEXT[i], context);
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_CHAR) {
					context = CHAR;
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
