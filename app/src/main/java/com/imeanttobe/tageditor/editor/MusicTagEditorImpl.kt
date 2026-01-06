package com.imeanttobe.tageditor.editor

import android.content.Context
import android.net.Uri
import android.util.Log
import com.imeanttobe.tageditor.model.MusicMetadata
import com.imeanttobe.tageditor.util.FileUtil
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import java.io.File
import java.io.FileNotFoundException
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class MusicTagEditorImpl @Inject constructor() : MusicTagEditor {
    init {
        Logger.getLogger("org.jaudiotagger").level = Level.OFF
    }

    override val supportedExtensions: Set<String>
        get() = setOf("mp3", "aiff", "flac", "ogg")

    /**
     * SAF 환경에서의 유효성 검사
     * 해당 Uri를 읽고 쓸 수 있는지(rw) 확인합니다.
     */
    private fun validateUri(context: Context, uri: Uri) {
        try {
            // "r" = 읽기 전용, "w" = 쓰기 전용, "rw" = 읽기/쓰기
            // "rw" 모드로 열 수 있다면 파일이 존재하고 권한도 있다는 뜻입니다.
            context.contentResolver.openFileDescriptor(uri, "rw")?.use {
                // 블록 내에서 아무것도 안 해도, 정상적으로 열렸다면 닫히면서 통과됨
            } ?: throw FileNotFoundException("Cannot open file descriptor for $uri")
        } catch (e: SecurityException) {
            // 권한이 없으면 예외 발생
            throw SecurityException("Cannot access uri: $uri. Check permissions or file existence.", e)
        }
    }

    override suspend fun read(context: Context, uri: Uri): Result<MusicMetadata> {
        return runCatching {
            // Uri 검증
            validateUri(context, uri)

            // 파일 읽기
            var tempFile: File? = null

            try {
                // 1. 앱 전용 디렉토리에 임시 파일로 복사
                tempFile = FileUtil.copyUriToTempFile(context, uri)

                // 2. 오디오 파일 읽기
                val audioFile = AudioFileIO.read(tempFile)
                val rawTag: Tag = audioFile.tag ?: audioFile.createDefaultTag()

                // 3. 태그 정보 추출 및 반환
                MusicMetadata(
                    title = rawTag.getFirst(FieldKey.TITLE),
                    artist = rawTag.getFirst(FieldKey.ARTIST),
                    album = rawTag.getFirst(FieldKey.ALBUM),
                    albumArtist = rawTag.getFirst(FieldKey.ALBUM_ARTIST),
                    year = rawTag.getFirst(FieldKey.YEAR),
                    genre = rawTag.getFirst(FieldKey.GENRE),
                    composer = rawTag.getFirst(FieldKey.COMPOSER),
                    trackNumber = rawTag.getFirst(FieldKey.TRACK),
                    discNumber = rawTag.getFirst(FieldKey.DISC_NO),
                    artwork = rawTag.firstArtwork?.binaryData
                )
            } finally {
                tempFile?.delete()
            }
        }.onFailure { e ->
            // Coroutine 취소 예외는 소비하지 말고 전파
            if (e is CancellationException) throw e
            
            // 오류 정보 전달
            e.printStackTrace()
            Log.e("MusicTagEditor", "Error reading music file: ${e.message}", e)
        }
    }

    override suspend fun write(context: Context, uri: Uri, metadata: MusicMetadata): Result<Unit> {
        return runCatching {
            // Uri 검증
            validateUri(context, uri)

            // 파일 읽기
            var tempFile: File? = null

            try {
                // 1. 앱 전용 디렉토리에 임시 파일로 복사
                tempFile = FileUtil.copyUriToTempFile(context, uri)

                // 2. 오디오 파일 읽기
                val audioFile = AudioFileIO.read(tempFile)
                var rawTag = audioFile.tag
                if (rawTag == null) {
                    rawTag = audioFile.createDefaultTag()
                    audioFile.tag = rawTag // [중요] 생성된 태그를 파일 객체에 연결
                }

                // 3. 태그 정보 준비
                metadata.applyToJAudioTaggerTag(rawTag)

                // 4. 변경 사항을 임시 파일에 저장
                AudioFileIO.write(audioFile)

                // 5. 임시 파일을 원본 Uri로 덮어쓰기
                FileUtil.copyTempFileToUri(context, tempFile, uri)

                // 6. 반환
                Unit
            } finally {
                tempFile?.delete()
            }
        }.onFailure { e ->
            // Coroutine 취소 예외는 소비하지 말고 전파
            if (e is CancellationException) throw e

            // 오류 정보 전달
            e.printStackTrace()
            Log.e("MusicTagEditor", "Error writing music file: ${e.message}", e)
        }
    }
}