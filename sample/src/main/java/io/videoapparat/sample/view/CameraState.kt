package io.videoapparat.sample.view

sealed class CameraState {
    object Picture : CameraState()

    sealed class Movie : CameraState() {

        object ReadyToRecord : Movie()
        object StopRecording : Movie()
    }
}