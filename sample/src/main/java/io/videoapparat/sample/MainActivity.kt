package io.videoapparat.sample

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.SeekBar
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.configuration.UpdateConfiguration
import io.fotoapparat.log.logcat
import io.fotoapparat.parameter.Flash
import io.fotoapparat.result.RecordingResult
import io.fotoapparat.result.transformer.scaled
import io.fotoapparat.selector.*
import io.videoapparat.sample.extensions.*
import io.videoapparat.sample.view.CameraState.Movie.ReadyToRecord
import io.videoapparat.sample.view.CameraState.Movie.StopRecording
import io.videoapparat.sample.view.CameraState.Picture
import io.videoapparat.sample.view.CameraStatePresenter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    private val permissionsDelegate = PermissionsDelegate(this)

    private var permissionsGranted: Boolean = false
    private var activeCamera: Camera = Camera.Back

    private lateinit var fotoapparat: Fotoapparat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Fotoapparat.setLicence("PASTE YOUR LICENSE KEY")

        permissionsGranted = permissionsDelegate.hasPermissions()

        if (permissionsGranted) {
            cameraView.visibility = View.VISIBLE
        } else {
            permissionsDelegate.requestCameraPermission()
        }

        fotoapparat = Fotoapparat(
                context = this,
                view = cameraView,
                focusView = focusView,
                logger = logcat(),
                lensPosition = activeCamera.lensPosition,
                cameraConfiguration = activeCamera.configuration,
                cameraErrorCallback = { Log.e(LOGGING_TAG, "Camera error: ", it) }
        )

        CameraStatePresenter(
                capture = capture,
                changeMode = changeMode,
                onCameraActionListener = { state ->
                    when (state) {
                        Picture -> takePicture()
                        ReadyToRecord -> startRecording()
                        StopRecording -> stopRecording()
                    }
                }
        )

        zoomSeekBar onProgressChanged updateZoom()
        switchCamera onClick changeCamera()
        torchSwitch onCheckedChanged toggleFlash()
    }

    private fun updateZoom(): (SeekBar, Int) -> Unit = { seekBar: SeekBar, progress: Int ->
        fotoapparat.setZoom(progress / seekBar.max.toFloat())
    }

    private fun takePicture() {
        val photoFile = File(
                getExternalFilesDir("photos"),
                "photo.jpg"
        )

        val photoResult = fotoapparat
                .autoFocus()
                .takePicture()

        photoResult.saveToFile(photoFile)

        photoResult
                .toBitmap(scaled(scaleFactor = 0.25f))
                .whenAvailable { photo ->
                    photo
                            ?.let {
                                Log.i(LOGGING_TAG, "New photo captured. Bitmap length: ${it.bitmap.byteCount}")

                                result.setResult(
                                        bitmap = it.bitmap,
                                        rotation = (-it.rotationDegrees).toFloat(),
                                        onClick = {
                                            openPhotoActivity(photoFile)
                                        }
                                )
                            }
                            ?: Log.e(LOGGING_TAG, "Couldn't capture photo.")
                }
    }

    private fun startRecording() {
        fotoapparat.startRecording(file = File(
                getExternalFilesDir("videos"),
                "video.mp4"
        ))
    }

    private fun stopRecording() {
        fotoapparat.stopRecording()
                .whenAvailable { recordingResult ->
                    Log.e(LOGGING_TAG, "Stop recording result: $recordingResult")

                    if (recordingResult is RecordingResult.Success) {
                        val videoFile = recordingResult.video.file

                        videoFile.makeThumbnail {
                            result.setResult(
                                    bitmap = it,
                                    rotation = 0f,
                                    onClick = {
                                        openVideoActivity(videoFile)
                                    }
                            )
                        }
                    }
                }
    }


    private fun changeCamera(): () -> Unit = {
        activeCamera = when (activeCamera) {
            Camera.Front -> Camera.Back
            Camera.Back -> Camera.Front
        }

        fotoapparat.switchTo(
                lensPosition = activeCamera.lensPosition,
                cameraConfiguration = activeCamera.configuration
        )

        adjustViewsVisibility()

        zoomSeekBar.progress = 0
        torchSwitch.isChecked = false

        Log.i(LOGGING_TAG, "New camera position: ${if (activeCamera == Camera.Back) "back" else "front"}")
    }

    private fun toggleFlash(): (CompoundButton, Boolean) -> Unit = { _, isChecked ->
        fotoapparat.updateConfiguration(
                UpdateConfiguration(
                        flashMode = if (isChecked) {
                            firstAvailable(
                                    torch(),
                                    off()
                            )
                        } else {
                            off()
                        }
                )
        )

        Log.i(LOGGING_TAG, "Flash is now ${if (isChecked) "on" else "off"}")
    }

    override fun onStart() {
        super.onStart()
        if (permissionsGranted) {
            fotoapparat.start()
            adjustViewsVisibility()
        }
    }

    override fun onStop() {
        super.onStop()
        if (permissionsGranted) {
            fotoapparat.stop()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
            permissionsGranted = true
            fotoapparat.start()
            adjustViewsVisibility()
            cameraView.visibility = View.VISIBLE
        }
    }

    private fun adjustViewsVisibility() {
        fotoapparat.getCapabilities()
                .whenAvailable { capabilities ->
                    capabilities
                            ?.let {
                                zoomSeekBar.visibility = if (it.canZoom) View.VISIBLE else View.GONE
                                torchSwitch.visibility = if (it.flashModes.contains(Flash.Torch)) View.VISIBLE else View.GONE
                            }
                            ?: Log.e(LOGGING_TAG, "Couldn't obtain capabilities.")
                }

        switchCamera.visibility = if (fotoapparat.isAvailable(front())) View.VISIBLE else View.GONE
    }

}

private fun ImageView.setResult(bitmap: Bitmap, rotation: Float, onClick: () -> Unit) {
    setImageBitmap(bitmap)
    this.rotation = rotation

    onClick(onClick)
}

private const val LOGGING_TAG = "Fotoapparat Example"

private sealed class Camera(
        val lensPosition: LensPositionSelector,
        val configuration: CameraConfiguration
) {

    object Back : Camera(
            lensPosition = back(),
            configuration = CameraConfiguration(
                    previewResolution = firstAvailable(
                            wideRatio(highestResolution()),
                            standardRatio(highestResolution())
                    ),
                    previewFpsRange = highestFps(),
                    flashMode = off(),
                    focusMode = firstAvailable(
                            continuousFocusPicture(),
                            autoFocus(),
                            fixed()
                    ),
                    frameProcessor = {
                        // Do something with the preview frame
                    }
            )
    )

    object Front : Camera(
            lensPosition = front(),
            configuration = CameraConfiguration(
                    previewResolution = firstAvailable(
                            wideRatio(highestResolution()),
                            standardRatio(highestResolution())
                    ),
                    previewFpsRange = highestFps(),
                    flashMode = off(),
                    focusMode = firstAvailable(
                            fixed(),
                            autoFocus()
                    )
            )
    )
}
