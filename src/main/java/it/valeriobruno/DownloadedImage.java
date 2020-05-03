package it.valeriobruno;

public interface DownloadedImage {

	/**Deletes the image*/
	public void deleteFromPhotos();
	public void resizeAsNewPhoto();
	public void deleteLocalCopy();
}
