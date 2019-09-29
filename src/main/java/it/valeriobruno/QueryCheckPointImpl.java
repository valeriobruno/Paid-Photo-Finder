package it.valeriobruno;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.api.client.util.Charsets;
import com.google.common.io.Files;

public class QueryCheckPointImpl {

	static final  File POINTER_FILENAME = new File("checkPoint.txt");
	private String stringPointer;

	public String getStringPointer() {
		return stringPointer;
	}

	public void setStringPointer(String stringPointer) {
		this.stringPointer = stringPointer;
	}
	
	public void loadPointer() {
		File pointerFile = POINTER_FILENAME;
		try {
			List<String> lines = Files.readLines(pointerFile, Charsets.UTF_8);
			this.stringPointer = lines.get(0);

		} catch (Exception e) {
			System.err.println("Can't read checkPoint file");
			e.printStackTrace();
			this.stringPointer = null;
		}
	}

	public void savePointer() {
		File pointerFile = POINTER_FILENAME;
		try {
			Files.write(this.stringPointer, pointerFile, Charsets.UTF_8);
		} catch (IOException e) {
			System.err.println("Can't write checkPoint file");
			e.printStackTrace();
		}
	}

	

}
