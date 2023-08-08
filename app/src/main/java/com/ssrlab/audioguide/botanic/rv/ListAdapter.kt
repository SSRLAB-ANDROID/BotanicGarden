package com.ssrlab.audioguide.botanic.rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.db.ExhibitObject
import com.ssrlab.audioguide.botanic.vm.ExhibitViewModel

class ListAdapter(
    private val list: ArrayList<ExhibitObject>,
    private val viewModel: ExhibitViewModel,
    private val actionFromHeader: () -> Unit,
    private val actionFromItem: () -> Unit
): RecyclerView.Adapter<ListAdapter.ListHolder>() {

    inner class ListHolder(item: View): RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            (when (viewType) {
                0 -> R.layout.rv_header
                1 -> R.layout.rv_secondary
                else -> R.layout.rv_exhibit
            }),
            parent,
            false
        )
        return ListHolder(itemView)
    }

    override fun getItemCount(): Int = list.size + 2

    override fun onBindViewHolder(holder: ListHolder, position: Int) {
        if (position >= 2) {
            val itemView = holder.itemView
            itemView.findViewById<ImageView>(R.id.rv_item_image).load(list[position - 2].imagePreview) {
                crossfade(true)
                size(100, 100)
                transformations(RoundedCornersTransformation(10f))
            }
            itemView.findViewById<TextView>(R.id.rv_item_title).text = list[position - 2].placeName
            itemView.setOnClickListener {
                actionFromItem()
                viewModel.id.value = position - 2
            }
        } else if (position == 0) {
            val itemView = holder.itemView
            itemView.findViewById<ImageView>(R.id.rv_header_language).setOnClickListener { actionFromHeader() }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> 0
            1 -> 1
            else -> 2
        }
    }
}