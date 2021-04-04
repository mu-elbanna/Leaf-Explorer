package com.example_gallary.album;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AlbumsUtils {

    public static ArrayList<Albums> getAllAlbums(Activity activity) {

        Uri externalImagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String imageData = MediaStore.Images.ImageColumns.DATA;
        String imagePath = MediaStore.Images.ImageColumns.DISPLAY_NAME;

        ArrayList<Albums> allAlbums = new ArrayList<>();

        Cursor cursor;
        int column_index_data;

        String absolutePathOfAlbums;

        String[] ImagesProjection = {imageData, imagePath};

        cursor = activity.getContentResolver().query(externalImagesUri, ImagesProjection, null,
                null, null);

        if (cursor != null) {
            column_index_data = cursor.getColumnIndexOrThrow(imageData);

            while (cursor.moveToNext()) {

                absolutePathOfAlbums = cursor.getString(column_index_data);

                Albums media = new Albums();

                media.setAlbumsUrl(absolutePathOfAlbums);

                allAlbums.add(media);

            }
        }

        return allAlbums;
    }

    public static String getFolderName(String path) {

        File folderName = new File(path);

        return folderName.getName();

    }

    public static String[] initFolders(Activity activity, ArrayList<Albums> albumsList) {

        int mediaSize = albumsList.size();

        String[] albumsPath = new String[mediaSize];

        Set<String> paths = new HashSet<>();

        for (int i = 0; i < albumsList.size(); i++) {

            albumsPath[i] = albumsList.get(i).getAlbumsPath();

            paths.add(albumsPath[i]);
        }

        return paths.toArray(new String[paths.size()]);
    }
}
