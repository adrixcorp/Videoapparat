package io.videoapparat.sample.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import io.videoapparat.sample.R

class CameraModeChangeView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    var state: CameraState = CameraState.Picture
        set(value) {
            when (value) {
                CameraState.Picture -> {
                    visibility = View.VISIBLE
                    setImageResource(R.drawable.ic_videocam_white)
                }
                CameraState.Movie.ReadyToRecord -> {
                    visibility = View.VISIBLE
                    setImageResource(R.drawable.ic_camera_white)
                }
                else -> visibility = View.INVISIBLE
            }

            field = value
        }

}