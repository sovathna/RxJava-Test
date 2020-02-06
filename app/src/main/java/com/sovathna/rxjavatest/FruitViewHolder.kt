package com.sovathna.rxjavatest

import android.content.res.ColorStateList
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView

class FruitViewHolder(
    itemView: View,
    clickListener: ((Int) -> Unit)?,
    favoriteClickListener: ((Int) -> Unit)?
) :
    RecyclerView.ViewHolder(itemView) {

    private val context = itemView.context
    private val tvName = itemView.findViewById<AppCompatTextView>(R.id.tv_name)
    private val ivFavorite = itemView.findViewById<AppCompatImageView>(R.id.iv_favorite)

    init {
        itemView.setOnClickListener {
            clickListener?.invoke(adapterPosition)
        }
        ivFavorite.setOnClickListener {
            favoriteClickListener?.invoke(adapterPosition)
        }
    }

    fun bindView(fruit: Fruit) {
        tvName.text = fruit.name

        val color =
            when {
                fruit.favorite -> R.color.colorFavoriteActive
                else -> R.color.colorFavoriteDefault
            }

        ImageViewCompat.setImageTintList(
            ivFavorite,
            ColorStateList.valueOf(ContextCompat.getColor(context, color))
        )
    }

}