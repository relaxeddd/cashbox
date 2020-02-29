package ru.cashbox.android.ui.cash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_view_good_check.view.*
import ru.cashbox.android.R
import ru.cashbox.android.common.CheckListenerBillElements
import ru.cashbox.android.model.CheckItem
import java.util.*

class AdapterCheckItems(private val listener: CheckListenerBillElements? = null, private val isEditable: Boolean = true)
        : ListAdapter<CheckItem, AdapterCheckItems.ViewHolder>(DiffCallback()) {

    val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                listener?.onSwipe(getItem(viewHolder.adapterPosition))
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_view_good_check, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), listener, isEditable)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: CheckItem, listener: CheckListenerBillElements?, isEditable: Boolean) {
            with(itemView) {
                val discount = "${item.discount}%"
                val price = String.format(Locale.getDefault(), "%,.2f", item.price) + resources.getString(R.string.rub)
                val priceTotal = String.format(Locale.getDefault(), "%,.2f", item.getFinalPrice())

                text_good_check_title.text = item.billName
                text_good_check_count.text = item.count.toString()
                text_good_check_dicsount.text = discount
                text_good_check_price.text = price
                text_good_check_price_total.text = priceTotal
                container_good_check.setOnClickListener { listener?.onClick(item) }

                if (isEditable) {
                    image_good_check_minus.setOnClickListener { listener?.onMinus(item) }
                    image_good_check_plus.setOnClickListener { listener?.onPlus(item) }
                } else {
                    image_good_check_minus.visibility = View.GONE
                    image_good_check_plus.visibility = View.GONE
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<CheckItem>() {

        override fun areItemsTheSame(oldItem: CheckItem, newItem: CheckItem): Boolean {
            return oldItem.timestamp == newItem.timestamp && oldItem.billId == newItem.billId
        }

        override fun areContentsTheSame(oldItem: CheckItem, newItem: CheckItem): Boolean {
            return oldItem.count == newItem.count && oldItem.billName == newItem.billName && oldItem.elementType == newItem.elementType
                    && oldItem.price == newItem.price
        }
    }
}