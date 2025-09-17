package com.example.financeapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.example.financeapp.R
import com.example.financeapp.data.model.CardsClass
import com.example.financeapp.databinding.ItemCardBinding
import com.example.financeapp.utils.Constance.ALL_TAG
import com.example.financeapp.utils.MoneyFormatter

class CardsAdapter(private var items: List<CardsClass>, private val tag:String? = null) : RecyclerView.Adapter<CardsAdapter.MyHolder>(){
    var onItemClick : ((CardsClass) -> Unit)? = null
    var onVisibilityToggle: ((CardsClass) -> Unit)? = null
    var onDeleteClick: ((CardsClass) -> Unit)? = null
    var onEditClick: ((CardsClass) -> Unit)? = null

    private val viewBinderHelper = ViewBinderHelper().apply {
        setOpenOnlyOne(true)
    }

    inner class MyHolder(view: View): RecyclerView.ViewHolder(view){

        val binding = ItemCardBinding.bind(view)

        fun bind(itemCard: CardsClass,
                 onItemClick : ((CardsClass) -> Unit)?,
                 onVisibilityToggle: ((CardsClass) -> Unit)?) = with(binding){

            viewBinderHelper.bind(swipeLayout, itemCard.id.toString())

            title.text = itemCard.title
            money.text = MoneyFormatter.format(itemCard.balance)

            if (tag == ALL_TAG){
                visibleBtnOnStart.visibility = View.VISIBLE
                visibleBtn.visibility = View.GONE
            }

            title.setOnClickListener {
                onItemClick?.invoke(itemCard)
            }

            deleteBtn.setOnClickListener {
                onDeleteClick?.invoke(itemCard)
            }

            editBtn.setOnClickListener {
                onEditClick?.invoke(itemCard)
            }

            visibleBtn.setImageResource(
                if (itemCard.visible == 0)
                    R.drawable.ic_visibility_off
                else
                    R.drawable.ic_visibility
            )

            visibleBtnOnStart.setOnClickListener {
                onVisibilityToggle?.invoke(itemCard)
            }

            visibleBtnOnStart.setImageResource(
                if (itemCard.visible == 0)
                    R.drawable.ic_visibility_off
                else
                    R.drawable.ic_visibility
            )

            visibleBtn.setOnClickListener {
                onVisibilityToggle?.invoke(itemCard)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return MyHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.bind(items[position], onItemClick, onVisibilityToggle)
    }

    fun updateData(newItems: List<CardsClass>) {
        items = newItems
        notifyDataSetChanged()
    }
}