package extractor;

public record Token(String file,
					String name,
					int startIndex,
					int endIndex,
					int braceDepth,
					int parenDepth,
					TypeContext context) {
}
