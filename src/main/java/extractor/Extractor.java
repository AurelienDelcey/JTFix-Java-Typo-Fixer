package extractor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import extractor.exception.ParserException;
import extractor.valueObject.ExtractorPayload;
import extractor.valueObject.FileExtraction;
import extractor.valueObject.TokenizedFile;
import loader.DataContext;
import shared.Failure;
import shared.PipeResult;
import shared.Success;

public class Extractor {
	
	public static PipeResult<ExtractorPayload> extract(Map<Path,DataContext> mapData){
		List<FileExtraction> extractionResult = null;
		try {
			extractionResult = mapData.keySet().stream()
											   .map((i)->extractOne(mapData, i))
											   .toList();
		} catch (ParserException e) {
			return new Failure<>(e.getMessage());
		}
		return new Success<>(mergeResult(extractionResult));
	}
	
	public static PipeResult<ExtractorPayload> extractParallel(Map<Path,DataContext> mapData){
		int workers = Runtime.getRuntime().availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(workers);
		
		List<Future<FileExtraction>> resultList = new ArrayList<>();
		List<FileExtraction> extractionResult = new ArrayList<>();
		
		for(Path path : mapData.keySet()) {
			Future<FileExtraction> extractedFile = exec.submit(()-> extractOne(mapData, path));
			resultList.add(extractedFile);
		}
		
		exec.shutdown();
		for(Future<FileExtraction> i : resultList) {
			try {
				FileExtraction tmp = i.get();
				extractionResult.add(tmp);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return new Failure<>("extractor failed: " + e.getMessage());
			} catch (ExecutionException e) {
				Throwable cause = e.getCause();
                if (cause instanceof ParserException) {
                    return new Failure<>("extractor failed: " +cause.getMessage());
                }
				return new Failure<>("extractor failed: "+ e.getMessage());
			}
		}
		return new Success<>(mergeResult(extractionResult));
	}

	private static FileExtraction extractOne(Map<Path, DataContext> mapData, Path path) {
		SingleFileExtractor extractorAgent = new SingleFileExtractor();
		 DataContext file = mapData.get(path);
		 return extractorAgent.tokenizeFile(path, file);
	}
	
	private static ExtractorPayload mergeResult( List<FileExtraction> extractionResult ) {
		Set<String> types = new HashSet<>();
		Set<String> enums = new HashSet<>();
		
		for (FileExtraction rawResult : extractionResult) {
            types.addAll(rawResult.knownTypes());
            enums.addAll(rawResult.knownEnums());
        }
		Map<Path, TokenizedFile> tokenMap = extractionResult.stream()
																.map(i->i.tokens())
																.collect(Collectors.toMap(i->i.path(), i->i));
		return new ExtractorPayload(tokenMap, types, enums);
	}

}
