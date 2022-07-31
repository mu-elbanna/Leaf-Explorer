package org.monora.uprotocol.client.android.util

import android.content.Context
import android.os.StatFs
import com.genonbeta.android.framework.io.DocumentFile
import com.genonbeta.android.framework.util.Files
import org.monora.uprotocol.client.android.R
import org.monora.uprotocol.client.android.model.StorageModel
import com.leaf.music.MPConstants
import java.io.File

object HomeFragHelper {

    fun getStorageLocation(context: Context): ArrayList<StorageModel> {

        val dataStorage = ArrayList<StorageModel>()
        dataStorage.clear()

        for (storage in getAvailableStorage(context)) {
            dataStorage.add(
                StorageModel(storage.storageName,
                    "Free : " + getAvailableInternalStorageSize(File(storage.storagePath)),
                    "Total : " + getTotalInternalMemorySize(File(storage.storagePath)),
                    "Used : " + getUsedInternalMemorySize(File(storage.storagePath)),
                    getAvailableInternalStoragePercentage(File(storage.storagePath)),
                    storage.storagePath)
            )
        }

        return dataStorage
    }

    private fun getAvailableStorage(context: Context): ArrayList<Storage> {
        val path = ArrayList<Storage>()
        var storageFile: File? = null
        var friendlyName: String? = null

        var i = 0

        for (mediaDir in MPConstants.referencedDirectoryList(context)) {
            if (mediaDir == null || !mediaDir.canWrite() || !mediaDir.canRead()) continue

            val splitPath = mediaDir.absolutePath.split(File.separator.toRegex()).toTypedArray()
            if (splitPath.size >= 2 && splitPath[1] == "storage") {
                if (splitPath.size >= 4 && splitPath[2] == "emulated") {
                    val file = File(MPConstants.buildPath(splitPath, 4))
                    if (file.canWrite() || file.canRead()) {
                        storageFile = file
                        friendlyName =
                            if ("0" == splitPath[3]) {
                                context.getString(R.string.internal_storage)
                            } else {
                                context.getString(R.string.emulated_media_directory, splitPath[3])
                            }
                    }
                } else if (splitPath.size >= 3) {
                    val file = File(MPConstants.buildPath(splitPath, 3))
                    if (file.canWrite() || file.canRead()) {
                        friendlyName = splitPath[2]
                        storageFile = file
                    }
                }
            }

            path.add(Storage(i, storageFile!!.absolutePath, friendlyName!!))
            i++
        }

        return path
    }

    fun getCategoryInfo(context: Context, size: Int): String {

        return "(" + size.toString() + " " + context.getString(R.string.files) + ")"
    }

    private fun getUsedInternalMemorySize(path: File): String {
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        val totalSize = totalBlocks * blockSize
        val availableSize = availableBlocks * blockSize
        return Files.formatLength(totalSize - availableSize, true)
    }

    private fun getTotalInternalMemorySize(path: File): String {
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return Files.formatLength(totalBlocks * blockSize, true)
    }

    private fun getAvailableInternalStorageSize(path: File): String {
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return Files.formatLength(availableBlocks * blockSize, true)
    }

    private fun getAvailableInternalStoragePercentage(path: File): Int {
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val totalSize = totalBlocks * blockSize
        val availableBlocks = stat.availableBlocksLong
        val availableSize = availableBlocks * blockSize
        // Log.d("here is", "" + availableSize * 100 / totalSize)
        val size = (availableSize * 100 / totalSize).toInt()
        return 100 - size
    }

    fun compareName(file1: DocumentFile, file2: DocumentFile): Int {
        val name1 = file1.getName()
        val name2 = file2.getName()
        return name1.compareTo(name2, ignoreCase = true)
    }
}

data class Storage(val id: Int, val storagePath: String, val storageName: String)