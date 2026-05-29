package extractor.tracker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import extractor.state.ExtractorEvent;
import extractor.state.TypeContext;

class ArrowTrackerTest {
	
	private static ArrowTracker arrowTracker;
	private final static TypeContext IGNORE = TypeContext.IN_COMMENT_LINE;
	private final static TypeContext VALID = TypeContext.IN_METHOD;
	private final static char[] OPEN_TWICE = "->->".toCharArray();
	private final static char[] CUT_ARROW_SQUENCE = "- > -.> -/> -a> -1> -!> -}> -)> -(>".toCharArray();
	private final static char[] CHAOS_SEQUENCE_OPEN_SIX_ARROW = "---><-><<<>>>-<>//...6-<->--<<<<<-<->>->>6<>--><->-<".toCharArray();
	
	@BeforeAll
	static void setup() {
		arrowTracker = new ArrowTracker();
	}

	@Test
	void arrowTracker_ShouldOpenTwiceArrowContex_WhenTwiceArrowDetected() {
		int counter = 0;
		
		for(int i = 0; i < OPEN_TWICE.length; i++) {
			Optional<ExtractorEvent> result = arrowTracker.trackArrowTransition(OPEN_TWICE[i], VALID);
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_ON_ARROW) {
					counter++;
				}
			}
		}
		assertTrue(counter == 2);
	}
	
	@Test
	void arrowTracker_ShouldNotOpenArrowContex_WhenArrowSequenceIsCut() {
		for(int i = 0; i < CUT_ARROW_SQUENCE.length; i++) {
			Optional<ExtractorEvent> result = arrowTracker.trackArrowTransition(CUT_ARROW_SQUENCE[i], VALID);
			if (result.isPresent()) {
				fail("an inextistent arrow sequence was detected.");
			}
		}
	}
	
	@Test
	void arrowTracker_ShouldNotOpenArrowContex_WhenContextIsIgnored() {
		int counter = 0;
		for(int i = 0; i < OPEN_TWICE.length; i++) {
			Optional<ExtractorEvent> result = arrowTracker.trackArrowTransition(OPEN_TWICE[i], IGNORE);
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_ON_ARROW) {
					counter++;
				}
			}
		}
		assertTrue(counter == 0);
	}
	
	@Test
	void arrowTracker_ShouldOpenOnceArrowContex_WhenContextChangeToIgnored() {
		int counter = 0;
		TypeContext context = VALID;
		
		for(int i = 0; i < OPEN_TWICE.length; i++) {
			Optional<ExtractorEvent> result = arrowTracker.trackArrowTransition(OPEN_TWICE[i], context);
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_ON_ARROW) {
					counter++;
					context = IGNORE;
				}
			}
		}
		assertTrue(counter == 1);
	}
	
	@Test
	void arrowTracker_ShouldOpenSixArrowContex() {
		int counter = 0;
		
		for(int i = 0; i < CHAOS_SEQUENCE_OPEN_SIX_ARROW.length; i++) {
			Optional<ExtractorEvent> result = arrowTracker.trackArrowTransition(CHAOS_SEQUENCE_OPEN_SIX_ARROW[i], VALID);
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_ON_ARROW) {
					counter++;
				}
			}
		}
		assertTrue(counter == 6);
	}
}
