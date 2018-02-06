package io.videoapparat.sample.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File

internal fun Context.openPhotoActivity(file: File) {
    startMediaActivity(
            context = this,
            file = file,
            mimeType = "image/*"
    )
}

internal fun Context.openVideoActivity(file: File) {
    startMediaActivity(
            context = this,
            file = file,
            mimeType = "video/mp4"
    )
}

private fun startMediaActivity(context: Context, file: File, mimeType: String) {
    val uri = Uri.parse(file.absolutePath)

    Intent(Intent.ACTION_VIEW, uri)
            .setDataAndType(uri, mimeType)
            .let { intent ->
                context.startActivity(intent)
            }
}