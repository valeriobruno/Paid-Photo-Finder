package it.valeriobruno.paid.photo.finder.repo;

import com.google.api.services.drive.Drive;
import it.valeriobruno.paid.photo.finder.ImageFile;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.swing.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReviewRepoImpl extends AbstractListModel<ImageFile> implements ReviewRepo {

	private static final int ITEMS_TO_REVIEW = 10;

	private static final long serialVersionUID = 3726437314345862433L;

	private final File repoDirectory;
	private final Drive driveService;
	private final List<ImageFile> images;

	private IgnoredImagesRegistry ignoredImages;

	public ReviewRepoImpl(File repoDirectory, Drive driveService) {
		this.repoDirectory = repoDirectory;
		this.driveService = driveService;
		this.images = new ArrayList<>(20); // 2 * PAGE_SIZE

	}

	public void init() throws Exception{
		if (!repoDirectory.exists()) {
			repoDirectory.mkdirs();
		} else if (!repoDirectory.isDirectory()) {
			throw new RuntimeException("Not a directory");
		}

		this.ignoredImages =  IgnoredImagesRegistry.loadIfExists(new File(this.repoDirectory,".ignored"));
		loadImages();

	}

	private void loadImages() {
		File[] imageFiles = repoDirectory.listFiles();
		Arrays.stream(imageFiles)
				.filter(file -> !file.getName().startsWith(".") && file.isFile())
				.map(ImageFile::fromFile)
				.filter( img -> !ignoredImages.isIgnored(img))
				.forEach(images::add);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.valeriobruno.paid.photo.finder.repo.ReviewRepo#hasEnoughToReview()
	 */
	public boolean hasEnoughToReview() {
		return images.size() >= ITEMS_TO_REVIEW;
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
		CompletableFuture.runAsync(() -> fireIntervalAdded(this,images.size()-1,images.size()-1));
	}

	@Override
	public void ignore(ImageFile file) throws Exception {
		ignoredImages.add(file);
		removeFromCache(file);
	}



	@Override
	public void resize(ImageFile file) throws Exception {
		throw new NotImplementedException();
	}

	@Override
	public void delete(ImageFile file) throws Exception {
		String fileId = file.getId();

		//delete from Google Drive
		driveService.files().delete(fileId).execute();

		//delete local copy
		File localFile = fileFromId(fileId);
		localFile.delete();

		removeFromCache(file);
	}

	private void removeFromCache(ImageFile file) {
		int index = images.indexOf(file);
		images.remove(file);
		//notify the UI
		CompletableFuture.runAsync(() -> fireIntervalRemoved(this, index, index) );
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
}
