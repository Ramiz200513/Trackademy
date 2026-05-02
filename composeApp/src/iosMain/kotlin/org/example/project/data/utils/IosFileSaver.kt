package org.example.project.data.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.example.project.domain.utils.FileSaver
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.popoverPresentationController

class IosFileSaver : FileSaver {

    @OptIn(ExperimentalForeignApi::class)
    override fun saveFile(bytes: ByteArray, fileName: String) {
        val data = bytes.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = bytes.size.toULong()
            )
        }

        val tempDir = NSTemporaryDirectory()
        val filePath = tempDir + fileName
        val fileUrl = NSURL.fileURLWithPath(filePath)

        val writeSuccess = data.writeToURL(fileUrl, atomically = true)

        if (writeSuccess) {
            presentActivityViewController(fileUrl)
        }
    }

    private fun presentActivityViewController(fileUrl: NSURL) {
        val activityViewController = UIActivityViewController(
            activityItems = listOf(fileUrl),
            applicationActivities = null
        )

        // Получаем текущее окно и контроллер более современным способом
        val window = UIApplication.sharedApplication.keyWindow 
            ?: UIApplication.sharedApplication.windows.firstOrNull() as? platform.UIKit.UIWindow
            
        val rootViewController = window?.rootViewController

        // Для iPad нужно указывать источник появления popover
        activityViewController.popoverPresentationController?.sourceView = rootViewController?.view

        rootViewController?.presentViewController(
            viewControllerToPresent = activityViewController,
            animated = true,
            completion = null
        )
    }
}
