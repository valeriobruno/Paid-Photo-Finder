package it.valeriobruno;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.AbstractListModel;

import com.google.api.services.drive.Drive;

public class ReviewRepoImpl extends AbstractListModel<String> implements ReviewRepo {

	private static final int ITEMS_TO_REVIEW = 10;

	private static final long serialVersionUID = 3726437314345862433L;

	private final File repoDirectory;
	private final Drive driveService;

	private final DownloadedFileFilter fileFilter;

	public ReviewRepoImpl(File repoDirectory, Drive driveService) {
		this.repoDirectory = repoDirectory;
		this.driveService = driveService;
		fileFilter = new DownloadedFileFilter();

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

				if (nrPhotos < ITEMS_TO_REVIEW)
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
				ostream = new BufferedOutputStream(new FileOutputStream(fileFromId(fileId)));

				Drive.Files.Get getq = driveService.files().get(fileId);

				getq.executeMediaAndDownloadTo(ostream);
			} else
				System.out.println("Should download this link: " + thumbnailLink);
		} finally {
			if (ostream != null)
				ostream.close();
		}
	}

	@Override
	public void delete(String fileId) throws Exception {
		driveService.files().delete(fileId).execute();
		File localFile = fileFromId(fileId);
		File[] files = repoDirectory.listFiles(fileFilter);
		for (int x = 0; x < files.length; x++) {
			if (files[x].equals(localFile)) {
				fireIntervalRemoved(this, x, x);
				break;
			}
		}
		localFile.delete();

	}

	public BufferedImage loadImage(String id) throws Exception {
		return ImageIO.read(new File(this.repoDirectory, id));
	}

	private File fileFromId(String id) {
		return new File(this.repoDirectory, id);
	}

	/// ListModel methods
	@Override
	public int getSize() {

		File[] files = repoDirectory.listFiles(fileFilter);
		return files.length;
	}

	@Override
	public String getElementAt(int index) {
		File[] files = repoDirectory.listFiles(fileFilter);
		return files[index].getName();
	}

	static class DownloadedFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			String name = pathname.getName();
			return !name.startsWith(".") && pathname.isFile();
		}

	}
}
