package fr.skyle.scanny.platform.sound

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import co.touchlab.kermit.Logger

private const val BEEP_VOLUME = 80
private const val BEEP_DURATION_MILLIS = 150

internal class AndroidScanBeeper : ScanBeeper {

    override fun beep() {
        try {
            // One for each beep, released once heard: a ToneGenerator holds an audio track while it lives
            val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, BEEP_VOLUME)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, BEEP_DURATION_MILLIS)
            Handler(Looper.getMainLooper()).postDelayed(
                { toneGenerator.release() },
                BEEP_DURATION_MILLIS * 2L,
            )
        } catch (exception: RuntimeException) {
            // The audio of the device is not available: the scan goes on without its beep
            Logger.w(throwable = exception) { "Failed to beep after a scan" }
        }
    }
}