package com.imeanttobe.tageditor.model

data class MusicMetadata(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val albumArtist: String = "",
    val year: String = "",
    val genre: String = "",
    val composer: String = "",
    val trackNumber: String = "",
    val discNumber: String = "",
    val artwork: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MusicMetadata

        if (title != other.title) return false
        if (artist != other.artist) return false
        if (album != other.album) return false
        if (albumArtist != other.albumArtist) return false
        if (year != other.year) return false
        if (genre != other.genre) return false
        if (composer != other.composer) return false
        if (trackNumber != other.trackNumber) return false
        if (discNumber != other.discNumber) return false
        if (artwork != null) {
            if (other.artwork == null) return false
            if (!artwork.contentEquals(other.artwork)) return false
        } else if (other.artwork != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + album.hashCode()
        result = 31 * result + albumArtist.hashCode()
        result = 31 * result + year.hashCode()
        result = 31 * result + genre.hashCode()
        result = 31 * result + composer.hashCode()
        result = 31 * result + trackNumber.hashCode()
        result = 31 * result + discNumber.hashCode()
        result = 31 * result + (artwork?.contentHashCode() ?: 0)
        return result
    }

}