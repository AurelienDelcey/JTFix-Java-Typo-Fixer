package scan;

import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.util.List;

import shared.Failure;
import shared.PipeResult;
import shared.Success;

import static java.nio.file.Files.find;

public class FileExplorer {
	
	private final static int MAX_SCAN_DEPTH = 20;
	
	public static PipeResult<List<Path>> exploreDirectory(Path root){
		try {
			List<Path> result = find(root,MAX_SCAN_DEPTH,
					(i,j)->j.isRegularFile() && i.getFileName().toString().endsWith(".java"),
					FileVisitOption.FOLLOW_LINKS).toList();
			return result.size() != 0 ? new Success<>(result): new Failure<>("No files found.");
		} catch (IOException e) {
			return new Failure<>(e.getMessage());
		}
	}
}
