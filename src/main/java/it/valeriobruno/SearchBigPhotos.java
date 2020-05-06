package it.valeriobruno;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.File.ImageMediaMetadata;
import com.google.api.services.drive.model.FileList;
import it.valeriobruno.paid.photo.finder.ReviewRepo;

public class SearchBigPhotos {

	public static final int PAGE_SIZE = 10;

	private final ReviewRepo reviewRepo;

	private final Drive driveService;

	private final QueryCheckPointImpl queryCheckPoint;

	public SearchBigPhotos(ReviewRepo reviewRepo, QueryCheckPointImpl queryCheckPoint, Drive driveService) {
		this.reviewRepo = reviewRepo;
		this.queryCheckPoint = queryCheckPoint;
		this.driveService = driveService;
	}

	public void startSearching() {
		while (!reviewRepo.hasEnoughToReview()) {
			Drive.Files.List listQuery;

			queryCheckPoint.loadPointer();
			String pageToken;
			try {

				System.out.println("querying Google Drive... ");
				listQuery = driveService.files().list().setPageSize(PAGE_SIZE)
						.setFields("nextPageToken, files(id, name, mimeType, imageMediaMetadata)")
						.setOrderBy("createdTime desc").setQ("mimeType contains 'image'") // image/jpeg image/png etc..
						.setPageToken(queryCheckPoint.getStringPointer());
				Date beforeQuery = new Date();
				FileList result = listQuery.execute();

				List<File> files = result.getFiles();

				pageToken = result.getNextPageToken();
				
				Date afterQuery = new Date();
				long elapsed = afterQuery.getTime() - beforeQuery.getTime();
				System.out.println("Query in (seconds) " + elapsed / 1000);
				files.stream().filter(file -> getImageResolution(file) >= 16000000L).forEach(file -> {
					try {

						reviewRepo.download(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});

			} catch (IOException e) {
				e.printStackTrace();
				return;
			}

			if (pageToken != null) {
				queryCheckPoint.setStringPointer(pageToken);
				System.out.println("Reached checkpoint: " + pageToken);
				queryCheckPoint.savePointer();
			} else {
				System.out.println("End of search!");
				break;
			}
		}
		System.out.println("Enough pictures to review. Or no-more pictures to download.");

	}

	private long getImageResolution(File file) {
		long res = 0L;
		ImageMediaMetadata metadata = file.getImageMediaMetadata();
		if (metadata != null && metadata.getHeight() != null && metadata.getWidth() != null) {
			res = ((long) metadata.getHeight()) * ((long) metadata.getWidth());
		}

		return res;
	}

}
