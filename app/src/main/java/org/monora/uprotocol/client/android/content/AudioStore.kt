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

package org.monora.uprotocol.client.android.content

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore.Audio.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.monora.uprotocol.client.android.R
import java.io.File
import javax.inject.Inject

class AudioStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun getFolders(songs: List<Song>): List<Folder> {
        val folderList = ArrayList<Folder>()
        val map: HashMap<String, Folder> = HashMap()

        return try {
            var id = 0
            for (music in songs) {
                if (map.containsKey(music.folder)) {
                    val album = map[music.folder]
                    album!!.song.add(music)
                    map[music.folder] = album
                } else {
                    val list = ArrayList<Song>()
                    list.add(music)
                    val album = Folder(id.toLong(), music.folder, list)
                    map[music.folder] = album
                    id += 1
                }
            }

            for (k in map.keys) {
                folderList.add(map[k]!!)
            }

            folderList

        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getPlaylists(): List<Playlist> {

        return emptyList()
    }

    fun getSongs(selection: String, selectionArgs: Array<String>): List<Song> {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            Media.BUCKET_DISPLAY_NAME
        else
            Media.DATA

        try {
            context.contentResolver.query(
                Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    Media._ID,
                    Media.ARTIST,
                    Media.ALBUM,
                    Media.ALBUM_ID,
                    collection,
                    Media.TITLE,
                    Media.DISPLAY_NAME,
                    Media.MIME_TYPE,
                    Media.SIZE,
                    Media.DATE_MODIFIED,
                    Media.DURATION
                ),
                selection,
                selectionArgs,
                Media.TITLE
            )?.use {
                if (it.moveToFirst()) {
                    val idIndex: Int = it.getColumnIndex(Media._ID)
                    val artistIndex: Int = it.getColumnIndex(Media.ARTIST)
                    val albumIndex: Int = it.getColumnIndex(Media.ALBUM)
                    val albumIdIndex = it.getColumnIndex(Media.ALBUM_ID)
                    val relativePathInd = it.getColumnIndex(collection)
                    val titleIndex: Int = it.getColumnIndex(Media.TITLE)
                    val displayNameIndex: Int = it.getColumnIndex(Media.DISPLAY_NAME)
                    val mimeTypeIndex: Int = it.getColumnIndex(Media.MIME_TYPE)
                    val sizeIndex: Int = it.getColumnIndex(Media.SIZE)
                    val dateModifiedIndex: Int = it.getColumnIndex(Media.DATE_MODIFIED)
                    val durationIndex: Int = it.getColumnIndex(Media.DURATION)

                    val result = ArrayList<Song>(it.count)

                    do {
                        try {
                            val id = it.getLong(idIndex)

                            val folderName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                it.getString(relativePathInd)
                            } else if (it.getString(relativePathInd) != null) {
                                val check = File(it.getString(relativePathInd)).parentFile
                                if (check != null) {
                                    check.name
                                } else
                                    "/"
                            } else {
                                "/"
                            }

                            result.add(
                                Song(
                                    id,
                                    it.getString(artistIndex),
                                    it.getString(albumIndex),
                                    folderName,
                                    it.getString(titleIndex),
                                    it.getString(displayNameIndex),
                                    it.getString(mimeTypeIndex),
                                    it.getLong(sizeIndex),
                                    it.getLong(dateModifiedIndex),
                                    it.getLong(durationIndex),
                                    ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, id),
                                    ContentUris.withAppendedId(Uri.parse(context.resources.getString(
                                        R.string.album_art_dir)), it.getLong(albumIdIndex))
                                )
                            )
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    } while (it.moveToNext())

                    return result
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return emptyList()
    }
}

@Parcelize
data class Folder(
    val id: Long,
    val title: String,
    val song: MutableList<Song>,
) : Parcelable

@Parcelize
data class Playlist(
    val id: Long,
    val name: String,
    val song: MutableList<Song>,
) : Parcelable

@Parcelize
data class Song(
    val id: Long,
    val artist: String,
    val album: String,
    val folder: String,
    val title: String,
    val displayName: String,
    val mimeType: String,
    val size: Long,
    val dateModified: Long,
    val duration: Long,
    val uri: Uri,
    val albumUri: Uri,
) : Parcelable {
    @IgnoredOnParcel
    var isSelected = false

    override fun equals(other: Any?): Boolean {
        return other is Song && uri == other.uri
    }
}
