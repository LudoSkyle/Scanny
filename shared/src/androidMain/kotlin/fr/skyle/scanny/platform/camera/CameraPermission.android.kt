package fr.skyle.scanny.platform.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

@Composable
actual fun rememberCameraPermissionState(): CameraPermissionState {
    val context = LocalContext.current
    val status = remember {
        mutableStateOf(if (context.isCameraGranted()) CameraPermissionStatus.GRANTED else CameraPermissionStatus.UNKNOWN)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        status.value = when {
            isGranted -> CameraPermissionStatus.GRANTED
            context.canAskCameraAgain() -> CameraPermissionStatus.DENIED
            // Android stops showing its dialog after the second refusal
            else -> CameraPermissionStatus.DENIED_FOREVER
        }
    }

    // Back from the settings of the device, where the permission may have changed
    LifecycleResumeEffect(status) {
        when {
            context.isCameraGranted() -> status.value = CameraPermissionStatus.GRANTED
            status.value == CameraPermissionStatus.GRANTED -> status.value = CameraPermissionStatus.UNKNOWN
        }
        onPauseOrDispose {}
    }

    return remember(launcher) {
        AndroidCameraPermissionState(
            context = context,
            statusState = status,
            launcher = launcher,
        )
    }
}

private class AndroidCameraPermissionState(
    private val context: Context,
    private val statusState: MutableState<CameraPermissionStatus>,
    private val launcher: ManagedActivityResultLauncher<String, Boolean>,
) : CameraPermissionState {

    override val status: CameraPermissionStatus
        get() = statusState.value

    override fun request() {
        launcher.launch(Manifest.permission.CAMERA)
    }

    override fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

private fun Context.isCameraGranted(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

private fun Context.canAskCameraAgain(): Boolean {
    val activity = findActivity() ?: return false
    return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }