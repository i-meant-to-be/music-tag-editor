package com.imeanttobe.tageditor.model

import android.util.Log
import com.imeanttobe.tageditor.util.FileUtil
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import org.jaudiotagger.tag.images.StandardArtwork

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
    val lyrics: String = "",
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
        if (lyrics != other.lyrics) return false
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
        result = 31 * result + lyrics.hashCode()
        result = 31 * result + (artwork?.contentHashCode() ?: 0)
        return result
    }

    fun applyToJAudioTaggerTag(tag: Tag) {
        // Apply string fields
        setTagField(tag, FieldKey.TITLE, this.title)
        setTagField(tag, FieldKey.ARTIST, this.artist)
        setTagField(tag, FieldKey.ALBUM, this.album)
        setTagField(tag, FieldKey.ALBUM_ARTIST, this.albumArtist)
        setTagField(tag, FieldKey.YEAR, this.year)
        setTagField(tag, FieldKey.GENRE, this.genre)
        setTagField(tag, FieldKey.COMPOSER, this.composer)
        setTagField(tag, FieldKey.TRACK, this.trackNumber)
        setTagField(tag, FieldKey.DISC_NO, this.discNumber)
        setTagField(tag, FieldKey.LYRICS, this.lyrics)

        // Apply artwork
        if (tag.firstArtwork == null && this.artwork != null) {
            try {
                val newArtwork = StandardArtwork()
                newArtwork.binaryData = this.artwork
                newArtwork.mimeType = detectMimeType(this.artwork)
                tag.deleteArtworkField()
                tag.setField(newArtwork)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MusicTagEditor", "Error setting artwork: ${e.message}")
            }
        }
    }

    // 헬퍼 함수: 태그 필드가 비어 있지 않아야만 설정하는 함수
    private fun setTagField(tag: Tag, key: FieldKey, value: String) {
        if (value.isNotEmpty()) {
            tag.setField(key, value)
        } else {
            tag.deleteField(key)
        }
    }


    // 헬퍼 함수: 바이트 배열 헤더로 이미지 MIME 타입 추론
    private fun detectMimeType(data: ByteArray): String {
        if (data.size < 4) return "image/jpeg" // 기본값

        // JPEG 매직 넘버: FF D8 FF
        if (data[0] == 0xFF.toByte() && data[1] == 0xD8.toByte() && data[2] == 0xFF.toByte()) {
            return "image/jpeg"
        }
        // PNG 매직 넘버: 89 50 4E 47
        if (data[0] == 0x89.toByte() && data[1] == 0x50.toByte() && data[2] == 0x4E.toByte()) {
            return "image/png"
        }

        return "image/jpeg" // 식별 불가 시 가장 흔한 jpg로 가정
    }
}