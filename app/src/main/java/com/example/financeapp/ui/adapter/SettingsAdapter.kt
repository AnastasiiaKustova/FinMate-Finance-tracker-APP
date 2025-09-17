package com.example.financeapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.data.model.SettingsItemClass
import com.example.financeapp.databinding.ItemSettingsBinding

class SettingsAdapter(private var items: List<SettingsItemClass>) : RecyclerView.Adapter<SettingsAdapter.MyViewHolder>(){
    var onItemClick : ((SettingsItemClass) -> Unit)? = null
    private val selectedItems = mutableSetOf<SettingsItemClass>()

    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        val binding = ItemSettingsBinding.bind(view)
        fun bind(item: SettingsItemClass) = with(binding){
            titleText.text = item.title

            if (selectedItems.contains(item)) {
                titleText.setBackgroundResource(R.drawable.rounded_shape_selected)
            } else {
                titleText.setBackgroundResource(R.drawable.rounded_shape)
            }

            layout.setOnClickListener {
                if (selectedItems.contains(item)) {
                    selectedItems.remove(item)
                } else {
                    selectedItems.add(item)
                }
                notifyItemChanged(adapterPosition)

                onItemClick?.invoke(item)
            }
        }
    }

    fun getSelectedItems(): List<SettingsItemClass> = selectedItems.toList()

    fun updateSelectedItems(newItems: List<SettingsItemClass>){
        selectedItems.addAll(newItems)
        notifyDataSetChanged()
    }

    fun resetSelectedItems(){
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun updateItems(newItems: List<SettingsItemClass>){
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_settings, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(items[position])
    }
}