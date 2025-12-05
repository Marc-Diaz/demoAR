package com.example.demo_boostar.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import com.google.ar.core.DepthPoint
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Point
import com.google.ar.core.Session
import com.google.ar.core.exceptions.NotYetAvailableException
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.ModelNode
import java.io.ByteArrayOutputStream
import java.nio.ByteOrder

private const val kModelFile = "models/prueba_cloth_Jordi.glb"
/*
 /$$   /$$ /$$   /$$ /$$     /$$ /$$$$$$$$
| $$  | $$| $$  | $$|  $$   /$$/| $$_____/
| $$  | $$| $$  | $$ \  $$ /$$/ | $$
| $$$$$$$$| $$  | $$  \  $$$$/  | $$$$$
| $$__  $$| $$  | $$   \  $$/   | $$__/
| $$  | $$| $$  | $$    | $$    | $$
| $$  | $$|  $$$$$$/    | $$    | $$$$$$$$
|__/  |__/ \______/     |__/    |________/



 /$$   /$$  /$$$$$$        /$$      /$$ /$$$$$$ /$$$$$$$  /$$$$$$$$  /$$$$$$         /$$$$$$  /$$$$$$$$ /$$$$$$$   /$$$$$$   /$$$$$$  /$$ /$$
| $$$ | $$ /$$__  $$      | $$$    /$$$|_  $$_/| $$__  $$| $$_____/ /$$__  $$       /$$__  $$|__  $$__/| $$__  $$ /$$__  $$ /$$__  $$| $$| $$
| $$$$| $$| $$  \ $$      | $$$$  /$$$$  | $$  | $$  \ $$| $$      | $$  \__/      | $$  \ $$   | $$   | $$  \ $$| $$  \ $$| $$  \__/| $$| $$
| $$ $$ $$| $$  | $$      | $$ $$/$$ $$  | $$  | $$$$$$$/| $$$$$   |  $$$$$$       | $$$$$$$$   | $$   | $$$$$$$/| $$$$$$$$|  $$$$$$ | $$| $$
| $$  $$$$| $$  | $$      | $$  $$$| $$  | $$  | $$__  $$| $$__/    \____  $$      | $$__  $$   | $$   | $$__  $$| $$__  $$ \____  $$|__/|__/
| $$\  $$$| $$  | $$      | $$\  $ | $$  | $$  | $$  \ $$| $$       /$$  \ $$      | $$  | $$   | $$   | $$  \ $$| $$  | $$ /$$  \ $$
| $$ \  $$|  $$$$$$/      | $$ \/  | $$ /$$$$$$| $$  | $$| $$$$$$$$|  $$$$$$/      | $$  | $$   | $$   | $$  | $$| $$  | $$|  $$$$$$/ /$$ /$$
|__/  \__/ \______/       |__/     |__/|______/|__/  |__/|________/ \______/       |__/  |__/   |__/   |__/  |__/|__/  |__/ \______/ |__/|__/



                         __    _
                    _wr""        "-q__
                 _dP                 9m_
               _#P                     9#_
              d#@                       9#m
             d##                         ###
            J###                         ###L
            {###K                       J###K
            ]####K      ___aaa___      J####F
        __gmM######_  w#P""   ""9#m  _d#####Mmw__
     _g##############mZ_         __g##############m_
   _d####M@PPPP@@M#######Mmp gm#########@@PPP9@M####m_
  a###""          ,Z"#####@" '######"\g          ""M##m
 J#@"             0L  "*##     ##@"  J#              *#K
 #"               `#    "_gmwgm_~    dF               `#_
7F                 "#_   ]#####F   _dK                 JE
]                    *m__ ##### __g@"                   F
                       "PJ#####LP"
 `                       0######_                      '
                       _0########_
     .               _d#####^#####m__              ,
      "*w_________am#####P"   ~9#####mw_________w*"
          ""9@#####@M""           ""P@#####@M""


*/

