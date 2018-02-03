package io.videoapparat.sample

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View


internal class PermissionsDelegate(private val activity: Activity) {

    private val noPermissionView by lazy { activity.findViewById<View>(R.id.no_permission) }

    fun hasPermissions(): Boolean = desiredPermissions.all { activity.hasPermission(it) }

    fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
                activity,
                desiredPermissions,
                REQUEST_CODE
        )
    }

    fun resultGranted(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ): Boolean = when {
        requestCode != REQUEST_CODE -> false
        !permissions.contentEquals(desiredPermissions) -> false
        grantResults.isEmpty() -> false
        else -> {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                noPermissionView.visibility = View.GONE
                true
            } else {
                requestCameraPermission()
                noPermissionView.visibility = View.VISIBLE
                false
            }
        }
    }

}

private val desiredPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

private const val REQUEST_CODE = 10

private fun Context.hasPermission(permission: String) = ContextCompat.checkSelfPermission(
        this,
        permission
) == PackageManager.PERMISSION_GRANTED
