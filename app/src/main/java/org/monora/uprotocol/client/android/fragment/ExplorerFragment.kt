package org.monora.uprotocol.client.android.fragment

import android.annotation.TargetApi
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.genonbeta.android.framework.io.DocumentFile
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.leaf.explorer.app.Storage
import com.leaf.explorer.utils.ArchiveUtils
import com.leaf.explorer.utils.CopyPasteUtils
import org.monora.uprotocol.client.android.R
import org.monora.uprotocol.client.android.adapter.FileAdapter
import org.monora.uprotocol.client.android.databinding.LayoutEmptyContentBinding
import org.monora.uprotocol.client.android.databinding.ListPathBinding
import org.monora.uprotocol.client.android.model.FileModel
import org.monora.uprotocol.client.android.util.Activities
import com.leaf.explorer.utils.ExplorerUtils
import org.monora.uprotocol.client.android.util.FileUtils
import dagger.hilt.android.AndroidEntryPoint
import org.monora.uprotocol.client.android.config.AppConfig
import org.monora.uprotocol.client.android.util.CommonErrors
import org.monora.uprotocol.client.android.viewmodel.*
import java.io.File
import java.io.FileNotFoundException

@AndroidEntryPoint
open class ExplorerFragment : Fragment(R.layout.layout_fragment_explorer) {

    @TargetApi(19)
    private val addAccess = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        val context = context

