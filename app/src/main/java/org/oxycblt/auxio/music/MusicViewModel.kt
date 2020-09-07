package org.oxycblt.auxio.music

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.oxycblt.auxio.R
import org.oxycblt.auxio.music.models.Album
import org.oxycblt.auxio.music.models.Artist
import org.oxycblt.auxio.music.models.Genre
import org.oxycblt.auxio.music.models.Song
import org.oxycblt.auxio.music.processing.MusicLoader
import org.oxycblt.auxio.music.processing.MusicLoaderResponse
import org.oxycblt.auxio.music.processing.MusicSorter

// Storage for music data.
class MusicViewModel(private val app: Application) : ViewModel() {

    // Coroutine
    private val loadingJob = Job()
    private val ioScope = CoroutineScope(
        loadingJob + Dispatchers.IO
    )

    // Values
    private val mGenres = MutableLiveData<List<Genre>>()
    val genres: LiveData<List<Genre>> get() = mGenres

    private val mArtists = MutableLiveData<List<Artist>>()
    val artists: LiveData<List<Artist>> get() = mArtists

    private val mAlbums = MutableLiveData<List<Album>>()
    val albums: LiveData<List<Album>> get() = mAlbums

    private val mSongs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> get() = mSongs

    private val mResponse = MutableLiveData<MusicLoaderResponse>()
    val response: LiveData<MusicLoaderResponse> get() = mResponse

    // UI control
    private val mRedo = MutableLiveData<Boolean>()
    val doReload: LiveData<Boolean> get() = mRedo

    private val mDoGrant = MutableLiveData<Boolean>()
    val doGrant: LiveData<Boolean> get() = mDoGrant

    private var started = false

    // Start the music loading sequence.
    // This should only be ran once, use redo() for all other loads.
    fun go() {
        if (!started) {
            started = true
            doLoad()
        }
    }

    private fun doLoad() {
        Log.i(this::class.simpleName, "Starting initial music load...")

        ioScope.launch {
            val start = System.currentTimeMillis()

            val loader = MusicLoader(app.contentResolver)

            withContext(Dispatchers.Main) {
                if (loader.response == MusicLoaderResponse.DONE) {
                    // If the loading succeeds, then process the songs and set them.
                    val sorter = MusicSorter(
                        loader.genres,
                        loader.artists,
                        loader.albums,
                        loader.songs,

                        app.getString(R.string.placeholder_unknown_genre),
                        app.getString(R.string.placeholder_unknown_artist),
                        app.getString(R.string.placeholder_unknown_album)
                    )

                    mSongs.value = sorter.songs.toList()
                    mAlbums.value = sorter.albums.toList()
                    mArtists.value = sorter.artists.toList()
                    mGenres.value = sorter.genres.toList()
                }

                mResponse.value = loader.response

                val elapsed = System.currentTimeMillis() - start

                Log.i(
                    this::class.simpleName,
                    "Music load completed successfully in ${elapsed}ms."
                )
            }
        }
    }

    // UI communication functions
    fun doneWithResponse() {
        mResponse.value = null
    }

    fun reload() {
        mRedo.value = true

        doLoad()
    }

    fun doneWithReload() {
        mRedo.value = false
    }

    fun grant() {
        mDoGrant.value = true
    }

    fun doneWithGrant() {
        mDoGrant.value = false
    }

    override fun onCleared() {
        super.onCleared()

        // Cancel the current loading job if the app has been stopped
        loadingJob.cancel()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
                return MusicViewModel(application) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class.")
        }
    }
}