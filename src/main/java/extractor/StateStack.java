package extractor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateStack {
	
	private static final Logger log = LoggerFactory.getLogger(StateStack.class);
	private final Deque<StateSnapshot> states = new ArrayDeque<>();

	public boolean isEmpty() {
		return states.isEmpty();
	}

	public StateSnapshot peek() {
		return states.peek();
	}

	public StateSnapshot pop() {
		log.debug("[STACK] close context request: {}", states.peek().getCurrentContext());
		return states.pop();
	}

	public void push(StateSnapshot snapshot) {
		log.debug("[STACK] open context requested: {}", snapshot);
		states.push(snapshot);
	}

	public void clear() {
		states.clear();
	}

	public List<StateSnapshot> debugView() {
		return List.copyOf(states);
	}
}
