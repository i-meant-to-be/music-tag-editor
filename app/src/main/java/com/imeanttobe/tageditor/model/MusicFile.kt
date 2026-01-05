package com.imeanttobe.tageditor.model

import android.net.Uri

data class MusicFile (
    val uri: Uri,
    val fileName: String,
    val fileExtension: String,
    val mimeType: String,
    val size: Long,
    val metadata: MusicMetadata
)