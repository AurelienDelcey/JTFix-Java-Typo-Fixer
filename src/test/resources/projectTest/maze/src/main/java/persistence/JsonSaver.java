package persistence;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSaver {
	
	private final ObjectMapper mapper;
	private final SavingContainer toSave;
	private final File file;
	
	
	public JsonSaver(SavingContainer toSave, String pathFile) {
		this.mapper = new ObjectMapper();
		this.toSave = toSave;
		this.file = new File(pathFile);
	}
	
	public void save() {
		try {
			this.mapper.writeValue(this.file, this.toSave);
		} catch (StreamWriteException e) {
			System.out.println("ERROR: save fail: " + e.getMessage());
			e.printStackTrace();
		} catch (DatabindException e) {
			System.out.println("ERROR: save fail: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ERROR: save fail: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
