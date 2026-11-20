package fr.skyle.scanny.fake

import fr.skyle.scanny.platform.sound.ScanBeeper

/** Counts the beeps played. */
internal class FakeScanBeeper : ScanBeeper {

    var beepCount = 0
        private set

    override fun beep() {
        beepCount += 1
    }
}