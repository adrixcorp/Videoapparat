package io.videoapparat.sample.view

import io.videoapparat.sample.view.CameraState.Movie.ReadyToRecord
import io.videoapparat.sample.view.CameraState.Movie.StopRecording
import io.videoapparat.sample.view.CameraState.Picture

class CameraStatePresenter(
        private val capture: CaptureButton,
        private val changeMode: CameraModeChangeView,
        private val onCameraActionListener: (CameraState) -> Unit
) {

    private var state: CameraState = Picture
        set(value) {
            capture.state = value
            changeMode.state = value
            field = value
        }

    init {
        state = Picture
        capture.setOnClickListener {
            onCameraActionListener(state)
            state = newStateFromAction()
        }

        changeMode.setOnClickListener {
            state = newStateFromModeChange()
        }
    }

    private fun newStateFromModeChange(): CameraState {
        return when (state) {
            Picture -> ReadyToRecord
            ReadyToRecord -> Picture
            StopRecording -> throw IllegalStateException("Cannot change mode while recording.")
        }
    }

    private fun newStateFromAction(): CameraState {
        return when (state) {
            Picture -> Picture
            ReadyToRecord -> StopRecording
            StopRecording -> ReadyToRecord
        }
    }

}