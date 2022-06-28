package com.groovernapp.data.models

import android.os.Parcelable
import com.groovernapp.MyUtils.Enums.JenisWisata

data class WisataData(
    val wisataId: Int = 0,
    var groupWisata: JenisWisata? = JenisWisata.ALAM,
    var namaWisata: String? = "",
    var hrgTiket: Int? = 0,
    var jamBuka: String? = null,
    var jamTutup: String? = null,
    var alamat: String? = null,
    var koordinat: String? = "",
    var deskripsi: String? = null,
    var distance: Double? = 0.0,
    var jmlPengunjung: Int? = null,
    var photo: String? = null,
    var adminId: Int? = 0
)