package com.example.taller_2.viewmodels

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.example.taller_2.utils.ImageFileUtils
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CameraPreviewState(
    val surfaceRequest: SurfaceRequest? = null,
    val useBackLens: Boolean = true,
)

class CameraPreviewViewModel : ViewModel() {
    // Enlaza la cámara con la interfaz de usuario
    private val _uiState = MutableStateFlow(CameraPreviewState())
    val uiState: StateFlow<CameraPreviewState> = _uiState.asStateFlow()

    // Se usan para controlar la cámara y para enfocar al tocar la vista previa
    private var surfaceMeteringPointFactory: SurfaceOrientedMeteringPointFactory? = null
    private var cameraControl: CameraControl? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var processCameraProvider: ProcessCameraProvider? = null
    private var selector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
        .build()

    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            surfaceMeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                newSurfaceRequest.resolution.width.toFloat(),
                newSurfaceRequest.resolution.height.toFloat()
            )
            _uiState.update { it.copy(surfaceRequest = newSurfaceRequest) }
        }
    }

    /**
     * Enlaza la cámara al ciclo de vida y se queda esperando hasta que la corrutina
     * que la llamó se cancele, momento en el que libera la cámara.
     */
    suspend fun bindToCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
        processCameraProvider = ProcessCameraProvider.awaitInstance(appContext)
        val camera = processCameraProvider?.bindToLifecycle(
            lifecycleOwner, selector, cameraPreviewUseCase, imageCapture
        )
        cameraControl = camera?.cameraControl

        try {
            awaitCancellation()
        } finally {
            cameraControl = null
            surfaceMeteringPointFactory = null
            processCameraProvider?.unbindAll()
        }
    }

    /**
     * Enfoca la cámara en el punto de la vista previa que el usuario tocó.
     */
    fun tapToFocus(tapCoords: Offset) {
        val point = surfaceMeteringPointFactory?.createPoint(tapCoords.x, tapCoords.y)
        if (point != null) {
            val meteringAction = FocusMeteringAction.Builder(point).build()
            cameraControl?.startFocusAndMetering(meteringAction)
        }
    }

    /**
     * Cambia entre la cámara frontal y la trasera.
     */
    fun toggleCameraLens() {
        val nextUseBackLens = !_uiState.value.useBackLens
        _uiState.update { it.copy(useBackLens = nextUseBackLens) }
        selector = if (nextUseBackLens) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        val owner = lifecycleOwner
        val provider = processCameraProvider
        if (owner != null && provider != null) {
            provider.unbindAll()
            val camera = provider.bindToLifecycle(owner, selector, cameraPreviewUseCase, imageCapture)
            cameraControl = camera.cameraControl
        }
    }

    /**
     * Toma una foto y la guarda en la galería del dispositivo.
     * Llama a [onPhotoSaved] con el nombre y la ubicación de la foto cuando se guarda,
     * o a [onPhotoError] si no se pudo tomar.
     */
    fun takePhoto(
        context: Context,
        onPhotoSaved: (name: String, uri: Uri) -> Unit,
        onPhotoError: () -> Unit
    ) {
        val photoName = ImageFileUtils.createImageName()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ImageFileUtils.createContentValues(photoName)
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri
                    if (savedUri != null) {
                        onPhotoSaved(photoName, savedUri)
                    } else {
                        onPhotoError()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraPreviewViewModel", "Error taking photo", exception)
                    onPhotoError()
                }
            }
        )
    }
}
