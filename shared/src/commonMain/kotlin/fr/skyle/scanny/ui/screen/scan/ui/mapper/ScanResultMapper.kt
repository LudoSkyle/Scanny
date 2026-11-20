package fr.skyle.scanny.ui.screen.scan.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.ui.core.code.mapper.toCodeAction
import fr.skyle.scanny.ui.core.code.mapper.toCodeRowItem
import fr.skyle.scanny.ui.core.code.mapper.toCodeSummary
import fr.skyle.scanny.ui.core.code.mapper.toCodeType
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResult
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResultItem

// A multiple scan that found a single code is shown as a simple one
internal fun Scan.toScanResult(canJoinWifi: (BarcodeContent.Wifi) -> Boolean): ScanResult =
    codes.singleOrNull()?.let { code ->
        ScanResult.Code(item = code.toScanResultItem(canJoinWifi = canJoinWifi))
    } ?: ScanResult.Group(
        scanId = id,
        codes = codes.map { code ->
            code.toCodeRowItem()
        },
    )

internal fun ScannedCode.toScanResultItem(canJoinWifi: (BarcodeContent.Wifi) -> Boolean): ScanResultItem =
    ScanResultItem(
        codeId = id,
        type = content.toCodeType(),
        format = format,
        summary = toCodeSummary(),
        rawValue = rawValue,
        action = content.toCodeAction(canJoinWifi = canJoinWifi),
    )