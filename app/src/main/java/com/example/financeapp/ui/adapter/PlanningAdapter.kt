package com.example.financeapp.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.data.model.PlanningClass
import com.example.financeapp.databinding.ItemsPlanningBinding
import com.example.financeapp.utils.DateUtils
import com.example.financeapp.utils.MoneyFormatter

class PlanningAdapter(private var items: List<PlanningClass>, private var categories: List<CategoryClass>) : RecyclerView.Adapter<PlanningAdapter.MyViewHolder>(){
    var onItemClick : ((PlanningClass) -> Unit)? = null
    class MyViewHolder(view: View): RecyclerView.ViewHolder(view){

        val binding = ItemsPlanningBinding.bind(view)

        fun bind(item: PlanningClass, categories: List<CategoryClass>, onItemClick : ((PlanningClass) -> Unit)?) = with(binding){

            datePeriod.text = DateUtils.dateForLabel(item.dateStart, item.dateEnd)

            planningValue.text = MoneyFormatter.format(item.planningValue)
            binding.spentValue.text = MoneyFormatter.format(item.spentValue)
            binding.remainderValue.text = MoneyFormatter.format(item.planningValue - item.spentValue)
            val procent = if (item.planningValue > 0) {
                (item.spentValue.toFloat() / item.planningValue * 100).toInt()
            } else 0
            binding.progressBar.progress = procent


            val category = categories.find { it.UUID == item.categoryUUID }
            category?.let {
                categoryIcon.setImageResource(it.iconId)
                item.categoryName = it.title
                if (it.colorId.isNotEmpty())
                    binding.progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor(it.colorId)))
            }

            categoryName.text = item.categoryName

            binding.layout.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.items_planning, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(items[position], categories, onItemClick)
    }

    fun updateItems(newItems: List<PlanningClass>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun updateCategories(newCategories: List<CategoryClass>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}