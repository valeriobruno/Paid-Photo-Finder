package it.valeriobruno.paid.photo.finder.gui;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JList;

import it.valeriobruno.paid.photo.finder.ImageFile;
import it.valeriobruno.paid.photo.finder.ReviewRepoImpl;
import it.valeriobruno.SearchBigPhotos;

import java.awt.*;

public class MainWindow {
	
	private final SearchBigPhotos search;

	public MainWindow(ReviewRepoImpl imageRepo, SearchBigPhotos search) {
		this.search = search;
		
		
		JFrame f = new JFrame();
		f.setSize(400, 400);
		f.getContentPane().setLayout(new BorderLayout());
		
		SelectedImagePanel selectedImagePanel = new SelectedImagePanel(imageRepo);
		
		
		JList<ImageFile> list = new JList<>(imageRepo);
		
		list.addListSelectionListener(selectedImagePanel);
		f.add(list, BorderLayout.WEST);
	
		
		
		f.add(selectedImagePanel,BorderLayout.CENTER);
		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		

		f.setVisible(true);
	}
}