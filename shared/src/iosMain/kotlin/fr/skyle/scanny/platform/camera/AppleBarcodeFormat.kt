package fr.skyle.scanny.platform.camera

import fr.skyle.scanny.domain.model.BarcodeFormat
import platform.AVFoundation.AVMetadataObjectType
import platform.AVFoundation.AVMetadataObjectTypeAztecCode
import platform.AVFoundation.AVMetadataObjectTypeCodabarCode
import platform.AVFoundation.AVMetadataObjectTypeCode128Code
import platform.AVFoundation.AVMetadataObjectTypeCode39Code
import platform.AVFoundation.AVMetadataObjectTypeCode39Mod43Code
import platform.AVFoundation.AVMetadataObjectTypeCode93Code
import platform.AVFoundation.AVMetadataObjectTypeDataMatrixCode
import platform.AVFoundation.AVMetadataObjectTypeEAN13Code
import platform.AVFoundation.AVMetadataObjectTypeEAN8Code
import platform.AVFoundation.AVMetadataObjectTypeITF14Code
import platform.AVFoundation.AVMetadataObjectTypeInterleaved2of5Code
import platform.AVFoundation.AVMetadataObjectTypePDF417Code
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.AVMetadataObjectTypeUPCECode
import platform.Vision.VNBarcodeSymbology
import platform.Vision.VNBarcodeSymbologyAztec
import platform.Vision.VNBarcodeSymbologyCodabar
import platform.Vision.VNBarcodeSymbologyCode128
import platform.Vision.VNBarcodeSymbologyCode39
import platform.Vision.VNBarcodeSymbologyCode39Checksum
import platform.Vision.VNBarcodeSymbologyCode39FullASCII
import platform.Vision.VNBarcodeSymbologyCode39FullASCIIChecksum
import platform.Vision.VNBarcodeSymbologyCode93
import platform.Vision.VNBarcodeSymbologyCode93i
import platform.Vision.VNBarcodeSymbologyDataMatrix
import platform.Vision.VNBarcodeSymbologyEAN13
import platform.Vision.VNBarcodeSymbologyEAN8
import platform.Vision.VNBarcodeSymbologyI2of5
import platform.Vision.VNBarcodeSymbologyI2of5Checksum
import platform.Vision.VNBarcodeSymbologyITF14
import platform.Vision.VNBarcodeSymbologyPDF417
import platform.Vision.VNBarcodeSymbologyQR
import platform.Vision.VNBarcodeSymbologyUPCE

// AVFoundation has no UPC-A type: it reports one as an EAN-13 starting with 0, see normalized()
internal val scannedMetadataObjectTypes: List<AVMetadataObjectType> =
    listOfNotNull(
        AVMetadataObjectTypeQRCode,
        AVMetadataObjectTypeDataMatrixCode,
        AVMetadataObjectTypeAztecCode,
        AVMetadataObjectTypePDF417Code,
        AVMetadataObjectTypeEAN13Code,
        AVMetadataObjectTypeEAN8Code,
        AVMetadataObjectTypeUPCECode,
        AVMetadataObjectTypeCode39Code,
        AVMetadataObjectTypeCode39Mod43Code,
        AVMetadataObjectTypeCode93Code,
        AVMetadataObjectTypeCode128Code,
        AVMetadataObjectTypeInterleaved2of5Code,
        AVMetadataObjectTypeITF14Code,
        AVMetadataObjectTypeCodabarCode,
    )

internal fun metadataObjectTypeToBarcodeFormat(type: AVMetadataObjectType): BarcodeFormat? =
    when (type) {
        AVMetadataObjectTypeQRCode -> BarcodeFormat.QR_CODE
        AVMetadataObjectTypeDataMatrixCode -> BarcodeFormat.DATA_MATRIX
        AVMetadataObjectTypeAztecCode -> BarcodeFormat.AZTEC
        AVMetadataObjectTypePDF417Code -> BarcodeFormat.PDF417
        AVMetadataObjectTypeEAN13Code -> BarcodeFormat.EAN_13
        AVMetadataObjectTypeEAN8Code -> BarcodeFormat.EAN_8
        AVMetadataObjectTypeUPCECode -> BarcodeFormat.UPC_E
        AVMetadataObjectTypeCode39Code, AVMetadataObjectTypeCode39Mod43Code -> BarcodeFormat.CODE_39
        AVMetadataObjectTypeCode93Code -> BarcodeFormat.CODE_93
        AVMetadataObjectTypeCode128Code -> BarcodeFormat.CODE_128
        AVMetadataObjectTypeInterleaved2of5Code, AVMetadataObjectTypeITF14Code -> BarcodeFormat.ITF
        AVMetadataObjectTypeCodabarCode -> BarcodeFormat.CODABAR
        else -> null
    }

internal fun symbologyToBarcodeFormat(symbology: VNBarcodeSymbology): BarcodeFormat? =
    when (symbology) {
        VNBarcodeSymbologyQR -> BarcodeFormat.QR_CODE
        VNBarcodeSymbologyDataMatrix -> BarcodeFormat.DATA_MATRIX
        VNBarcodeSymbologyAztec -> BarcodeFormat.AZTEC
        VNBarcodeSymbologyPDF417 -> BarcodeFormat.PDF417
        VNBarcodeSymbologyEAN13 -> BarcodeFormat.EAN_13
        VNBarcodeSymbologyEAN8 -> BarcodeFormat.EAN_8
        VNBarcodeSymbologyUPCE -> BarcodeFormat.UPC_E
        VNBarcodeSymbologyCode39,
        VNBarcodeSymbologyCode39Checksum,
        VNBarcodeSymbologyCode39FullASCII,
        VNBarcodeSymbologyCode39FullASCIIChecksum,
        -> BarcodeFormat.CODE_39
        VNBarcodeSymbologyCode93, VNBarcodeSymbologyCode93i -> BarcodeFormat.CODE_93
        VNBarcodeSymbologyCode128 -> BarcodeFormat.CODE_128
        VNBarcodeSymbologyI2of5, VNBarcodeSymbologyI2of5Checksum, VNBarcodeSymbologyITF14 -> BarcodeFormat.ITF
        VNBarcodeSymbologyCodabar -> BarcodeFormat.CODABAR
        else -> null
    }