fun createAnchorNode(
    engine: Engine,
    modelLoader: ModelLoader,
    materialLoader: MaterialLoader,
    anchor: Anchor
): AnchorNode {
    val anchorNode = AnchorNode(engine = engine, anchor = anchor)
    val modelNode = ModelNode(
        modelInstance = modelLoader.createModelInstance(kModelFile),
        // Scale to fit in a 0.5 meters cube
        scaleToUnits = 0.5f
    ).apply {
        // Model Node needs to be editable for independent rotation from the anchor rotation
        isEditable = true
        editableScaleRange = 0.2f..0.75f
    }
    val boundingBoxNode = CubeNode(
        engine,
        size = modelNode.extents,
        center = modelNode.center,
        materialInstance = materialLoader.createColorInstance(Color.White.copy(alpha = 0.5f))
    ).apply {
        isVisible = false
    }
    modelNode.addChildNode(boundingBoxNode)
    anchorNode.addChildNode(modelNode)

    listOf(modelNode, anchorNode).forEach {
        it.onEditingChanged = { editingTransforms ->
            boundingBoxNode.isVisible = editingTransforms.isNotEmpty()
        }
    }
    return anchorNode
}
fun landmarkToAnchorHitTest(
    landmark: NormalizedLandmark,
    frame: Frame,
    surfaceWidth: Int,
    surfaceHeight: Int,
): Coordenadas? {
    // Convertir coordenadas normalizadas a píxeles de pantalla
    val xPx = (landmark.x() * surfaceWidth)
    val yPx = (landmark.y()) * surfaceHeight

    var coordenadas: Coordenadas? = null
    Log.d("landmarkToAnchorHitTest", "x=$xPx y=$yPx")
    // Realizar hit test
    val hits: List<HitResult> = frame.hitTest(xPx, yPx)
    val hitResult =
        hits.firstOrNull {
            val trackable = it.trackable
            trackable is Plane || trackable is Point || trackable is DepthPoint
        }
    Log.d("landmarkToAnchorHitTest", "${hitResult?.hitPose}")
    //Tomar el primer resultado válido que intersecta un plano o una nube de puntos
    try {
        val pose = hitResult?.createAnchor()?.pose
        if (pose != null)
            coordenadas = Coordenadas(pose.tx(), pose.ty(), pose.tz())
    }
    catch (e: Exception){
        Log.d("landmarkToAnchorHitTest", "ERROR")
    }

    return coordenadas
}
/*
fun landmarkToWorldPos(
    landmark: NormalizedLandmark,
    frame: Frame,
    surfaceWidth: Int,
    surfaceHeight: Int,
    depthImage: Image
): Coordenadas? {
    // Convertir coordenadas normalizadas a píxeles de pantalla
    val xPx = (landmark.x() * surfaceWidth)
    val yPx = (1 - landmark.y()) * surfaceHeight
    val zM = getMeterDepth(depthImage, xPx.toInt(), yPx.toInt()) * -1

    var coordenadas: Coordenadas? = null
    Log.d("landmarkToAnchorHitTest", "x=$xPx y=$yPx")
    // Realizar hit test
    val hits: List<HitResult> = frame.hitTest(xPx, yPx)
    val hitResult =
        hits.firstOrNull {
            val trackable = it.trackable
            trackable is Plane || trackable is Point || trackable is DepthPoint
        }
    Log.d("landmarkToAnchorHitTest", "${hitResult?.hitPose}")
    //Tomar el primer resultado válido que intersecta un plano o una nube de puntos
    try {
        val pose = hitResult?.createAnchor()?.pose
        if (pose != null)
            coordenadas = Coordenadas(pose.tx(), pose.ty(), zM)
    }
    catch (e: Exception){
        Log.d("landmarkToAnchorHitTest", "ERROR")
    }

    return coordenadas
}
*/
fun landmarkToWorldPos(landmark: NormalizedLandmark, frame: Frame, depthImage: Image): Coordenadas{
    val textureIntrinsics = frame.camera.textureIntrinsics
    val rgbWidth = textureIntrinsics.imageDimensions[0]
    val rgbHeight = textureIntrinsics.imageDimensions[1]

    val depthWidth = depthImage.width
    val depthHeight = depthImage.height
    val xPxRGB = (landmark.x() * rgbWidth)
    val yPxRGB = ((landmark.y()) * rgbHeight)

    val xPxDepth = xPxRGB * depthWidth / rgbWidth
    val yPxDepth = yPxRGB * depthHeight / rgbHeight
    val zM = getMeterDepth(depthImage, xPxDepth.toInt(), yPxDepth.toInt())

    val intrinsics = frame.camera.imageIntrinsics
    val coordenadas3d = unproject(
        xPx = xPxDepth,
        yPx = yPxDepth,
        zM = zM,
        fx = intrinsics.focalLength[0],
        fy = intrinsics.focalLength[1],
        cx = intrinsics.principalPoint[0],
        cy = intrinsics.principalPoint[1]
    )
    coordenadas3d.z *= -1
    val cameraPose = frame.camera.pose
    val world = cameraPose.transformPoint(floatArrayOf(coordenadas3d.x, coordenadas3d.y, coordenadas3d.z))
    return Coordenadas(world[0], world[1], world[2])
}

/** Obtain the depth in millimeters for [depthImage] at coordinates ([x], [y]). */
fun getMeterDepth(depthImage: Image, x: Int, y: Int): Float {
    // The depth image has a single plane, which stores depth for each
    // pixel as 16-bit unsigned integers.
    val plane = depthImage.planes[0]
    val byteIndex = x * plane.pixelStride + y * plane.rowStride
    val buffer = plane.buffer.order(ByteOrder.nativeOrder())
    val depthSample = buffer.getShort(byteIndex)
    return depthSample / 1000f
}

fun unproject(
    xPx: Float,
    yPx: Float,
    zM: Float,
    fx: Float,
    fy: Float,
    cx: Float,
    cy: Float
): Coordenadas {
    val x = (xPx - cx) * zM / fx
    val y = (yPx - cy) * zM / fy
    val z = zM
    return Coordenadas(x, y, z)
}
fun frameToMPImage(frame: Frame): MPImage? {
    var mpImage: MPImage? = null
    try {
        frame.acquireCameraImage()?.use { arImage ->
            val bitmap = yuvToRgbBitmap(arImage)
            mpImage = BitmapImageBuilder(bitmap).build()
        }
    }
    catch (e: NotYetAvailableException){
    }

    return mpImage
}

fun yuvToRgbBitmap(image: Image): Bitmap {
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
    val jpegBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
}

fun computeTorsoCenter(
    p1: Float3, // hombro izquierdo
    p2: Float3, // hombro derecho
    p3: Float3, // cadera izquierda
    p4: Float3  // cadera derecha
): Float3 {

    val x = (p1.x + p2.x + p3.x + p4.x) / 4f
    val y = (p1.y + p2.y + p3.y + p4.y) / 4f
    val z = (p1.z + p1.z + p3.z + p4.z) / 4f

    return Float3(x, y, z)
}