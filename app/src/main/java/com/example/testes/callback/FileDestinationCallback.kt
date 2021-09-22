package com.example.testes.callback

import android.net.Uri

interface FileDestinationCallback {
    fun onSuccess(destinationUri: Uri)
    fun onError()
}