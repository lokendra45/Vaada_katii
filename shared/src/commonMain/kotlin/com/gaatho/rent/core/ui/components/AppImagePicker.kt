package com.gaatho.rent.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.crop
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.filekit.toImageSrc
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.util.encodeToByteArray
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.launch

/**
 * A reusable component that encapsulates:
 * 1. Bottom Sheet to choose between Gallery / Camera
 * 2. FileKit launchers for both
 * 3. Krop ImageCropper logic
 * 4. Image conversion to ByteArray
 *
 * @param show Controls the visibility of the Image Source Picker bottom sheet.
 * @param onDismiss Called when the bottom sheet is dismissed or a file is picked.
 * @param onImageCropped Called with the filename and cropped bytes after a successful crop.
 * @param title Title to display on the Cropper UI.
 */
@Composable
fun AppImagePicker(
    show: Boolean,
    onDismiss: () -> Unit,
    onImageCropped: (fileName: String, bytes: ByteArray) -> Unit,
    title: String = "Adjust Image"
) {
    val scope = rememberCoroutineScope()
    val cropper = rememberImageCropper()

    // --- Gallery Launcher ---
    val galleryLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image
    ) { file ->
        if (file != null) {
            scope.launch {
                val imageSrc = file.toImageSrc()
                when (val result = cropper.crop(imageSrc)) {
                    CropResult.Cancelled -> {}
                    is CropError -> {}
                    is CropResult.Success -> {
                        val bytes = result.bitmap.encodeToByteArray()
                        onImageCropped(file.name, bytes)
                    }
                }
            }
        }
        onDismiss()
    }

    // --- Camera Launcher ---
    val cameraLauncher = rememberCameraPickerLauncher { file ->
        if (file != null) {
            scope.launch {
                val imageSrc = file.toImageSrc()
                when (val result = cropper.crop(imageSrc)) {
                    CropResult.Cancelled -> {}
                    is CropError -> {}
                    is CropResult.Success -> {
                        val bytes = result.bitmap.encodeToByteArray()
                        onImageCropped(file.name, bytes)
                    }
                }
            }
        }
        onDismiss()
    }

    // --- Bottom Sheet ---
    if (show) {
        AppImageSourcePicker(
            onDismissRequest = onDismiss,
            onGalleryClick = { galleryLauncher.launch() },
            onCameraClick = { cameraLauncher.launch() }
        )
    }

    // --- Cropper UI ---
    // AppImageCropper internally checks `cropper.cropState != null`
    AppImageCropper(
        imageCropper = cropper,
        title = title
    )
}
