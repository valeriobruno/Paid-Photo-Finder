package it.valeriobruno.paid.photo.finder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageFile {

    private String id;
    private File path;

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
        return ImageIO.read(path);
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
