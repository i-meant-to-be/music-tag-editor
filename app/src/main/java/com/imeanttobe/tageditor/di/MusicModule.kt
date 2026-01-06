package com.imeanttobe.tageditor.di

import com.imeanttobe.tageditor.editor.MusicTagEditor
import com.imeanttobe.tageditor.editor.MusicTagEditorImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MusicModule {
    @Provides
    @Singleton
    fun provideMusicTagEditor(): MusicTagEditor = MusicTagEditorImpl()
}