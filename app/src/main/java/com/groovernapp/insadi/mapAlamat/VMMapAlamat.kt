package com.groovernapp.insadi.mapAlamat

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.groovernapp.MyUtils.Enums.Status
import com.groovernapp.MyUtils.Resource
import com.groovernapp.insadi.databinding.ActMapAlamatBinding
import kotlinx.coroutines.launch

class VMMapAlamat: ViewModel() {
    val TAG = javaClass.simpleName
    val MLDlocsearch = MutableLiveData<Resource<Address>>()
    var LLlocTemp: Address? = null
    var LocMngr: LocationManager? = null

    fun searchLocation(gMap: GoogleMap): (ed: EditText?)->Unit = {
        if (it != null)
            DataBindingUtil.getBinding<ActMapAlamatBinding>(it)?.apply {
                MapalamatAB.setExpanded(false)
                MapalamatFAB.hide()
                BottomSheetBehavior.from(MapAlamatBSMmapinstruc.root).state = BottomSheetBehavior.STATE_COLLAPSED
                getLocation(gMap, Geocoder(it.context), MapalamatEDtbaddress.text.toString().trim())
            }
    }

    fun <L>getLocation(gMap:GoogleMap, geo: Geocoder, location: L, bDragMode: Boolean = false, iretrycounter: Int = 0) {
        Resource.loading<Address>("Mencari alamat ...")
        viewModelScope.launch {
//            withContext(Dispatchers.IO){
            try {
                when(location){
                    is String -> geo.getFromLocationName(location, 5)
                    is Location -> geo.getFromLocation(location.latitude, location.longitude, 1)
                    is LatLng -> geo.getFromLocation(location.latitude, location.longitude, 1)
                    else -> null
                }?.let {
                    if (location is String && it.size == 0) {
                        Resource.failed(if(bDragMode) LLlocTemp else null,
                            "Alamat tidak ditemukan. Mohon lengkapi alamat anda dengan nama kota, kabupaten, atau daerah.")
                    }
                    else locRestriction(gMap, it[0], bDragMode)
                }
            }catch (e: Throwable){
                Log.e(TAG, "Error : $e")
                if (iretrycounter < 3){
                    getLocation(gMap, geo, location, bDragMode, iretrycounter+1)
                    Resource.loading("Mencari ulang alamat ...")
                }else {
                    Resource.failed(if(bDragMode) LLlocTemp else null, "Pencarian gagal. Silahkan coba beberapa saat lagi.")
                }
            }.let {
                if (it?.status == Status.SUCCESS && !bDragMode) LLlocTemp = it.data
                MLDlocsearch.postValue(it) }
//            }
        }
    }
    fun getLocation(context: Context, gMap: GoogleMap) = (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager).apply {
        LocMngr = this
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            MLDlocsearch.postValue(Resource.loading("Mencari posisi saat ini ..."))
            requestLocationUpdates(LocationManager.GPS_PROVIDER, 500,0f, (object: LocationListener {
                override fun onLocationChanged(loc: Location) {
                    Log.i(TAG, "Location Changed : ${loc.latitude},${loc.longitude}")
                    LocMngr?.removeUpdates(this)
                    getLocation(gMap, Geocoder(context), loc)
                }

                override fun onProviderDisabled(provider: String) {
                    MLDlocsearch.postValue(Resource.failed("GPS mati"))
                }
            }).also {
                Handler(Looper.getMainLooper()).postDelayed({
                    LocMngr?.removeUpdates(it)
                    getLastKnownLocation(LocationManager.GPS_PROVIDER).let {
                        if (it != null) getLocation(gMap, Geocoder(context), it)
                        else MLDlocsearch.postValue(Resource.failed("Pencarian gagal. Silahkan coba beberapa saat lagi."))
                    }
                }, 10000)
            })
        }
    }

    private fun locRestriction(gMap: GoogleMap, address: Address, bDragMode: Boolean): Resource<Address> =
        address.run {
            Log.i(
                TAG, """addressinfo :
                        Locality    : $locality
                        subLocality  : $subLocality
                        AdminArea    : $adminArea
                        SubAdminArea : $subAdminArea
                        Country      : $countryName
                        Country Code : $countryCode
                    """.trimIndent()
            )
            FloatArray(1).let {
                LLlocTemp?.let { locTemp ->
                    Location.distanceBetween(locTemp.latitude, locTemp.longitude, gMap.cameraPosition.target.latitude,
                        gMap.cameraPosition.target.longitude, it)
                }
                Log.i(TAG, "distance : ${it[0]}")
                if (countryCode != "SA" && countryCode != "ID") {
                    Log.i(TAG, "Hasil titik kejauhan")
                    "Lokasi diluar negara Saudi atau Indonesia"
                } else if (bDragMode && it[0] > 1000) {
                    Log.i(TAG, "Hasil titik kejauhan 1km")
                    "Tidak boleh geser lebih dari 1Km"
                } else {
                    Log.i(TAG, "Hasil titik memenuhi syarat")
                    ""
                }
            }.let {
                if (it.isNotEmpty()){
                    Resource.failed(if(bDragMode) LLlocTemp else null, it)
                }else {
                    Resource.success(address, if (LocMngr != null){
                        LocMngr = null
                        address.getAddressLine(0)
                    }else null)
                }
            }
        }
}
