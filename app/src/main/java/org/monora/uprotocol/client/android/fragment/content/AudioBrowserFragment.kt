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

package org.monora.uprotocol.client.android.fragment.content

import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.leaf.music.util.MusicPlayerRemote
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.monora.uprotocol.client.android.R
import org.monora.uprotocol.client.android.content.Folder
import org.monora.uprotocol.client.android.content.Playlist
import org.monora.uprotocol.client.android.content.Song
import org.monora.uprotocol.client.android.data.MediaRepository
import org.monora.uprotocol.client.android.data.SelectionRepository
import org.monora.uprotocol.client.android.databinding.*
import org.monora.uprotocol.client.android.util.Activities
import org.monora.uprotocol.client.android.viewmodel.EmptyContentViewModel
import org.monora.uprotocol.client.android.viewmodel.SharingSelectionViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AudioBrowserFragment : Fragment(R.layout.layout_audio_browser) {
    private val browserViewModel: AudioBrowserViewModel by viewModels()

    private val selectionViewModel: SharingSelectionViewModel by activityViewModels()

    private var isMusicActivity = false
    private var songPlay = ArrayList<Song>()

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            when (browserViewModel.showingContent.value) {
                is AudioBrowserViewModel.Content.AllFolders, is AudioBrowserViewModel.Content.AllPlaylists -> {
                    browserViewModel.showAllSongs()
                }
                is AudioBrowserViewModel.Content.PlaylistSongs -> browserViewModel.showPlaylists()
                is AudioBrowserViewModel.Content.FolderSongs -> browserViewModel.showFolders()
                else -> isEnabled = false
            }
        }
    }

    private fun insertData(songList: List<Song>) {
        songPlay.clear()
        for (path in songList) {
            loadSongsFolderData(path)
        }
    }

    private fun loadSongsFolderData(path: Song) {
        songPlay.add(Song(path.id, path.artist, path.album, path.folder,
            path.title, path.displayName, path.mimeType, path.size, path.dateModified, path.duration, path.uri, path.albumUri))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chip_group)
        val filterChip = view.findViewById<Chip>(R.id.filter_chip)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyView = LayoutEmptyContentBinding.bind(view.findViewById(R.id.emptyView))
        val songsAdapter = SongBrowserAdapter { song, position, clickType ->
            when (clickType) {
                SongBrowserAdapter.ClickType.Default -> {
                    if (isMusicActivity) {
                        MusicPlayerRemote.sendAllSong(songPlay, position)
                    } else {
                        Activities.view(view.context, song.uri, song.mimeType)
                    }
                }
                SongBrowserAdapter.ClickType.ToggleSelect -> {
                    selectionViewModel.setSelected(song, song.isSelected)
                }
            }
        }
        val foldersAdapter = FolderBrowserAdapter {
            browserViewModel.showFolderSongs(it)
        }
        val playlistsAdapter = PlaylistBrowserAdapter {
            browserViewModel.showPlaylistSongs(it)
        }
        val emptyContentViewModel = EmptyContentViewModel()
        val layoutManager = recyclerView.layoutManager

        check(layoutManager is GridLayoutManager) {
            "Expected a grid layout manager"
        }

        val recylerViewLayoutParams = recyclerView.layoutParams
        val defaultSpanCount = layoutManager.spanCount
        val listMargin = resources.getDimension(R.dimen.short_content_width_padding_between).toInt()
        val listPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics
        ).toInt()

        emptyView.viewModel = emptyContentViewModel
        emptyView.executePendingBindings()
        songsAdapter.setHasStableIds(true)
        foldersAdapter.setHasStableIds(true)

        filterChip.setOnCloseIconClickListener {
            browserViewModel.showAllSongs()
        }

        browserViewModel.showingContent.observe(viewLifecycleOwner) {
            layoutManager.spanCount = if (it.isList) 1 else defaultSpanCount

            if (it.isList) {
                recyclerView.setPadding(0, listPadding, 0, listPadding)
            } else {
                recyclerView.setPadding(listPadding)
            }

            if (recylerViewLayoutParams is ViewGroup.MarginLayoutParams) {
                if (it.isList) {
                    recylerViewLayoutParams.setMargins(listMargin, 0, listMargin, 0)
                } else {
                    recylerViewLayoutParams.setMargins(0)
                }
            }

            filterChip.visibility = if (it.isFiltered) View.VISIBLE else View.GONE

            if (it.isFiltered) {
                chipGroup.check(R.id.filter_chip)
            }

            when (it) {
                is AudioBrowserViewModel.Content.AllFolders -> {
                    recyclerView.adapter = foldersAdapter
                    foldersAdapter.submitList(it.list)
                    chipGroup.check(R.id.show_folders)
                }
                is AudioBrowserViewModel.Content.AllPlaylists -> {
                    recyclerView.adapter = playlistsAdapter
                    playlistsAdapter.submitList(it.list)
                    chipGroup.check(R.id.show_playlist)
                }
                is AudioBrowserViewModel.Content.AllSongs -> {
                    recyclerView.adapter = songsAdapter
                    songsAdapter.submitList(it.list)
                    chipGroup.check(R.id.show_all_songs)
                    insertData(it.list)
                }
                is AudioBrowserViewModel.Content.FolderSongs -> {
                    filterChip.setChipIconResource(R.drawable.ic_folder_white_24dp)
                    filterChip.text = it.folder.title
                    recyclerView.adapter = songsAdapter
                    songsAdapter.submitList(it.list)
                    insertData(it.list)
                }
                is AudioBrowserViewModel.Content.PlaylistSongs -> {
                    filterChip.setChipIconResource(R.drawable.ic_library_music_white_24dp)
                    filterChip.text = it.artist.name
                    recyclerView.adapter = songsAdapter
                    songsAdapter.submitList(it.list)
                    insertData(it.list)
                }
            }

            when (it.type) {
                AudioBrowserViewModel.Content.Type.Songs -> {
                    emptyView.emptyText.setText(R.string.empty_music_list)
                    emptyView.emptyImage.setImageResource(R.drawable.ic_music_note_white_24dp)
                    emptyContentViewModel.with(recyclerView, songsAdapter.currentList.isNotEmpty())
                }
                AudioBrowserViewModel.Content.Type.Folders -> {
                    emptyView.emptyText.setText(R.string.no_folders)
                    emptyView.emptyImage.setImageResource(R.drawable.ic_folder_white_24dp)
                    emptyContentViewModel.with(recyclerView, foldersAdapter.currentList.isNotEmpty())
                }
                AudioBrowserViewModel.Content.Type.Playlists -> {
                    emptyView.emptyText.setText(R.string.coming_soon)
                    emptyView.emptyImage.setImageResource(R.drawable.ic_library_music_white_24dp)
                    emptyContentViewModel.with(recyclerView, playlistsAdapter.currentList.isNotEmpty())
                }
            }

            backPressedCallback.isEnabled = it !is AudioBrowserViewModel.Content.AllSongs
        }

        selectionViewModel.externalState.observe(viewLifecycleOwner) {
            songsAdapter.notifyDataSetChanged()
        }

        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.show_all_songs -> browserViewModel.showAllSongs()
                R.id.show_folders -> browserViewModel.showFolders()
                R.id.show_playlist -> {
                    browserViewModel.showPlaylists()
                    Toast.makeText(requireContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    override fun onPause() {
        super.onPause()
        backPressedCallback.remove()
    }

    fun isMusicActivity(value: Boolean) {
        isMusicActivity = value
    }
}

class FolderBrowserAdapter(
    val clickListener: (Folder) -> Unit
) : ListAdapter<Folder, FolderViewHolder>(FolderItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        return FolderViewHolder(
            ListFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            clickListener,
        )
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_FOLDER
    }

    companion object {
        const val VIEW_TYPE_FOLDER = 0
    }
}

