package com.leaf.music.helper;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import org.monora.uprotocol.client.android.R;
import org.monora.uprotocol.client.android.content.Song;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicLibraryHelper {

    public static List<Song> fetchMusicLibrary(Context context, Uri data) {
        String collection;
        List<Song> musicList = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            collection = MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME;
        else
            collection = MediaStore.Audio.AudioColumns.DATA;

        String[] projection = new String[]{
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.AudioColumns.MIME_TYPE,
                MediaStore.Audio.AudioColumns.SIZE,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.DISPLAY_NAME,
                MediaStore.Audio.AudioColumns.DURATION,  // error from android side, it works < 29
                MediaStore.Audio.AudioColumns.ALBUM_ID,
                MediaStore.Audio.AudioColumns.ALBUM,
                collection,
                MediaStore.Audio.AudioColumns._ID,
                MediaStore.Audio.AudioColumns.DATE_MODIFIED
        };

        String selection = MediaStore.Audio.AudioColumns.IS_MUSIC + " = 1";
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        @SuppressLint("Recycle")
        Cursor musicCursor = context.getContentResolver().query(data, projection, selection, null, sortOrder);
//MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        int artistInd = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST);
        int mimeTypeInd = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.MIME_TYPE);
        int sizeIndex = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.SIZE);
        int titleInd = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE);
        int displayNameInd = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME);
        int durationInd = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION);
        int albumIdInd = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID);
        int albumInd = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM);
        int relativePathInd = musicCursor.getColumnIndexOrThrow(collection);
        int idInd = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID);
        int dateModifiedInd = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_MODIFIED);

        while (musicCursor.moveToNext()) {
            String artist = musicCursor.getString(artistInd);
            String title = musicCursor.getString(titleInd);
            String displayName = musicCursor.getString(displayNameInd);
            String album = musicCursor.getString(albumInd);
            String relativePath = musicCursor.getString(relativePathInd);
            String mimeType = musicCursor.getString(mimeTypeInd);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                relativePath += "/";
            else if (relativePath != null) {
                File check = new File(relativePath).getParentFile();
                if (check != null) {
                    relativePath = check.getName() + "/";
                }
            } else {
                relativePath = "/";
            }
            int dateAdded = musicCursor.getInt(dateModifiedInd);
            long size = musicCursor.getLong(sizeIndex);
            long id = musicCursor.getLong(idInd);
            long duration = musicCursor.getLong(durationInd);
            long albumId = musicCursor.getLong(albumIdInd);
            Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, albumId);
            Uri albumArt = ContentUris.withAppendedId(Uri.parse(context.getResources().getString(R.string.album_art_dir)), albumId);

            musicList.add(new Song(id, artist, album, relativePath, title, displayName, mimeType, size, dateAdded, duration, uri, albumArt

            ));
        }

        if (!musicCursor.isClosed())
            musicCursor.close();

        return musicList;
    }

    public static Bitmap getThumbnail(Context context, String uri) {
        try {
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(Uri.parse(uri), "r");
            if (uri == null)
                return null;

            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor());
            fileDescriptor.close();

            return bitmap;
        } catch (IOException e) {
            return null;
        }
    }
}
