package com.example.testes

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testes.adapter.ListAdapter
import com.example.testes.callback.FileDestinationCallback
import com.example.testes.databinding.ActivityHomeBinding
import com.example.testes.listener.OnPermissionGrantedListener

class HomeActivity : AppCompatActivity() {
    private val requestWritePermission = 101
    private lateinit var adapter: ListAdapter
    private var currentFileItem: FileItem? = null
    private var onPermissionGrantedListener: OnPermissionGrantedListener? = null

    private val startForResult = FileUtil.startForResult(this, object : FileDestinationCallback {
        override fun onSuccess(destinationUri: Uri) {
            currentFileItem?.apply {
                val activity = this@HomeActivity
                val msg = if (FileUtil.saveBase64File(activity, base64, destinationUri)) {
                    "$name salvo com sucesso"
                } else "Houve um erro ao salvar o arquivo"

                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
            }
        }

        override fun onError() {
            // Método não utilizado
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val list = mutableListOf(
            FileItem(this, "Zelda Wallpaper", "Jpeg", "image/jpeg"),
            FileItem(this, "Among Us", "Png", "image/png"),
            FileItem(this, "Documento", "Pdf", "application/pdf"),
            FileItem(this, "Documento 2", "Docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        )

        adapter = ListAdapter(list)
        adapter.setOnListItemClickListener { _: View, position: Int ->
            val fileItem = list[position]
            val options = arrayOf("Salvar", "Compartilhar")
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Escolha uma Opção")
            builder.setItems(options) { dialog, which ->
                dialog.dismiss()

                if (which == 0) {
                    checkStoragePermission { selectExternalStorageFolder(fileItem) }
                } else if (which == 1) {
                    fileItem.apply {
                        FileUtil.shareBase64File(this@HomeActivity, base64, name, mimeType)
                    }
                }
            }
            builder.show()
        }
        binding.list.adapter = adapter
        binding.list.layoutManager = LinearLayoutManager(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == requestWritePermission) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGrantedListener?.onPermissionGranted()
                return
            }
            Toast.makeText(this, "É necessário dar permissão para baixar o arquivo.", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkStoragePermission(listener: OnPermissionGrantedListener) {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val granted = PackageManager.PERMISSION_GRANTED
        if (ContextCompat.checkSelfPermission(this, permission) != granted) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestWritePermission)
            return
        }
        onPermissionGrantedListener = listener
        onPermissionGrantedListener?.onPermissionGranted()
    }

    private fun selectExternalStorageFolder(fileItem: FileItem) {
        val fileName = fileItem.name + FileUtil.getExtensionFromMimeType(fileItem.mimeType)
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        currentFileItem = fileItem
        startForResult.launch(intent)
    }
}