package extractor.tracker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import extractor.state.ExtractorEvent;
import extractor.state.TypeContext;

class TextBlockTrackerTest {

	private TextBlockTracker textBlockTracker;
	private final static TypeContext VALID = TypeContext.IN_METHOD;
	private final static TypeContext STRING = TypeContext.IN_TEXT_BLOCK;
	private final static char[] SIX_TEXTBLOCK_CONTEXT = "\"\"\"''''\"\"\"\" \"\"\"   \"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"   \"\"\"    \"\"\"this is a text block\"\"\" ".toCharArray();
	private final static char[] ZERO_TEXT_BLOCK = "\"\" \" \" \" \"\" \"\"'\"'\"a\"\"!\"\"".toCharArray();
	
	@BeforeEach
	void setup() {
		textBlockTracker = new TextBlockTracker();
	}
	
	@Test
	void textBlockTracker_ShouldOpenAndCloseSixContext_WhenSixContextWasProcess() {
		int openCounter = 0;
		int closeCounter = 0;
		 TypeContext context = VALID;
		for(int i = 0; i < ZERO_TEXT_BLOCK.length; i++) {
			Optional<ExtractorEvent> result = textBlockTracker.trackTextBlockTransition(ZERO_TEXT_BLOCK[i], context);
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_TEXT_BLOCK) {
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
	
	@Test
	void textBlockTracker_ShouldNotOpenAndCloseContext_WhenSequenceIsCut() {
		int openCounter = 0;
		int closeCounter = 0;
		 TypeContext context = VALID;
		for(int i = 0; i < SIX_TEXTBLOCK_CONTEXT.length; i++) {
			Optional<ExtractorEvent> result = textBlockTracker.trackTextBlockTransition(SIX_TEXTBLOCK_CONTEXT[i], context);
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_TEXT_BLOCK) {
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
			names = {"IN_COMMENT_LINE","IN_COMMENT_BLOCK","IN_CHAR"})
	void textBlockTracker_ShouldNotOpenOrCloseContext_WhenContextIsIgnored(TypeContext context) {
		int openCounter = 0;
		int closeCounter = 0;
		for(int i = 0; i < SIX_TEXTBLOCK_CONTEXT.length; i++) {
			Optional<ExtractorEvent> result = textBlockTracker.trackTextBlockTransition(SIX_TEXTBLOCK_CONTEXT[i], context);
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_TEXT_BLOCK) {
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
