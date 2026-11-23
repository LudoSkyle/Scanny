package fr.skyle.scanny.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Enough cold starts for a median that holds
private const val ITERATIONS = 10
private const val TIMEOUT_MILLIS = 10_000L

/**
 * The cold start of the release app to its scan screen, without any ahead of time compilation, then
 * with the baseline profile: what the profile saves. Run with
 * `ANDROID_SERIAL=<device> ./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest`.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun startupWithoutCompilation() =
        startup(compilationMode = CompilationMode.None())

    @Test
    fun startupWithBaselineProfile() =
        startup(compilationMode = CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require))

    private fun startup(compilationMode: CompilationMode) =
        rule.measureRepeated(
            packageName = "fr.skyle.scanny",
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            iterations = ITERATIONS,
            startupMode = StartupMode.COLD,
            setupBlock = {
                // Granted before the start: the prompt of the system would cover the scan screen
                device.executeShellCommand("pm grant $packageName android.permission.CAMERA")
                pressHome()
            },
        ) {
            startActivityAndWait()
            device.wait(Until.hasObject(By.desc("Create a code")), TIMEOUT_MILLIS)
        }
}
