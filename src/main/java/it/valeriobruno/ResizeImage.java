package it.valeriobruno;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ResizeImage {

	public static final long MAX_RES = 16000000L;

	public BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha) {
		int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
		Graphics2D g = scaledBI.createGraphics();
		if (preserveAlpha) {
			g.setComposite(AlphaComposite.Src);
		}
		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
		g.dispose();
		return scaledBI;
	}
	
	
	public int calculateMaxPctSize(int width, int height)
	{
		for (int pct=99; pct>0; pct--)
		{
			int newWidth = Math.floorDiv(width*pct,100);
			int newHeight = Math.floorDiv(height*pct,100);

			long resolution = ((long)newHeight) * ((long) newWidth);

			if(resolution < MAX_RES)
				return pct;
		}

		throw new RuntimeException("Can't calculate resize ratio");
	}
	
	
	

	public static void main(String[] args) throws IOException {
		ResizeImage ri = new ResizeImage();
		
		BufferedImage bsrc, bdest;
		
		bsrc = ImageIO.read(new File("compress/big.jpg"));
	
		int pctg=50;
		
		bdest = ri.createResizedCopy(bsrc, Math.floorDiv(bsrc.getWidth()*pctg,100), Math.floorDiv(bsrc.getHeight()*pctg,100), true);
	
		ImageIO.write(bdest,"jpg",new File("compress/compressed.jpg"));
		
	}
}
