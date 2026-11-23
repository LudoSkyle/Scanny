package fr.skyle.scanny.platform.sound

import platform.AudioToolbox.AudioServicesPlaySystemSound

// The "Tink" sound of iOS, silent when the phone is set to silent
private const val SCAN_SOUND_ID = 1057u

internal class IosScanBeeper : ScanBeeper {

    override fun beep() {
        AudioServicesPlaySystemSound(SCAN_SOUND_ID)
    }
}