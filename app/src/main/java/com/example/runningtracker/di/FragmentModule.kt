package com.example.runningtracker.di

import androidx.navigation.NavOptions
import com.example.runningtracker.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped

@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {

    @FragmentScoped
    @Provides
    fun provideNavOptions() = NavOptions.Builder().setPopUpTo(R.id.setupFragment, true).build()

}