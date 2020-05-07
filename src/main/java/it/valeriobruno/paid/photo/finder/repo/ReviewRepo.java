package it.valeriobruno.paid.photo.finder.repo;

import it.valeriobruno.paid.photo.finder.ImageFile;

import java.io.IOException;

public interface ReviewRepo {

	boolean hasEnoughToReview();

	void download(com.google.api.services.drive.model.File fileId) throws IOException;
	void delete(ImageFile file) throws Exception;
}