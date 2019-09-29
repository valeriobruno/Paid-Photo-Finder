package it.valeriobruno;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.api.services.drive.Drive;

public class ReviewRepoImpl implements ReviewRepo {

	private final File repoDirectory;
	private final Drive driveService;

	public ReviewRepoImpl(File repoDirectory, Drive driveService) {
		this.repoDirectory = repoDirectory;
		this.driveService = driveService;

		if (!repoDirectory.exists())
			repoDirectory.mkdirs();
		else if (!repoDirectory.isDirectory())
			throw new RuntimeException("Not a directory");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.valeriobruno.ReviewRepo#hasEnoughToReview()
	 */
	public boolean hasEnoughToReview() {
		boolean result = false;

		if (repoDirectory.exists()) {
			try {
				long nrPhotos = Files.list(Paths.get(repoDirectory.toURI())).count();

				if (nrPhotos < 100)
					result = false;
				else
					result = true;

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public void download(com.google.api.services.drive.model.File file) throws IOException {
		String fileId = file.getId();
		System.out.println("Downloading " + fileId);
		BufferedOutputStream ostream = null;
		try {
			String thumbnailLink = file.getThumbnailLink();
			if (thumbnailLink == null) {
				ostream = new BufferedOutputStream(new FileOutputStream(new File(this.repoDirectory, fileId)));

				Drive.Files.Get getq = driveService.files().get(fileId);

				getq.executeMediaAndDownloadTo(ostream);
			} else
				System.out.println("Should download this link: " + thumbnailLink);
		} finally {
			if (ostream != null)
				ostream.close();
		}
	}
}
