package com.example.financeapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.data.model.CardsClass
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.data.model.OperationClass
import com.example.financeapp.databinding.ItemStatisticAdmissionBinding
import com.example.financeapp.utils.CategoryIcons
import com.example.financeapp.utils.DateUtils
import com.example.financeapp.utils.MoneyFormatter
import com.example.financeapp.utils.StringNames

class ItemsOfAdmissionAdapter(
    private var categories: List<CategoryClass>,
    private var cards: List<CardsClass>
) : ListAdapter<OperationClass, ItemsOfAdmissionAdapter.MyViewHolder>(MyViewHolder.DiffCallback()) {
    class MyViewHolder(view: View): RecyclerView.ViewHolder(view){

        val binding = ItemStatisticAdmissionBinding.bind(view)

        fun bind(item: OperationClass, categories: List<CategoryClass>, cards: List<CardsClass>) = with(binding){

            val stringNames = StringNames(root.context)

            moneyText.text = MoneyFormatter.format(item.money)
            cardText.text = item.cardName

            val category = categories.find { it.UUID == item.categoryUUID }
            category?.let {
                categoryIcon.setImageResource(it.iconId)
                item.categoryName = it.title
            } ?: run {
                categoryIcon.setImageResource(CategoryIcons.getEmptyIcon())
                item.categoryName = stringNames.NOT_FOUND
            }
            categoryText.text = item.categoryName

            val card = cards.find { it.UUID == item.cardUUID }
            card?.let {
                item.cardName = it.title
            } ?: run {
                item.cardName = stringNames.NOT_FOUND
            }
            cardText.text = item.cardName

            operationDate.text = DateUtils.dateSQLtoString(item.date)
        }

        class DiffCallback : DiffUtil.ItemCallback<OperationClass>() {
            override fun areItemsTheSame(oldItem: OperationClass, newItem: OperationClass): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: OperationClass, newItem: OperationClass): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_statistic_admission, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position), categories, cards)
    }

    fun updateData(newItems: List<OperationClass>) {
        submitList(newItems)
    }

    fun updateCategories(newCategories: List<CategoryClass>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    fun updateCards(newCards: List<CardsClass>) {
        cards = newCards
        notifyDataSetChanged()
    }
}