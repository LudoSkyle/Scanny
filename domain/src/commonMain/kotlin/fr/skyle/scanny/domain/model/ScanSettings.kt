package fr.skyle.scanny.domain.model

data class ScanSettings(
    val isVibrationAfterScanEnabled: Boolean,
    val isOpenLinkAfterScanEnabled: Boolean,
    val isRawContentShown: Boolean,
)