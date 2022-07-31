package com.leaf.music.repository

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.leaf.music.util.Constants.baseProjection
import org.monora.uprotocol.client.android.content.Song
import org.monora.uprotocol.client.android.R
import java.io.File

class SongRepository(private val context: Context) {

    fun getAllSongs(): List<Song> {
        return songs(makeSongCursor())
    }

    private fun songs(cursor: Cursor?): List<Song> {
        val songs = arrayListOf<Song>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursorImpl(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

    private fun getSongFromCursorImpl(cursor: Cursor): Song {
        val title = cursor.getString(0)
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID))
        val relativePathInd = cursor.getString(2)
        val artistName = cursor.getString(3)
        val albumName = cursor.getString(4)
        val albumId = cursor.getLong(6)
        val duration = cursor.getLong(5)
        val displayName = cursor.getString(7)
        val mimeType = cursor.getString(8)

        val sizeIndex = cursor.getLong(9)
        val dateModified = cursor.getLong(10)

        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
        val albumArt = ContentUris.withAppendedId(Uri.parse(context.resources.getString(R.string.album_art_dir)), albumId)

        val folderName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            relativePathInd
        } else if (relativePathInd != null) {
            val check = File(relativePathInd).parentFile
            if (check != null) {
                check.name
            } else
                "/"
        } else {
            "/"
        }

        return Song(id, artistName, albumName, folderName, title, displayName, mimeType, sizeIndex, dateModified, duration, uri, albumArt)
    }

    @SuppressLint("Recycle")
    private fun makeSongCursor(): Cursor? {
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        return try {
            context.applicationContext.contentResolver.query(
                uri,
                baseProjection,
                null,
                null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER
            )
        } catch (e: SecurityException) {
            null
        }
    }
}