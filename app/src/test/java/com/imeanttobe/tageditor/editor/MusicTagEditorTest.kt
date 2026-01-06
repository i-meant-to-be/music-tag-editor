package com.imeanttobe.tageditor.editor

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.imeanttobe.tageditor.model.MusicMetadata
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class MusicTagEditorTest {
    private lateinit var editor: MusicTagEditorImpl
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        editor = MusicTagEditorImpl()
    }

    @Test
    fun `MP3 파일 읽기 및 쓰기 테스트`() = runBlocking {
        verifyFileReadAndWrite(fileName = "sample.mp3")
    }

    @Test
    fun `FLAC 파일 읽기 및 쓰기 테스트`() = runBlocking {
        verifyFileReadAndWrite(fileName = "sample.flac")
    }

    @Test
    fun `OGG 파일 읽기 및 쓰기 테스트`() = runBlocking {
        verifyFileReadAndWrite(fileName = "sample.ogg")
    }

    @Test
    fun `AIFF 파일 읽기 및 쓰기 테스트`() = runBlocking {
        verifyFileReadAndWrite(fileName = "sample.aiff")
    }

    private suspend fun verifyFileReadAndWrite(fileName: String) {
        // 1. 리소스 파일 준비
        val testFile = copyResourceToCache(fileName)
        val testUri = Uri.fromFile(testFile)

        try {
            // 2. 초기 읽기 시도
            val initialResult = editor.read(context, testUri)
            assertTrue("초기 읽기 실패 ($fileName)", initialResult.isSuccess)

            // 3. 기존 메타데이터가 잘 읽혔는지 확인
            val initialMetadata = initialResult.getOrNull()
            assertNotNull(initialMetadata)

            assertEquals(initialMetadata?.title, "Sample Title")
            assertEquals(initialMetadata?.artist, "Sample Artist")
            assertEquals(initialMetadata?.album, "Sample Album")
            assertEquals(initialMetadata?.albumArtist, "Sample Album Artist")
            assertEquals(initialMetadata?.year, "2000")
            assertEquals(initialMetadata?.genre, "Sample Genre")

            // 4. 수정할 데이터 준비 (랜덤성을 위해 현재 시간 활용 가능)
            val uniqueTitle = "Test Title - $fileName"
            val uniqueArtist = "Test Artist"

            val newMetadata = MusicMetadata(
                title = uniqueTitle,
                artist = uniqueArtist,
                album = "Test Album",
                year = "2024",
                genre = "Test Genre",
                albumArtist = "",
                composer = "",
                trackNumber = "",
                discNumber = "",
                artwork = null
            )

            // 5. 쓰기 시도
            val writeResult = editor.write(context, testUri, newMetadata)
            assertTrue("쓰기 실패 ($fileName)", writeResult.isSuccess)

            // 6. 다시 읽어서 변경된 값 확인
            val reloadResult = editor.read(context, testUri)
            assertTrue("재읽기 실패 ($fileName)", reloadResult.isSuccess)

            val loadedMetadata = reloadResult.getOrNull()
            assertNotNull(loadedMetadata)
            assertEquals("Title이 변경되지 않음 ($fileName)", uniqueTitle, loadedMetadata?.title)
            assertEquals("Artist가 변경되지 않음 ($fileName)", uniqueArtist, loadedMetadata?.artist)

            println("PASS: $fileName 테스트 성공")

        } finally {
            // 7. 테스트 종료 후 임시 파일 삭제 (테스트가 실패해도 실행됨)
            if (testFile.exists()) {
                testFile.delete()
            }
        }
    }

    /**
     * resources 폴더의 파일을 앱 캐시 디렉토리로 복사하는 헬퍼 함수
     */
    private fun copyResourceToCache(fileName: String): File {
        val resourceStream = javaClass.classLoader?.getResourceAsStream(fileName)
            ?: throw IllegalArgumentException("Test resource not found: $fileName. src/test/resources에 파일이 있는지 확인하세요.")

        val tempFile = File(context.cacheDir, "temp_$fileName")

        FileOutputStream(tempFile).use { output ->
            resourceStream.copyTo(output)
        }
        return tempFile
    }
}