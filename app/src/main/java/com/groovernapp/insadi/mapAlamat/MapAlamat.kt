package com.groovernapp.insadi.mapAlamat

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.View.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.groovernapp.MyUtils.Enums.Status
import com.groovernapp.MyUtils.MyUtils
import com.groovernapp.MyUtils.gvaDialogs
import com.groovernapp.MyUtils.gvaFunctions
import com.groovernapp.insadi.IntentResult
import com.groovernapp.insadi.R
import com.groovernapp.insadi.databinding.ActMapAlamatBinding
import com.groovernapp.insadi.databinding.BsmMapinstrucBinding
import okhttp3.*
import java.util.*

class MapAlamat : AppCompatActivity(), OnMapReadyCallback {
    private val TAG = javaClass.simpleName
    private var LLdefault = LatLng(24.681687972876205, 46.62450646105876); private var LLlokasitemp: LatLng = LLdefault
    private val REQCODE_ACCESSFINELOCATION = 999
    private var Markdest: Marker? = null
    private var Bdragmode: Boolean? = null
    private lateinit var Vbind: ActMapAlamatBinding
    private lateinit var BSMVbind: BsmMapinstrucBinding
    private lateinit var BSBmapinstruc: BottomSheetBehavior<ConstraintLayout>
    private val VMmapalamat: VMMapAlamat by viewModels()
//    private lateinit var loadingScreen: LoadingScreen
    private lateinit var IntntRslt: IntentResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Vbind = DataBindingUtil.setContentView(this, R.layout.act_map_alamat)
//        BSMVbind = BsmMapinstrucBinding.bind(Vbind.root)

