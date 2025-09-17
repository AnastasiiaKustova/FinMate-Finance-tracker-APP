package com.example.financeapp.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.example.financeapp.R
import com.example.financeapp.data.model.CardsClass
import com.example.financeapp.utils.Constance
import com.example.financeapp.data.model.CategoryClass
import com.example.financeapp.data.model.OperationClass
import com.example.financeapp.databinding.ItemTransactionBinding
import com.example.financeapp.utils.CategoryIcons
import com.example.financeapp.utils.CommentUtils

import com.example.financeapp.utils.MoneyFormatter
import com.example.financeapp.utils.StringNames
import java.util.Locale

class ItemsAdapter(
    private var categories: List<CategoryClass>,
    private var cards: List<CardsClass>,
    private val viewBinderHelper: ViewBinderHelper )  : ListAdapter<OperationClass, ItemsAdapter.MyViewHolder>(
    DiffCallback()
){
    var onItemClick : ((OperationClass) -> Unit)? = null
    var onDeleteClick : ((OperationClass) -> Unit)? = null
    var onEditClick : ((OperationClass) -> Unit)? = null

    private lateinit var stringNames : StringNames

    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view){

        val colorWhite60 = ContextCompat.getColor(itemView.context, R.color.White60)


        val binding = ItemTransactionBinding.bind(view)

        fun bind(itemOperation: OperationClass, categories: List<CategoryClass>) = with(binding){

            stringNames = StringNames(binding.root.context)

            viewBinderHelper.bind(swipeLayout, itemOperation.id.toString())

            val card = cards.find { it.UUID == itemOperation.cardUUID }
            card?.let {
                itemOperation.cardName = card.title
            } ?: run {
                itemOperation.cardName = stringNames.NOT_FOUND
            }

            when (itemOperation.type) {
                Constance.create -> itemForCreate(itemOperation)
                Constance.minus -> itemForMinus(itemOperation)
                else -> itemDefault(itemOperation, categories)
            }

            when (itemOperation.type) {
                Constance.admission, Constance.expense -> viewBinderHelper.unlockSwipe(itemOperation.id.toString())
                else -> viewBinderHelper.lockSwipe(itemOperation.id.toString())
            }

            layout.setOnClickListener {
                onItemClick?.invoke(itemOperation)
            }

            deleteBtn.setOnClickListener {
                onDeleteClick?.invoke(itemOperation)
            }
            editBtn.setOnClickListener {
                onEditClick?.invoke(itemOperation)
            }
        }

        fun itemForCreate(itemOperation: OperationClass) = with(binding){
            itemCategory.text = itemOperation.cardName
            itemMoney.text = "+ ${MoneyFormatter.format(itemOperation.money)}"
            itemName.text = stringNames.NEW_CARD_TITLE

            imageView.setImageResource(CategoryIcons.addCardIcon)
            imageView.setColorFilter(colorWhite60)
        }

        fun itemForMinus(itemOperation: OperationClass) = with(binding){

            itemCategory.text = "${stringNames.FROM} ${itemOperation.cardName.lowercase(Locale.getDefault())}"

            val cardTo = CommentUtils.decodeComment(itemOperation.comment)

            cardTo?.let { UUID ->
                val card = cards.find { it.UUID == UUID }
                card?.let {
                    itemCategory.text = itemCategory.text.toString() + " ${stringNames.TO} ${card.title.lowercase(Locale.getDefault())}"
                    itemOperation.cardToName = card.title
                }
            }

            itemMoney.text = MoneyFormatter.format(itemOperation.money)
            itemName.text = stringNames.MOVE_CARD_TITLE

            imageView.setImageResource(CategoryIcons.switchMoneyIcon)
            imageView.setColorFilter(colorWhite60)
        }

        fun itemDefault(itemOperation: OperationClass, categories: List<CategoryClass>) = with(binding){

            itemCard.text = itemOperation.cardName

            itemMoney.text = if (itemOperation.type == Constance.admission)
                "+ ${MoneyFormatter.format(itemOperation.money)}"
            else
                "- ${MoneyFormatter.format(itemOperation.money)}"

            val category = categories.find { it.UUID == itemOperation.categoryUUID }
            category?.let {
                if (!it.colorId.isNullOrEmpty()) {
                    iconColor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(it.colorId)))
                }
                imageView.setImageResource(it.iconId)
                itemOperation.categoryName = category.title
            }?: run {
                imageView.setImageResource(CategoryIcons.getEmptyIcon())
                itemOperation.categoryName = stringNames.NOT_FOUND
            }

            imageView.setColorFilter(colorWhite60)

            if (itemOperation.comment.isNullOrBlank()) {
                itemCategory.visibility = View.GONE
                itemName.text = itemOperation.categoryName
            } else {
                itemCategory.visibility = View.VISIBLE
                itemCategory.text = itemOperation.categoryName
                itemName.text = itemOperation.comment
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<OperationClass>() {
        override fun areItemsTheSame(oldItem: OperationClass, newItem: OperationClass): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: OperationClass, newItem: OperationClass): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position), categories)
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