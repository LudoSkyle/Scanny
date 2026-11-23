package fr.skyle.scanny.platform.gallery

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import co.touchlab.kermit.Logger
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import fr.skyle.scanny.platform.camera.mlKitScannerOptions
import fr.skyle.scanny.platform.camera.toDetectedBarcode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
actual fun rememberImageBarcodeScanner(onResult: (ImageScanResult) -> Unit): () -> Unit {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                currentOnResult(
                    scanImage(
                        context = context,
                        uri = uri,
                    ),
                )
            }
        }
    }

    return remember(launcher) {
        {
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
}

private suspend fun scanImage(
    context: Context,
    uri: Uri,
): ImageScanResult {
    val barcodeScanner = BarcodeScanning.getClient(mlKitScannerOptions)

    return try {
        val image = InputImage.fromFilePath(context, uri)
        val detectedBarcode = barcodeScanner.process(image).await()
            .firstNotNullOfOrNull { barcode -> barcode.toDetectedBarcode() }

        if (detectedBarcode != null) ImageScanResult.Detected(barcode = detectedBarcode) else ImageScanResult.NoBarcode
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        // An image that cannot be read holds no code for the user
        Logger.w(throwable = exception) { "Failed to read the image $uri" }
        ImageScanResult.NoBarcode
    } finally {
        barcodeScanner.close()
    }
}