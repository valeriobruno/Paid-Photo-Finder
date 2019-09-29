package it.valeriobruno;

import java.io.IOException;

import com.google.api.services.drive.model.File;

public interface ReviewRepo {

	boolean hasEnoughToReview();

	void download(File fileId) throws IOException;
}