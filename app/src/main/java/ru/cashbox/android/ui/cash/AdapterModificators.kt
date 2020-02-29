package ru.cashbox.android.ui.cash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.clear
import coil.api.load
import kotlinx.android.synthetic.main.item_view_modificator.view.*
import ru.cashbox.android.R
import ru.cashbox.android.common.ClickListenerModificators
import ru.cashbox.android.common.GONE
import ru.cashbox.android.common.IMG_PATH
import ru.cashbox.android.model.TechMapModificator
import kotlin.math.roundToInt

class AdapterModificators(private val apiAddress: String?) : ListAdapter<TechMapModificator, AdapterModificators.ViewHolder>(DiffCallback()) {

    val selectedModificators = ArrayList<TechMapModificator>()
    private val ixsWithTitle = ArrayList<Int>()

    val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val item = getItem(viewHolder.adapterPosition)

            if (getModificatorGroupCount(selectedModificators, item.groupId) > 1) {
                if (item.count.roundToInt() > 1) {
                    item.count -= 1
                } else {
                    item.count = 0.0
                    selectedModificators.remove(item)
                }
            }
            notifyDataSetChanged()
        }
    }

    private val itemClickListener = object: ClickListenerModificators {
        override fun onClick(item: TechMapModificator) {
            val groupModificatorsCount = getModificatorGroupCount(selectedModificators, item.groupId)

            if (groupModificatorsCount < item.maxModificatorsCount) {
            //if (!selectedModificators.contains(item) || item.count.roundToInt() < 3) {
                var modificatorToRemove: TechMapModificator? = null
                var oldCount = 0.0
                var isTheSameItem = false

                /*for (modificator in selectedModificators) {
                    if (modificator.groupId == item.groupId && item.id != -1L) {
                        modificatorToRemove = modificator
                        oldCount = modificatorToRemove.count
                        break
                    }
                }*/
                /*isTheSameItem = item.id == modificatorToRemove?.id
                if (modificatorToRemove != null && modificatorToRemove.count.roundToInt() <= 1 || item.id == modificatorToRemove?.id) {
                    selectedModificators.remove(modificatorToRemove)
                }*/
                if (item.count.roundToInt() == 0) {
                    item.count = 1.0
                } else {
                    item.count += 1
                }
                /*if (isTheSameItem && oldCount.roundToInt() == 0) {
                    item.count = 2.0
                } else if (isTheSameItem) {
                    item.count += 1
                }*/
                //item.count += if (oldCount.roundToInt() == 0) 2 else oldCount.roundToInt() + 1
                if (!selectedModificators.contains(item)) {
                    selectedModificators.add(item)
                }
                notifyDataSetChanged()
            }
        }
    }

    override fun submitList(list: List<TechMapModificator>?) {
        val items = ArrayList<TechMapModificator>(list ?: ArrayList())
        var itemsLength = items.size

        for (i in 0 until itemsLength) {
            val item = items[i]

            if (i > 0 && i + 1 < itemsLength && items[i + 1].groupId != item.groupId && (i + 1) % 3 != 0) {
                items.add(i + 1, TechMapModificator(-1L, null, "", item.groupId,
                    item.groupName, item.count, item.maxModificatorsCount))
                itemsLength++
            }
        }

        val isInitSelected = selectedModificators.isEmpty()

        ixsWithTitle.clear()
        for ((ix, item) in items.withIndex()) {
            if (ix == 0 || items[ix - 1].groupId != item.groupId) {
                ixsWithTitle.add(ix)
                if (isInitSelected) {
                    item.count = 1.0
                    selectedModificators.add(item)
                }
            }
        }

        super.submitList(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_view_modificator, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(ixsWithTitle.contains(position), getItem(position), apiAddress ?: "", selectedModificators, itemClickListener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(isShowGroupTitle: Boolean, item: TechMapModificator, apiAddress: String, selectedModificators: List<TechMapModificator>, listener: ClickListenerModificators) {
            with(itemView) {
                if (item.id != -1L) {
                    val background = ContextCompat.getColor(context, if (selectedModificators.contains(item)) R.color.gray_select else R.color.colorTransparent)

                    image_modificator_image.visibility = View.VISIBLE
                    image_modificator_image.clear()
                    if (item.imageUrl != null) {
                        image_modificator_image.load(apiAddress + IMG_PATH + item.imageUrl) {
                            placeholder(R.color.colorAccent)
                            error(R.color.colorAccent)
                        }
                    } else {
                        image_modificator_image.setImageResource(R.color.colorAccent)
                    }
                    check_box_modificator.isChecked = item.count.roundToInt() > 0
                    if (item.count.roundToInt() <= 1) {
                        text_modificator_count.visibility = View.GONE
                    } else {
                        text_modificator_count.visibility = View.VISIBLE
                        text_modificator_count.text = item.count.roundToInt().toString()
                    }
                    //container_modificator.setBackgroundColor(background)
                    text_modificator_group_title.text = if (isShowGroupTitle) {
                        item.groupName + " (" + item.maxModificatorsCount + ")"
                    } else ""
                    text_modificator_title.text = item.name
                    setOnClickListener {
                        listener.onClick(item)
                    }
                } else {
                    val background = ContextCompat.getColor(context, R.color.colorTransparent)

                    container_modificator.setBackgroundColor(background)
                    image_modificator_image.visibility = View.INVISIBLE
                    text_modificator_title.text = ""
                    text_modificator_group_title.text = ""
                    setOnClickListener {}
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<TechMapModificator>() {

        override fun areItemsTheSame(oldItem: TechMapModificator, newItem: TechMapModificator): Boolean {
            return oldItem.id == newItem.id && oldItem.groupId == newItem.groupId
        }

        override fun areContentsTheSame(oldItem: TechMapModificator, newItem: TechMapModificator): Boolean {
            return oldItem.name == newItem.name && oldItem.imageUrl == newItem.imageUrl && oldItem.groupName == newItem.groupName
        }
    }

    companion object {
        private fun getModificatorGroupCount(selectedModificators: List<TechMapModificator>, groupId: Long) : Int {
            var count = 0

            for (modificator in selectedModificators) {
                if (modificator.groupId == groupId) count += modificator.count.roundToInt()
            }

            return count
        }
    }
}