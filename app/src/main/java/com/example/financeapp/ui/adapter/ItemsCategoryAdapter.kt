package com.example.financeapp.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.utils.Constance
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.databinding.ItemCategoryBinding
import com.example.financeapp.utils.CategoryIcons
import com.example.financeapp.utils.StringNames

class ItemsCategoryAdapter(
    private var items: List<CategoryClass>,
    private var colorId: String? = null,
    private val withTitle: Boolean = true
) : RecyclerView.Adapter<ItemsCategoryAdapter.CategoryHolder>() {
    var onItemClick: ((CategoryClass, Int) -> Unit)? = null
    var myPosition = 0

    inner class CategoryHolder(view: View) : RecyclerView.ViewHolder(view) {

        val binding = ItemCategoryBinding.bind(view)

        val colorWhite = ContextCompat.getColor(itemView.context, R.color.White)
        val colorTransparent = ContextCompat.getColor(itemView.context, R.color.LightGrey)

        private lateinit var stringNames: StringNames

        fun bind(itemCategory: CategoryClass, position: Int, withTitle: Boolean) = with(binding) {

            stringNames = StringNames(binding.root.context)

            if (colorId != null) {
                iconColor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colorId)))
                selected.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colorId)))
            } else {
                iconColor.setBackgroundTintList(ColorStateList.valueOf(colorTransparent))
                selected.setBackgroundTintList(ColorStateList.valueOf(colorTransparent))
            }

            val isSpecial =
                itemCategory.title == stringNames.ADD_TITLE || itemCategory.title == stringNames.MORE_TITLE

            if (!isSpecial && myPosition == position)
                selected.setBackgroundTintList(ColorStateList.valueOf(colorWhite))
            else {
                val color = (iconColor.backgroundTintList?.defaultColor ?: colorTransparent)
                selected.setBackgroundTintList(ColorStateList.valueOf(color))
            }

            icon.setColorFilter(colorWhite)

            if (itemCategory.title.isBlank() || !withTitle || isSpecial) {
                title.visibility = View.GONE
            } else {
                title.visibility = View.VISIBLE
                title.text = itemCategory.title
            }

            if (itemCategory.iconId == 0)
                icon.setImageResource(CategoryIcons.errorIcon)
            else
                icon.setImageResource(itemCategory.iconId)

            root.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(items[pos], pos)
                }
            }
        }
    }

    override fun onBindViewHolder(
        holder: CategoryHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            when (payloads[0] as Int) {
                1 -> {
                    val selectedColor =
                        ContextCompat.getColor(holder.itemView.context, R.color.White)
                    holder.binding.selected.setBackgroundTintList(
                        ColorStateList.valueOf(selectedColor)
                    )
                    myPosition = position
                }

                2 -> {
                    val color = (holder.binding.iconColor.backgroundTintList?.defaultColor
                        ?: R.color.LightGrey)
                    holder.binding.selected.setBackgroundTintList(ColorStateList.valueOf(color))
                }

                else -> super.onBindViewHolder(holder, position, payloads)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    fun setSelectedPosition(position: Int) {
        if (position != myPosition) {
            val oldPosition = myPosition
            myPosition = position
            notifyItemChanged(oldPosition, 2)
            notifyItemChanged(myPosition, 1)
        }
    }

    fun unselectPosition() {
        notifyItemChanged(myPosition, 2)
    }

    fun unselectAllPositions() {
        myPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        holder.bind(items[position], position, withTitle)
    }

    fun updateData(newItems: List<CategoryClass>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun updateColor(newColorId: String) {
        colorId = newColorId
        notifyDataSetChanged()
    }

    fun getCategories(): List<CategoryClass> {
        return items
    }
}