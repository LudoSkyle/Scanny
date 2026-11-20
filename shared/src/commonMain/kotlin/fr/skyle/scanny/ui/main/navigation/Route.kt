package fr.skyle.scanny.ui.main.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object Scan : Route

    @Serializable
    data class ScanDetail(val codeId: Long) : Route

    @Serializable
    data object ScanHistory : Route
}