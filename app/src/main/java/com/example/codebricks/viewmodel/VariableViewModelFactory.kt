package com.example.codebricks.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VariableViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VariableViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VariableViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 