        (supportFragmentManager.findFragmentById(R.id.Gmapfrags) as SupportMapFragment).getMapAsync(this@MapAlamat)
        IntntRslt = IntentResult(activityResultRegistry).also { lifecycle.addObserver(it) }
    }

    override fun onResume() {
        super.onResume()
        MyUtils.dlgFailConnection.setContext(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Vbind.run {
            gMap = googleMap
            vm = VMmapalamat
            setUI()
            setObserver()

            googleMap.run {
                addMarker(createMarker("KBRI Saudi", bitmapFromVector(this@MapAlamat, R.mipmap.ic_launcher_round)!!, LLdefault))
                intent.getStringExtra("alamat").let {
                    if (it.isNullOrEmpty()) moveCamera(CameraUpdateFactory.newLatLngZoom(LLdefault, 19f))
                    else {
                        MapalamatEDtbaddress.setText(it)
                        if (intent.getParcelableExtra<LatLng>("kooralamat") != null) {
                            VMmapalamat.getLocation(this, Geocoder(this@MapAlamat), intent.getParcelableExtra<LatLng>("kooralamat"))
                        } else if (it.isNotEmpty()) {
                            VMmapalamat.getLocation(this, Geocoder(this@MapAlamat), it)
                        }
                    }
                }
            }
        }
    }

    private fun setUI() = Vbind.run {
//        loadingScreen = LoadingScreen(this@MapAlamat, root as ViewGroup)
        MapAlamatBSMmapinstruc.BSMmapinstrucBTsetmap.setOnClickListener { setMap() }

        gMap?.apply {
            setOnMapClickListener {
                gvaFunctions.showKeyboard(this@MapAlamat, false)
                MapalamatEDtbaddress.clearFocus()
            }
            setOnCameraMoveListener {
                if (Bdragmode != null) {
                    MapAlamatCLmarker.visibility = VISIBLE
                    if (Markdest != null) {
                        Markdest?.remove()
                    }
                }
            }
            setOnCameraIdleListener {
                if (Bdragmode == true){
                    MapAlamatCLmarker.visibility = GONE
                    Markdest = addMarker(
                        createMarker("Alamat anda", bitmapFromVector(this@MapAlamat, R.drawable.ic_person)!!,
                            cameraPosition.target)
                    )
                    VMmapalamat.getLocation(this, Geocoder(this@MapAlamat), cameraPosition.target, true)
                }else if (Bdragmode == false) Bdragmode = true
            }
        }

        MapalamatEDtbaddress.setOnEditorActionListener { tv, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                gvaFunctions.showKeyboard(this@MapAlamat, false)
                MapalamatAB.setExpanded(false)
                MapalamatFAB.hide()
                BottomSheetBehavior.from(MapAlamatBSMmapinstruc.root).state = BottomSheetBehavior.STATE_COLLAPSED
                VMmapalamat.getLocation(gMap!!, Geocoder(tv.context), MapalamatEDtbaddress.text.toString().trim())
                true
            } else {
                false
            }
        }
        MapalamatFAB.setOnClickListener{ /*getMyLastLoction(gMap)*/
            BSBmapinstruc.state = BottomSheetBehavior.STATE_COLLAPSED
//            VMmapalamat.getLocation(this@MapAlamat, gMap!!)
            getMyLastLoction(gMap!!)
        }

        BSBmapinstruc = BottomSheetBehavior.from(MapAlamatBSMmapinstruc.BSMmapinstrucCLrootview).apply {
            addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback(){
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when(newState){
                        BottomSheetBehavior.STATE_DRAGGING -> state = BottomSheetBehavior.STATE_EXPANDED
                        BottomSheetBehavior.STATE_COLLAPSED -> Bdragmode = false
                        BottomSheetBehavior.STATE_EXPANDED -> Bdragmode = true
                        else -> {}
                    }
                }
                override fun onSlide(bottomSheet: View, slideOffset: Float) {  }

            })
        }

        Log.e(TAG, "BSBstate : ${BSBmapinstruc.state}")
        MapalamatIMGmarker.setImageBitmap(bitmapFromVector(this@MapAlamat, R.drawable.ic_person))
    }
    private fun setObserver() = VMmapalamat.run {
        MLDlocsearch.observe(this@MapAlamat) {
            if (it.data?.let { addrs -> LatLng(addrs.latitude, addrs.longitude) } != LLdefault)
                when (it.status) {
                    Status.LOADING -> Vbind.run {
//                        loadingScreen.show(it.msg)
                        MapalamatAB.setExpanded(false)
                        MapalamatFAB.hide()
                    }
                    Status.FAILED -> Vbind.run {
//                        loadingScreen.dismissDlg()
                        MapalamatAB.setExpanded(true)
                        MapalamatFAB.show()
                        if (it.msg != null) {
                            if (it.msg.lowercase(Locale.getDefault())
                                    .contains("alamat tidak ditemukan") ||
                                it.msg.lowercase(Locale.getDefault()).contains("pencarian gagal")
                            ) {
                                gvaDialogs.alertDialog(this@MapAlamat, Vbind.MapalamatClrootview)
                                    .tittleText(
                                        if (it.msg.lowercase(Locale.getDefault())
                                                .contains("alamat tidak ditemukan")
                                        ) "Alamat tidak ditemukan"
                                        else "Pencarian gagal"
                                    )
                                    .alertText(it.msg)
                                    .showBTno(false)
                                    .setBTyesText(getString(R.string.ok))
                                    .setBTyesListener { dlg ->
                                        dlg.dismiss()
                                        if (it.msg.lowercase(Locale.getDefault())
                                                .contains("alamat tidak ditemukan")
                                        ) {
                                            Vbind.MapalamatEDtbaddress.apply {
                                                requestFocus()
                                                setSelection(text.toString().length)
                                            }
                                            gvaFunctions.showKeyboard(this@MapAlamat, true)
                                        }
                                        Bdragmode = false
                                        if (it.data != null) moveMarker(gMap!!, it.data)
                                    }
                                    .build().show()
                            } else if (it.msg.lowercase(Locale.getDefault()).contains("gps mati")) {
                                gvaDialogs.alertDialog(this@MapAlamat, MapalamatClrootview)
                                    .tittleText(getString(R.string.gps_anda_mati))
                                    .alertText(getString(R.string.gps_harus_menyala))
                                    .setBTnoText(getString(R.string.batal))
                                    .setBTnoListener { dlg ->
                                        dlg.dismiss()
                                    }
                                    .setBTyesText(getString(R.string.hidupkan))
                                    .setBTyesListener { dlg ->
                                        IntntRslt.launchActivityResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) {
                                            dlg.dismiss()
                                            getLocation(this@MapAlamat, gMap!!)
                                        }
                                    }.build().show()
                            } else {
                                Bdragmode = false
                                if (it.data != null) moveMarker(gMap!!, it.data)
                                Toast.makeText(this@MapAlamat, it.msg, Toast.LENGTH_LONG).show()
                            }

                        } else Toast.makeText(
                            this@MapAlamat,
                            "Error Null Message",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Status.SUCCESS -> Vbind.run {
//                        loadingScreen.dismissDlg()
                        gMap?.apply {
                            if (BSBmapinstruc.state != BottomSheetBehavior.STATE_EXPANDED) {
                                uiSettings.apply {
                                    isScrollGesturesEnabled = false
                                    isZoomGesturesEnabled = false
                                    isScrollGesturesEnabledDuringRotateOrZoom = false
                                }
                                if (it.msg != null) MapalamatEDtbaddress.setText(it.msg)
                                MapAlamatCLmarker.visibility = VISIBLE

                                Bdragmode = false
                                moveMarker(gMap!!, it.data!!)
                            }
                        }
                    }
                }
        }
    }

    private fun getMyLastLoction(gMap: GoogleMap) = Vbind.run {
        if (ActivityCompat.checkSelfPermission(this@MapAlamat, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(this@MapAlamat).lastLocation
                .addOnCompleteListener(this@MapAlamat) { task ->
//                    Log.e(TAG, "task success? ${task.isSuccessful} | result : ${task.result}")
                    if (task.isSuccessful) {
                        if (task.result != null) VMmapalamat.getLocation(gMap, Geocoder(this@MapAlamat), task.result)
                        else Toast.makeText(this@MapAlamat,
                            "Lokasi anda tidak ditemukan. Silahkan coba lagi nanti", Toast.LENGTH_LONG).show()
                    } else Toast.makeText(this@MapAlamat,
                        "Lokasi anda tidak ditemukan. Silahkan coba lagi nanti", Toast.LENGTH_LONG).show()
                }
        }else ActivityCompat.requestPermissions(this@MapAlamat, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQCODE_ACCESSFINELOCATION)
    }
    private fun moveMarker(gMap: GoogleMap, location: Address) = gMap.run {
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 16f),
            object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    uiSettings.apply {
                        isScrollGesturesEnabled = true
                        isZoomGesturesEnabled = true
                        isScrollGesturesEnabledDuringRotateOrZoom = true
                    }

                    LLlokasitemp = LatLng(location.latitude, location.longitude)
                    Vbind.MapalamatAB.setExpanded(true)
                    Vbind.MapalamatFAB.show()

                    Vbind.MapAlamatBSMmapinstruc.BSMmapinstrucBTsetmap.apply {
                        layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                            bottomMargin = navbarHeight(this@MapAlamat)
                        }
                    }
                    BSBmapinstruc.state = BottomSheetBehavior.STATE_EXPANDED
                }

                override fun onCancel() {}
            })
    }

    private fun bitmapFromVector(context: Context, @DrawableRes vectorDrawableResourceId: Int): Bitmap? {
        val background: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_mapmarker)
        background?.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)
        val vectorDrawable: Drawable? = ContextCompat.getDrawable(context, vectorDrawableResourceId)

        val ratio = (vectorDrawable!!.intrinsicWidth).toFloat()/(vectorDrawable.intrinsicHeight).toFloat()
        val patokan = gvaFunctions.typedvalueToDp(32f, this)
