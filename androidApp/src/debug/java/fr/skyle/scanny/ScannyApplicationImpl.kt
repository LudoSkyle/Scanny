package fr.skyle.scanny

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.platformLogWriter

class ScannyApplicationImpl : ScannyApplication() {

    override fun logWriters(): List<LogWriter> =
        super.logWriters() + platformLogWriter()
}