        if (uri != null && context != null) {
            viewModel.insertSafFolder(uri)
        }
    }

    private val viewModel: FilesViewModel by viewModels()
    private val selectionViewModel: SharingSelectionViewModel by activityViewModels()

    private val clientPickerViewModel: ClientPickerViewModel by activityViewModels()
    private val sharingViewModel: SharingViewModel by viewModels()
    private val contentBrowserViewModel: ContentBrowserViewModel by activityViewModels()

    private lateinit var pathsPopupMenu: PopupMenu
    private var paste: CopyPasteUtils.PasteBuilder? = null
    private var viewModelGoBack = true

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        private var afterPopup = false

        override fun handleOnBackPressed() {
            if (viewModelGoBack) {
                if (viewModel.goUp()) {
                    afterPopup = false
                } else if (afterPopup) {
                    isEnabled = false
                    activity?.onBackPressedDispatcher?.onBackPressed()
                } else {
                    afterPopup = true
                    pathsPopupMenu.show()
                }
            } else {
                Toast.makeText(requireContext(), R.string.xplore_goBack, Toast.LENGTH_LONG).show()
            }
        }
    }

    private var currentPath : DocumentFile? = null
    private var fileList = ArrayList<FileModel>()
    private lateinit var pasteButton: FloatingActionButton
    private lateinit var unselectButton: FloatingActionButton
    private var unselectShow = false
    val filter = IntentFilter()

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val a = intent.action ?: return
            if (a == AppConfig.REFRESH_UPDATE) refreshList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        filter.addAction(AppConfig.REFRESH_UPDATE)
    }

    private fun getArchive(file: DocumentFile): Storage.ArchiveReader? {
        val archive = try {
            viewModel.storage.fromArchive(file.getUri(), true)
        } catch (e: FileNotFoundException) {
            null
        }
        viewModel.storage.closeSu()
        return archive
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyView = LayoutEmptyContentBinding.bind(view.findViewById(R.id.emptyView))
        val floatingViewsContainer = view.findViewById<CoordinatorLayout>(R.id.floatingViewsContainer)
        pasteButton = view.findViewById(R.id.pasteButton)
        unselectButton = view.findViewById(R.id.unselectButton)

        val snackbar = Snackbar.make(view, R.string.sending, Snackbar.LENGTH_INDEFINITE)

        val adapter = FileAdapter { fileModel, clickType ->
            when (clickType) {
                FileAdapter.ClickType.Default -> {
                    if (fileModel.file.isDirectory()) {
                        if (viewModelGoBack) {
                            viewModel.requestPath(fileModel.file)
                        }
                    } else if (getArchive(fileModel.file) != null && getArchive(fileModel.file)!!.isDirectory) {
                        if (viewModelGoBack) {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Open As")
                                .setNegativeButton(R.string.file) { _, _ ->
                                    Activities.view(view.context, fileModel.file)
                                }
                                .setPositiveButton(R.string.menu_archive) { _, _ ->
                                    viewModel.requestPath(fileModel.file)
                                }
                                .show()

                        }
                    } else {
                        Activities.view(view.context, fileModel.file)
                    }
                }
                FileAdapter.ClickType.ToggleSelect -> {
                    selectionViewModel.setSelected(fileModel, fileModel.isSelected)
                }
            }
        }

        unselectButton.setOnClickListener {
            unselectButton.visibility = View.GONE
            pasteButton.visibility = View.GONE

            for (fileModel in fileList) {
                fileModel.isSelected = false
                selectionViewModel.setSelected(fileModel, fileModel.isSelected)
            }
            refreshList()
        }

        val emptyContentViewModel = EmptyContentViewModel()

        val pathRecyclerView = view.findViewById<RecyclerView>(R.id.pathRecyclerView)
        val pathSelectorButton = view.findViewById<View>(R.id.pathSelectorButton)
        val pathAdapter = PathAdapter {
            viewModel.requestPath(it.file)
        }
        val safAddedSnackbar = Snackbar.make(floatingViewsContainer, R.string.add_success, Snackbar.LENGTH_LONG)

        pathsPopupMenu = PopupMenu(requireContext(), pathSelectorButton).apply {
            MenuCompat.setGroupDividerEnabled(menu, true)
        }
        pathSelectorButton.setOnClickListener {
            pathsPopupMenu.show()
        }

        emptyView.viewModel = emptyContentViewModel
        emptyView.emptyText.setText(R.string.empty_files_list)
        emptyView.emptyImage.setImageResource(R.drawable.ic_insert_drive_file_white_24dp)
        emptyView.executePendingBindings()

        adapter.setHasStableIds(true)
        recyclerView.adapter = adapter
        pathAdapter.setHasStableIds(true)
        pathRecyclerView.adapter = pathAdapter

        pathAdapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    pathRecyclerView.scrollToPosition(pathAdapter.itemCount - 1)
                }
            }
        )

        viewModel.files.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            emptyContentViewModel.with(recyclerView, it.isNotEmpty())
        }

        viewModel.pathTree.observe(viewLifecycleOwner) {
            pathAdapter.submitList(it)
        }

        viewModel.safFolders.observe(viewLifecycleOwner) {
            pathsPopupMenu.menu.clear()
            val availableStorage = viewModel.getAvailableStorage()

            pathsPopupMenu.setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.storage_folder) {
                     viewModel.requestStorageFolder()
                } else if (menuItem.itemId == R.id.default_storage_folder) {
                     viewModel.requestDefaultStorageFolder()
                } else if (menuItem.groupId == R.id.locations_custom) {
                    viewModel.requestPath(it[menuItem.itemId])
                } else if (menuItem.groupId == R.id.locations_storage) {
                    viewModel.requestPath(availableStorage[menuItem.itemId])
                } else if (menuItem.itemId == R.id.add_storage) {
                    addAccess.launch(null)
                } else if (menuItem.itemId == R.id.clear_storage_list) {
                    viewModel.clearStorageList()
                } else {
                    return@setOnMenuItemClickListener false
                }

                return@setOnMenuItemClickListener true
            }
            pathsPopupMenu.inflate(R.menu.file_browser)
            pathsPopupMenu.menu.findItem(R.id.storage_folder).isVisible = viewModel.isCustomStorageFolder
            pathsPopupMenu.menu.findItem(R.id.clear_storage_list).isVisible = it.isNotEmpty()

            availableStorage.forEachIndexed { index, documentFile ->
                val directoryName = if ("0" == documentFile.getName()) getString(R.string.internal_storage) else documentFile.getName()

                pathsPopupMenu.menu.add(R.id.locations_storage, index, Menu.NONE, directoryName).apply {
                    setIcon(R.drawable.ic_save_white_24dp)
                }
            }

            it.forEachIndexed { index, safFolder ->
                pathsPopupMenu.menu.add(R.id.locations_custom, index, Menu.NONE, safFolder.name).apply {
                    setIcon(R.drawable.ic_save_white_24dp)
                }
            }
        }

        viewModel.path.observe(viewLifecycleOwner) {
            currentPath = it.file
        }

        selectionViewModel.externalState.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
        }

        viewModel.safAdded.observe(viewLifecycleOwner) {
            viewModel.requestPath(it)
            safAddedSnackbar.show()
        }

        sharingViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is SharingState.Running -> snackbar.setText(R.string.sending).show()
                is SharingState.Error -> snackbar.setText(CommonErrors.messageOf(view.context, it.exception)).show()
                is SharingState.Success -> {
                    snackbar.dismiss()
                    findNavController().navigate(
                        ContentBrowserFragmentDirections.actionContentBrowserFragmentToNavTransferDetails(it.transfer)
                    )
                }
            }
        }

        clientPickerViewModel.bridge.observe(viewLifecycleOwner) { bridge ->
            val (groupId, items) = contentBrowserViewModel.items ?: return@observe
            sharingViewModel.consume(bridge, groupId, items)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.explorer_menu, menu)

        val selections = menu.findItem(R.id.selections)
        val share = menu.findItem(R.id.share)
        val shareOnWeb = menu.findItem(R.id.shareOnWeb)
        val createFolder = menu.findItem(R.id.create_folder)
        val rename = menu.findItem(R.id.rename)
        val delete = menu.findItem(R.id.delete)
        val copyTo = menu.findItem(R.id.copy)
        val moveTo = menu.findItem(R.id.move)
        val actionArchive = menu.findItem(R.id.actionArchive)
        val shareExternal = menu.findItem(R.id.share_external)
        val detail = menu.findItem(R.id.detail)

        val prefs = Activities.getLeafSharedPreferences(requireContext(), AppConfig.SHOW_HIDDEN)
        val showHidden = prefs.getBoolean(AppConfig.SHOW_HIDDEN, false)
        menu.findItem(R.id.show_hidden_files).isChecked = showHidden

        selectionViewModel.selectionState.observe(this) {
            val enable = it.isNotEmpty()

            fileList.clear()
            for (data in it) {
                if (data is FileModel) {
                    getFileModel(data)
                }
            }

            viewModelGoBack = !enable
            unselectButton.visibility = if (enable || unselectShow) View.VISIBLE else View.GONE
            selections.title = it.size.toString()

            copyTo.isEnabled = enable
            actionArchive.isEnabled = enable
            detail.isEnabled = enable && it.size == 1
            createFolder.isEnabled = !enable && getArchive(currentPath!!) == null
            selections.isEnabled = enable

            shareExternal.isEnabled = enable && getArchive(currentPath!!) == null
            share.isEnabled = enable && getArchive(currentPath!!) == null
            shareOnWeb.isEnabled = enable && getArchive(currentPath!!) == null
            rename.isEnabled =  enable && it.size == 1 && getArchive(currentPath!!) == null
            delete.isEnabled = enable && getArchive(currentPath!!) == null
            moveTo.isEnabled = enable && getArchive(currentPath!!) == null
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share -> findNavController().navigate(
                ExplorerFragmentDirections.actionExplorerFragmentToPrepareIndexFragment()
            )
            R.id.shareOnWeb -> findNavController().navigate(
                ExplorerFragmentDirections.actionExplorerFragmentToWebShareLauncherFragment()
            )
            R.id.selections -> findNavController().navigate(
                ExplorerFragmentDirections.actionExplorerFragmentToSelectionsFragment()
            )
            R.id.create_folder -> createFolderDialog.show()
            R.id.rename -> {
                if (fileList.size == 1) {
                    renameFileModelDialog(fileList[0])
                }
            }
            R.id.delete -> findNavController().navigate(
                ExplorerFragmentDirections.actionExplorerFragmentToFileDeleteDialog()
            )
            R.id.copy -> pasteButtonShow(currentPath!!.getUri(), false)
            R.id.move -> pasteButtonShow(currentPath!!.getUri(), true)
            R.id.actionArchive -> actionArchive(currentPath!!.getUri())
            R.id.share_external -> ExplorerUtils.shareIntentAll(requireContext(), fileList)
            R.id.detail -> findNavController().navigate(
                ExplorerFragmentDirections.actionExplorerFragmentToFileDetailsDialog()
            )
            R.id.show_hidden_files -> {
                val showHidden = Activities.getLeafPrefBoolean(requireContext(), AppConfig.SHOW_HIDDEN, false)

                if (showHidden) {
                    Activities.editBooleanPreferences(requireContext(), AppConfig.SHOW_HIDDEN, false)
                    item.isChecked = false
                } else {
                    Activities.editBooleanPreferences(requireContext(), AppConfig.SHOW_HIDDEN, true)
                    item.isChecked = true
                }

                refreshList()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        refreshList()
        requireContext().registerReceiver(receiver, filter)
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    override fun onPause() {
        super.onPause()
        backPressedCallback.remove()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(receiver)
        createFolderDialog.dismiss()
    }

    private fun refreshList() {
        selectionViewModel.externalState.value = Unit
        if (currentPath != null) {
            viewModel.requestPath(currentPath!!)
        }
    }

    private fun actionArchive(copyPath: Uri) {
        val copiedItems = ArrayList<DocumentFile>()

        for (fileModel in fileList) {
            copiedItems.add(fileModel.file)

            fileModel.isSelected = false
            selectionViewModel.setSelected(fileModel, fileModel.isSelected)
        }

        unselectShow = true
        pasteButton.visibility = View.VISIBLE
        refreshList()

        pasteButton.setOnClickListener {
            ArchiveUtils.CreateZipFile(copyPath, requireContext(), viewModel.storage, copiedItems, currentPath!!.getUri())
            pasteButton.visibility = View.GONE
            unselectShow = false
            unselectButton.visibility = View.GONE
            refreshList()
        }
    }

    private fun pasteButtonShow(copyPath: Uri, move: Boolean) {
        val copiedItems = ArrayList<DocumentFile>()

        for (fileModel in fileList) {
            copiedItems.add(fileModel.file)

            fileModel.isSelected = false
            selectionViewModel.setSelected(fileModel, fileModel.isSelected)
        }

        unselectShow = true
        pasteButton.visibility = View.VISIBLE
        refreshList()

        pasteButton.setOnClickListener {
            if (FileUtils.getAbsolutePath(currentPath!!.getUri()) == FileUtils.getAbsolutePath(copyPath)) {
                Toast.makeText(context, "Choose Another Path", Toast.LENGTH_LONG).show()
            } else {
                pasteAction(copyPath, copiedItems, move, currentPath!!.getUri())
            }
        }
    }

    private fun pasteAction(copyUri: Uri, items: MutableList<DocumentFile>, move: Boolean, destination: Uri) {
        val copiedItems = ArrayList<DocumentFile>()

        paste = object : CopyPasteUtils.PasteBuilder(context) {
            override fun success() {
                super.success()
                pasteButton.visibility = View.GONE
                unselectShow = false
                unselectButton.visibility = View.GONE
                refreshList()
            }

            override fun dismiss() {
                super.dismiss()
                paste = null
                pasteButton.visibility = View.GONE
                unselectShow = false
                unselectButton.visibility = View.GONE
                refreshList()
            }
        }

        for (pUri in items) {
            if (pUri.isDirectory() && FileUtils.getAbsolutePath(currentPath!!.getUri()).path!!.startsWith(FileUtils.getAbsolutePath(pUri.getUri()).path!!)) {

            } else {
                copiedItems.add(pUri)
            }
        }

        paste!!.create(copyUri, CopyPasteUtils.getNode(requireContext(), viewModel.storage, copiedItems), move, destination)
        paste!!.show()
    }

    private val createFolderDialog by lazy {
        val view = layoutInflater.inflate(R.layout.layout_create_folder, null, false)
        val editText = view.findViewById<EditText>(R.id.editText)

        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.create_folder)
            .setView(view)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.create, null)
            .create().also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                        val displayName = editText.text.toString().trim()
                        editText.error = if (displayName.isEmpty()) {
                            getString(R.string.error_empty_field)
                        } else if (viewModel.createFolder(displayName)) {
                            dialog.dismiss()
                            editText.text.clear()
                            null
                        } else {
                            getString(R.string.create_folder_failure)
                        }
                    }
                }
            }
    }

    private fun getFileModel(fileModel: FileModel) {
        fileList.add(fileModel)
    }

    private fun renameFileModelDialog(renameFileModel: FileModel) {
        val reDocFile = renameFileModel.file
        var extWarning = false
        var extension: String? = null
        try {
            extension = reDocFile.getUri().path!!.substring(reDocFile.getUri().path!!.lastIndexOf("."))
        } catch (e: Exception) {
        }

        val view = layoutInflater.inflate(R.layout.layout_create_folder, null, false)
        val editText = view.findViewById<EditText>(R.id.editText)

        editText.setText(reDocFile.getName())
        renameFileModel.isSelected = false
        selectionViewModel.setSelected(renameFileModel, renameFileModel.isSelected)

        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.rename)
            .setView(view)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.rename, null)
            .setCancelable(false)
            .create().also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
                        dialog.dismiss()
                        editText.text.clear()
                        refreshList()
                    }

                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                        val displayName = editText.text.toString().trim()
                        var extension2: String? = null
                        try {
                            extension2 = displayName.substring(displayName.lastIndexOf("."))
                        } catch (e: Exception) {
                        }

                        if (!extWarning) {
                            if (extension != null) {
                                if (extension2 != null)  {
                                    if (extension2 != extension) {
                                        extWarning = true
                                    }
                                } else {
                                    extWarning = true
                                }
                            } else {
                                if (extension2 != null)  {
                                    extWarning = true
                                }
                            }
                        } else {
                            extWarning = false
                        }

                        editText.error = if (displayName.isEmpty()) {
                            getString(R.string.error_empty_field)
                        } else if (displayName == reDocFile.getName()) {
                            getString(R.string.already_exits)
                        } else if (extWarning && !reDocFile.isDirectory()) {
                            getString(R.string.extWarning)
                        } else if (viewModel.renameFileModel(reDocFile, displayName)) {
                            dialog.dismiss()
                            editText.text.clear()
                            null
                        } else {
                            getString(R.string.unknown_failure)
                        }
                    }
                }
            }
            .show()
    }


}

