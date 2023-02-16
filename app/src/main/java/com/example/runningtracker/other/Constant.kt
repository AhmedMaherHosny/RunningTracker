package com.example.runningtracker.other

import android.Manifest
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.model.LatLng

object Constant {

    const val RUNNING_DATABASE_NAME = "running_db"
    val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"
    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L
    const val POLYLINE_COLOR = Color.RED
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 15f
    const val TIMER_UPDATE_INTERVAL = 50L
    const val SHARED_PREFERENCES_NAME = "sharedPref"
    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_NAME = "KEY_NAME"
    const val KEY_WEIGHT = "KEY_WEIGHT"
    const val CANCEL_TRACKING_DIALOG_TAG = "CANCEL_TRACKING_DIALOG_TAG"
    const val MENU_KEY = "MENU_KEY"
}