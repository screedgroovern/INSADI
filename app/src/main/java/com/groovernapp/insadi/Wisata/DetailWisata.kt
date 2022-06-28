package com.groovernapp.insadi.Wisata

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.groovernapp.MyUtils.Enums.JenisWisata
import com.groovernapp.MyUtils.Enums.Status
import com.groovernapp.MyUtils.MyUtils
import com.groovernapp.MyUtils.MyUtils.toDataModel
import com.groovernapp.MyUtils.MyUtils.toIntWithSafety
import com.groovernapp.MyUtils.MyUtils.visibleOrGone
import com.groovernapp.MyUtils.SparateThousands
import com.groovernapp.MyUtils.gvaDialogs
import com.groovernapp.MyUtils.gvaFunctions
import com.groovernapp.MyUtils.gvaFunctions.intentToGmaps
import com.groovernapp.ViewModels.VMWisata
import com.groovernapp.data.models.WisataData
import com.groovernapp.insadi.IntentResult
import com.groovernapp.insadi.R
import com.groovernapp.insadi.databinding.ActDetailWisataBinding
import com.groovernapp.insadi.mapAlamat.MapAlamat
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.ByteArrayOutputStream

class DetailWisata: AppCompatActivity() {

    private val TAG = this::class.java.simpleName
    val VMwisata: VMWisata by viewModels()
    private lateinit var vbind: ActDetailWisataBinding 
    private val isAdminView by lazy { intent.getBooleanExtra("isAdminView", false) }
    private val data by lazy { intent.getStringExtra("data").toDataModel(WisataData::class.java) }
    private var bmpImageForUpload: Bitmap? = null

    private lateinit var intentResult: IntentResult
            
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vbind = DataBindingUtil.setContentView(this, R.layout.act_detail_wisata)
        vbind.run {
            isAdminView = this@DetailWisata.isAdminView
        }

