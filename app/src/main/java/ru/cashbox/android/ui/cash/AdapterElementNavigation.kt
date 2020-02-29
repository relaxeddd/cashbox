package ru.cashbox.android.ui.cash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_view_good_navigation.view.*
import ru.cashbox.android.R
import ru.cashbox.android.common.ClickListenerElements
import ru.cashbox.android.model.Element

class AdapterElementNavigation(private val listener: ClickListenerElements) : ListAdapter<Element, AdapterElementNavigation.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_view_good_navigation, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Element, listener: ClickListenerElements) {
            with(itemView) {
                button_good_category.text = item.name
                button_good_category.setOnClickListener{
                    listener.onClick(item)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Element>() {

        override fun areItemsTheSame(oldItem: Element, newItem: Element): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Element, newItem: Element): Boolean {
            return oldItem.name == newItem.name && oldItem.elementType == newItem.elementType && oldItem.imageUrl == newItem.imageUrl
        }
    }
}