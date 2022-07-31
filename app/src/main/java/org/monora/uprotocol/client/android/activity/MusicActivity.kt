package org.monora.uprotocol.client.android.activity

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.leaf.music.repository.SongRepository
import com.leaf.music.ui.SharingModelFactory
import com.leaf.music.ui.SongViewModel
import com.leaf.music.ui.SongViewModelFactory
import com.leaf.music.ui.fragment.PlayerFragment
import com.leaf.music.util.Constants.PREF_NAME
import com.leaf.music.util.MusicPlayerRemote
import com.leaf.music.util.PlayerHelper
import dagger.hilt.android.AndroidEntryPoint
import org.monora.uprotocol.client.android.R
import org.monora.uprotocol.client.android.app.Activity
import org.monora.uprotocol.client.android.content.Song
import org.monora.uprotocol.client.android.data.SelectionRepository
import org.monora.uprotocol.client.android.fragment.content.AudioBrowserFragment
import org.monora.uprotocol.client.android.viewmodel.SharingSelectionViewModel

@AndroidEntryPoint
class MusicActivity : Activity() {

    private val selectionViewModel: SharingSelectionViewModel by viewModels {
        SharingModelFactory(this, SelectionRepository())
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dimBackground: View
    private val currentSong get() = PlayerHelper.getCurrentSong(applicationContext, sharedPreferences)

    private val panelState: Int
        get() = bottomSheetBehavior.state

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    dimBackground.visibility = View.GONE
                }
                else -> {
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            setMiniPlayerAlpha(slideOffset)
            dimBackground.visibility = View.VISIBLE
            dimBackground.alpha = slideOffset
        }
    }

    private var serviceToken: MusicPlayerRemote.ServiceToken? = null

    private val selectedSong = ArrayList<Song>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dimBackground = findViewById(R.id.dimBackground)
        val slidingPanel = findViewById<FrameLayout>(R.id.slidingPanel)

        val repository = SongRepository(this)
        val viewModelFactory = SongViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(SongViewModel::class.java)

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        serviceToken = MusicPlayerRemote.bindToService(this, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                reloadPlayerFragment()
                if (MusicPlayerRemote.playerService?.mediaPlayer == null && currentSong != null) {
                    MusicPlayerRemote.playerService?.initMediaPlayer(currentSong!!.id)
                    viewModel.songLiveData.observe(this@MusicActivity) {
                        if (it.isNotEmpty()) {
                            MusicPlayerRemote.sendAllSong(it, -1)
                        } else {
                            MusicPlayerRemote.sendAllSong(
                                emptyList(),
                                -1
                            )
                        }
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
            }
        })

        bottomSheetBehavior = BottomSheetBehavior.from(slidingPanel)
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)

        slidingPanel.setOnClickListener {
            expandPanel()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_music, menu)

        val selections = menu.findItem(R.id.selections)
        val playSelected = menu.findItem(R.id.playSelected)

        selectionViewModel.selectionState.observe(this) {
            val enable = it.isNotEmpty()

            selectedSong.clear()
            for (data in it) {
                if (data is Song) {
                    getSong(data)
                }
            }

            selections.title = it.size.toString()
            selections.isEnabled = enable
            playSelected.isEnabled = enable
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun getSong(song: Song) {
        selectedSong.add(song)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.playSelected -> MusicPlayerRemote.sendAllSong(selectedSong, 0)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        //MusicPlayerRemote.unbindFromService(serviceToken)
    }

    override fun onBackPressed() {
        if (!handleBackPress()) super.onBackPressed()
    }

    private fun handleBackPress(): Boolean {
        if (panelState == BottomSheetBehavior.STATE_EXPANDED) {
            collapsePanel()
            return true
        }
        return false
    }

    private fun collapsePanel() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        setMiniPlayerAlpha(0f)
    }

    private fun expandPanel() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        setMiniPlayerAlpha(1f)
    }

    private val playerFragment = PlayerFragment()
    private val audioBrowserFragment = AudioBrowserFragment()
    fun reloadPlayerFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.musicHostFragment, audioBrowserFragment)
            .commit()
        audioBrowserFragment.isMusicActivity(true)

        supportFragmentManager.beginTransaction()
            .replace(R.id.playerFragmentContainer, playerFragment)
            .commit()
    }

    private fun setMiniPlayerAlpha(slideOffset: Float) {
        playerFragment.setMiniPlayerAlpha(slideOffset)
    }
}