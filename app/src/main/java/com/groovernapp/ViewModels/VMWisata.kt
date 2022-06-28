package com.groovernapp.ViewModels

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.groovernapp.MyUtils.ObservableViewModel
import com.groovernapp.MyUtils.Resource
import com.groovernapp.MyUtils.SparateThousands
import com.groovernapp.data.Repositories.WisataRepo
import com.groovernapp.data.models.WisataData
import com.groovernapp.insadi.R

class VMWisata: ObservableViewModel() {
    private val TAG = this::class.java.simpleName
    val MLDlistWisata = MutableLiveData<Resource<ArrayList<WisataData>>>()
    val MLDlistSuggest = MutableLiveData<Resource<ArrayList<WisataData>>>()
    val MLDuploadPhoto = MutableLiveData<Resource<Boolean>>()
    val MLDaddWisata = MutableLiveData<Resource<Boolean>>()

    var selectedGroupWisataId = 0

    fun listWisata(searchData: String = "") = WisataRepo.listWisata(MLDlistWisata, searchData, selectedGroupWisataId)
    fun listSuggest(searchData: String = "") = WisataRepo.listSuggest(MLDlistSuggest, searchData, selectedGroupWisataId)

    fun addWisata(data: WisataData) = WisataRepo.addWisata(MLDaddWisata, data)
    fun uploadWisataPhoto(wisataId: Int, image: String) = WisataRepo.uploadFotoWIsata(MLDuploadPhoto, wisataId, image)
    fun deleteWisata(wisataId: Int) = WisataRepo.deleteWisata(MLDlistWisata, wisataId)


    companion object{
        @BindingAdapter("setHargaTiket")
        @JvmStatic fun setHargaTiket(v: TextView, hrgTiket: Int){
            v.text = if (hrgTiket <= 0) "Gratis"
                else SparateThousands.getDecimalFormattedString(hrgTiket, true)
        }
        @BindingAdapter("setJamBuka", "setJamTutup")
        @JvmStatic fun setJamWisata(v: TextView, jamBuka: String, jamTutup: String){
            v.text = v.context.getString(R.string.format_jam_wisata, jamBuka, jamTutup)
        }
    }
}