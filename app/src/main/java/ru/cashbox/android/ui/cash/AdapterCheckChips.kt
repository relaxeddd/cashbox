package ru.cashbox.android.ui.cash

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_view_check_chip.view.*
import ru.cashbox.android.R
import ru.cashbox.android.common.ClickListenerCheck
import ru.cashbox.android.model.Bill

class AdapterCheckChips(private val listener: ClickListenerCheck) : ListAdapter<Bill, AdapterCheckChips.ViewHolder>(DiffCallback()) {

    var checkedCheck: Bill? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_view_check_chip, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(getItem(position), item == checkedCheck, listener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Bill, isSelected: Boolean, listener: ClickListenerCheck) {
            with(itemView) {
                chip_check.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_16))
                chip_check.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, if (isSelected) R.color.blue else R.color.colorPrimary))
                chip_check.text = resources.getString(R.string.check_number, item.id.toString())
                chip_check.setOnClickListener{
                    listener.onClick(item)
                }
            }
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