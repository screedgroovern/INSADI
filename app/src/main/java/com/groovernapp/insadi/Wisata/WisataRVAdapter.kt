package com.groovernapp.insadi.Wisata

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.groovernapp.MyUtils.MyUtils.visibleOrGone
import com.groovernapp.MyUtils.gvaFunctions.intentToGmaps
import com.groovernapp.data.models.WisataData
import com.groovernapp.insadi.MainView
import com.groovernapp.insadi.databinding.RvSpaceBinding
import com.groovernapp.insadi.databinding.RvWisataBinding

class WisataRVAdapter(val act: Activity, val adminView: Boolean): ListAdapter<WisataData?, RecyclerView.ViewHolder>(DiffCallback()) {
    class DiffCallback : DiffUtil.ItemCallback<WisataData?>() {
        override fun areItemsTheSame(oldItem: WisataData, newItem: WisataData): Boolean  = oldItem.wisataId == newItem.wisataId

        override fun areContentsTheSame(oldItem: WisataData, newItem: WisataData): Boolean = oldItem == newItem
    }

    class VHcontent(val vbind: RvWisataBinding): RecyclerView.ViewHolder(vbind.root)
    class VHfooter(val vbind: RvSpaceBinding): RecyclerView.ViewHolder(vbind.root)

    val CONTENT = 2; val FOOTER = 33

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == FOOTER){
            VHfooter(RvSpaceBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }else VHcontent(RvWisataBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = holder.run {
        when(this){
            is VHcontent -> vbind.run {
                wisatadata = getItem(position)
                root.setOnClickListener {
                    (act as MainView).showDetailWisata(getItem(position))
                }
                RVWisataBTdirection.setOnClickListener {
                    getItem(position)?.koordinat.intentToGmaps(it.context)
                }
                executePendingBindings()
            }
        }
    }

    override fun getItemViewType(position: Int): Int = if (position == itemCount-1) FOOTER else CONTENT
}