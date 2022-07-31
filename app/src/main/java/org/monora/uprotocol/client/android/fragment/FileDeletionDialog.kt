package org.monora.uprotocol.client.android.fragment

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.genonbeta.android.framework.io.DocumentFile
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import org.monora.uprotocol.client.android.R
import org.monora.uprotocol.client.android.config.AppConfig
import org.monora.uprotocol.client.android.model.FileModel
import org.monora.uprotocol.client.android.viewmodel.SharingSelectionViewModel
import java.io.File
import java.io.FileNotFoundException

@AndroidEntryPoint
class FileDeletionDialog : BottomSheetDialogFragment() {
    private val selectionViewModel: SharingSelectionViewModel by activityViewModels()

    private var fileList = ArrayList<FileModel>()
    private val copiedItems: MutableList<Uri> = ArrayList()
    private lateinit var text1: TextView
    private lateinit var textDetails: TextView
    private lateinit var deleteButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private lateinit var deleteProgress: ProgressBar
    private var mTotalDeletion = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_file_deletion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        text1 = view.findViewById(R.id.text1)
        textDetails = view.findViewById(R.id.textDetails)
        deleteProgress = view.findViewById(R.id.progress)
        deleteButton = view.findViewById(R.id.deleteButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        cancelButton.setOnClickListener {
            dismiss()
        }

        selectionViewModel.selectionState.observe(this) {
            for (data in it) {
                if (data is FileModel) {
                    getFileModel(data)
                }

                if (fileList.size == it.size) {
                    actionDelete()
                }
            }
        }

        isCancelable = false
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        selectionViewModel.externalState.value = Unit
        requireContext().sendBroadcast(Intent(AppConfig.REFRESH_UPDATE))
    }

    private fun getFileModel(fileModel: FileModel) {
        fileList.add(fileModel)
    }

    private fun actionDelete() {
        val details = getString(R.string.text_total) + " : " + fileList.size.toString()
        textDetails.text = details

        for (itemUri in fileList) {
            copiedItems.add(itemUri.file.getUri())
        }
        deleteButton.visibility = View.VISIBLE
        deleteButton.setOnClickListener {
            val permanently = Runnable {
                deleteProgress.visibility = View.VISIBLE
                text1.text = getString(R.string.wait)
                deleteButton.visibility = View.GONE
                deletionDialog(requireContext(), copiedItems)
            }
            val deletePermanent = true
            if (deletePermanent) {
                permanently.run()
            }
        }
    }

    private fun deletionDialog(context: Context, copiedItems: MutableList<Uri>) {
        for (currentUri in copiedItems) {
            try {
                val file: DocumentFile = DocumentFile.fromUri(context, currentUri)
                delete(file)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
        for (fileModel in fileList) {
            fileModel.isSelected = false
            selectionViewModel.setSelected(fileModel, fileModel.isSelected)
        }
        dismiss()
    }

    private fun delete(file: DocumentFile) {

        val splitPath = file.getUri().path.toString().split(File.separator.toRegex()).toTypedArray()
        if (splitPath[1] == "storage" || splitPath[2] == "emulated") {
            deleteEmulated(File(file.filePath!!))
        } else {
            val isDirectory = file.isDirectory()
            val isFile = file.isFile()
            if (isDirectory) {
                deleteDirectory(file)
            }
            if (file.delete(requireContext())) {
                if (isFile) {
                    mTotalDeletion++
                }
              //  listener.onFileDeletion()
            } else {
              //  listener.onFileDeletion()
            }
        }

    }

    private fun deleteEmulated(file: File) {
        if (file.isDirectory) {
            for (anotherFile in file.listFiles()!!) {
                deleteEmulated(anotherFile)
            }
        }
        if (file.delete()) {
            if (file.isFile) {
                mTotalDeletion++
            }
           // listener.onFileDeletion()
        } else {
           // listener.onFileDeletion()
        }
    }

    private fun deleteDirectory(folder: DocumentFile) {
        val files = folder.listFiles(requireContext())
        for (anotherFile in files) {
            delete(anotherFile)
        }
    }
}