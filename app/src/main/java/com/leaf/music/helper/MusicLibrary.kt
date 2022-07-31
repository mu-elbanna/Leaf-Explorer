package com.leaf.music.helper

import org.monora.uprotocol.client.android.content.Song

object MusicLibrary {

    fun getSongFromIntent(queriedDisplayName: String, mDeviceMusicList: MutableList<Song>) =
        mDeviceMusicList.firstOrNull { s -> s.displayName == queriedDisplayName }
}