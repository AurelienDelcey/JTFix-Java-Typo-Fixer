package extractor.tracker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import extractor.state.ExtractorEvent;
import extractor.state.TypeContext;

class GenericTrackerTest {

	private GenericTracker genericTracker;
	private final static TypeContext VALID = TypeContext.IN_METHOD;
	private final static TypeContext GENERIC = TypeContext.IN_GENERIC;
	private final static char[] SIX_GENERIC_CONTEXT = "<><<>><><<>>".toCharArray();
	private final static char[] BOOLEAN = "a<S&&S>l||T<a&a>T?((a<<b)>(c>>>1)||(a>>2)<=c):jp()!=null&&a<b&&c>a||b<<}1>c>>2;}".toCharArray();
	private final static char[] HYBRID_ONE_CONTEXT = "boolean check = a < b && java.util.Collections.<String>emptyList() != null;".toCharArray();
	
	@BeforeEach
	void setup() {
		genericTracker = new GenericTracker();
	}
	
	@Test
	void genericTracker_ShouldOpenAndCloseSixContext_WhenSixContextWasProcess() {
		int openCounter = 0;
		int closeCounter = 0;
		 Deque<TypeContext> context = new ArrayDeque<TypeContext>();
		 context.push(VALID);
		for(int i = 0; i < SIX_GENERIC_CONTEXT.length; i++) {
			Optional<ExtractorEvent> result = genericTracker.trackGenericTransition(SIX_GENERIC_CONTEXT[i], SIX_GENERIC_CONTEXT, i, context.peek());
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_GENERIC) {
					context.push(GENERIC);
					openCounter++;
				}
				if(result.get() == ExtractorEvent.CLOSE_CONTEXT) {
					context.pop();
					closeCounter++;
				}
			}
		}
		assertEquals(6,openCounter);
		assertEquals(6,closeCounter);
	}
	
	@Test
	void genericTracker_ShouldNotOpenAndCloseContext_WhenLesserThanIsInBoolean() {
		int openCounter = 0;
		int closeCounter = 0;
		 Deque<TypeContext> context = new ArrayDeque<TypeContext>();
		 context.push(VALID);
		for(int i = 0; i < BOOLEAN.length; i++) {
			Optional<ExtractorEvent> result = genericTracker.trackGenericTransition(BOOLEAN[i], BOOLEAN, i, context.peek());
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_GENERIC) {
					context.push(GENERIC);
					openCounter++;
				}
				if(result.get() == ExtractorEvent.CLOSE_CONTEXT) {
					context.pop();
					closeCounter++;
				}
			}
		}
		assertEquals(0,openCounter);
		assertEquals(0,closeCounter);
	}
	
	@Test
	void genericTracker_ShouldOpenAndCloseOneContext_WhenOneGenericContextIsInBoolean() {
		int openCounter = 0;
		int closeCounter = 0;
		 Deque<TypeContext> context = new ArrayDeque<TypeContext>();
		 context.push(VALID);
		for(int i = 0; i < HYBRID_ONE_CONTEXT.length; i++) {
			Optional<ExtractorEvent> result = genericTracker.trackGenericTransition(HYBRID_ONE_CONTEXT[i], HYBRID_ONE_CONTEXT, i, context.peek());
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_GENERIC) {
					context.push(GENERIC);
					openCounter++;
				}
				if(result.get() == ExtractorEvent.CLOSE_CONTEXT) {
					context.pop();
					closeCounter++;
				}
			}
		}
		assertEquals(1,openCounter);
		assertEquals(1,closeCounter);
	}
	
	@ParameterizedTest
	@EnumSource(
			value = TypeContext.class,
			mode = EnumSource.Mode.INCLUDE,
			names = {"IN_COMMENT_LINE","IN_COMMENT_BLOCK","IN_TEXT_BLOCK","IN_CHAR","IN_STRING"})
	void genericTracker_ShouldNotOpenOrCloseContext_WhenContextIsIgnored(TypeContext context) {
		int openCounter = 0;
		int closeCounter = 0;
		 Deque<TypeContext> contextStack = new ArrayDeque<TypeContext>();
		 contextStack.push(context);
		for(int i = 0; i < SIX_GENERIC_CONTEXT.length; i++) {
			Optional<ExtractorEvent> result = genericTracker.trackGenericTransition(SIX_GENERIC_CONTEXT[i], SIX_GENERIC_CONTEXT, i, contextStack.peek());
			if (result.isPresent()) {
				if(result.get() == ExtractorEvent.OPEN_GENERIC) {
					contextStack.push(GENERIC);
					openCounter++;
				}
				if(result.get() == ExtractorEvent.CLOSE_CONTEXT) {
					contextStack.pop();
					closeCounter++;
				}
			}
		}
		assertEquals(0,openCounter);
		assertEquals(0,closeCounter);
	}

}
