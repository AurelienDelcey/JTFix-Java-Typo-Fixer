package extractor.tracker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdentifierTrackerTest {

	private IdentifierTracker identifierTracker;

	private final static char[] TIGHT_VARIABLES = "a+b-c==d".toCharArray();
	private final static char[] WEIRD_VALID_IDENTS = "int _$$my_var$ = __ + $123;".toCharArray();
	private final static char[] NUMBER_TRAP = "123var1 + 456 + var2".toCharArray();
	private final static char[] EOF_TRAP = "return theLastVar".toCharArray();

	@BeforeEach
	void setup() {
		identifierTracker = new IdentifierTracker();
	}
	@Test
	void identifierTracker_ShouldExtractTightVariables_WhenNoSpacesArePresent() {
		List<String> expected = List.of("a", "b", "c", "d");
		List<String> actual = extractIdentifiers(TIGHT_VARIABLES);
		assertEquals(expected, actual);
	}

	@Test
	void identifierTracker_ShouldExtractWeirdValidIdentifiers_WhenDollarAndUnderscoreAreUsed() {
		List<String> expected = List.of("int", "_$$my_var$", "__", "$123");
		List<String> actual = extractIdentifiers(WEIRD_VALID_IDENTS);
		assertEquals(expected, actual);
	}

	@Test
	void identifierTracker_ShouldIgnoreNumbers_WhenTheyAreNotAttachedToAnIdentifier() {
		List<String> expected = List.of("var1", "var2");
		List<String> actual = extractIdentifiers(NUMBER_TRAP);
		assertEquals(expected, actual);
	}

	@Test
	void identifierTracker_ShouldExtractLastIdentifier_WhenFileEndsAbruptly() {
		List<String> expected = List.of("return", "theLastVar");
		List<String> actual = extractIdentifiers(EOF_TRAP);
		assertEquals(expected, actual);
	}
	
	private List<String> extractIdentifiers(char[] chars) {
		List<String> extractedTokens = new ArrayList<>();
		
		for (int i = 0; i < chars.length; i++) {
			int startIndex = identifierTracker.trackIdentifierBoundary(chars[i], i);
			
			if (startIndex != -1) {
				String token = new String(chars, startIndex, i - startIndex);
				extractedTokens.add(token);
			}
		}
		
		int finalStartIndex = identifierTracker.flushPendingIdentifier();
		if (finalStartIndex != -1) {
			String finalToken = new String(chars, finalStartIndex, chars.length - finalStartIndex);
			extractedTokens.add(finalToken);
		}
		
		return extractedTokens;
	}
}