package com.imeanttobe.tageditor.editor

import android.content.Context
import android.net.Uri
import com.imeanttobe.tageditor.model.MusicMetadata

interface MusicTagEditor {
    val supportedExtensions: Set<String>

    suspend fun read(context: Context, uri: Uri): Result<MusicMetadata>
    suspend fun write(context: Context, uri: Uri, metadata: MusicMetadata): Result<Unit>
}