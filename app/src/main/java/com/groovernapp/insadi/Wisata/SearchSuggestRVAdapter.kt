package com.groovernapp.insadi.Wisata

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.groovernapp.data.models.WisataData
import com.groovernapp.insadi.databinding.RvSearchSuggestBinding

class SearchSuggestRVAdapter(val onClickAction: (wisataName: String)->Unit): ListAdapter<WisataData?, RecyclerView.ViewHolder>(DiffCallback()) {
    class DiffCallback : DiffUtil.ItemCallback<WisataData?>() {
        override fun areItemsTheSame(oldItem: WisataData, newItem: WisataData): Boolean  = oldItem.wisataId == newItem.wisataId

        override fun areContentsTheSame(oldItem: WisataData, newItem: WisataData): Boolean = oldItem == newItem
    }

    class VHcontent(val vbind: RvSearchSuggestBinding): RecyclerView.ViewHolder(vbind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        VHcontent(RvSearchSuggestBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = holder.run {
        when(this){
            is VHcontent -> vbind.run {
                wisatadata = getItem(position)
                root.setOnClickListener {
                    onClickAction.invoke(getItem(position)?.namaWisata?:"")
                }
                executePendingBindings()
            }
        }
    }
}