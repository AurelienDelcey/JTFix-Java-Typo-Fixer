package extractor.tracker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import extractor.state.ExtractorEvent;
import extractor.state.TypeContext;

class CommentTrackerTest {
	
	private CommentTracker commentTracker;
	private final static TypeContext VALID = TypeContext.IN_METHOD;
	private final static TypeContext COMMENTLINE = TypeContext.IN_COMMENT_LINE;
	private final static TypeContext COMMENTBLOCK = TypeContext.IN_COMMENT_BLOCK;
	private final static char[] SIX_LINE_COMMENT = "a//1\nb//2\nc//3\nd//4\ne//5\nf//6\n".toCharArray();
	private final static char[] SIX_BLOCK_COMMENT = "/*1*/a/*2*/b/*3*/c/*4*/d/*5*/e/*6*/".toCharArray();
	private final static char[] COMMENT_TRAPS = "/* // invisible */ // le /* ne fait rien\n /* // */".toCharArray();
	
	@BeforeEach
	void setup() {
		commentTracker = new CommentTracker();
	}
	
	@Test
	void commentTracker_ShouldOpenAndCloseSixLineContext_WhenSixContextWasProcess() {
		int openCounter = 0;
		int closeCounter = 0;
		 TypeContext context = VALID;
		for(int i = 0; i < SIX_LINE_COMMENT.length; i++) {
			Optional<ExtractorEvent> result = commentTracker.trackCommentTransition(SIX_LINE_COMMENT[i], context);
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_COMMENT_LINE) {
					context = COMMENTLINE;
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
			names = {"IN_COMMENT_BLOCK","IN_TEXT_BLOCK","IN_STRING"})
	void commentTracker_ShouldNotOpenOrCloseLineContext_WhenContextIsIgnored(TypeContext context) {
		int openCounter = 0;
		int closeCounter = 0;
		for(int i = 0; i < SIX_LINE_COMMENT.length; i++) {
			Optional<ExtractorEvent> result = commentTracker.trackCommentTransition(SIX_LINE_COMMENT[i], context);
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_COMMENT_LINE) {
					context = COMMENTLINE;
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
	void commentTracker_ShouldOpenAndCloseSixBlockContext_WhenSixContextWasProcess() {
		int openCounter = 0;
		int closeCounter = 0;
		 TypeContext context = VALID;
		for(int i = 0; i < SIX_BLOCK_COMMENT.length; i++) {
			Optional<ExtractorEvent> result = commentTracker.trackCommentTransition(SIX_BLOCK_COMMENT[i], context);
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_COMMENT_BLOCK) {
					context = COMMENTBLOCK;
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
			names = {"IN_COMMENT_LINE","IN_TEXT_BLOCK","IN_STRING"})
	void commentTracker_ShouldNotOpenOrCloseBlockContext_WhenContextIsIgnored(TypeContext context) {
		int openCounter = 0;
		int closeCounter = 0;
		for(int i = 0; i < SIX_BLOCK_COMMENT.length; i++) {
			Optional<ExtractorEvent> result = commentTracker.trackCommentTransition(SIX_BLOCK_COMMENT[i], context);
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_COMMENT_BLOCK) {
					context = COMMENTBLOCK;
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
	void commentTracker_ShouldOpenAndCloseThreeCommentContext_WhenThreeContextWasMixed() {
		int openLineCounter = 0;
		int openBlockCounter = 0;
		int closeCounter = 0;
		 TypeContext context = VALID;
		for(int i = 0; i < COMMENT_TRAPS.length; i++) {
			Optional<ExtractorEvent> result = commentTracker.trackCommentTransition(COMMENT_TRAPS[i], context);
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_COMMENT_LINE) {
					context = COMMENTLINE;
					openLineCounter++;
				}
				if(result.get() == ExtractorEvent.OPEN_COMMENT_BLOCK) {
					context = COMMENTBLOCK;
					openBlockCounter++;
				}
				if(result.get() == ExtractorEvent.CLOSE_CONTEXT) {
					context = VALID;
					closeCounter++;
				}
			}
		}
		assertEquals(2,openBlockCounter);
		assertEquals(1,openLineCounter);
		assertEquals(3,closeCounter);
	}
}
