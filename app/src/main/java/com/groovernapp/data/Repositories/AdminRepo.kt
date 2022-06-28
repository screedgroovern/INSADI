package com.groovernapp.data.Repositories

import androidx.lifecycle.MutableLiveData
import com.groovernapp.data.models.UserData
import com.groovernapp.data.RESTClient
import com.groovernapp.MyUtils.Enums.JenisJabatan
import com.groovernapp.MyUtils.Resource
import java.util.*

object AdminRepo {
    private val TAG = javaClass.simpleName
    private val classname = "admin"

    fun login(mld: MutableLiveData<Resource<UserData>>, username: String, pwd: String, msg: String? = null) {
        mld.postValue(Resource.loading("Logging in ..."))
        RESTClient.command("$classname;login")
            .addParam("username", username)
            .addParam("pwd", pwd)
            .post("login", mld){
                if (it.optBoolean("resp")) {
                    it.optJSONObject("data")?.run {
                        Resource.success(UserData(optInt("uid"), optString("username"), optString("regData"), optString("nama"),
                            optString("alamat"), optString("telp"), JenisJabatan.getEnumName(optString("jabatan"))),
                            msg ?: it.optString("msg"))
                    }
                }else Resource.failed(it.optString("msg"))
            }
    }

    fun listAdmin(mld: MutableLiveData<Resource<ArrayList<UserData>>>, searchData: String = "") {
        mld.postValue(Resource.loading("Logging in ..."))
        RESTClient.command("$classname;listAdmin")
            .addParam("searchData", searchData)
            .post("listAdmin", mld){
                if (it.optBoolean("resp")) {
                    it.optJSONArray("data")?.let { jsa->
                        Resource.success(
                            ArrayList<UserData>().apply{
                                for (i in 0 until jsa.length()){
                                    jsa.getJSONObject(i).apply {
                                        add(UserData(optInt("uid"), optString("username"), optString("regDate"),
                                            optString("nama"), optString("alamat"), optString("telp"),
                                            JenisJabatan.getEnumName(optString("jabatan"))))
                                    }
                                }
                            }
                        )
                    }
                }else Resource.failed(it.optString("msg"))
            }
    }

    fun addAdmin(mld: MutableLiveData<Resource<ArrayList<UserData>>>, data: UserData, pwd: String) {
        mld.postValue(Resource.loading("Tambah/Edit Admin ..."))
        RESTClient.command("$classname;addAdmin")
            .addParam("uid", data.uid)
            .addParam("username", data.username?:"")
            .addParam("nama", data.nama?:"")
            .addParam("telp", data.telp?:"")
            .addParam("alamat", data.alamat?:"")
            .addParam("pwd", pwd)
            .post("addAdmin", mld){
                if (it.optBoolean("resp")) {
                    listAdmin(mld, it.optString("msg"))
                    null
                }else Resource.failed(it.optString("msg"))
            }
    }

    fun deleteAdmin(mld: MutableLiveData<Resource<ArrayList<UserData>>>, uid: Int) {
        mld.postValue(Resource.loading("Tambah/Edit Admin ..."))
        RESTClient.command("$classname;deleteAdmin")
            .addParam("uid", uid)
            .post("deleteAdmin", mld){
                if (it.optBoolean("resp")) {
                    listAdmin(mld, it.optString("msg"))
                    null
                }else Resource.failed(it.optString("msg"))
            }
    }
}