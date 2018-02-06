package io.videoapparat.sample.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import io.videoapparat.sample.R
import io.videoapparat.sample.view.CameraState.Movie.ReadyToRecord
import io.videoapparat.sample.view.CameraState.Movie.StopRecording
import io.videoapparat.sample.view.CameraState.Picture

class CaptureButton @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    var state: CameraState = Picture
        set(value) {
            setImageResource(when (value) {
                Picture -> R.drawable.capture
                ReadyToRecord -> R.drawable.record
                StopRecording -> R.drawable.stop_recording_inactive
            })

            field = value
        }

}