package com.example.financeapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.example.financeapp.R
import com.example.financeapp.data.model.CardsClass
import com.example.financeapp.utils.Constance
import com.example.financeapp.utils.DateUtils
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.data.model.OperationClass
import com.example.financeapp.data.model.OperationClassWithDate
import com.example.financeapp.databinding.ItemsForDateBinding
import com.example.financeapp.utils.StringNames

class ItemsWithDateAdapter(var items: List<OperationClassWithDate>, private var categories: List<CategoryClass>, private var cards: List<CardsClass>) : RecyclerView.Adapter<ItemsWithDateAdapter.ItemsWithDateHolder>(){
    var onItemClick : ((OperationClass) -> Unit)? = null
    var onDeleteClick : ((OperationClass) -> Unit)? = null
    var onEditClick : ((OperationClass) -> Unit)? = null

    private val sharedBinderHelper = ViewBinderHelper().apply {
        setOpenOnlyOne(true)
    }

    private lateinit var stringNames : StringNames

    inner class ItemsWithDateHolder(view: View): RecyclerView.ViewHolder(view){

        val binding = ItemsForDateBinding.bind(view)
        private val innerAdapter = ItemsAdapter(categories, cards, sharedBinderHelper)

        fun bind(itemOperation: OperationClassWithDate, onItemClick : ((OperationClass) -> Unit)?) = with(binding){

            stringNames = StringNames(binding.root.context)

            val today = DateUtils.getCurrentDate()
            val yesterday = DateUtils.getYesterday()

            if (itemOperation.date == today)
                dateText.text = stringNames.TODAY_TITLE
            else if (itemOperation.date == yesterday)
                dateText.text = stringNames.YESTERDAY_TITLE
            else
                dateText.text = DateUtils.dateSQLtoString(itemOperation.date)

            if (itemsList.adapter == null) {
                itemsList.layoutManager = LinearLayoutManager(itemView.context)
                itemsList.adapter = innerAdapter
            }

            innerAdapter.updateCategories(categories)
            innerAdapter.updateCards(cards)
            innerAdapter.updateData(itemOperation.dataList)

            innerAdapter.onItemClick = { it ->
                onItemClick?.invoke(it)
            }
            innerAdapter.onDeleteClick = { it ->
                onDeleteClick?.invoke(it)
            }
            innerAdapter.onEditClick = { it ->
                onEditClick?.invoke(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsWithDateHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.items_for_date, parent, false)
        return ItemsWithDateHolder(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: ItemsWithDateHolder, position: Int) {
        holder.bind(items[position], onItemClick)
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
