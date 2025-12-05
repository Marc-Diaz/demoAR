package com.example.demo_boostar.viewmodels

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.demo_boostar.utils.frameToMPImage
import com.google.ar.core.Frame
import com.google.ar.core.exceptions.NotYetAvailableException
import com.google.mediapipe.examples.poselandmarker.PoseLandmarkerHelper
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.core.RunningMode
import io.github.sceneview.ar.arcore.isTracking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
/$$$$$$$  /$$$$$$$$ /$$       /$$$$$$  /$$$$$$  /$$$$$$$   /$$$$$$
| $$__  $$| $$_____/| $$      |_  $$_/ /$$__  $$| $$__  $$ /$$__  $$
| $$  \ $$| $$      | $$        | $$  | $$  \__/| $$  \ $$| $$  \ $$
| $$$$$$$/| $$$$$   | $$        | $$  | $$ /$$$$| $$$$$$$/| $$  | $$
| $$____/ | $$__/   | $$        | $$  | $$|_  $$| $$__  $$| $$  | $$
| $$      | $$      | $$        | $$  | $$  \ $$| $$  \ $$| $$  | $$
| $$      | $$$$$$$$| $$$$$$$$ /$$$$$$|  $$$$$$/| $$  | $$|  $$$$$$/
|__/      |________/|________/|______/ \______/ |__/  |__/ \______/



/$$   /$$  /$$$$$$        /$$$$$$$$ /$$$$$$   /$$$$$$   /$$$$$$  /$$$$$$$  /$$ /$$
| $$$ | $$ /$$__  $$      |__  $$__//$$__  $$ /$$__  $$ /$$__  $$| $$__  $$| $$| $$
| $$$$| $$| $$  \ $$         | $$  | $$  \ $$| $$  \__/| $$  \ $$| $$  \ $$| $$| $$
| $$ $$ $$| $$  | $$         | $$  | $$  | $$| $$      | $$$$$$$$| $$$$$$$/| $$| $$
| $$  $$$$| $$  | $$         | $$  | $$  | $$| $$      | $$__  $$| $$__  $$|__/|__/
| $$\  $$$| $$  | $$         | $$  | $$  | $$| $$    $$| $$  | $$| $$  \ $$
| $$ \  $$|  $$$$$$/         | $$  |  $$$$$$/|  $$$$$$/| $$  | $$| $$  | $$ /$$ /$$
|__/  \__/ \______/          |__/   \______/  \______/ |__/  |__/|__/  |__/|__/|__/


                        @         @
                         @         @
                     @   @         @   @
                     @  @@         @@  @
                     @@ @@@       @@@ @@
             @      @@   @@@     @@@   @@      @
            @@      @@   @@@     @@@   @@      @@
           @@      @@    @@@@   @@@@    @@      @@
           @@     @@@    @@@@  @@@@@    @@@     @@@
       @  @@@    @@@@    @@@@   @@@@    @@@@   @@@@  @
       @@ @@@@@  @@@@   @@@@@   @@@@@   @@@@  @@@@@ @@
       @@ @@@@@  @@@@@@@@@@@     @@@@@@@@@@@  @@@@@ @@
       @@ @@@@@  @@@@@@@@@@@     @@@@@@@@@@@  @@@@@ @@
      @@@  @@@@   @@@@@@@@@@@@@@@@@@@@@@@@@   @@@@  @@@
     @@@@  @@@@   @@@@@@@@@@@@@@@@@@@@@@@@@   @@@@  @@@@
    @@@@   @@@@@ @@@@@@@@@@@@@@@@@@@@@@@@@@@ @@@@@   @@@@
   @@@@    @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   @@@@
   @@@@@  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@  @@@@
    @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
     @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
     @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
      @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
     @@@@@           @@@@@@@@@@@@@@@@@@@           @@@@@
     @@@@@@             @@@@@@@@@@@@@             @@@@@@
      @@@@@@@        ..     @@@@@@@@@     ..        @@@@@@
       @@@@@@@@             @@@@@           | $$  $$$$| $$  | $$         | $$  | $$  | $$| $$      | $$__  $$| $$__  $$|__/|__/
  @@@@@@@@
        @@@@@@@@@@           @@@           @@@@@@@@@@
           @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
              @@@@@@@@@@@@@@@   @@@@@@@@@@@@@@@
                  @@@@@@@@@@     @@@@@@@@@@
                   @@@@@@@@       @@@@@@@@
                  @@@@@@@@@       @@@@@@@@@
                  @@@@@@@@@ @@@@@ @@@@@@@@@
                 @@@@@@@@@@@@@@@@@@@@@@@@@@@
                 @@@  @@@@@@@@@@@@@@@@@  @@@
                  @@  @@@@  @@@@@  @@@@  @@
                      @@@@  @@@@@  @@@@


*/

class PoseViewModel() : ViewModel() {

    // LiveData para exponer los resultados del pose
    private val _poseResults = MutableLiveData<PoseLandmarkerHelper.ResultBundle?>()
    val poseResults: LiveData<PoseLandmarkerHelper.ResultBundle?> = _poseResults

    // LiveData para notificar errores
    private val _poseErrors = MutableLiveData<String>()
    val poseErrors: LiveData<String> = _poseErrors

    private val poseThread = HandlerThread("PoseThread").apply { start() }
    private val poseHandler = Handler(poseThread.looper)

    private val _landmarks = MutableLiveData<List<NormalizedLandmark>>()
    val landmarks = _landmarks

    // PoseLandmarkerHelper
    private var _poseHelper: PoseLandmarkerHelper? = null

    fun startPoseLandMarker(context: Context){
        _poseHelper = PoseLandmarkerHelper(
            context = context,
            runningMode = RunningMode.LIVE_STREAM,
            currentDelegate = PoseLandmarkerHelper.DELEGATE_CPU,
            poseLandmarkerHelperListener = object : PoseLandmarkerHelper.LandmarkerListener {
                override fun onError(error: String, errorCode: Int) {
                    _poseErrors.postValue(error)
                }

                override fun onResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
                    _poseResults.postValue(resultBundle)
                    CoroutineScope(Dispatchers.Main).launch{
                        _landmarks.value = resultBundle.results.get(0).landmarks().firstOrNull()
                        Log.d("PoseViewModel", "${_landmarks.value}")
                    }
                }
            }
        )
    }

    fun stopPoseHelper() {
        _poseHelper?.clearPoseLandmarker()
        _poseHelper = null
    }
    @Volatile
    private var isProcessing = false

    // Limpiar recursos cuando se destruye el ViewModel
    override fun onCleared() {
        super.onCleared()
        _poseHelper?.clearPoseLandmarker()
    }

    fun detectImage(frame: Frame, degrees: Int, isFrontCamera: Boolean = false) {
        var image: MPImage? = null

        if (!frame.camera.isTracking) return
        try {
            image = frameToMPImage(frame)
        }
        catch (e: NotYetAvailableException){
        }
        finally {
            if (isProcessing && image != null) {
                image.close() // descarta el frame si otro está en proceso
                return
            }
            isProcessing = true
            poseHandler.post {
                try {
                    if (image != null)
                        _poseHelper?.detectLiveStream(image, isFrontCamera, degrees.toFloat())
                }
                finally {
                    isProcessing = false
                }
            }
        }

    }
}
