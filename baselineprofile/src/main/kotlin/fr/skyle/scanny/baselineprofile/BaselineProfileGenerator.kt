package fr.skyle.scanny.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Long enough for a screen of the release app to come in on an emulator
private const val TIMEOUT_MILLIS = 10_000L

/**
 * The journeys whose code is compiled ahead of time at install: the start of the app on the scan
 * screen, which makes the startup profile too, then the editor of a code and the history. Run with
 * `ANDROID_SERIAL=<device> ./gradlew :androidApp:generateReleaseBaselineProfile`, on an emulator of
 * Android 13 or later, in English: it installs the release app, which would replace the one of a phone.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    // The start of the app on the scan screen, in the startup profile too: its code goes first in the dex
    @Test
    fun startup() =
        rule.collect(
            packageName = PACKAGE_NAME,
            includeInStartupProfile = true,
        ) {
            grantCamera()
            pressHome()
            startActivityAndWait()
            device.wait(Until.hasObject(By.desc("Create a code")), TIMEOUT_MILLIS)
        }

    // The editor of a code and the history, compiled ahead of time but left out of the startup profile
    @Test
    fun journeys() =
        rule.collect(packageName = PACKAGE_NAME) {
            grantCamera()
            pressHome()
            startActivityAndWait()
            device.wait(Until.hasObject(By.desc("Create a code")), TIMEOUT_MILLIS)

            // The editor, its fields and the code it draws
            device.findObject(By.desc("Create a code")).click()
            device.wait(Until.hasObject(By.text("Code")), TIMEOUT_MILLIS)
            device.findObject(By.text("Text *"))?.click()
            device.findObject(By.focused(true))?.text = "Scanny"
            device.wait(Until.hasObject(By.desc("The code")), TIMEOUT_MILLIS)
            device.pressBack()
            device.pressBack()

            // The history
            device.wait(Until.hasObject(By.desc("History")), TIMEOUT_MILLIS)
            device.findObject(By.desc("History")).click()
            device.waitForIdle()
            device.pressBack()
        }

    // Granted before the start: the prompt of the system would cover the scan screen
    private fun MacrobenchmarkScope.grantCamera() {
        device.executeShellCommand("pm grant $packageName android.permission.CAMERA")
    }

    private companion object {
        const val PACKAGE_NAME = "fr.skyle.scanny"
    }
}
