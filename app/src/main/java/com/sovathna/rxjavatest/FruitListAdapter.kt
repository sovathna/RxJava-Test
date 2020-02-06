package com.sovathna.rxjavatest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter

class FruitListAdapter : ListAdapter<Fruit, FruitViewHolder>(Fruit.ITEM_CALLBACK) {

    private var itemClickListener: ((Int, Fruit) -> Unit)? = null
    private var favoriteItemClickListener: ((Int, Fruit) -> Unit)? = null

    fun setItemClickListener(itemClickListener: ((Int, Fruit) -> Unit)?) {
        this.itemClickListener = itemClickListener
    }

    fun setFavoriteItemClickListener(favoriteItemClickListener: ((Int, Fruit) -> Unit)?) {
        this.favoriteItemClickListener = favoriteItemClickListener
    }

    private fun onItemClick(position: Int) {
        itemClickListener?.invoke(position, getItem(position))
    }

    private fun onFavoriteItemClick(position: Int) {
        favoriteItemClickListener?.invoke(position, getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        FruitViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.view_holder_fruit, parent, false),
            this::onItemClick,
            this::onFavoriteItemClick
        )

    override fun onBindViewHolder(holder: FruitViewHolder, position: Int) {
        holder.bindView(getItem(position))
    }
}