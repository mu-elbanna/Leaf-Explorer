package com.leaf.music.util

import android.content.Context
import android.content.SharedPreferences
import org.monora.uprotocol.client.android.content.Song

object PlayerHelper {
    fun getCurrentSong(context: Context, sharedPreferences: SharedPreferences): Song? {
        return if (MusicPlayerRemote.playerService?.mediaPlayer != null && MusicPlayerRemote.playerService?.currentSong != null) {
            MusicPlayerRemote.playerService?.currentSong
        } else {
            SharedPreferenceUtil.getCurrentSong(context, sharedPreferences)
        }
    }
}