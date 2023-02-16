package com.example.runningtracker.other

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.runningtracker.other.Constant.LOCATION_PERMISSIONS
import com.example.runningtracker.services.Polyline
import java.util.concurrent.TimeUnit


fun hasLocationPermissions(context: Context): Boolean {
    return LOCATION_PERMISSIONS.all { permission ->
        ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun hasBackGroundLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun createDialog(
    context: Context,
    title: String,
    msg: String,
    namePositiveBtn: String,
    actionPositiveBtn: DialogInterface.OnClickListener,
    nameNegativeBtn: String,
    actionNegativeBtn: DialogInterface.OnClickListener
) {
    AlertDialog.Builder(context).apply {
        setTitle(title)
        setMessage(msg)
        setPositiveButton(namePositiveBtn, actionPositiveBtn)
        setNegativeButton(nameNegativeBtn, actionNegativeBtn)
        setCancelable(false)
    }.show()
}

fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
    var milliseconds = ms
    val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
    milliseconds -= TimeUnit.HOURS.toMillis(hours)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
    milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
    if (!includeMillis) {
        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds"
    }
    milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
    milliseconds /= 10
    return "${if (hours < 10) "0" else ""}$hours:" +
            "${if (minutes < 10) "0" else ""}$minutes:" +
            "${if (seconds < 10) "0" else ""}$seconds:" +
            "${if (milliseconds < 10) "0" else ""}$milliseconds"
}

fun calculatePolylineLength(polyline: Polyline): Float {
    var distance = 0f
    for (i in 0..polyline.size - 2) {
        val pos1 = polyline[i]
        val pos2 = polyline[i + 1]
        val result = FloatArray(1)
        Location.distanceBetween(
            pos1.latitude,
            pos1.longitude,
            pos2.latitude,
            pos2.longitude,
            result
        )
        distance += result[0]
    }
    return distance
}
