package it.valeriobruno.paid.photo.finder;

import com.google.api.services.drive.Drive;
import it.valeriobruno.paid.photo.finder.gui.MainWindow;
import it.valeriobruno.paid.photo.finder.repo.ReviewRepoImpl;
import it.valeriobruno.paid.photo.finder.search.DriveServiceBuilder;
import it.valeriobruno.paid.photo.finder.search.QueryCheckPointImpl;
import it.valeriobruno.paid.photo.finder.search.SearchBigPhotos;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class Start {

	public static void main(String[] args) throws Exception {
		Drive driveService = new DriveServiceBuilder().buildDriveService();
		ReviewRepoImpl reviewRepo = new ReviewRepoImpl(new java.io.File("./review"), driveService);
		reviewRepo.init();
		final SearchBigPhotos sbp = new SearchBigPhotos(reviewRepo, new QueryCheckPointImpl(), driveService);

		reviewRepo.addListDataListener(new ListDataListener(){
			@Override
			public void intervalAdded(ListDataEvent e) {
				//nothing
			}

			@Override
			public void intervalRemoved(ListDataEvent evt) {
				try {
					sbp.startSearching();
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
				//nothing
			}
		});
		new MainWindow(reviewRepo, sbp);
		sbp.startSearching();
	}

}