//        val left = background!!.intrinsicWidth / 4
//        val top = background.intrinsicHeight / 10
        var left: Int
        var top: Int
//        var right = 0
//        var bottom = 0
        if (ratio < 1){
            left = (background!!.intrinsicWidth / 2) - ((ratio*patokan).toInt()/2)
            top = (background.intrinsicWidth / 2) - (patokan/2)
//            right = left + (ratio*patokan).toInt()
//            bottom = patokan
        }else if (ratio > 1){
            left = (background!!.intrinsicWidth / 2) - (patokan/2)
            top = (background.intrinsicWidth / 2) - ((ratio*patokan).toInt()/2)
//            right = patokan
//            bottom = (ratio*patokan).toInt()
        }else {
            left = (background!!.intrinsicWidth / 2) - (patokan/2)
            top = (background.intrinsicWidth / 2) - (patokan/2)
//            right = patokan
//            bottom = patokan
        }
        vectorDrawable.setTint(ResourcesCompat.getColor(resources, R.color.green_soft, null))
        vectorDrawable.setBounds(left, top, left + (patokan * ratio).toInt(), top + patokan)
        val bitmap = Bitmap.createBitmap(background.intrinsicWidth, background.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        background.draw(canvas)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    private fun setMap() = Vbind.run {
        BSBmapinstruc.state = BottomSheetBehavior.STATE_COLLAPSED
        finishAfterTransition()
        setResult(RESULT_OK, Intent().apply {
            putExtra("address", MapalamatEDtbaddress.text.toString().trim())
            putExtra("location", LatLng(gMap?.cameraPosition?.target?.latitude?:0.0,
                gMap?.cameraPosition?.target?.longitude?:0.0))
        })
    }

    fun navbarHeight(activity: Activity): Int {
        val temporaryHidden = activity.window.decorView.visibility and SYSTEM_UI_FLAG_HIDE_NAVIGATION != 0
        if (temporaryHidden) return 0
        val decorView = activity.window.decorView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return window.decorView.rootWindowInsets.displayCutout?.safeInsetBottom?:40
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decorView.rootWindowInsets?.let{
                Log.e(TAG, "navbarinset : ${it.stableInsetBottom}")
                return it.stableInsetBottom
            }
        }
        return 40
    }

    private fun createMarker(ttl: String, icon: Bitmap, position: LatLng) =
        MarkerOptions().position(position).title(ttl).apply {
            icon(BitmapDescriptorFactory.fromBitmap(icon))
            anchor(0.51325f, 1f)
        }
}