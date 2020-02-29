package ru.cashbox.android.ui.cash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.clear
import coil.api.load
import kotlinx.android.synthetic.main.item_view_good_select.view.*
import ru.cashbox.android.R
import ru.cashbox.android.common.ClickListenerElements
import ru.cashbox.android.common.IMG_PATH
import ru.cashbox.android.common.getLetterImage
import ru.cashbox.android.model.Element

class AdapterElements(private val apiAddress: LiveData<String>, private val listener: ClickListenerElements)
        : ListAdapter<Element, AdapterElements.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_view_good_select, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), apiAddress.value ?: "", listener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            private var openAnimBlock = false
        }

        init {
            openAnimBlock = false
        }

        fun bind(item: Element, apiAddress: String, listener: ClickListenerElements) {
            with(itemView) {
                val animation = AnimationUtils.loadAnimation(context, R.anim.pulse)

                image_good_select_image.clear()
                if (item.imageUrl != null) {
                    image_good_select_image.load(apiAddress + IMG_PATH + item.imageUrl) {
                        placeholder(R.color.colorAccent)
                        error(R.color.colorAccent)
                    }
                } else {
                    image_good_select_image.setImageBitmap(getLetterImage(context, item.name))
                }
                text_good_select_title.text = item.name
                setOnClickListener{
                    if (!openAnimBlock) {
                        openAnimBlock = true
                        animation.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation) {}
                            override fun onAnimationRepeat(animation: Animation) {}
                            override fun onAnimationEnd(animation: Animation) {
                                openAnimBlock = false
                                listener.onClick(item)
                            }
                        })
                        image_good_select_image.startAnimation(animation)
                    }
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