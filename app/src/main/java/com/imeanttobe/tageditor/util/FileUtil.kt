package com.imeanttobe.tageditor.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtil {
    // 임시 폴더명 상수
    private const val TEMP_DIR_NAME = "saf_temp"

    /**
     * [1] 파일 이름 가져오기
     * SAF Uri에서 표시되는 파일 이름(Display Name)을 추출합니다.
     */
    fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = it.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path?.substringAfterLast('/')
        }
        return result ?: "unknown_file"
    }

    /**
     * [2] 파일 확장자 가져오기
     * 파일 이름에서 확장자를 추출합니다. (소문자 변환)
     */
    fun getFileExtension(context: Context, uri: Uri): String {
        val fileName = getFileName(context, uri)
        return fileName.substringAfterLast('.', "").lowercase()
    }

    /**
     * [3] MIME 타입 가져오기
     * 예: "audio/mpeg"
     */
    fun getMimeType(context: Context, uri: Uri): String {
        return context.contentResolver.getType(uri) ?: "application/octet-stream"
    }

    /**
     * [4] Uri -> 임시 파일(File) 복사
     * 태그 라이브러리가 'File' 객체를 요구할 때 사용합니다.
     * 앱의 캐시 디렉토리에 원본과 동일한 확장자를 가진 임시 파일을 생성하고 내용을 복사합니다.
     * 단, 파일 삭제는 이 함수를 실행하는 측에서 진행하거나, [6]번 함수로 해야 합니다.
     */
    @Throws(IOException::class)
    suspend fun copyUriToTempFile(context: Context, uri: Uri): File = withContext(Dispatchers.IO) {
        // 임시 폴더 생성
        val tempDir = File(context.cacheDir, TEMP_DIR_NAME)
        if (!tempDir.exists()) tempDir.mkdirs()

        val extension = getFileExtension(context, uri)
        // 확장자가 없으면 tmp, 있으면 해당 확장자 사용 (라이브러리 인식용)
        val suffix = if (extension.isNotEmpty()) ".$extension" else ".tmp"

        // 캐시 디렉토리에 빈 파일 생성
        val tempFile = File.createTempFile("tag_editor_", suffix, tempDir)

        // Stream을 열어 데이터 복사
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IOException("Cannot open input stream for uri: $uri")

        tempFile
    }

    /**
     * [5] 임시 파일(File) -> 원본 Uri 덮어쓰기
     * 태그 라이브러리가 수정한 임시 파일을 다시 원본 위치(SAF Uri)에 저장합니다.
     */
    @Throws(IOException::class)
    suspend fun copyTempFileToUri(context: Context, tempFile: File, uri: Uri) = withContext(Dispatchers.IO) {
        // "wt" 모드: Write + Truncate (기존 내용을 지우고 처음부터 씀)
        context.contentResolver.openOutputStream(uri, "w")?.use { outputStream ->
            tempFile.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IOException("Cannot open output stream for uri: $uri")
    }

    /**
     * [6] 임시 파일 제거
     * 혹시 앱이 비정상적으로 종료되어 임시 파일이 남았을 경우를 대비,
     * 모든 임시 파일과 캐시 디렉토리 등을 삭제하는 함수입니다.
     * 성능 향상을 위해 입출력 디스패처에서 비동기적으로 실행합니다.
     */
    suspend fun clearOldTempFiles(context: Context) = withContext(Dispatchers.IO) {
        try {
            val tempDir = File(context.cacheDir, TEMP_DIR_NAME)

            // 폴더가 존재하고 디렉토리가 맞다면 내부 파일 삭제
            if (tempDir.exists() && tempDir.isDirectory) {
                tempDir.listFiles()?.forEach { file ->
                    file.delete()
                }
            }
        } catch (e: SecurityException) {
            Log.e("FileUtil", "Security exception while clearing temp files.", e)
        } catch (e: Exception) {
            Log.e("FileUtil", "Error clearing old temp files.", e)
        }
    }
}
