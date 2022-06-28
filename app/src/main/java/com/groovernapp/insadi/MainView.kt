package com.groovernapp.insadi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.groovernapp.MyUtils.Enums.JenisJabatan
import com.groovernapp.MyUtils.Enums.JenisWisata
import com.groovernapp.MyUtils.Enums.Status
import com.groovernapp.MyUtils.MyUtils
import com.groovernapp.MyUtils.MyUtils.goGone
import com.groovernapp.MyUtils.MyUtils.goVisible
import com.groovernapp.MyUtils.MyUtils.populateChip
import com.groovernapp.MyUtils.MyUtils.toDataModel
import com.groovernapp.MyUtils.MyUtils.toJson
import com.groovernapp.MyUtils.MyUtils.visibleOrGone
import com.groovernapp.MyUtils.Resource
import com.groovernapp.ViewModels.VMWisata
import com.groovernapp.data.models.UserData
import com.groovernapp.data.models.WisataData
import com.groovernapp.insadi.Admin.Admin
import com.groovernapp.insadi.Wisata.DetailWisata
import com.groovernapp.insadi.Wisata.SearchSuggestRVAdapter
import com.groovernapp.insadi.Wisata.WisataRVAdapter
import com.groovernapp.insadi.databinding.ActMainviewBinding
import com.pixplicity.easyprefs.library.Prefs

class MainView: AppCompatActivity() {
    private val TAG = this::class.java.simpleName
    private lateinit var Vbind: ActMainviewBinding
    val VMwisata: VMWisata by viewModels()
    private val adpWisata = WisataRVAdapter(this, getUserData().jabatan != JenisJabatan.PENGUNJUNG)
    private val adpSuggest by lazy {
        SearchSuggestRVAdapter {
            Vbind.apply {
                MVEDsearch.setText(it)
                cvSearchSuggest.visibility = GONE
            }
            VMwisata.listWisata(it)
        }
    }
    private var dlgWisata: BottomSheetDialog? = null
    private var Bexit = false
    private var _searchTxt = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Vbind = ActMainviewBinding.inflate(layoutInflater)
        setContentView(Vbind.root)

        setMyLocationDetector(5, 10f)
        setUI()
        setObserver()
    }

    override fun onResume() {
        super.onResume()
        MyUtils.dlgFailConnection.setContext(this)
        if (adpSuggest.currentList.size > 0) VMwisata.listWisata()
    }

    override fun onBackPressed() {
        if (Bexit) finish()
        else {
            Bexit = true
            Toast.makeText(this, "Tekan tombol \"Kembali\" sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({ Bexit = false }, 3000)
        }
    }

    private fun setUI() = Vbind.run {
        getUserData().apply {
            Log.e(TAG, "userData : $this")
            MVTVjabatan.text = jabatan?.value
            MVTVnama.text = nama
            MVTVtelp.text = telp
            MVTValamat.text = alamat
            cgAdminView.visibleOrGone(jabatan != JenisJabatan.PENGUNJUNG)
            MVFABaddpariwisata.visibleOrGone(jabatan != JenisJabatan.PENGUNJUNG)

            MVBTlistadmin.apply {
                if (jabatan != JenisJabatan.MASTER) {
                    if (jabatan == JenisJabatan.PENGUNJUNG) {
                        text = getString(R.string.login)
                        setOnClickListener {
                            startActivity(Intent(this@MainView, Login::class.java))
                        }
                    } else this.goGone()
                } else {
                    setOnClickListener {
                        startActivity(Intent(this@MainView, Admin::class.java))
                    }
                }
            }

            cpgGroupWisata.apply {
                val groupWisata = JenisWisata.values()
                val arrGroupWisata = arrayListOf(
                    groupWisata[0].value.capitalize(),
                    groupWisata[1].value.capitalize(),
                    groupWisata[2].value.capitalize(),
                    groupWisata[3].value.capitalize(),
                    groupWisata[4].value.capitalize())
                populateChip(arrGroupWisata)
                setOnCheckedChangeListener { _, checkedId ->
                    VMwisata.selectedGroupWisataId = checkedId
                    VMwisata.listWisata()
                }
                check(0)
            }

            MVBTlogout.setOnClickListener {
                Prefs.remove("userData")
                MyUtils.MLDuserdata.value = null
                finish()
                startActivity(Intent(this@MainView, MainView::class.java))
            }
        }
        MVEDsearch.run {
            doAfterTextChanged {
                Log.e(TAG, "searchSuggest : ${it.toString()}")
                if (it.isNullOrEmpty()) {
                    VMwisata.listWisata()
                    cvSearchSuggest.goGone()
                }else {
                    _searchTxt = it.toString()
                    Handler(mainLooper).postDelayed({
                        Log.e(TAG, "${text.toString()} == $_searchTxt")
                        if (text.toString() == _searchTxt) {
                            cvSearchSuggest.goVisible()
                            VMwisata.listSuggest(it.toString())
                        }
                    }, 1500)
                }
            }
            setOnEditorActionListener { tv, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH || event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                    VMwisata.listWisata(tv.text.toString())
                    true
                } else false
            }
        }
        MVRVlistpariwisata.apply {
            adapter = adpWisata
            addItemDecoration(DividerItemDecoration(this@MainView, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(this@MainView, R.drawable.divider_rv_10dp)!!)
            })
        }
        MVSRlistpariwisata.apply {
            setOnRefreshListener {
                Handler(Looper.getMainLooper()).post {
                    isRefreshing = false
                    MVEDsearch.setText("")
                    VMwisata.listWisata()
                }
            }
        }
        rvSearchSuggest.adapter = adpSuggest
        MVFABaddpariwisata.setOnClickListener { showDetailWisata() }
    }
    private fun setObserver() = VMwisata.run {
        MLDlistWisata.observe(this@MainView){
            when(it?.status){
                Status.LOADING -> {}
                Status.FAILED -> {}
                Status.SUCCESS ->  Vbind.run {
                    adpWisata.submitList(it.data?.apply { add(WisataData()) }?.toList())
                    dlgWisata?.dismiss()
                }
                else -> {}
            }
        }
        MLDlistSuggest.observe(this@MainView){
            when(it?.status){
                Status.LOADING -> {}
                Status.FAILED -> {}
                Status.SUCCESS ->  Vbind.run {
                    Log.e(TAG, "response : ${it.data}")
                    adpSuggest.submitList(it.data?.toList())
                    cvSearchSuggest.visibility = if (it.data?.size?:0 > 0) VISIBLE else GONE
                }
                else -> {}
            }
        }
    }

    private fun getUserData() = Prefs.getString("userData").let {
        if (it.isNotEmpty()){
            MyUtils.MLDuserdata.value = Resource.success(it.toDataModel(UserData::class.java))
        }
        MyUtils.MLDuserdata.value?.data?: UserData()
    }

    fun showDetailWisata(data: WisataData? = WisataData()) =
        startActivity(Intent(this, DetailWisata::class.java).apply {
            putExtra("isAdminView", getUserData().jabatan != JenisJabatan.PENGUNJUNG)
            putExtra("data", data.toJson())
        })

    private fun setMyLocationDetector(refreshRate: Long, distanceRate :Float) =
        if (ActivityCompat.checkSelfPermission(this@MainView, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            (getSystemService(LOCATION_SERVICE) as LocationManager).requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                refreshRate,
                distanceRate
            ){
                Prefs.putDouble("mLat", it.latitude)
                Prefs.putDouble("mLong", it.longitude)
            }
        }else ActivityCompat.requestPermissions(this@MainView, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 999)
}