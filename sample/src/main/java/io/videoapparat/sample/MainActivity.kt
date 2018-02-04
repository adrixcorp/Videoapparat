package io.videoapparat.sample

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.configuration.UpdateConfiguration
import io.fotoapparat.log.logcat
import io.fotoapparat.parameter.Flash
import io.fotoapparat.result.RecordingResult
import io.fotoapparat.result.Video
import io.fotoapparat.result.transformer.scaled
import io.fotoapparat.selector.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import java.io.File


class MainActivity : AppCompatActivity() {

    private val permissionsDelegate = PermissionsDelegate(this)

    private var permissionsGranted: Boolean = false
    private var activeCamera: Camera = Camera.Back

    private lateinit var fotoapparat: Fotoapparat
    private var recording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Fotoapparat.setLicence("PASTE YOUR LICENSE HERE")

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

        capture onClick takePicture()
        record onClick record()
        zoomSeekBar onProgressChanged updateZoom()
        switchCamera onClick changeCamera()
        torchSwitch onCheckedChanged toggleFlash()
    }

    private fun updateZoom(): (SeekBar, Int) -> Unit = { seekBar: SeekBar, progress: Int ->
        fotoapparat.setZoom(progress / seekBar.max.toFloat())
    }

    private fun takePicture(): () -> Unit = {
        val photoResult = fotoapparat
                .autoFocus()
                .takePicture()

        photoResult
                .saveToFile(File(
                        getExternalFilesDir("photos"),
                        "photo.jpg"
                ))

        photoResult
                .toBitmap(scaled(scaleFactor = 0.25f))
                .whenAvailable { photo ->
                    photo
                            ?.let {
                                Log.i(LOGGING_TAG, "New photo captured. Bitmap length: ${it.bitmap.byteCount}")
                                with(result) {
                                    setImageBitmap(it.bitmap)
                                    rotation = (-it.rotationDegrees).toFloat()
                                }
                            }
                            ?: Log.e(LOGGING_TAG, "Couldn't capture photo.")
                }
    }


    private fun record(): () -> Unit = {
        when {
            recording -> stopRecording()
            else -> startRecording()
        }

        recording = !recording
    }

    private fun startRecording() {
        fotoapparat.startRecording(file = File(
                getExternalFilesDir("videos"),
                "video.mp4"
        ))
    }

    private fun stopRecording() {
        val recordingResult = fotoapparat.stopRecording()

        recordingResult.whenAvailable { it ->
            Log.e(LOGGING_TAG, "Stop recording result: $it")

            if (it is RecordingResult.Success) {
                it.video.makeThumbnail {
                    with(result) {
                        setImageBitmap(it)
                        rotation = 0f
                    }
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

        Log.i(LOGGING_TAG, "New camera position: ${if (activeCamera is Camera.Back) "back" else "front"}")
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

private fun Video.makeThumbnail(function: (Bitmap) -> Unit) = launch {
    val thumbnail = ThumbnailUtils.createVideoThumbnail(
            file.absolutePath,
            MediaStore.Images.Thumbnails.MINI_KIND
    )
    withContext(UI) {
        thumbnail?.let(function)
    }
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