class PathContentViewModel(fileModel: FileModel) {
    val isRoot = fileModel.file.getUri() == ROOT_URI

    val isFirst = fileModel.file.parent == null

    val title = fileModel.file.getName()

    companion object {
        val ROOT_URI: Uri = Uri.fromFile(File("/"))
    }
}

class FilePathViewHolder constructor(
    private val clickListener: (FileModel) -> Unit,
    private var binding: ListPathBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(fileModel: FileModel) {
        binding.viewModel = PathContentViewModel(fileModel)
        binding.button.setOnClickListener {
            clickListener(fileModel)
        }
        binding.button.isEnabled = fileModel.file.canRead()
        binding.executePendingBindings()
    }
}

class PathAdapter(
    private val clickListener: (FileModel) -> Unit,
) : ListAdapter<FileModel, FilePathViewHolder>(FileModelItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilePathViewHolder {
        return FilePathViewHolder(
            clickListener,
            ListPathBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: FilePathViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).listId
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_PATH
    }

    companion object {
        const val VIEW_TYPE_PATH = 0
    }
}

class FileModelItemCallback : DiffUtil.ItemCallback<FileModel>() {
    override fun areItemsTheSame(oldItem: FileModel, newItem: FileModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: FileModel, newItem: FileModel): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
    }
}

