package com.sovathna.rxjavatest

import androidx.recyclerview.widget.DiffUtil

data class Fruit(
    val name: String,
    val favorite: Boolean = false
) {
    companion object {
        val ITEM_CALLBACK = object : DiffUtil.ItemCallback<Fruit>() {
            override fun areItemsTheSame(oldItem: Fruit, newItem: Fruit) =
                oldItem.name == newItem.name

            override fun areContentsTheSame(oldItem: Fruit, newItem: Fruit) =
                oldItem == newItem
        }
    }
}