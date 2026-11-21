package fr.skyle.scanny.domain.model

data class HistorySettings(
    val isScanHistoryEnabled: Boolean,
    val isConsecutiveDuplicateSkipped: Boolean,
    val retention: HistoryRetention,
)