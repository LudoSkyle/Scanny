package fr.skyle.scanny.platform.camera

import androidx.compose.runtime.Composable

enum class CameraPermissionStatus {
    // Not asked yet, or asked and the answer is awaited
    UNKNOWN,
    GRANTED,
    // Refused, and the platform still lets the app ask again
    DENIED,
    // Refused for good: only the settings of the device can grant it now
    DENIED_FOREVER,
}

interface CameraPermissionState {

    val status: CameraPermissionStatus

    fun request()

    fun openAppSettings()
}

@Composable
expect fun rememberCameraPermissionState(): CameraPermissionState