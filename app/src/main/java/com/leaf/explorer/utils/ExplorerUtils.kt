package com.leaf.explorer.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.genonbeta.android.framework.io.DocumentFile
import org.monora.uprotocol.client.android.R
import org.monora.uprotocol.client.android.model.FileModel

object ExplorerUtils {

    private val fileModelList = ArrayList<DocumentFile>()
    fun shareIntentAll(context: Context, selectedItemList: MutableList<FileModel>) {
        fileModelList.clear()
        for (list in selectedItemList) {
            getShareUri(context, list.file)
        }
        try {
            shareIntent(context, fileModelList)
        } catch (e: Exception) {
            Toast.makeText(context, R.string.unknown_failure, Toast.LENGTH_LONG).show()
        }

    }

    private fun shareIntent(context: Context, selectedItemList: MutableList<DocumentFile>) {
        if (selectedItemList.size > 0) {
            val shareIntent = Intent()
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.action = if (selectedItemList.size > 1) Intent.ACTION_SEND_MULTIPLE else Intent.ACTION_SEND

            if (selectedItemList.size > 1) {
                val uriList = ArrayList<Uri>()
                val mimeGrouper = Utils.MIMEGrouper()

                for (sharedItem in selectedItemList) {
                    uriList.add(sharedItem.getSecureUri(context, context.getString(R.string.file_provider)))
                    if (!mimeGrouper.isLocked) mimeGrouper.process(sharedItem.getType())
                }
                shareIntent.type = mimeGrouper.toString()
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
            } else if (selectedItemList.size == 1) {
                val sharedItem  = selectedItemList[0]
                shareIntent.type = sharedItem.getType()
                shareIntent.putExtra(Intent.EXTRA_STREAM, sharedItem.getSecureUri(context, context.getString(R.string.file_provider)))
            }
            try {
                context.startActivity(
                    Intent.createChooser(
                        shareIntent,
                        context.getString(R.string.text_fileShareAppChoose)
                    )
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                Toast.makeText(context, R.string.unknown_failure, Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(context, R.string.unknown_failure, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getShareUri(context: Context, fileModel: DocumentFile) {
        if (fileModel.isDirectory()) {
            val listFile = fileModel.listFiles(context)
            for (file in listFile) {
                getShareUri(context, file)
            }
        } else{
            fileModelList.add(fileModel)
        }
    }

}