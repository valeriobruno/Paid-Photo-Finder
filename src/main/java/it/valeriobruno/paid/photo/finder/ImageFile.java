package it.valeriobruno.paid.photo.finder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageFile {

    public static final long PAY_RES = 16000000L;

    private String id;
    private File path;

    private BufferedImage actualImage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
        this.path = path;
    }


    public BufferedImage load() throws Exception {
        if(actualImage == null)
            actualImage = ImageIO.read(path);

        return actualImage;
    }

    public int calculateMaxPctSize()
    {
       if(actualImage == null)
           return -1;


        for (int pct=99; pct>0; pct--)
        {
            int newWidth = Math.floorDiv( actualImage.getWidth()*pct,100);
            int newHeight = Math.floorDiv( actualImage.getHeight()*pct,100);

            long resolution = ((long)newHeight) * ((long) newWidth);

            if(resolution < PAY_RES)
                return pct;
        }

        throw new RuntimeException("Can't calculate resize ratio");
    }
    public BufferedImage createResizedCopy(int scaledWidth, int scaledHeight, boolean preserveAlpha) {
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(actualImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }

    public BufferedImage shrink() {
        int percentage = calculateMaxPctSize();
        BufferedImage shrunkImage = createResizedCopy(Math.floorDiv(actualImage.getWidth() * percentage, 100), Math.floorDiv(actualImage.getHeight() * percentage, 100), true);
        return shrunkImage;
    }

    public static ImageFile fromFile(File f) {
        ImageFile image = new ImageFile();
        image.id = f.getName();
        image.path = f;

        return image;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageFile imageFile = (ImageFile) o;

        return id.equals(imageFile.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
