package fr.skyle.scanny.data.file

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

// Application Support holds the data files of the app: not shown to the user, and backed up
@OptIn(ExperimentalForeignApi::class)
internal fun applicationSupportPath(fileName: String): String {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )

    return checkNotNull(directory?.URLByAppendingPathComponent(fileName)?.path) {
        "The Application Support directory of the app is not available"
    }
}