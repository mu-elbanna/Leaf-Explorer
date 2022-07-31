package com.leaf.music.util

import android.os.Build
import android.provider.MediaStore
import org.monora.uprotocol.client.android.R

object Constants {

    private const val packageName = R.string.package_name
    const val PREF_NAME = "$packageName.SHARED_PREF"
    const val POSITION_KEY = "${packageName}.position"
    const val CURRENT_SONG_DURATION_KEY = "$packageName.currentSongDurationKey"
    const val SAVE_CURRENT_SONG_KEY = "Save currently playing song"
    const val REQ_CODE = 0
    const val NOTIFICATION_CHANNEL_ID = "${packageName}.Music Player"
    const val NOTIFICATION_CHANNEL_NAME = "${packageName}.Music"
    const val NOTIFICATION_ID = 101

    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME
    else
        MediaStore.Audio.AudioColumns.DATA

    val baseProjection = arrayOf(
        MediaStore.Audio.AudioColumns.TITLE,
        MediaStore.Audio.AudioColumns._ID,
        collection,
        MediaStore.Audio.AudioColumns.ARTIST,
        MediaStore.Audio.AudioColumns.ALBUM,
        MediaStore.Audio.AudioColumns.DURATION,
        MediaStore.Audio.AudioColumns.ALBUM_ID,
        MediaStore.Audio.AudioColumns.DISPLAY_NAME,
        MediaStore.Audio.AudioColumns.MIME_TYPE,
        MediaStore.Audio.AudioColumns.SIZE,
        MediaStore.Audio.AudioColumns.DATE_MODIFIED
    )
}