package com.groovernapp.data.models

import com.groovernapp.MyUtils.Enums.JenisJabatan

data class UserData(val uid: Int = 0, var username: String? = "", var regDate: String? = "", var nama: String? = "",
                    var alamat: String? = "", var telp: String? = "", var jabatan: JenisJabatan? = JenisJabatan.PENGUNJUNG)