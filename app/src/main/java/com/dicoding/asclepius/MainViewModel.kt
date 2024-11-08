package com.dicoding.asclepius

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val currentImageUri = MutableLiveData<Uri?>()
}
