package com.example.financeapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.data.model.CardsClass
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.data.model.OperationClass
import com.example.financeapp.data.model.OperationClassWithDate
import com.example.financeapp.databinding.ItemsStatisticAdmissionBinding
import com.example.financeapp.utils.MoneyFormatter

class StatisticAdmissionAdapter(private var items: List<OperationClassWithDate>, private var categories: List<CategoryClass>, private var cards: List<CardsClass>) : RecyclerView.Adapter<StatisticAdmissionAdapter.MyViewHolder>(){

    var onItemClick : ((OperationClassWithDate) -> Unit)? = null

    inner class MyViewHolder(view: View, categories: List<CategoryClass>): RecyclerView.ViewHolder(view){

        val binding = ItemsStatisticAdmissionBinding.bind(view)
        private val innerAdapter = ItemsOfAdmissionAdapter(categories, cards)

        fun bind(itemOperation: OperationClassWithDate) = with(binding){
            dateText.text = itemOperation.date
            moneyText.text = MoneyFormatter.format(itemOperation.info.money ?: 0)

            if (itemsList.adapter == null) {
                itemsList.layoutManager = LinearLayoutManager(itemView.context)
                itemsList.adapter = innerAdapter
            }

            innerAdapter.updateData(itemOperation.dataList)
            innerAdapter.updateCategories(categories)
            innerAdapter.updateCards(cards)

            layout.setOnClickListener {
               onItemClick?.invoke(itemOperation)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.items_statistic_admission, parent, false)
        return MyViewHolder(view, categories)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun updateItems(newItems: List<OperationClassWithDate>) {
        items = newItems
        notifyDataSetChanged()
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


