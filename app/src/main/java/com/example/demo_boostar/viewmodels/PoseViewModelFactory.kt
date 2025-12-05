package com.example.demo_boostar.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PoseViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PoseViewModel::class.java)) {
            return PoseViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}