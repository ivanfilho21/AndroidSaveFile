package com.example.testes.adapter.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.testes.FileItem
import com.example.testes.R
import com.example.testes.databinding.ViewHolderItemBinding
import com.example.testes.listener.OnListItemClickListener

class ListViewHolder(
    parent: ViewGroup,
    private val onListItemClickListener: OnListItemClickListener?,
    private val context: Context = parent.context,
    private val binding: ViewHolderItemBinding =
        ViewHolderItemBinding.inflate(LayoutInflater.from(context), parent, false)
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(fileItem: FileItem) {
        binding.apply {
            title.text = fileItem.name
            subtitle.text = fileItem.type
            setIcon(icon, fileItem.type)

            listItem.setOnClickListener { view ->
                onListItemClickListener?.onClick(view, adapterPosition)
            }
        }
    }

    private fun setIcon(icon: ImageView, fileType: String) {
        val drawableId: Int = when {
            fileType.equals("png", true) -> R.drawable.ic_baseline_image_24
            fileType.equals("jpeg", true) -> R.drawable.ic_baseline_image_24
            fileType.equals("jpg", true) -> R.drawable.ic_baseline_image_24
            fileType.equals("gif", true) -> R.drawable.ic_baseline_image_24
            fileType.equals("svg", true) -> R.drawable.ic_baseline_image_24
            fileType.equals("bmp", true) -> R.drawable.ic_baseline_image_24
            else -> R.drawable.ic_baseline_file_24
        }
        icon.setImageResource(drawableId)
    }
}