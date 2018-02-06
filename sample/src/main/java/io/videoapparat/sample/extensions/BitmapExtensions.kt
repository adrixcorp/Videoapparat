package io.videoapparat.sample.extensions

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.provider.MediaStore
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import java.io.File

internal fun File.makeThumbnail(function: (Bitmap) -> Unit) = launch {
    val thumbnail = ThumbnailUtils.createVideoThumbnail(
            absolutePath,
            MediaStore.Images.Thumbnails.MINI_KIND
    )

    withContext(UI) {
        thumbnail?.let(function)
    }
}