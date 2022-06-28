package com.groovernapp.ViewModels

import androidx.lifecycle.ViewModel
import com.groovernapp.data.Repositories.AdminRepo
import com.groovernapp.MyUtils.MyUtils
import com.groovernapp.MyUtils.gvaFunctions

class VMLogin: ViewModel() {
    private val TAG = this::class.java.simpleName

    fun login(username: String, pwd: String) = AdminRepo.login(MyUtils.MLDuserdata, username, gvaFunctions.toMd5(pwd)?:"")
}