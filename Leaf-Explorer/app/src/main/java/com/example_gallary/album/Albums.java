package com.example_gallary.album;

import java.io.File;

public class Albums {

    public String albumPath;

    public void setAlbumsUrl(String albumUrl) {

        File f = new File(albumUrl);
        String absolutePath = f.getPath();

        String filename = absolutePath.substring(absolutePath.lastIndexOf(File.separator) + 1);

        setAlbumsPath(absolutePath.replace(filename, ""));

    }

    public String getAlbumsPath() {
        return albumPath;
    }

    public void setAlbumsPath(String albumPath) {
        this.albumPath = albumPath;
    }

}
