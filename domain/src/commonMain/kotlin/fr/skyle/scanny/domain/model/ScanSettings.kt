package fr.skyle.scanny.domain.model

data class ScanSettings(
    val isVibrationAfterScanEnabled: Boolean,
    val isBeepAfterScanEnabled: Boolean,
    val isCopyAfterScanEnabled: Boolean,
    val isOpenContentAfterScanEnabled: Boolean,
    val isFrontCameraUsed: Boolean,
    // The sheet of a scan shows the text of the code rather than its fields
    val isRawContentShown: Boolean,
)