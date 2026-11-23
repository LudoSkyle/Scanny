package fr.skyle.scanny.platform.gallery

import fr.skyle.scanny.platform.ui.topViewController
import platform.Foundation.NSData
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.darwin.NSObject

/**
 * Tells the data of the image the user picked, or null if it cannot be loaded, on a background
 * thread. Nothing is told when the user cancels.
 */
internal class ImagePickerDelegate(
    private val onImagePicked: (NSData?) -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(
        picker: PHPickerViewController,
        didFinishPicking: List<*>,
    ) {
        picker.dismissViewControllerAnimated(
            flag = true,
            completion = null,
        )

        // No result when the user cancels
        val itemProvider = (didFinishPicking.firstOrNull() as? PHPickerResult)?.itemProvider ?: return
        itemProvider.loadDataRepresentationForTypeIdentifier(typeIdentifier = "public.image") { data, _ ->
            onImagePicked(data)
        }
    }
}

/** Shows the photos of the user, for one image to be picked. */
internal fun presentImagePicker(delegate: ImagePickerDelegate) {
    val configuration = PHPickerConfiguration().apply {
        filter = PHPickerFilter.imagesFilter
        selectionLimit = 1
    }
    val picker = PHPickerViewController(configuration = configuration).apply {
        this.delegate = delegate
    }
    topViewController()?.presentViewController(
        viewControllerToPresent = picker,
        animated = true,
        completion = null,
    )
}
