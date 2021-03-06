package org.oxycblt.auxio.database

/**
 * A database entity that stores a simplified representation of a song in a queue.
 * @property id The database entity's id
 * @property songName The song name for this queue item
 * @property albumName The album name for this queue item, used to make searching quicker
 * @property isUserQueue A bool for if this queue item is a user queue item or not
 * @author OxygenCobalt
 */
data class QueueItem(
    var id: Long = 0L,
    val songName: String = "",
    val albumName: String = "",
    val isUserQueue: Boolean = false
) {
    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_SONG_NAME = "song_name"
        const val COLUMN_ALBUM_NAME = "album_name"
        const val COLUMN_IS_USER_QUEUE = "is_user_queue"
    }
}
