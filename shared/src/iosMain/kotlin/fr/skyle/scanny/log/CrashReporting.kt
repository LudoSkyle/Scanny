package fr.skyle.scanny.log

import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook
import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter

/**
 * Sends the warnings, the errors and the crashes of Kotlin to Crashlytics, as ScannyApplication does
 * on Android. Called by the app of iosApp once Firebase starts: a build without the private Firebase
 * file has none.
 */
@OptIn(ExperimentalKermitApi::class)
fun startCrashReporting() {
    enableCrashlytics()
    Logger.setLogWriters(
        CrashlyticsLogWriter(
            minSeverity = Severity.Warn,
            minCrashSeverity = Severity.Warn,
        ),
    )
    // An uncaught Kotlin exception reaches Crashlytics with its Kotlin stack, before the app stops
    setCrashlyticsUnhandledExceptionHook()
}
