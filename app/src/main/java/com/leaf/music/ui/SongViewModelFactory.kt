package com.leaf.music.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leaf.music.repository.SongRepository
import org.monora.uprotocol.client.android.data.SelectionRepository
import org.monora.uprotocol.client.android.viewmodel.SharingSelectionViewModel

@Suppress("UNCHECKED_CAST")
class SongViewModelFactory(private val repository: SongRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SongViewModel(repository) as T
    }
}

@Suppress("UNCHECKED_CAST")
class SharingModelFactory(private val context: Context, private val selectionRepository: SelectionRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SharingSelectionViewModel(context, selectionRepository) as T
    }
}