package com.example.demo_boostar.viewmodels



import androidx.lifecycle.ViewModel




class ARViewModel(): ViewModel() {

}

/*
    Scene(
        modifier = Modifier.fillMaxSize(),
        engine = engine,

        // Core rendering components
        view = rememberView(engine),
        renderer = rememberRenderer(engine),
        scene = rememberScene(engine),

        // Asset loaders
        modelLoader = modelLoader,
        materialLoader = materialLoader,
        environmentLoader = environmentLoader,
        mainLightNode = rememberMainLightNode(engine){
            intensity = 100_000f
        },

        // Configure camera position
        cameraNode = rememberCameraNode(engine) {
            position = Position(z = 4.0f)
        },

        // Enable user interaction with the camera
        cameraManipulator = rememberCameraManipulator(),

        // Add 3D models and objects to the scene
        childNodes = rememberNodes {
            // Add a glTF model
            add(
                ModelNode(
                    // Create a single instance model from assets file
                    modelInstance = modelLoader.createModelInstance(
                        assetFileLocation = "models/prueba_cloth_Jordi.glb"
                    ),
                    // Make the model fit into a 1 unit cube
                    scaleToUnits = 1.0f
                )
            )


        },

        // Handle user interactions
        onGestureListener = rememberOnGestureListener(
            onDoubleTapEvent = { event, tappedNode ->
                tappedNode?.let { it.scale *= 2.0f }
            }
        ),
        // Frame update callback
        onFrame = { frameTimeNanos ->

        }
    )
    */
