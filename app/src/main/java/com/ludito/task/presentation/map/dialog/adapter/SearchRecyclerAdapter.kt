package com.ludito.task.presentation.map.dialog.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ludito.task.R
import com.ludito.task.databinding.ItemPlaceBinding
import com.ludito.task.presentation.map.model.PlaceItem

class SearchRecyclerAdapter(val onItemClicked: (item: PlaceItem) -> Unit) :
    RecyclerView.Adapter<SearchRecyclerAdapter.SearchViewHolder>() {

    private val items: MutableList<PlaceItem> = ArrayList()

    fun setList(list: List<PlaceItem>) {
        items.clear()
        items.addAll(list)
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return SearchViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.initView(items[position])
    }

    inner class SearchViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val binding = ItemPlaceBinding.bind(itemView)

        fun initView(item: PlaceItem) {
            binding.txtNamePlace.text = item.name
            binding.txtDescriptionPlace.text = item.description
            binding.txtDistancePlace.text = "${item.distance} m"

            binding.root.setOnClickListener { onItemClicked(item) }
        }
    }

}