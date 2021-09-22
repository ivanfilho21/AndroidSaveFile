package com.example.testes

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class FileItem(private val context: Context, val name: String, val type: String, val mimeType: String) {
    var base64: String = ""

    init {
        base64 = getBase64FromFile(context, name)
    }

    private fun getBase64FromFile(context: Context, name: String): String {
        val reader = BufferedReader(InputStreamReader(context.assets.open("$name.txt")))
        val sb = StringBuilder()

        do {
            val line = reader.readLine()
            sb.append(line)
        } while (line != null)

        return sb.toString()
    }
}