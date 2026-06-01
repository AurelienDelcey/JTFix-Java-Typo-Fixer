package extractor.tracker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import extractor.state.ExtractorEvent;
import extractor.state.TypeContext;

class ArrowTrackerTest {
	
	private ArrowTracker arrowTracker;
	private final static TypeContext IGNORE = TypeContext.IN_COMMENT_LINE;
	private final static TypeContext VALID = TypeContext.IN_METHOD;
	private final static char[] OPEN_TWICE = "->->".toCharArray();
	private final static char[] CUT_ARROW_SQUENCE = "- > -.> -/> -a> -1> -!> -}> -)> -(>".toCharArray();
	private final static char[] CHAOS_SEQUENCE_OPEN_SIX_ARROW = "---><-><<<>>>-<>//...6-<->--<<<<<-<->>->>6<>--><->-<".toCharArray();
	
	@BeforeEach
	void setup() {
		arrowTracker = new ArrowTracker();
	}

	@Test
	void arrowTracker_ShouldOpenTwiceArrowContex_WhenTwiceArrowDetected() {
		int counter = runCharArray(OPEN_TWICE, VALID);
		assertEquals(2, counter);
	}

	@Test
	void arrowTracker_ShouldNotOpenArrowContex_WhenArrowSequenceIsCut() {
		for(int i = 0; i < CUT_ARROW_SQUENCE.length; i++) {
			Optional<ExtractorEvent> result = arrowTracker.trackArrowTransition(CUT_ARROW_SQUENCE[i], VALID);
			assertTrue(result.isEmpty());
		}
	}
	
	@Test
	void arrowTracker_ShouldNotOpenArrowContex_WhenContextIsIgnored() {
		int counter = runCharArray(OPEN_TWICE, IGNORE);
		assertEquals(0, counter);
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
		assertEquals(1, counter);
	}
	
	@Test
	void arrowTracker_ShouldOpenSixArrowContex() {
		int counter = runCharArray(CHAOS_SEQUENCE_OPEN_SIX_ARROW, VALID);
		assertEquals(6, counter);
	}

	private int runCharArray(char[] charArray, TypeContext context) {
		int counter = 0;
		for(int i = 0; i < charArray.length; i++) {
			Optional<ExtractorEvent> result = arrowTracker.trackArrowTransition(charArray[i], context);
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_ON_ARROW) {
					counter++;
				}
			}
		}
		return counter;
	}
}
