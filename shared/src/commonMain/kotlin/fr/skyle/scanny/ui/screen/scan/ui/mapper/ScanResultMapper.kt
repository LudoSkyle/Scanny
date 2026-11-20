package fr.skyle.scanny.ui.screen.scan.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResultItem
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResultType

internal fun ScannedCode.toScanResultItem(): ScanResultItem =
    ScanResultItem(
        codeId = id,
        type = when (content) {
            is BarcodeContent.Text -> ScanResultType.TEXT
            is BarcodeContent.Url -> ScanResultType.LINK
            is BarcodeContent.Email -> ScanResultType.EMAIL
            is BarcodeContent.Sms -> ScanResultType.SMS
            is BarcodeContent.Wifi -> ScanResultType.WIFI
            is BarcodeContent.Contact -> ScanResultType.CONTACT
        },
        rawValue = rawValue,
    )