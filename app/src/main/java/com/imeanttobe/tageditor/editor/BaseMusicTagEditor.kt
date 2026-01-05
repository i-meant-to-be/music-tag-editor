package com.imeanttobe.tageditor.editor

import android.content.Context
import android.net.Uri
import com.imeanttobe.tageditor.model.MusicMetadata
import java.io.FileNotFoundException

abstract class BaseMusicTagEditor : MusicTagEditor {
    /**
     * SAF 환경에서의 유효성 검사
     * 해당 Uri를 읽고 쓸 수 있는지(rw) 확인합니다.
     */
    protected fun validateUri(context: Context, uri: Uri) {
        try {
            // "r" = 읽기 전용, "w" = 쓰기 전용, "rw" = 읽기/쓰기
            // "rw" 모드로 열 수 있다면 파일이 존재하고 권한도 있다는 뜻입니다.
            context.contentResolver.openFileDescriptor(uri, "rw")?.use {
                // 블록 내에서 아무것도 안 해도, 정상적으로 열렸다면 닫히면서 통과됨
            } ?: throw FileNotFoundException("Cannot open file descriptor for $uri")
        } catch (e: Exception) {
            // 권한이 없거나 파일이 없으면 예외 발생
            throw SecurityException("Cannot access uri: $uri. Check permissions or file existence.", e)
        }
    }

    // 실제 구현은 자식 클래스에게 위임 (매개변수도 Context, Uri로 통일)
    abstract suspend fun parseInternal(context: Context, uri: Uri): MusicMetadata
    abstract suspend fun saveInternal(context: Context, uri: Uri, metadata: MusicMetadata)

    // 인터페이스 구현 (템플릿 메서드 패턴)
    override suspend fun read(context: Context, uri: Uri): Result<MusicMetadata> = runCatching {
        validateUri(context, uri) // File 대신 Uri 검증
        parseInternal(context, uri) // 자식 클래스 호출 시에도 context, uri 전달
    }

    override suspend fun write(context: Context, uri: Uri, metadata: MusicMetadata): Result<Unit> = runCatching {
        validateUri(context, uri)
        saveInternal(context, uri, metadata)
    }
}