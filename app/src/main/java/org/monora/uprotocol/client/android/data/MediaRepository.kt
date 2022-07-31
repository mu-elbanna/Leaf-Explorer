/*
 * Copyright (C) 2021 Veli Tasalı
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.monora.uprotocol.client.android.data

import android.provider.MediaStore.Audio.Media.ALBUM_ID
import android.provider.MediaStore.Audio.Media.IS_MUSIC
import org.monora.uprotocol.client.android.content.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    private val appStore: AppStore,
    private val audioStore: AudioStore,
    private val imageStore: ImageStore,
    private val videoStore: VideoStore,
) {
    fun getAllFolders() = audioStore.getFolders(audioStore.getSongs("$IS_MUSIC = ?", arrayOf("1")))

    fun getAllPlaylists() = audioStore.getPlaylists()

    fun getAllApps() = appStore.getAll()

    fun getAllSongs() = audioStore.getSongs("$IS_MUSIC = ?", arrayOf("1"))

    fun getFolderSongs(folder: Folder) = folder.song

    fun getPlaylistSongs(playlist: Playlist) = playlist.song

    fun getImageBuckets() = imageStore.getBuckets()

    fun getImages(bucket: ImageBucket) = imageStore.getImages(bucket)

    fun getVideoBuckets() = videoStore.getBuckets()

    fun getVideos(bucket: VideoBucket) = videoStore.getVideos(bucket)
}
