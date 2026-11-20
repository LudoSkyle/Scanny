package fr.skyle.scanny.ui.screen.scan.ui.model

enum class ScanResultType {
    TEXT,
    LINK,
    EMAIL,
    SMS,
    WIFI,
    CONTACT,
}

data class ScanResultItem(
    val type: ScanResultType,
    val rawValue: String,
)