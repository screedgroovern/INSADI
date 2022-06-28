package com.groovernapp.insadi.Admin

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.groovernapp.MyUtils.gvaDialogs
import com.groovernapp.data.models.UserData
import com.groovernapp.insadi.databinding.RvAdminBinding
import com.groovernapp.insadi.databinding.RvSpaceBinding

class AdminRVAdapter(val act: Activity): ListAdapter<UserData?, RecyclerView.ViewHolder>(DiffCallback()) {
    class DiffCallback : DiffUtil.ItemCallback<UserData?>() {
        override fun areItemsTheSame(oldItem: UserData, newItem: UserData): Boolean  = oldItem.uid == newItem.uid

        override fun areContentsTheSame(oldItem: UserData, newItem: UserData): Boolean = oldItem == newItem
    }

    class VHcontent(val vbind: RvAdminBinding): RecyclerView.ViewHolder(vbind.root)
    class VHfooter(val vbind: RvSpaceBinding): RecyclerView.ViewHolder(vbind.root)

    val CONTENT = 2; val FOOTER = 33

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == FOOTER){
            VHfooter(RvSpaceBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }else VHcontent(RvAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = holder.run {
        when(this){
            is VHcontent -> vbind.run {
                userdata = getItem(position)
                RVAdminBTedit.setOnClickListener {
                    (act as Admin).showDlgAdmin(getItem(position))
                }
                RVAdminBTdelete.setOnClickListener {
                    gvaDialogs.alertDialog(act)
                        .tittleText("Delete Admin")
                        .alertText("Yakin akan menghapus admin \"${userdata?.username?:""}\"")
                        .setBTyesListener{
                            it.dismiss()
                            (act as Admin).VMadmin.deleteAdmin(getItem(position)!!.uid)
                        }.build().show()
                }
                executePendingBindings()
            }
        }
    }

    override fun getItemViewType(position: Int): Int = if (position == itemCount-1) FOOTER else CONTENT
}