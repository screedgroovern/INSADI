package com.groovernapp.MyUtils.Enums

enum class JenisJabatan(val value: String) {
    PENGUNJUNG("PENGUNJUNG"),
    ADMIN("ADMIN"),
    MASTER("MASTER");

    companion object{
        fun getEnumName(value: String): JenisJabatan? = values().associateBy(JenisJabatan::value)[value]
    }
}