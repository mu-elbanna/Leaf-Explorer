package com.example_gallary.album;

import java.io.File;
import java.io.FilenameFilter;

public class MediaFromAlbums {

    public static final String[] EXTENSIONS = new String[]{
            "gif", "png", "ico", "tiff", "webp", "jpeg",
            "bpg", "svg", "bmp", "jpg", "mp4",
            "avi", "mpg", "mkv", "webm", "flv",
            "wmv", "mov", "qt", "m4p", "m4v",
            "mpeg", "mp2", "m2v", "3gp", "3g2",
            "f4v", "f4p", "f4a"
    };

    public static final FilenameFilter MEDIA_FILTER = new FilenameFilter() {

        @Override
        public boolean accept(final File dir, final String name) {
            for (final String ext : EXTENSIONS) {
                if (name.endsWith("." + ext)) {
                    return (true);
                }
            }
            return false;
        }
    };

    public static String[] listMedia(String path) {

        File[] listFile;

        String[] FilePathStrings;

        File file = new File(path);

        listFile = file.listFiles(MEDIA_FILTER);

        FilePathStrings = new String[listFile.length];

        for (int i = 0; i < listFile.length; i++) {

            FilePathStrings[i] = listFile[i].getAbsolutePath();

        }

        return FilePathStrings;

    }
}
