package com.sergiogarcialopez.catalogo.ui.producto

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProductoViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Producto Fragment"
    }
    val text: LiveData<String> = _text
}