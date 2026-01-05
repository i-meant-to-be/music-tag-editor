package com.imeanttobe.tageditor.editor.impl

import android.content.Context
import android.net.Uri
import com.imeanttobe.tageditor.editor.BaseMusicTagEditor
import com.imeanttobe.tageditor.model.MusicMetadata

class Mp3TagEditor(override val supportedExtensions: Set<String> = setOf("mp3")) : BaseMusicTagEditor() {
    override suspend fun parseInternal(
        context: Context,
        uri: Uri
    ): MusicMetadata {

    }

    override suspend fun saveInternal(
        context: Context,
        uri: Uri,
        metadata: MusicMetadata
    ) {

    }
}