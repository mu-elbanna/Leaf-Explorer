package com.leaf.music.ui.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.leaf.music.helper.MusicLibrary
import com.leaf.music.helper.MusicLibraryHelper
import org.monora.uprotocol.client.android.content.Song
import com.leaf.music.services.PlayerService
import com.leaf.music.util.*
import com.leaf.music.util.Constants.PREF_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.monora.uprotocol.client.android.GlideApp
import org.monora.uprotocol.client.android.R

class PlayerFragment : Fragment(R.layout.fragment_player), View.OnClickListener, SongChangeNotifier,
    PlayPauseStateNotifier, SeekCompletionNotifier {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var imgThumbnail: ImageView
    private lateinit var fabPlayPause: FloatingActionButton
    private lateinit var fabPlayPause2: ImageView
    private lateinit var txtSongTitle: TextView
    private lateinit var txtArtistName: TextView
    private lateinit var txtEndDuration: TextView
    private lateinit var txtStartDuration: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var miniPlayer: LinearLayout

    private lateinit var albumArt: ImageView
    private lateinit var songName: TextView
    private lateinit var songDetails: TextView
    private lateinit var progressIndicator: LinearProgressIndicator

    private var musicUri: Uri? = null
    private var songPlay: MutableList<Song>? = null

    private val playerService: PlayerService?
        get() = MusicPlayerRemote.playerService
    private val currentSong: Song?
        get() = PlayerHelper.getCurrentSong(requireContext(), sharedPreferences)

    private fun loadIntentSongs(path: Song) {
        songPlay = ArrayList()
        songPlay!!.add(Song(path.id, path.artist, path.album,
            path.folder, path.title, path.displayName, path.mimeType,
            path.size, path.dateModified, path.duration, path.uri, path.albumUri))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (playerService != null) {
            playerService!!.setSongChangeCallback(this)
            playerService!!.setPlayPauseStateCallback(this)
            playerService!!.setSeekCompleteNotifierCallback(this)
        }

        val mIntent = requireActivity().intent
        musicUri = mIntent.data
        if (musicUri != null) {
            try {
                handleIntent(mIntent)
            } catch (ignored: Exception) {
                Toast.makeText(context, R.string.unknown_failure, Toast.LENGTH_SHORT).show()
            }
        }

        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtSongTitle = view.findViewById(R.id.txtSongTitle)
        txtArtistName = view.findViewById(R.id.txtArtistName)
        fabPlayPause = view.findViewById(R.id.fabPlayPause)
        fabPlayPause2 = view.findViewById(R.id.control_play_pause)
        val btnNext = view.findViewById<ImageView>(R.id.btnNext)
        val btnPrevious = view.findViewById<ImageView>(R.id.btnPrevious)
        val btnPlayList = view.findViewById<ImageView>(R.id.btnPlayList)
        val btnShuffle = view.findViewById<ImageView>(R.id.btnShuffle)
        seekBar = view.findViewById(R.id.seekBar)
        txtStartDuration = view.findViewById(R.id.txtStartDuration)
        txtEndDuration = view.findViewById(R.id.txtEndDuration)
        val rlPlayer = view.findViewById<ConstraintLayout>(R.id.rlPlayer)
        val txtEmptySong = view.findViewById<MaterialButton>(R.id.txtEmptySong)
        imgThumbnail = view.findViewById(R.id.imgThumbnail)
        miniPlayer = view.findViewById(R.id.miniPlayer)

        albumArt = view.findViewById(R.id.albumArt)
        progressIndicator = view.findViewById(R.id.song_progress)
        songName = view.findViewById(R.id.song_title)
        songDetails = view.findViewById(R.id.song_details)

        txtEmptySong.setOnClickListener {
            activity?.recreate()
        }

        if (SharedPreferenceUtil.getCurrentSong(requireContext(), sharedPreferences) != null || musicUri != null) {
            txtSongTitle.isSelected = true
            txtArtistName.isSelected = true
            updateUi()

            fabPlayPause.setOnClickListener(this)
            fabPlayPause2.setOnClickListener(this)
            btnNext.setOnClickListener(this)
            btnPrevious.setOnClickListener(this)
            btnPlayList.setOnClickListener(this)
            btnShuffle.setOnClickListener(this)

            setUpPlayPauseButton()

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        MusicPlayerRemote.seekTo(progress)
                        txtStartDuration.text = millisToString(progress)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) = Unit
                override fun onStopTrackingTouch(p0: SeekBar?) = Unit
            })
        } else {
            rlPlayer.visibility = View.GONE
            txtEmptySong.visibility = View.VISIBLE
            fabPlayPause2.visibility = View.GONE
        }

        if (songPlay != null) {
            MusicPlayerRemote.sendAllSong(songPlay!!, 0)
        }
    }

    //method to handle intent to play audio file from external app
    private fun handleIntent(intent: Intent) {

        val mDeviceMusicList = MusicLibraryHelper.fetchMusicLibrary(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)

        intent.data?.let { returnUri ->
            requireContext().contentResolver.query(returnUri, null, null, null, null)
        }?.use { cursor ->
            try {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                val song = MusicLibrary.getSongFromIntent(cursor.getString(displayNameIndex), mDeviceMusicList)

                if (song != null) {
                    loadIntentSongs(song)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (activity != null) {
            updateUi()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fabPlayPause -> {
                MusicPlayerRemote.playPause()
            }
            R.id.control_play_pause -> {
                MusicPlayerRemote.playPause()
            }
            R.id.btnNext -> {
                MusicPlayerRemote.playNextSong()
            }
            R.id.btnPrevious -> {
                MusicPlayerRemote.playPreviousSong()
            }
            R.id.btnShuffle -> {
                Toast.makeText(requireContext(), "Coming Soon", Toast.LENGTH_SHORT).show()
            }
            R.id.btnPlayList -> {
                Toast.makeText(requireContext(), "Coming Soon", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCurrentSongChange() {
        if (activity != null) {
            updateUi()
            setUpSeekBar()
            setUpPlayPauseButton()
        }
        if (playerService != null)
            playerService!!.restartNotification()
    }

    override fun onPlayPauseStateChange() {
        if (activity != null) {
            setUpSeekBar()
            setUpPlayPauseButton()
        }
        if (playerService != null) {
            playerService!!.setMediaSessionAction()
            playerService!!.restartNotification()
        }
    }

    override fun onSeekComplete() {
        if (activity != null)
            setUpSeekBar()
    }

    private fun updateUi() {
        if (currentSong != null) {
            GlideApp.with(requireContext())
                .load(currentSong!!.albumUri)
                .placeholder(R.drawable.album_art)
                .into(imgThumbnail)

            GlideApp.with(requireContext())
                .load(currentSong!!.albumUri)
                .placeholder(R.drawable.album_art)
                .centerCrop()
                .into(albumArt)
        }

        setUpSeekBar()

        if (currentSong != null) {
            txtSongTitle.text = currentSong!!.title
            songName.text = currentSong!!.title
            setUpSeekBar()
            txtArtistName.text = currentSong!!.artist
            songDetails.text = currentSong!!.artist
        }
    }

    private fun setUpSeekBar() = lifecycleScope.launch(Dispatchers.Main) {
        txtEndDuration.text = millisToString(MusicPlayerRemote.songDurationMillis)
        txtStartDuration.text = millisToString(MusicPlayerRemote.currentSongPositionMillis)
        seekBar.max = MusicPlayerRemote.songDurationMillis
        progressIndicator.max = MusicPlayerRemote.songDurationMillis
        if (playerService?.mediaPlayer != null) {
            try {
                seekBar.progress = MusicPlayerRemote.currentSongPositionMillis
                progressIndicator.progress = MusicPlayerRemote.currentSongPositionMillis
                while (playerService?.mediaPlayer!!.isPlaying) {
                    txtStartDuration.text =
                        millisToString(MusicPlayerRemote.currentSongPositionMillis)
                    seekBar.progress = MusicPlayerRemote.currentSongPositionMillis
                    progressIndicator.progress = MusicPlayerRemote.currentSongPositionMillis
                    delay(100)
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }
    }

    private fun millisToString(duration: Int): String {
        val minutes = duration / 1000 / 60
        val seconds = duration / 1000 % 60

        var timeString = "$minutes:"
        if (minutes < 10) {
            timeString = "0$minutes:"
        }
        if (seconds < 10) timeString += "0"
        timeString += seconds

        return timeString
    }

    private fun setUpPlayPauseButton() {
        if (playerService != null &&
            playerService!!.mediaPlayer != null &&
            playerService!!.isPlaying()
        ) {
            fabPlayPause.setImageResource(R.drawable.ic_pause_white_24dp)
            fabPlayPause2.setImageResource(R.drawable.ic_pause_white_24dp)
        } else {
            fabPlayPause.setImageResource(R.drawable.ic_play_arrow_white_24dp)
            fabPlayPause2.setImageResource(R.drawable.ic_play_arrow_white_24dp)
        }
    }

    fun setMiniPlayerAlpha(slideOffset: Float) {
        val alpha = 1 - slideOffset
        miniPlayer.alpha = alpha
        miniPlayer.visibility = if (alpha == 0f) View.GONE else View.VISIBLE
    }
}