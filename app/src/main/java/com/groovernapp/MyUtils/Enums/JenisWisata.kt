package com.groovernapp.MyUtils.Enums

import com.groovernapp.insadi.R

enum class JenisWisata(val value: String, val idx: Int, val icon: Int) {
    ALAM("ALAM", 1, R.drawable.ic_wisata_alam),
    BUATAN("BUATAN", 2, R.drawable.ic_wisata_buatan),
    RELIGI("RELIGI", 3, R.drawable.ic_wisata_religi),
    KULINER("KULINER", 4, R.drawable.ic_wisata_kuliner),
    TERFAVORIT("TERFAVORIT", 5, 0);

    companion object{
        fun getEnumName(value: String): JenisWisata? = values().associateBy(JenisWisata::value)[value]
    }
}