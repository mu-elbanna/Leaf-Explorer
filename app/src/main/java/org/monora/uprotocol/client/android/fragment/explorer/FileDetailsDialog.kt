package org.monora.uprotocol.client.android.fragment.explorer

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.genonbeta.android.framework.io.DocumentFile
import com.genonbeta.android.framework.util.Files
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import org.monora.uprotocol.client.android.R
import org.monora.uprotocol.client.android.config.AppConfig
import org.monora.uprotocol.client.android.model.FileModel
import org.monora.uprotocol.client.android.util.FileUtils
import org.monora.uprotocol.client.android.viewmodel.SharingSelectionViewModel

@AndroidEntryPoint
class FileDetailsDialog : BottomSheetDialogFragment() {
    private val selectionViewModel: SharingSelectionViewModel by activityViewModels()

    private var fileList = ArrayList<FileModel>()
    private var total: Long = 0
    private var folderContent = 0
    private var fileContent = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_file_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectionViewModel.selectionState.observe(this) {
            for (data in it) {
                if (data is FileModel) {
                    getFileModel(data)
                }

                if (fileList.size == it.size) {
                    actionDetails(view)
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        selectionViewModel.externalState.value = Unit
    }

    private fun getFileModel(fileModel: FileModel) {
        fileList.add(fileModel)
    }

    private fun actionDetails(view: View) {
        val name: TextView = view.findViewById(R.id.name)
        val path: TextView = view.findViewById(R.id.path)
        val size: TextView = view.findViewById(R.id.size)
        val date: TextView = view.findViewById(R.id.date)
        val content: TextView = view.findViewById(R.id.content)

        if (fileList.size == 1) {
            for (fileModel in fileList) {
                getFileInfo(fileModel.file)

                name.text = fileModel.file.getName()
                path.text = FileUtils.getAbsolutePath(fileModel.file.getUri()).path ?: "null"
                size.text = Files.formatLength(total, false)
                date.text = "null" //MusicLibraryHelper.formatDate(fileModel.file.getLastModified())
                content.text = requireContext().getString(R.string.folders) + " : " + folderContent + " | " +
                        requireContext().getString(R.string.files) + " : " + fileContent
            }
        }
    }

    private fun getFileInfo(docFile: DocumentFile) {
        if (docFile.isDirectory()) {
            total += docFile.getLength()
            folderContent += 1
            val fList = docFile.listFiles(requireContext())
            for (f in fList) {
                getFileInfo(f)
            }
        } else {
            total += docFile.getLength()
            fileContent += 1

        }
    }
}