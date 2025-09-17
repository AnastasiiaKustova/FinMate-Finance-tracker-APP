package com.example.financeapp.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.databinding.ItemCategoryListBinding

class ItemsCategoryListAdapter(var items: List<CategoryClass>) : RecyclerView.Adapter<ItemsCategoryListAdapter.CategoryHolder>(){
    var onItemClick : ((CategoryClass, Int) -> Unit)? = null
    inner class CategoryHolder(view: View): RecyclerView.ViewHolder(view){

        val binding = ItemCategoryListBinding.bind(view)

        fun bind(category: CategoryClass, onItemClick : ((CategoryClass, Int) -> Unit)?) = with(binding){
            title.text = category.title
            if (!category.colorId.isNullOrEmpty())
                iconColor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(category.colorId)))
            icon.setImageResource(category.iconId)
            editBtn.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(category, pos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_list, parent, false)
        return CategoryHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    fun updateData(newItems: List<CategoryClass>) {
        items = newItems
        notifyDataSetChanged()
    }
}