package com.example.testes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.testes.databinding.ActivityHomeBinding
import java.io.BufferedReader
import java.io.InputStreamReader

class HomeActivity : AppCompatActivity() {
    private val requestWritePermission = 101
    private lateinit var fileName: String
    private lateinit var mimeType: String
    private lateinit var base64: String

    private val startForResult = FileUtil.startForResult(this, object : FileDestinationCallback {
        override fun onSuccess(destinationUri: Uri) {
            val msg = if (FileUtil.saveBase64File(this@HomeActivity, base64, destinationUri)) {
                "$fileName salvo com sucesso"
            } else "Houve um erro ao salvar o arquivo"

            Toast.makeText(this@HomeActivity, msg, Toast.LENGTH_LONG).show()
        }

        override fun onError() {
            // Método não utilizado
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val reader = BufferedReader(InputStreamReader(assets.open("base64file.txt")))
        val sb = StringBuilder()

        do {
            val line = reader.readLine()
            sb.append(line)
        } while (line != null)

        base64 = sb.toString()
        mimeType = "application/pdf"
        fileName = "dummy_pdf_file_www_2021-set-18.pdf"

        binding.tvDownloadFile.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    requestWritePermission
                )
                return@setOnClickListener
            }
            selectExternalStorageFolder(fileName)
        }

        binding.tvShareFile.setOnClickListener {
            FileUtil.shareBase64File(this, base64, fileName, mimeType)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == requestWritePermission) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectExternalStorageFolder(fileName)
                return
            }
            Toast.makeText(this, "É necessário dar permissão para baixar o arquivo.", Toast.LENGTH_LONG).show()
        }
    }

    private fun selectExternalStorageFolder(fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        startForResult.launch(intent)
    }
}