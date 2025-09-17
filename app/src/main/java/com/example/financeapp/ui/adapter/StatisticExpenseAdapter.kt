package com.example.financeapp.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.data.model.OperationClassWithDate
import com.example.financeapp.data.model.PlanningClass
import com.example.financeapp.databinding.ItemStatisticBinding
import com.example.financeapp.utils.MoneyFormatter
import com.example.financeapp.utils.StringNames

class StatisticExpenseAdapter(
    var items: List<OperationClassWithDate>,
    private var categories: List<CategoryClass>,
    private var plannings: List<PlanningClass>,
    private var money: Int,
    private var selectedCategoryUUID: String = ""
) : RecyclerView.Adapter<StatisticExpenseAdapter.CategoryHolder>(){
    var onItemClick : ((OperationClassWithDate) -> Unit)? = null
    inner class CategoryHolder(view: View): RecyclerView.ViewHolder(view){

        val binding = ItemStatisticBinding.bind(view)
        lateinit var stringNames: StringNames

        fun bind(itemOperation: OperationClassWithDate, categories: List<CategoryClass>, money: Int, onItemClick : ((OperationClassWithDate) -> Unit)?) = with(binding){

            if (selectedCategoryUUID != "" && itemOperation.info.categoryUUID != selectedCategoryUUID)
                itemLayout.visibility = View.GONE
            else
                itemLayout.visibility = View.VISIBLE

            stringNames = StringNames(root.context)

            val percent = if (money != 0) {
                (itemOperation.info.money?.toFloat() ?: 0f) / money * 100
            } else 0f

            if (percent < 1)
                itemPercent.text = "<\u202f1\u202f%"
            else
                itemPercent.text = String.format("%.0f", percent) + "\u202f%"
            itemMoney.text = MoneyFormatter.format(itemOperation.info.money ?: 0)


            val category = categories.find { it.UUID == itemOperation.info.categoryUUID }
            category?.let {
                if (it.colorId.isNotEmpty())
                    iconColor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(it.colorId)))
                imageView.setImageResource(it.iconId)

                itemOperation.info.categoryName = it.title
            }
            itemCategory.text = itemOperation.info.categoryName
            imageView.setColorFilter(ContextCompat.getColor(itemView.context, R.color.White60))


            val planning = plannings.filter { it.categoryUUID == itemOperation.info.categoryUUID  }
            for (p in planning){
                itemOperation.info.planningMoney = (itemOperation.info.planningMoney?: 0) + p.planningValue
            }

            if (itemOperation.info.planningMoney == 0 || itemOperation.info.planningMoney == null)
                itemPlanning.visibility = View.GONE
            else
            {
                itemPlanning.text = "${stringNames.OF} ${MoneyFormatter.format(itemOperation.info.planningMoney ?: 0)}"
                if (itemOperation.info.planningMoney ?: 0 < itemOperation.info.money ?: 0)
                    itemPlanning.setTextColor(ContextCompat.getColor(itemView.context, R.color.Red))
            }

            layout.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(items[pos])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_statistic, parent, false)
        return CategoryHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        holder.bind(items[position], categories, money, onItemClick)
    }

    fun updateItems(newItems: List<OperationClassWithDate>, newMoney: Int) {
        items = newItems
        money = newMoney
        notifyDataSetChanged()
    }

    fun updateCategories(newCategories: List<CategoryClass>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    fun updatePlaning(newPlanning: List<PlanningClass>){
        plannings = newPlanning
        notifyDataSetChanged()
    }

    fun updateSelectedCategoryUUID(newSelectedCategoryUUID: String){
        selectedCategoryUUID = newSelectedCategoryUUID
        notifyDataSetChanged()
    }
}