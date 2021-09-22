package com.example.testes.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.testes.FileItem
import com.example.testes.adapter.viewholder.ListViewHolder
import com.example.testes.listener.OnListItemClickListener

class ListAdapter(private val list: List<FileItem>) : RecyclerView.Adapter<ListViewHolder>() {
    private var onClickListener: OnListItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(parent, onClickListener)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnListItemClickListener(listener: OnListItemClickListener) {
        onClickListener = listener
    }
}