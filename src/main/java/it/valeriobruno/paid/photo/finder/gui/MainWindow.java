package it.valeriobruno.paid.photo.finder.gui;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import it.valeriobruno.ReviewRepoImpl;
import it.valeriobruno.SearchBigPhotos;

public class MainWindow {
	
	private final SearchBigPhotos search;

	public MainWindow(ReviewRepoImpl imageRepo, SearchBigPhotos search) {
		this.search = search;
		
		
		JFrame f = new JFrame();
		
		SelectedImagePanel selectedImagePanel = new SelectedImagePanel(imageRepo);
		
		
		JList<String> list = new JList<>(imageRepo);
		
		list.addListSelectionListener(selectedImagePanel);
		f.add(list);
	
		
		
		f.add(selectedImagePanel);
		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		
		f.setSize(400, 400);
		f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.X_AXIS));
		f.setVisible(true);
	}
}