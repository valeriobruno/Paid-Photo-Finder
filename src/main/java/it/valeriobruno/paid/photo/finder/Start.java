package it.valeriobruno.paid.photo.finder;

import com.google.api.services.drive.Drive;

import it.valeriobruno.paid.photo.finder.search.QueryCheckPointImpl;
import it.valeriobruno.paid.photo.finder.search.SearchBigPhotos;
import it.valeriobruno.paid.photo.finder.gui.MainWindow;
import it.valeriobruno.paid.photo.finder.search.DriveServiceBuilder;

public class Start {

	public static void main(String[] args) throws Exception {
		Drive driveService = new DriveServiceBuilder().buildDriveService();
		ReviewRepoImpl reviewRepo = new ReviewRepoImpl(new java.io.File("./review"), driveService);
		SearchBigPhotos sbp = new SearchBigPhotos(reviewRepo, new QueryCheckPointImpl(), driveService);

		new MainWindow(reviewRepo, sbp);
		sbp.startSearching();
	}

}
