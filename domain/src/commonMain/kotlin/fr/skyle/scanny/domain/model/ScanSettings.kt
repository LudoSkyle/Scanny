package fr.skyle.scanny.domain.model

data class ScanSettings(
    val isVibrationAfterScanEnabled: Boolean,
    val isBeepAfterScanEnabled: Boolean,
    val isCopyAfterScanEnabled: Boolean,
    val isOpenContentAfterScanEnabled: Boolean,
    val isFrontCameraUsed: Boolean,
)