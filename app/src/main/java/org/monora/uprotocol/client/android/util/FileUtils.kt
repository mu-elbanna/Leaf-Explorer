package org.monora.uprotocol.client.android.util

import android.net.Uri
import android.os.Environment
import com.genonbeta.android.framework.io.DocumentFile
import java.io.File

object FileUtils {

    private const val PRIMARY = "primary"

    fun getAbsolutePath(uri : Uri): Uri {
        if (uri.path!!.startsWith("/storage")) {
            return uri
        } else {
            val uriPath = uri.path!! // e.g. /tree/primary:Music
            val storageId = uriPath.substringBefore(':').substringAfterLast('/')

            val rootFolder = uriPath.substringAfter(':', "")
            val getPath = rootFolder.substringAfter(':', "")
            val newUri = if (storageId == PRIMARY) {
                DocumentFile.fromFile(File("/storage/emulated/0/$getPath")).getUri()
            } else {
                DocumentFile.fromFile(File("/storage/$storageId/$getPath")).getUri()
            }

            return newUri
        }
    }

    fun getMediaStorePath(volume: String, relativePath: String, name: String, extension: String) : String {
        return if (volume == "external_primary") {
            "/storage/emulated/0/$relativePath$name.mp3"
        } else {
            "/storage/$volume/$relativePath$name.mp3"
        }
    }

}