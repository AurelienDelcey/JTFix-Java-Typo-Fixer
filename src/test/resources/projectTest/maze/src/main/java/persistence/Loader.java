package persistence;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Loader {
	
	private final ObjectMapper loader;
	
	public Loader() {
		this.loader = new ObjectMapper();
	}
	
	public SavingContainer load(File path) {
		SavingContainer loadedData = null;
		try {
			loadedData = this.loader.readValue(path, SavingContainer.class);
		} catch (JsonProcessingException e) {
			System.out.println("ERROR: fail loading data: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ERROR: fail loading data: " + e.getMessage());
			e.printStackTrace();
		}
		return loadedData;
	}
}