class FolderContentViewModel(folder: Folder) {
    val artist = folder.song.size.toString() + " songs"

    val title = folder.title

    val uri = folder.song[0].albumUri

    val count = folder.song.size - 1
    val folderSongs = if (folder.song.size > 1) "+ $count" else ""
}

class FolderItemCallback : DiffUtil.ItemCallback<Folder>() {
    override fun areItemsTheSame(oldItem: Folder, newItem: Folder): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Folder, newItem: Folder): Boolean {
        return oldItem == newItem
    }
}

class FolderViewHolder(
    private val binding: ListFolderBinding,
    private val clickListener: (Folder) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(folder: Folder) {
        binding.viewModel = FolderContentViewModel(folder)
        binding.root.setOnClickListener {
            clickListener(folder)
        }
    }
}

class PlaylistBrowserAdapter(
    val clickListener: (Playlist) -> Unit
) : ListAdapter<Playlist, PlaylistViewHolder>(PlaylistItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        return PlaylistViewHolder(
            ListPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            clickListener,
        )
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_PLAYLIST
    }

    companion object {
        const val VIEW_TYPE_PLAYLIST = 0
    }
}

class PlaylistViewHolder(
    private val binding: ListPlaylistBinding,
    private val clickListener: (Playlist) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(playlist: Playlist) {
        binding.viewModel = PlaylistContentViewModel(playlist)
        binding.root.setOnClickListener {
            clickListener(playlist)
        }
    }
}

