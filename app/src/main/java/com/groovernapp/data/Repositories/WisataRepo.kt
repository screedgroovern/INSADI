package com.groovernapp.data.Repositories

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.groovernapp.data.RESTClient
import com.groovernapp.MyUtils.Enums.JenisWisata
import com.groovernapp.MyUtils.Resource
import com.groovernapp.data.models.WisataData
import com.pixplicity.easyprefs.library.Prefs
import java.util.*

object WisataRepo {
    private val TAG = javaClass.simpleName
    private val classname = "wisata"

    fun listWisata(mld: MutableLiveData<Resource<ArrayList<WisataData>>>, searchData: String = "", groupWisataId: Int = 0) {
        mld.postValue(Resource.loading("Mencari daftar wisata ..."))
        RESTClient.command("$classname;listWisata")
            .apply { if (groupWisataId != 0) addParam("groupWisataId", groupWisataId) }
            .addParam("latitude", Prefs.getDouble("mLat"))
            .addParam("longitude", Prefs.getDouble("mLong"))
            .addParam("searchData", searchData)
            .post("listWisata", mld){
                if (it.optBoolean("resp")) {
                    it.optJSONArray("data")?.let { jsa->
                        Resource.success(
                            ArrayList<WisataData>().apply{
                                for (i in 0 until jsa.length()){
                                    jsa.getJSONObject(i).apply {
                                        add(WisataData(
                                            optInt("wisataId"),
                                            JenisWisata.getEnumName(optString("groupWisata")),
                                            optString("namaWisata"),
                                            optInt("hrgTiket",0),
                                            optString("jamBuka"),
                                            optString("jamTutup"),
                                            optString("alamat"),
                                            optString("koordinat"),
                                            optString("deskripsi"),
                                            optDouble("distance",0.0),
                                            optInt("jmlPengunjung",0),
                                            RESTClient.url.plus(optString("photo")),
                                            optInt("adminId")))
                                    }
                                }
                            }
                        )
                    }
                }else Resource.failed(it.optString("msg"))
            }
    }

    fun listSuggest(mld: MutableLiveData<Resource<ArrayList<WisataData>>>, searchData: String = "", groupWisataId: Int = 0) {
        mld.postValue(Resource.loading("Mencari daftar wisata ..."))
        RESTClient.command("$classname;listSuggest")
            .apply { if (groupWisataId != 0) addParam("groupWisataId", groupWisataId) }
            .addParam("latitude", Prefs.getDouble("mLat"))
            .addParam("longitude", Prefs.getDouble("mLong"))
            .addParam("searchData", searchData)
            .post("listWisata", mld){
                if (it.optBoolean("resp")) {
                    it.optJSONArray("data")?.let { jsa->
                        Resource.success(
                            ArrayList<WisataData>().apply{
                                for (i in 0 until jsa.length()){
                                    jsa.getJSONObject(i).apply {
                                        add(WisataData(
                                            optInt("wisataId"),
                                            JenisWisata.getEnumName(optString("groupWisata")),
                                            optString("namaWisata"),
                                            distance = optDouble("distance",0.0)
                                        ))
                                    }
                                }
                            }
                        )
                    }
                }else Resource.failed(it.optString("msg"))
            }
    }

    fun addWisata(mld: MutableLiveData<Resource<Boolean>>, data: WisataData) {
        mld.postValue(Resource.loading("Tambah/Edit Admin ..."))
        RESTClient.command("$classname;addWisata")
            .addParam("wisataId", data.wisataId)
            .addParam("groupWisataId", data.groupWisata?.idx?:0)
            .addParam("namaWisata", data.namaWisata?:"")
            .addParam("hrgTiket", data.hrgTiket?:"")
            .addParam("jamBuka", data.jamBuka?:"")
            .addParam("jamTutup", data.jamTutup?:"")
            .addParam("alamat", data.alamat?:"")
            .addParam("koordinat", data.koordinat?:"")
            .addParam("deskripsi", data.deskripsi?:"")
            .addParam("jmlPengunjung", data.jmlPengunjung?:0)
            .addParam("photo", data.photo?:"")
            .addParam("adminId", data.adminId?:"")
            .post("addWisata", mld){
                if (it.optBoolean("resp")) {
                    Resource.success(it.optBoolean("resp"))
                }else Resource.failed(it.optString("msg"))
            }
    }

    fun uploadFotoWIsata(mld: MutableLiveData<Resource<Boolean>>, wisataId: Int, image: String){
        mld.postValue(Resource.loading("Upload foto wisata..."))
        RESTClient.command("$classname;uploadimgwisata")
            .addParam("image", image)
            .addParam("wisataId", wisataId)
            .post("uploadimgwisata", mld){
                if (it.optBoolean("resp")) {
                    Resource.success(it.optBoolean("resp"))
                }else Resource.failed(it.optString("msg"))
            }
    }

    fun deleteWisata(mld: MutableLiveData<Resource<ArrayList<WisataData>>>, wisataId: Int) {
        mld.postValue(Resource.loading("Tambah/Edit Admin ..."))
        RESTClient.command("$classname;deleteWisata")
            .addParam("wisataId", wisataId)
            .post("deleteWisata", mld){
                if (it.optBoolean("resp")) {
                    listWisata(mld)
                    null
                }else Resource.failed(it.optString("msg"))
            }
    }
}