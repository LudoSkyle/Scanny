package fr.skyle.scanny.ui.screen.history.ui.mapper

import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.creations
import fr.skyle.scanny.resources.scans
import fr.skyle.scanny.ui.screen.history.ui.model.HistoryView
import org.jetbrains.compose.resources.StringResource

internal fun HistoryView.toText(): StringResource =
    when (this) {
        HistoryView.SCANS -> Res.string.scans
        HistoryView.CREATIONS -> Res.string.creations
    }
