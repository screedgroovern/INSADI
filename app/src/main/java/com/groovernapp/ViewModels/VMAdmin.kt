package com.groovernapp.ViewModels

import androidx.lifecycle.MutableLiveData
import com.groovernapp.MyUtils.ObservableViewModel
import com.groovernapp.data.models.UserData
import com.groovernapp.data.Repositories.AdminRepo
import com.groovernapp.MyUtils.Resource
import com.groovernapp.MyUtils.gvaFunctions

class VMAdmin: ObservableViewModel() {
    private val TAG = this::class.java.simpleName
    val MLDlistAdmin = MutableLiveData<Resource<ArrayList<UserData>>>()

    fun listAdmin(searchData: String = "") = AdminRepo.listAdmin(MLDlistAdmin, searchData)

    fun addAdmin(data: UserData, pwd: String) = AdminRepo.addAdmin(MLDlistAdmin, data, gvaFunctions.toMd5(pwd)!!)
    fun deleteAdmin(uid: Int) = AdminRepo.deleteAdmin(MLDlistAdmin, uid)
}