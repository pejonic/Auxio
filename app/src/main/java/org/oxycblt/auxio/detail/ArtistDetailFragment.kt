package org.oxycblt.auxio.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.oxycblt.auxio.detail.adapters.ArtistDetailAdapter
import org.oxycblt.auxio.logD
import org.oxycblt.auxio.music.Album
import org.oxycblt.auxio.music.Artist
import org.oxycblt.auxio.music.BaseModel
import org.oxycblt.auxio.music.Genre
import org.oxycblt.auxio.music.MusicStore
import org.oxycblt.auxio.music.Song
import org.oxycblt.auxio.ui.ActionMenu
import org.oxycblt.auxio.ui.newMenu

/**
 * The [DetailFragment] for an artist.
 * TODO: Show a list of songs?
 * @author OxygenCobalt
 */
class ArtistDetailFragment : DetailFragment() {
    private val args: ArtistDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // If DetailViewModel isn't already storing the artist, get it from MusicStore
        // using the ID given by the navigation arguments
        if (detailModel.currentArtist.value == null ||
            detailModel.currentArtist.value?.id != args.artistId
        ) {
            detailModel.updateArtist(
                MusicStore.getInstance().artists.find {
                    it.id == args.artistId
                }!!
            )
        }

        val detailAdapter = ArtistDetailAdapter(
            detailModel, playbackModel, viewLifecycleOwner,
            doOnClick = {
                if (!detailModel.isNavigating) {
                    detailModel.updateNavigationStatus(true)

                    findNavController().navigate(
                        ArtistDetailFragmentDirections.actionShowAlbum(it.id)
                    )
                }
            },
            doOnLongClick = { view, data -> newMenu(view, data, ActionMenu.FLAG_IN_ARTIST) }
        )

        // --- UI SETUP ---

        binding.lifecycleOwner = this

        setupToolbar()
        setupRecycler(detailAdapter)

        // --- VIEWMODEL SETUP ---

        detailModel.artistSortMode.observe(viewLifecycleOwner) { mode ->
            logD("Updating sort mode to $mode")

            // Header detail data is always included
            val data = mutableListOf<BaseModel>(detailModel.currentArtist.value!!).also {
                it.addAll(mode.getSortedAlbumList(detailModel.currentArtist.value!!.albums))
            }

            detailAdapter.submitList(data)
        }

        detailModel.navToItem.observe(viewLifecycleOwner) {
            if (it != null) {
                // If the artist matches, no need to do anything, otherwise launch a new detail
                if (it is Artist) {
                    if (it.id == detailModel.currentArtist.value!!.id) {
                        binding.detailRecycler.scrollToPosition(0)
                        detailModel.doneWithNavToItem()
                    } else {
                        findNavController().navigate(
                            ArtistDetailFragmentDirections.actionShowArtist(it.id)
                        )
                    }
                } else if (it !is Genre) {
                    // Determine the album id of the song or album, and then launch it otherwise
                    val albumId = if (it is Song) it.album.id else it.id

                    findNavController().navigate(
                        ArtistDetailFragmentDirections.actionShowAlbum(albumId)
                    )
                }
            }
        }

        // Highlight albums if they are being played
        playbackModel.parent.observe(viewLifecycleOwner) { parent ->
            if (parent is Album?) {
                detailAdapter.setCurrentAlbum(parent, binding.detailRecycler)
            } else {
                detailAdapter.setCurrentAlbum(null, binding.detailRecycler)
            }
        }

        logD("Fragment created.")

        return binding.root
    }
}