        setUi()
        setObservers()
    }
    
    private fun setUi() = vbind.run {
        intentResult = IntentResult(activityResultRegistry).also { lifecycle.addObserver(it) }
        data?.apply {
            Log.e(TAG, "read -> id : ${wisataId} || group : ${groupWisata}")
            TVttl.text = if (wisataId == 0) {
                getString(R.string.tambah_wisata)
            } else {
                getString(R.string.edit_wisata)
            }
            ivWisataPhoto.setOnClickListener {
                getImageContract.launch("image/*")
            }
            ACTVjeniswisata.apply {
                doAfterTextChanged {
                    groupWisata =
                        JenisWisata.getEnumName(it.toString().trim().split(" ")[1].uppercase())
                }
                setText(
                    getString(
                        R.string.format_nama_wisata,
                        (groupWisata ?: JenisWisata.ALAM).value.lowercase()
                            .replaceFirstChar { it.uppercase() })
                )
                if (this@DetailWisata.isAdminView) {
                    setAdapter(
                        ArrayAdapter(
                            context,
                            R.layout.model_textviewspinner,
                            arrayOf(
                                "Wisata Alam",
                                "Wisata Buatan",
                                "Wisata Religi",
                                "Wisata Kuliner"
                            )
                        ).apply {
                            setDropDownViewResource(R.layout.model_textviewspinner)
                        }
                    )
                }
            }
            EDnamawisata.apply {
                doAfterTextChanged { namaWisata = it.toString() }
                setText(namaWisata ?: "")
            }
            EDhrgtiket.apply {
                addTextChangedListener(SparateThousands(this, true))
                doAfterTextChanged { hrgTiket = it.toString().filter { it.isDigit() }.toInt() }
                setText((hrgTiket ?: 0).toString())
            }
            EDjambukatutup.apply {
                val dateFormat = "HH:mm"
                doAfterTextChanged {
                    it.toString().split(" - ").let { bukaTutup ->
                        val bukaMillis =
                            gvaFunctions.stringDatetoMilliseconds(bukaTutup[0], dateFormat) ?: 0
                        val tutupMillis =
                            gvaFunctions.stringDatetoMilliseconds(bukaTutup[1], dateFormat) ?: 0

                        jamBuka = bukaTutup[0]
                        jamTutup = if (bukaMillis > tutupMillis) bukaTutup[0] else bukaTutup[1]
                    }
                }
                setText(
                    getString(
                        R.string.format_jam_wisata,
                        jamBuka ?: "00:00",
                        jamTutup ?: "00:00"
                    )
                )
                if (this@DetailWisata.isAdminView) {
                    setOnClickListener {
                        val arrJamBuka = (jamBuka ?: "00:00").split(":")
                        val arrJamTutup = (jamTutup ?: "00:00").split(":")
                        MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_24H)
                            .setTitleText("Tentukan Jam Buka")
                            .setHour(arrJamBuka[0].toInt())
                            .setMinute(arrJamBuka[1].toInt())
                            .build()
                            .also { jamBukaTimePicker ->
                                jamBukaTimePicker.addOnPositiveButtonClickListener {
                                    MaterialTimePicker.Builder()
                                        .setTimeFormat(TimeFormat.CLOCK_24H)
                                        .setTitleText("Tentukan Jam Tutup")
                                        .setHour(arrJamTutup[0].toInt())
                                        .setMinute(arrJamTutup[1].toInt())
                                        .build()
                                        .also { jamTutupTimePicker ->
                                            jamTutupTimePicker.addOnPositiveButtonClickListener {
                                                val sjambuka = jamBukaTimePicker.run {
                                                    "${
                                                        hour.toString().padStart(2, '0')
                                                    }:${minute.toString().padStart(2, '0')}"
                                                }
                                                val sjamtutup = jamTutupTimePicker.run {
                                                    "${
                                                        hour.toString().padStart(2, '0')
                                                    }:${minute.toString().padStart(2, '0')}"
                                                }
                                                Log.e(TAG, "$sjambuka - $sjamtutup")

                                                setText(
                                                    getString(
                                                        R.string.format_jam_wisata,
                                                        sjambuka,
                                                        sjamtutup
                                                    )
                                                )
                                            }
                                        }.show(supportFragmentManager, "JAMTUTUP")
                                }
                            }.show(supportFragmentManager, "JAMBUKA")
                    }
                }
            }
            btBack.setOnClickListener { finish() }
            EDalamat.apply {
                if (this@DetailWisata.isAdminView) {
                    setOnClickListener {
                        intentResult.launchActivityResult(
                            Intent(context, MapAlamat::class.java).apply {
                                putExtra("alamat", text.toString())
                            }, ActivityOptionsCompat.makeSceneTransitionAnimation(this@DetailWisata)
                        ) {
                            if (it.resultCode == RESULT_OK) {
                                val koor = it.data?.getParcelableExtra<LatLng>("location")
                                koordinat = "${koor?.latitude ?: "0.0"},${koor?.longitude ?: "0.0"}"
                                alamat = it.data?.getStringExtra("address") ?: ""
                                setText(alamat)
                            }
                        }
                    }
                }
                setText(alamat ?: "")
            }
            EDdeskripsi.apply {
                doAfterTextChanged { deskripsi = it.toString() }
                setText(deskripsi ?: "")
            }
            EDjmlpengunjung.apply {
                doAfterTextChanged { jmlPengunjung = it.toString().toIntWithSafety() }
                setText((jmlPengunjung?:0).toString())
            }
        }

        BTdirection.apply {
            setOnClickListener {
                data?.koordinat.intentToGmaps(it.context)
            }
        }
        BTdelete.apply {
            if (this@DetailWisata.isAdminView) {
                visibleOrGone((data?.wisataId ?: 0) != 0)
                setOnClickListener {
                    gvaDialogs.alertDialog(it.context)
                        .tittleText("Delete Wisata")
                        .alertText("Yakin akan menghapus wisata \"${data?.namaWisata ?: ""}\"")
                        .setBTyesListener {
                            finish()
                            VMwisata.deleteWisata(data!!.wisataId)
                        }.build().show()
                }
            }
        }
        BTyes.setOnClickListener {
            if (!this@DetailWisata.isAdminView) {
                finish()
            }else {
                TILnamawisata.apply {
                    if (editText?.text.toString().isEmpty()) {
                        error = "Nama wisata harus diisi"
                    } else isErrorEnabled = false
                }
                TILalamat.apply {
                    if (editText?.text.toString().isEmpty()) {
                        error = "Alamat wisata harus diisi"
                    } else isErrorEnabled = false
                }
                TILdeskripsi.apply {
                    if (editText?.text.toString().isEmpty()) {
                        error = "Berikan deskripsi wisata agar pengujung tertarik"
                    } else isErrorEnabled = false
                }
                if (!(TILnamawisata.isErrorEnabled || TILalamat.isErrorEnabled || TILdeskripsi.isErrorEnabled)) {
                    data?.adminId = MyUtils.MLDuserdata.value?.data?.uid ?: 0
                    Log.e(TAG, "data : $data")
                    VMwisata.addWisata(data!!)
                    finish()
                }
            }
        }
        BTno.apply {
            visibleOrGone(this@DetailWisata.isAdminView)
            setOnClickListener {
                finish()
            }
        }
    }

    private fun setObservers() = VMwisata.apply {
        MLDaddWisata.observe(this@DetailWisata) {
            when(it.status){
                Status.LOADING -> {}
                Status.FAILED -> {}
                Status.SUCCESS -> if (it.data == true) VMwisata.uploadWisataPhoto(data?.wisataId?:0,)
            }
        }
        MLDuploadPhoto.observe(this@DetailWisata) {
            when(it.status){
                Status.LOADING -> {}
                Status.FAILED -> {}
                Status.SUCCESS -> if (it.data == true) finish()
            }
        }
    }

    private fun getStringImage(bmp: Bitmap) = ByteArrayOutputStream().let { baos ->
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray()
        Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private val getImageContract = registerForActivityResult(ActivityResultContracts.GetContent()){ uri ->
        Log.d(TAG, "uris : $uri")
        uri?.let {
            startCrop(it)
        }
    }

    private fun startCrop(uri: Uri) {
        val cropIntent = CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(2,1)
            .setBorderCornerColor(ContextCompat.getColor(this, R.color.blue_dark))
            .setGuidelinesColor(ContextCompat.getColor(this, R.color.blue_light))
            .getIntent(this)

        cropResult.launch(cropIntent)
    }

    private val cropResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        when (it.resultCode) {
            RESULT_OK -> {
                CropImage.getActivityResult(it.data)?.let { result ->
                    bmpImageForUpload = BitmapFactory.decodeFile(result.uri.path?:"")
                    vbind.ivWisataPhoto.setImageBitmap(bmpImageForUpload)
                }
            }
        }
    }
}