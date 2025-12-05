package com.example.demo_boostar.viewmodels



import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.demo_boostar.utils.PermissionStatus

class PermissionViewModel: ViewModel() {
    private val _permissionsStatus = mutableStateOf<Map<String, PermissionStatus>>(emptyMap())
    val permissionsStatus: State<Map<String, PermissionStatus>> = _permissionsStatus

    fun updatePermissionStatus(permission: String, status: PermissionStatus) {
        _permissionsStatus.value = _permissionsStatus.value.toMutableMap().apply {
            this[permission] = status
        }
    }
}