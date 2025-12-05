package com.example.demo_boostar.ui.screens

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demo_boostar.utils.PermissionStatus
import com.example.demo_boostar.viewmodels.PermissionViewModel


@Composable
fun PantallaPermisos(navigateToHome : () -> Unit){
    val activity = LocalContext.current
    val viewModel = viewModel<PermissionViewModel>()
    val permissions = listOf(
        Manifest.permission.CAMERA,

    )
    val permissionsStatus = viewModel.permissionsStatus.value
    var alreadyRequested by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String, Boolean> ->
        permissions.forEach { permission ->
            val granted = result[permission] ?: false
            val status = when {
                granted -> PermissionStatus.Granted
                ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity, permission) -> PermissionStatus.Denied
                else -> PermissionStatus.PermanentlyDenied
            }
            viewModel.updatePermissionStatus(permission, status)
        }
    }
    LaunchedEffect(Unit) {
        if (!alreadyRequested) {
            alreadyRequested = true
            launcher.launch(permissions.toTypedArray())
        }
    }

    if (permissions.all { permissionsStatus[it] == PermissionStatus.Granted }) navigateToHome()
}