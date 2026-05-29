package extractor.valueObject;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public record ExtractorPayload(Map<Path, TokenizedFile> tokenMap,
								Set<String> knownTypes,
								Set<String> knownEnums) {

}
