package io.videoapparat.sample.view

import android.widget.SeekBar

/**
 * Abstract class exposing only [SeekBar.OnSeekBarChangeListener.onProgressChanged].
 */
abstract class OnProgressChanged : SeekBar.OnSeekBarChangeListener {
    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {

    }
}
