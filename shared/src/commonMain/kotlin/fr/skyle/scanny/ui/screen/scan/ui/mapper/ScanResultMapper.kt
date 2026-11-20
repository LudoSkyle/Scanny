package fr.skyle.scanny.ui.screen.scan.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.ui.core.code.mapper.toCodeAction
import fr.skyle.scanny.ui.core.code.mapper.toCodeSummary
import fr.skyle.scanny.ui.core.code.mapper.toCodeType
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResultItem

internal fun ScannedCode.toScanResultItem(canJoinWifi: (BarcodeContent.Wifi) -> Boolean): ScanResultItem =
    ScanResultItem(
        codeId = id,
        type = content.toCodeType(),
        format = format,
        summary = toCodeSummary(),
        rawValue = rawValue,
        action = content.toCodeAction(canJoinWifi = canJoinWifi),
    )