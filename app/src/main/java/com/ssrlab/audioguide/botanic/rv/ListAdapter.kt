package com.ssrlab.audioguide.botanic.rv

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.db.ExhibitObject
import com.ssrlab.audioguide.botanic.vm.ExhibitViewModel

class ListAdapter(
    private val list: ArrayList<ExhibitObject>,
    private val context: Context,
    private val viewModel: ExhibitViewModel,
    private val actionFromHeader: () -> Unit,
    private val actionFromItem: () -> Unit
): RecyclerView.Adapter<ListAdapter.ListHolder>() {

    inner class ListHolder(item: View): RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            (if (viewType == 0) R.layout.rv_header
            else R.layout.rv_exhibit),
            parent,
            false
        )
        return ListHolder(itemView)
    }

    override fun getItemCount(): Int = list.size + 1

    override fun onBindViewHolder(holder: ListHolder, position: Int) {
        if (position >= 1) {
            val itemView = holder.itemView
            itemView.findViewById<ImageView>(R.id.rv_item_image).load(R.drawable.ic_image_plug) {
                crossfade(true)
                size(100, 100)
                transformations(RoundedCornersTransformation(10f))
            }
            itemView.findViewById<TextView>(R.id.rv_item_title).text = position.toString()
            itemView.setOnClickListener { actionFromItem() }
        } else if (position == 0) {
            val itemView = holder.itemView
            itemView.findViewById<ImageView>(R.id.rv_header_mail).setOnClickListener { actionFromHeader() }
            itemView.findViewById<ImageView>(R.id.rv_header_language).setOnClickListener { Toast.makeText(context, "Language", Toast.LENGTH_SHORT).show() }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0
        else 1
    }
}