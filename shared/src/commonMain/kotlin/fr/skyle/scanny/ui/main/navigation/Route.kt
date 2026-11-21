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
    data class ScanGroup(val scanId: Long) : Route

    @Serializable
    data object History : Route

    @Serializable
    data object CreateCode : Route

    @Serializable
    data class CreationDetail(val creationId: Long) : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Feedback : Route
}