package ru.cashbox.android.ui.cash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_view_check.view.*
import ru.cashbox.android.R
import ru.cashbox.android.common.ClickListenerCheck
import ru.cashbox.android.model.Bill
import ru.cashbox.android.model.CheckStatus
import java.util.*

class AdapterChecks(private val listener: ClickListenerCheck) : ListAdapter<Bill, AdapterChecks.ViewHolder>(DiffCallback()) {

    var checkedCheck: Bill? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_view_check, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(getItem(position), item == checkedCheck, listener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Bill, isSelected: Boolean, listener: ClickListenerCheck) {
            with(itemView) {
                val price = String.format(Locale.getDefault(), "%,.2f", item.sum) + resources.getString(R.string.rub)

                text_check_number.text = resources.getString(R.string.number, item.id.toString())
                text_check_price.text = price
                text_check_description.text = buildDescription(item)
                image_check_status.setImageResource(when(item.status) {
                    CheckStatus.PAYED -> R.color.colorCheckPaid
                    CheckStatus.CLOSED -> R.color.colorCheckClosed
                    else -> R.color.colorCheckRevert
                })
                text_check_description.setTextColor(ContextCompat.getColor(context, if (isSelected) R.color.white else R.color.gray))
                container_check.setBackgroundColor(ContextCompat.getColor(context, if (isSelected) R.color.colorSelected else R.color.colorPrimary))
                container_check.setOnClickListener{
                    listener.onClick(item)
                }
            }
        }

        private fun buildDescription(item: Bill) : String {
            var description = ""
            item.items.forEach { description += it.billName + ", " }
            item.techmaps.forEach { description += it.billName + ", " }

            return if (description.length > 2) description.substring(0, description.length - 2) else ""
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Bill>() {

        override fun areItemsTheSame(oldItem: Bill, newItem: Bill): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Bill, newItem: Bill): Boolean {
            return oldItem == newItem
        }
    }
}