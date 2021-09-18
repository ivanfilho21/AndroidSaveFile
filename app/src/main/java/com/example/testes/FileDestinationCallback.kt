package com.example.testes

import android.net.Uri

interface FileDestinationCallback {
    fun onSuccess(destinationUri: Uri)
    fun onError()
}