class PlaylistContentViewModel(playlist: Playlist) {
    val name = playlist.name

    val numberOfAlbums = playlist.song.size
}

class PlaylistItemCallback : DiffUtil.ItemCallback<Playlist>() {
    override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem == newItem
    }
}

class SongBrowserAdapter(
    val clickListener: (Song, Int, ClickType) -> Unit
) : ListAdapter<Song, SongViewHolder>(SongItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            ListSongBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            clickListener,
        )
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_SONG
    }

    enum class ClickType {
        Default,
        ToggleSelect,
    }

    companion object {
        const val VIEW_TYPE_SONG = 0
    }
}

class SongItemCallback : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem == newItem
    }
}

class SongContentViewModel(song: Song) {
    val artist = song.artist

    val title = song.title

    val mimeType = song.mimeType

    val albumUri = song.albumUri
}

class SongViewHolder(
    private val binding: ListSongBinding,
    private val clickListener: (Song, Int, SongBrowserAdapter.ClickType) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(song: Song, position: Int) {
        binding.viewModel = SongContentViewModel(song)
        binding.root.setOnClickListener {
            clickListener(song, position, SongBrowserAdapter.ClickType.Default)
        }
        binding.selection.setOnClickListener {
            song.isSelected = !song.isSelected
            it.isSelected = song.isSelected
            clickListener(song, position, SongBrowserAdapter.ClickType.ToggleSelect)
        }
        binding.selection.isSelected = song.isSelected
        binding.executePendingBindings()
    }
}

@HiltViewModel
class AudioBrowserViewModel @Inject internal constructor(
    private val mediaRepository: MediaRepository,
    private val selectionRepository: SelectionRepository,
) : ViewModel() {
    private val _showingContent = MutableLiveData<Content>()

    val showingContent = liveData<Content> {
        emitSource(_showingContent)
    }

    private fun filterSongs(list: List<Song>): List<Song> {
        selectionRepository.whenContains(list) { item, selected -> item.isSelected = selected }
        return list
    }

    fun showAllSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            _showingContent.postValue(Content.AllSongs(filterSongs(mediaRepository.getAllSongs())))
        }
    }

    fun showFolders() {
        viewModelScope.launch(Dispatchers.IO) {
            _showingContent.postValue(Content.AllFolders(mediaRepository.getAllFolders()))
        }
    }

    fun showPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            _showingContent.postValue(Content.AllPlaylists(mediaRepository.getAllPlaylists()))
        }
    }

    fun showFolderSongs(folder: Folder) {
        viewModelScope.launch(Dispatchers.IO) {
            _showingContent.postValue(Content.FolderSongs(folder, filterSongs(mediaRepository.getFolderSongs(folder))))
        }
    }

    fun showPlaylistSongs(playlist: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            _showingContent.postValue(Content.PlaylistSongs(playlist, mediaRepository.getPlaylistSongs(playlist)))
        }
    }

    init {
        showAllSongs()
    }

    sealed class Content(val type: Type, val isList: Boolean = false, val isFiltered: Boolean = false) {
        class AllSongs(val list: List<Song>) : Content(Type.Songs, isList = true)

        class AllFolders(val list: List<Folder>) : Content(Type.Folders)

        class AllPlaylists(val list: List<Playlist>) : Content(Type.Playlists)

        class FolderSongs(val folder: Folder, val list: List<Song>) : Content(Type.Songs, isList = true, isFiltered = true)

        class PlaylistSongs(val artist: Playlist, val list: List<Song>) : Content(Type.Songs, isList = true, isFiltered = true)

        enum class Type {
            Songs,
            Folders,
            Playlists,
        }
    }
}
