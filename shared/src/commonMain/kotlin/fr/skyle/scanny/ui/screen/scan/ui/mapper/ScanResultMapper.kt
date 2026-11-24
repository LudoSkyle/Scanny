package fr.skyle.scanny.ui.screen.scan.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.ui.core.code.mapper.toCodeAction
import fr.skyle.scanny.ui.core.code.mapper.toCodeFields
import fr.skyle.scanny.ui.core.code.mapper.toCodeRowItem
import fr.skyle.scanny.ui.core.code.mapper.toCodeType
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResult
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResultItem

// A multiple scan that found a single code is shown as a simple one
internal fun Scan.toScanResult(
    canJoinWifi: (BarcodeContent.Wifi) -> Boolean,
    isRawContentShown: Boolean,
): ScanResult =
    codes.singleOrNull()?.let { code ->
        ScanResult.Code(
            item = code.toScanResultItem(
                canJoinWifi = canJoinWifi,
                isRawContentShown = isRawContentShown,
            ),
        )
    } ?: ScanResult.Group(
        scanId = id,
        codes = codes.map { code ->
            code.toCodeRowItem()
        },
        isFavorite = isFavorite,
    )

// The text of the code as it was read, rather than its fields, when the user asks for it
internal fun ScannedCode.toScanResultItem(
    canJoinWifi: (BarcodeContent.Wifi) -> Boolean,
    isRawContentShown: Boolean,
): ScanResultItem =
    ScanResultItem(
        codeId = id,
        scanId = scanId,
        type = content.toCodeType(),
        format = format,
        fields = if (isRawContentShown) {
            listOf(
                CodeField(
                    label = null,
                    value = rawValue,
                ),
            )
        } else {
            content.toCodeFields()
        },
        rawValue = rawValue,
        action = content.toCodeAction(canJoinWifi = canJoinWifi),
        isFavorite = isFavorite,
    )