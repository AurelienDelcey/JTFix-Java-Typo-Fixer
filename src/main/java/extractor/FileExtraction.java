package extractor;

import java.util.Set;

public record FileExtraction(TokenizedFile tokens,
								Set<String> knownTypes,
								Set<String> knownEnums) {

}
