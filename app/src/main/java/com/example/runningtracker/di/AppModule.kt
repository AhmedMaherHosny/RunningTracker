package com.example.runningtracker.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.navigation.NavOptions
import androidx.room.Room
import com.example.runningtracker.R
import com.example.runningtracker.db.RunningDatabase
import com.example.runningtracker.other.Constant.KEY_FIRST_TIME_TOGGLE
import com.example.runningtracker.other.Constant.KEY_NAME
import com.example.runningtracker.other.Constant.KEY_WEIGHT
import com.example.runningtracker.other.Constant.RUNNING_DATABASE_NAME
import com.example.runningtracker.other.Constant.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.FragmentScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    )
        .fallbackToDestructiveMigration()
        .build()

    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context): SharedPreferences =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, 0)

    /*in this fun there is an error occurs when the app lunches for the first time the name -> null*/
    @Singleton
    @Provides
    fun provideName(sharedPref: SharedPreferences) =
        sharedPref.getString(KEY_NAME, "No value") ?: ""

    @Singleton
    @Provides
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(KEY_WEIGHT, 65f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref: SharedPreferences) =
        sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, true)
}