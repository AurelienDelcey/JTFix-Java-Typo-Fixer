package loader;

import java.nio.file.Path;

public record LoadingResult(Path path, DataContext context) {

}
