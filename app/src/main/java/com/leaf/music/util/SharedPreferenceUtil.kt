package com.leaf.music.util

import android.content.Context
import android.content.SharedPreferences
import android.provider.MediaStore
import com.leaf.music.helper.MusicLibrary
import com.leaf.music.helper.MusicLibraryHelper
import org.monora.uprotocol.client.android.content.Song
import com.leaf.music.util.Constants.SAVE_CURRENT_SONG_KEY

object SharedPreferenceUtil {

    fun saveCurrentSong(currentSong: Song, sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        editor.putString(SAVE_CURRENT_SONG_KEY, currentSong.displayName)
        editor.apply()
    }

    fun getCurrentSong(context: Context, sharedPreferences: SharedPreferences): Song? {
        val songName = sharedPreferences.getString(SAVE_CURRENT_SONG_KEY, null)
        val mDeviceMusicList = MusicLibraryHelper.fetchMusicLibrary(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)

        return if (songName != null) {
            MusicLibrary.getSongFromIntent(songName, mDeviceMusicList)
        } else
            null
    }

    fun saveCurrentPosition(sharedPreferences: SharedPreferences, currentPosition: Int) {
        with(sharedPreferences.edit()) {
            putInt(Constants.CURRENT_SONG_DURATION_KEY, currentPosition)
            apply()
        }
    }

    fun getPosition(sharedPreferences: SharedPreferences): Int {
        return sharedPreferences.getInt(Constants.POSITION_KEY, 0)
    }
}