package com.ludito.task.presentation.bookmark.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ludito.task.R
import com.ludito.task.databinding.ItemAddressBinding
import com.ludito.task.presentation.map.model.PlaceItem

class BookmarkRecyclerAdapter(val onItemClicked: (item: PlaceItem) -> Unit) :
    RecyclerView.Adapter<BookmarkRecyclerAdapter.BookmarkViewHolder>() {

    private val items: MutableList<PlaceItem> = ArrayList()

    fun setList(list: List<PlaceItem>) {
        items.clear()
        items.addAll(list)
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_address, parent, false)
        return BookmarkViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.initView(items[position])
    }

    inner class BookmarkViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val binding = ItemAddressBinding.bind(itemView)

        fun initView(item: PlaceItem) {
            binding.txtName.text = item.name
            binding.txtDescription.text = item.description
            binding.root.setOnClickListener { onItemClicked(item) }
        }
    }

}