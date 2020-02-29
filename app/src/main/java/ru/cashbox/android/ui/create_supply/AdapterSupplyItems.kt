package ru.cashbox.android.ui.create_supply

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_view_buy_record.view.*
import ru.cashbox.android.R
import ru.cashbox.android.common.ListenerBuyRecord
import ru.cashbox.android.model.BuyRecord
import java.util.*

class AdapterSupplyItems(private val listener: ListenerBuyRecord? = null) : ListAdapter<BuyRecord, AdapterSupplyItems.ViewHolder>(DiffCallback()) {

    val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            listener?.onSwipe(getItem(viewHolder.adapterPosition))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_view_buy_record, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: BuyRecord, listener: ListenerBuyRecord?) {
            with(itemView) {
                val price = String.format(Locale.getDefault(), "%,.2f", item.price)
                val priceTotal = String.format(Locale.getDefault(), "%,.2f", (item.count * item.price))

                text_buy_record_title.text = item.name
                text_buy_record_amount.text = item.count.toString()
                text_buy_record_price.text = price
                text_buy_record_price_total.text = priceTotal
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<BuyRecord>() {

        override fun areItemsTheSame(oldItem: BuyRecord, newItem: BuyRecord): Boolean {
            return oldItem.timestamp == newItem.timestamp && oldItem.itemId == newItem.itemId && oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BuyRecord, newItem: BuyRecord): Boolean {
            return oldItem.count == newItem.count && oldItem.name == newItem.name && oldItem.price == newItem.price
                    && oldItem.type == newItem.type
        }
    }
}