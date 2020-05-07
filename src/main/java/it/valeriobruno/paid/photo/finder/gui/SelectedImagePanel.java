package it.valeriobruno.paid.photo.finder.gui;

import it.valeriobruno.paid.photo.finder.ImageFile;
import it.valeriobruno.paid.photo.finder.repo.ReviewRepoImpl;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

public class SelectedImagePanel extends JPanel implements ListSelectionListener {

	private static final long serialVersionUID = 1L;

	private final ReviewRepoImpl imageRepo;
	private final JLabel imageLabel;
	private final JPanel imagePanel;
	private final JButton reduceButton;
	private final JButton deleteButton;
	private final JButton keepButton;

	//model
	private BufferedImage selectedImage;
	private ImageFile imageFile;
	
	public SelectedImagePanel(ReviewRepoImpl imageRepo) {

		this.imageRepo = imageRepo;

		this.setLayout(new BorderLayout(0, 5));

		imagePanel = new JPanel();
		this.add(imagePanel, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		this.add(buttonPanel, BorderLayout.SOUTH);

		imageLabel = new JLabel();
		imagePanel.add(imageLabel);

		reduceButton = new JButton("Reduce resolution");
		//reduceButton.setAction();
		buttonPanel.add(reduceButton);

		deleteButton = new JButton("Delete image");
		buttonPanel.add(deleteButton);

		keepButton = new JButton("Keep image as-is");
		buttonPanel.add(keepButton);

		ButtonListener bl = new ButtonListener();
		reduceButton.addActionListener(bl);
		keepButton.addActionListener(bl);
		deleteButton.addActionListener(bl);
		
		//imagePanel.addComponentListener(new ImagePanelCompListener());
	}

	@Override
	public void valueChanged(ListSelectionEvent event) {
		if (!event.getValueIsAdjusting()) {
			
			@SuppressWarnings("unchecked")
			JList<ImageFile> source = (JList<ImageFile>) event.getSource();
			ImageFile imageFile = source.getSelectedValue();

			if (imageFile != null) {
				try {
					selectedImage = imageFile.load();

					showImage();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.imageFile = imageFile;
				
			} else {
				this.imageLabel.setIcon(null);
				this.selectedImage = null;
				this.imageFile = null;
			}
		}

	}

	private void showImage() throws Exception {
		if (this.selectedImage != null) {
			Point p = calculateImgResize();
			this.imageLabel.setIcon(new ImageIcon(selectedImage.getScaledInstance(p.x, p.y, Image.SCALE_FAST)));
		}
	}

	private Point calculateImgResize() {
		Dimension panelSize = this.getSize(); //note: the full panel, with the buttons


		int spaceForButtons = 40;
		int width;
		int height;
		if (panelSize.getHeight() - spaceForButtons >= panelSize.getWidth()) {
			width = (int) panelSize.getWidth();
			height = (width * selectedImage.getHeight() / selectedImage.getWidth());
		} else {
			height = (int) panelSize.getHeight() - spaceForButtons;
			width = height * selectedImage.getWidth() /selectedImage.getHeight();
		}

		return new Point(width, height);
	}

	class ImagePanelCompListener implements ComponentListener {

		@Override
		public void componentResized(ComponentEvent e) {

			try {
				SelectedImagePanel.this.showImage();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		@Override
		public void componentMoved(ComponentEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void componentShown(ComponentEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void componentHidden(ComponentEvent e) {
			// TODO Auto-generated method stub

		}
	}

	class ButtonListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e) {
			try{
				if(imageFile != null) {
					if (e.getSource() == deleteButton) {
						imageRepo.delete(imageFile);

					} else if (e.getSource() == keepButton) {
						imageRepo.ignore(imageFile);
					}
					else if (e.getSource() == reduceButton)
					{
						imageRepo.resize(imageFile);
					}

					SelectedImagePanel.this.imageFile = null;
					SelectedImagePanel.this.selectedImage = null;
					SelectedImagePanel.this.imageLabel.setIcon(null);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
	}
}
