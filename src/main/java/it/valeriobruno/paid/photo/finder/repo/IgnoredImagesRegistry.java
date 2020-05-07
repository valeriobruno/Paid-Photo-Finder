package it.valeriobruno.paid.photo.finder.repo;

import it.valeriobruno.paid.photo.finder.ImageFile;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class IgnoredImagesRegistry {
    private List<String> identifiers;
    private final File registryFile;


    private IgnoredImagesRegistry(File registryFile) {
        this.registryFile = registryFile;
        identifiers = new LinkedList<>();
    }

    public void add(ImageFile image) {
        identifiers.add(image.getId());
        this.save();
    }

    private void save() {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(this.registryFile)));

            outputStream.writeObject(this.identifiers);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isIgnored(ImageFile f)
    {
        return identifiers.contains(f.getId());
    }
    public static IgnoredImagesRegistry load(File registryFile) throws Exception {

        final IgnoredImagesRegistry ignoredImagesRegistry = new IgnoredImagesRegistry(registryFile);


        ObjectInputStream outputStream = new ObjectInputStream(
                new BufferedInputStream(
                        new FileInputStream(registryFile)));

        ignoredImagesRegistry.identifiers = (List<String>) outputStream.readObject();
        outputStream.close();
        return ignoredImagesRegistry;

    }

    public static IgnoredImagesRegistry loadIfExists(File registryFile) throws Exception {
        IgnoredImagesRegistry registry;

        if(registryFile.exists())
            registry = load(registryFile);
        else registry = new IgnoredImagesRegistry(registryFile);

        return registry;
    }
}

