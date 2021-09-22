package com.example.testes.listener

import android.view.View

fun interface OnListItemClickListener {
    fun onClick(view: View, position: Int)
}