package extractor;

import java.nio.file.Path;

public record TokenOccurenceContext(Path file,
									String name,
									int line,
									int beginIndex,
									int endIndex,
									int braceDepth,
									int parenDepth,
									TypeContext context,
									String previousToken,
									String nextToken) {

}
