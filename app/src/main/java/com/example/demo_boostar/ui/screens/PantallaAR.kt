package com.example.demo_boostar.ui.screens

import android.media.Image
import android.os.Build
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.demo_boostar.utils.createAnchorNode
import com.example.demo_boostar.viewmodels.PoseViewModel

import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.TrackingFailureReason
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.xr.runtime.math.Vector3
import com.example.demo_boostar.utils.Coordenadas
import com.example.demo_boostar.utils.landmarkToAnchorHitTest
import com.example.demo_boostar.utils.landmarkToWorldPos
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.core.exceptions.NotYetAvailableException
import com.google.mediapipe.formats.proto.LandmarkProto
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.ar.rememberARCameraStream
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberView

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun PantallaAR(poseViewModel: PoseViewModel) {

    val context = LocalContext.current
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val cameraNode = rememberARCameraNode(engine)
    val view = rememberView(engine)
    val collisionSystem = rememberCollisionSystem(view)
    val cameraStream = rememberARCameraStream(materialLoader)
    cameraStream.cameraTexture

    val poseLandmarks by poseViewModel.landmarks.observeAsState()
    var trackingFailureReason by remember { mutableStateOf<TrackingFailureReason?>(null) }
    // Lista de AnchorNodes de landmarks
    val landmarkAnchors = rememberNodes()
    var torsoNode by remember { mutableStateOf<AnchorNode?>(null) }
    var frame by remember { mutableStateOf<Frame?>(null) }
    var nodes by remember { mutableStateOf<MutableList<AnchorNode>>(mutableListOf()) }
    DisposableEffect(Unit) {
        poseViewModel.startPoseLandMarker(context)

        onDispose {
            poseViewModel.stopPoseHelper()
        }
    }


    ARScene(
        modifier = Modifier.fillMaxSize(),
        childNodes = landmarkAnchors, // agregamos los anchors a la escena
        engine = engine,
        view = view,
        modelLoader = modelLoader,
        collisionSystem = collisionSystem,
        cameraNode = cameraNode,
        sessionCameraConfig = { session ->
            val cameraFilter = CameraConfigFilter(session)
                .setFacingDirection(CameraConfig.FacingDirection.BACK)
            session.getSupportedCameraConfigs(cameraFilter).first()
        },
        planeRenderer = true,
        sessionConfiguration = { session, config ->
            config.depthMode =
                if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC))
                    Config.DepthMode.AUTOMATIC else Config.DepthMode.DISABLED
            config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
            config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
        },
        onTrackingFailureChanged = { trackingFailureReason = it },
        onSessionUpdated = { session, updatedFrame ->
            frame = updatedFrame
            var depthImage: Image? = null
            frame?.let { frame ->
                try {
                    depthImage = frame.acquireDepthImage16Bits()
                } catch (e: NotYetAvailableException) {
                }
                val displayRotation =
                    context.display.rotation  // Devuelve Surface.ROTATION_0, _90, _180, _270

                val rotationDegrees = when (displayRotation) {
                    Surface.ROTATION_0 -> 0
                    Surface.ROTATION_90 -> 90
                    Surface.ROTATION_180 -> 180
                    Surface.ROTATION_270 -> 270
                    else -> 0
                }

                poseViewModel.detectImage(frame, rotationDegrees)

                val filteredLandmarks = poseLandmarks?.filterIndexed { i, _ -> i == 11 || i == 12 || i == 23 || i == 24 }
                val coordenadas = mutableListOf<Coordenadas>()
                filteredLandmarks?.takeIf { it.size == 4 }?.forEach { landmark ->

                    landmarkToAnchorHitTest(
                        landmark = landmark,
                        frame = frame,
                        surfaceWidth = view.viewport.width,
                        surfaceHeight = view.viewport.height,
                        )?.let { coordenada ->
                            Log.d("PantallaAR HitTest", "$coordenada")
                        coordenadas.add(coordenada)
                                    }
                    depthImage?.let {
                        /*
                        landmarkToWorldPos(landmark, frame, depthImage).let { coordenada ->
                        Log.d("PantallaAR ImagenProfundidad", "$coordenada")
                        coordenadas.add(coordenada)*/
                    }
                    Log.d("PantallaAR", "landmarks=$filteredLandmarks")
                    Log.d("PantallaAR", "anchors=$coordenadas")
                    coordenadas.takeIf { it.size == 4 }?.let { a ->
                        // Calcular centro de los 4 landmarks
                        val centralX = (a[0].x + a[1].x + a[2].x + a[3].x) / 4f
                        val centralY = (a[0].y + a[1].y + a[2].y + a[3].y) / 4f
                        val centralZ = (a[0].z + a[1].z + a[2].z + a[3].z) / 4f

                        val pose = Pose(
                            floatArrayOf(centralX, centralY, centralZ),
                            floatArrayOf(0.7071f, 0f, 0f, 0.7071f)
                        )
                        Log.d("PantallaAR", "$pose")
                        try {
                            if (torsoNode == null) {
                                // Crear AnchorNode la primera vez
                                val anchor = session.createAnchor(pose)
                                torsoNode = createAnchorNode(engine, modelLoader, materialLoader, anchor, "prueba_cloth_Jordi.glb")
                                torsoNode!!.setScale(4f)
                                landmarkAnchors.add(0, torsoNode!!)

                                Log.d("PantallaAR UNICO", "Nodo Colocado")
                            } else {
                                // Actualizar posición del Node existente
                                torsoNode!!.worldPosition = Float3(centralX, centralY, centralZ)
                                torsoNode!!.worldRotation = Float3(0f, 0f, 0f)
                                Log.d("PantallaAR UNICO", "Reposicionado")
                            }
                        } catch (e: Exception) {
                            Log.d("ERROR PantallaAR", "$e")
                        }
                    }
                /*
                poseLandmarks?.forEachIndexed { i, landmark ->
                    depthImage?.let {
                        val coordenada = landmarkToWorldPos(
                            landmark = landmark,
                            frame = frame,
                            depthImage = depthImage,
                        )
                        coordenada?.let {
                            Log.d("ARScene", "$coordenada")
                            val pose = Pose(
                                floatArrayOf(coordenada.x, coordenada.y, coordenada.z),
                                floatArrayOf(0.7071f, 0f, 0f, 0.7071f)
                            )
                            Log.d("PantallaAR", "$pose")
                            try {
                                if (landmarkAnchors.size <= i) {
                                    // Crear AnchorNode la primera vez
                                    val anchor = session.createAnchor(pose)
                                    landmarkAnchors.add(
                                        i,
                                        createAnchorNode(
                                            engine,
                                            modelLoader,
                                            materialLoader,
                                            anchor
                                        )
                                    )

                                    Log.d("PantallaAR UNICO", "Nodo Colocado")
                                } else {
                                    landmarkAnchors[i].worldPosition =
                                        Float3(coordenada.x, coordenada.y, coordenada.z)
                                    landmarkAnchors[i].worldRotation = Float3(0f, 0f, 0f)
                                    Log.d("PantallaAR UNICO", "Reposicionado")
                                }
                            } catch (e: Exception) {
                                Log.d("ERROR PantallaAR", "$e")
                            }
                        }
                    }*/
                }

            }
            depthImage?.close()
        },
    )
}
