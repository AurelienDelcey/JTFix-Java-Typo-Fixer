package extractor.valueObject;

import extractor.state.StateSnapshot;

public record Token(String file,
					String name,
					int startIndex,
					int endIndex,
					StateSnapshot context) {
}
