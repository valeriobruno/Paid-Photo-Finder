package it.valeriobruno.paid.photo.finder;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractListModel;

import com.google.api.services.drive.Drive;

public class ReviewRepoImpl extends AbstractListModel<ImageFile> implements ReviewRepo {

	private static final int ITEMS_TO_REVIEW = 10;

	private static final long serialVersionUID = 3726437314345862433L;

	private final File repoDirectory;
	private final Drive driveService;
	private final List<ImageFile> images;

	private final DownloadedFileFilter fileFilter;

	public ReviewRepoImpl(File repoDirectory, Drive driveService) {
		this.repoDirectory = repoDirectory;
		this.driveService = driveService;
		this.fileFilter = new DownloadedFileFilter();


		if (!repoDirectory.exists())
			repoDirectory.mkdirs();
		else if (!repoDirectory.isDirectory())
			throw new RuntimeException("Not a directory");

		this.images = new ArrayList<>(20); // 2 * PAGE_SIZE
		File[] imageFiles = this.repoDirectory.listFiles(new DownloadedFileFilter());
		Arrays.stream(imageFiles).map(ImageFile::fromFile).forEach(images::add);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.valeriobruno.paid.photo.finder.ReviewRepo#hasEnoughToReview()
	 */
	public boolean hasEnoughToReview() {
		boolean result = false;

		if (repoDirectory.exists()) {
			try {
				long nrPhotos = Files.list(Paths.get(repoDirectory.toURI())).count();
				result = nrPhotos < ITEMS_TO_REVIEW;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public void download(com.google.api.services.drive.model.File file) throws IOException {
		String fileId = file.getId();
		File downloadedFile = null;
		System.out.println("Downloading " + fileId);
		BufferedOutputStream ostream = null;
		try {
			String thumbnailLink = file.getThumbnailLink();
			if (thumbnailLink == null) {
				downloadedFile = fileFromId(fileId);
				ostream = new BufferedOutputStream(new FileOutputStream(downloadedFile));

				Drive.Files.Get getq = driveService.files().get(fileId);

				getq.executeMediaAndDownloadTo(ostream);
			} else
				System.out.println("Should download this link: " + thumbnailLink);
		} finally {
			if (ostream != null)
				ostream.close();
		}

		images.add(ImageFile.fromFile(downloadedFile));
		fireIntervalAdded(this,images.size()-1,images.size()-1);
	}

	@Override
	public void delete(ImageFile file) throws Exception {
		String fileId = file.getId();
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


	private File fileFromId(String id) {
		return new File(this.repoDirectory, id);
	}

	/// ListModel methods
    @Override
    public int getSize() {
        return images.size();
    }

    @Override
    public ImageFile getElementAt(int index) {
        return images.get(index);
    }

	static class DownloadedFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			String name = pathname.getName();
			return !name.startsWith(".") && pathname.isFile();
		}

	}


}
