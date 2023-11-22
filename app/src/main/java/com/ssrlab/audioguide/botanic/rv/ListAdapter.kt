package com.ssrlab.audioguide.botanic.rv

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.ssrlab.audioguide.botanic.MainActivity
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.db.ExhibitObject
import com.ssrlab.audioguide.botanic.vm.ExhibitViewModel

class ListAdapter(
    private val list: ArrayList<ExhibitObject>,
    private val viewModel: ExhibitViewModel,
    private val actionFromItem: () -> Unit,
    private val activity: MainActivity,
    private val window: PopupWindow
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
                placeholder(R.drawable.ic_flower)
                scale(Scale.FIT)
                transformations(RoundedCornersTransformation(32f))
            }
            itemView.findViewById<TextView>(R.id.rv_item_title).text = list[position - 2].placeName.substringAfter(". ")
            itemView.setOnClickListener {
                actionFromItem()
                viewModel.id.value = position - 2
            }
        } else if (position == 0) {
            val itemView = holder.itemView
            val languageButton = itemView.findViewById<ImageView>(R.id.rv_header_language)
            languageButton.setOnClickListener { showLanguageList(languageButton) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> 0
            1 -> 1
            else -> 2
        }
    }

    @SuppressLint("InflateParams")
    private fun showLanguageList(itemView: View) {
        if (window.isShowing) window.dismiss()
        else {
            val view = LayoutInflater.from(activity).inflate(R.layout.layout_language, null)

            window.contentView = view
            val languageBe = view.findViewById<TextView>(R.id.lang_be)
            val languageRu = view.findViewById<TextView>(R.id.lang_ru)
            val languageEn = view.findViewById<TextView>(R.id.lang_en)

            setLanguageAction(languageBe, "be")
            setLanguageAction(languageRu, "ru")
            setLanguageAction(languageEn, "en")

            window.showAsDropDown(itemView)
        }
    }

    private fun setLanguageAction(view: View, locale: String) {
        view.setOnClickListener {
            activity.savePreferences(locale)
            window.dismiss()
        }
    }
}