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

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.genonbeta.android.framework.io.DocumentFile
import com.leaf.explorer.app.Storage
import com.leaf.explorer.utils.JavaUtils
import com.leaf.music.MPConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import org.monora.uprotocol.client.android.R
import org.monora.uprotocol.client.android.database.SafFolderDao
import org.monora.uprotocol.client.android.database.model.SafFolder
import org.monora.uprotocol.client.android.model.FileModel
import java.io.File
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val safFolderDao: SafFolderDao,
) {
    private val contextWeak = WeakReference(context)

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    val internalDirectory: DocumentFile by lazy {
        var primaryDir: File = context.externalCacheDir!!

        val splitPath = primaryDir.absolutePath.split(File.separator.toRegex()).toTypedArray()
        if (splitPath.size >= 2 && splitPath[1] == "storage") {
            if (splitPath.size >= 4 && splitPath[2] == "emulated") {
                val file = File(JavaUtils.buildPath(splitPath, 4))
                if ("0" == splitPath[3] && (file.canWrite() || file.canRead())) {
                    primaryDir = file
                }
            }
        }

        DocumentFile.fromFile(primaryDir)
    }

    val availableStorage: ArrayList<DocumentFile> by lazy {
        val path = ArrayList<DocumentFile>()

        for (mediaDir in JavaUtils.referencedDirectoryList(context)) {
            if (mediaDir == null || !mediaDir.canWrite()) continue

            val splitPath = mediaDir.absolutePath.split(File.separator.toRegex()).toTypedArray()
            if (splitPath.size >= 2 && splitPath[1] == "storage") {
                if (splitPath.size >= 4 && splitPath[2] == "emulated") {
                    val file = File(JavaUtils.buildPath(splitPath, 4))
                    if (file.canWrite()) {
                        path.add(DocumentFile.fromFile(file))
                    }
                } else if (splitPath.size >= 3) {
                    val file = File(JavaUtils.buildPath(splitPath, 3))
                    if (file.canWrite()) {
                        path.add(DocumentFile.fromFile(file))
                    }
                }
            }
        }

        path
    }

    var appDirectory: DocumentFile
        get() {
            val context = contextWeak.get()!!
            val defaultPath = defaultAppDirectory
            val preferredPath = preferences.getString(KEY_STORAGE_PATH, null)

            if (preferredPath != null) {
                try {
                    val storageFolder = DocumentFile.fromUri(context, Uri.parse(preferredPath))
                    if (storageFolder.isDirectory() && storageFolder.canWrite()) return storageFolder
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (defaultPath.isFile) defaultPath.delete()
            if (!defaultPath.isDirectory) defaultPath.mkdirs()
            return DocumentFile.fromFile(defaultPath)
        }
        set(value) {
            preferences.edit {
                putString(KEY_STORAGE_PATH, value.getUri().toString())
            }
        }

    val defaultAppDirectory: File by lazy {

        var primaryDir: File? = null

        if (Build.VERSION.SDK_INT >= 30) {

            val primaryDir30: File = context.externalCacheDir!!

            val splitPath = primaryDir30.absolutePath.split(File.separator.toRegex()).toTypedArray()
            if (splitPath.size >= 2 && splitPath[1] == "storage") {
                if (splitPath.size >= 4 && splitPath[2] == "emulated") {
                    val file = File(MPConstants.buildPath(splitPath, 4) + File.separator + Environment.DIRECTORY_DOWNLOADS)
                    primaryDir = if ("0" == splitPath[3]) {
                        if (file.isDirectory && file.canWrite() || file.mkdirs()) {
                            file
                        } else {
                            primaryDir30
                        }
                    } else {
                        primaryDir30
                    }

                }
            }

        } else {
            primaryDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!primaryDir.isDirectory && !primaryDir.mkdirs() || !primaryDir.canWrite()) {
                primaryDir = Environment.getExternalStorageDirectory()
            }
        }

        File(primaryDir.toString() + File.separator + context.getString(R.string.app_name))
    }

    suspend fun clearStorageList() = safFolderDao.removeAll()

    fun getStorage(): Storage {
        return Storage(context)
    }

    fun getFileList(file: DocumentFile): List<FileModel> {
        val context = contextWeak.get() ?: return emptyList()
        val r = getStorage().fromArchive(file.getUri(), true)
        if (r != null) {
            check(r.isDirectory) {
                "${file.originalUri} is not a directory."
            }

            return r.list().map {
                val docFile = DocumentFile.fromArchive(file, it.uri, it.name, it.dir, it.size, it.last)
                val listArchive = getStorage().fromArchive(docFile.getUri(), true)
                FileModel(
                    docFile,
                    if (docFile.isDirectory()) listArchive.list().size else 0
                )
            }
        } else {
            check(file.isDirectory()) {
                "${file.originalUri} is not a directory."
            }

            return file.listFiles(context).map {
                FileModel(it, it.takeIf { it.isDirectory() }?.listFiles(context)?.size ?: 0)
            }
        }
    }

    fun getSafFolders() = safFolderDao.getAll()

    suspend fun insertFolder(safFolder: SafFolder) = safFolderDao.insert(safFolder)

    companion object {
        const val KEY_STORAGE_PATH = "storage_path"
    }
}
