package com.groovernapp.MyUtils

import android.app.Activity
import android.content.Context
import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.groovernapp.data.models.UserData
import com.groovernapp.insadi.R
import org.json.JSONArray
import java.security.MessageDigest

object MyUtils {
    class CLAnimationseq(iduration: Long) : TransitionSet() {
        init {
            duration = iduration
            ordering = ORDERING_TOGETHER
            addTransition(ChangeBounds().setInterpolator(DecelerateInterpolator()))
            addTransition(Fade(Fade.OUT))
            addTransition(Fade(Fade.IN))
        }
    }
    private val TAG = this::class.java.simpleName

    val MLDuserdata = MutableLiveData<Resource<UserData>>()
    var dlgFailConnection = DlgFailConnection()

    fun toMd5(data: String?): String? = if (data != null) {
        val HEX_CHARS = "0123456789ABCDEF"
        val bytes = MessageDigest
            .getInstance("MD5")
            .digest(data.toByteArray())
        StringBuilder(bytes.size * 2).apply {
            bytes.forEach {
                val i = it.toInt()
                append(HEX_CHARS[i shr 4 and 0x0f])
                append(HEX_CHARS[i and 0x0f])
            }
        }.toString()
    }else null

    fun showKeyboard(act: Activity, show: Boolean) {
        val imm: InputMethodManager =
            act.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (show) {
            imm.showSoftInput(act.currentFocus, 0)
        } else {
            imm.hideSoftInputFromWindow(act.currentFocus?.windowToken, 0)
        }
    }

    fun animateCLseq(cl: ConstraintLayout, cset: ConstraintSet, iduration: Long) {
        TransitionManager.beginDelayedTransition(cl, CLAnimationseq(iduration))
        cset.applyTo(cl)
    }

    //----- Gson() needed
    fun <T> String?.toDataModel(typeToken: TypeToken<T>): T? =
        try {
            Gson().fromJson(this?:"", typeToken.type)
        }catch (e: Exception){
            Log.e(this@MyUtils.toString(),"toDataModel : $e")
            null
        }
    fun <T> String?.toDataModel(classOfT: Class<T>, nullInException: Boolean = false): T? =
        try {
            Gson().fromJson(this?:"", classOfT)
        }catch (e: Exception){
            Log.e(this@MyUtils.toString(),"toDataModel : $e\ntheJson : $this")
            if (nullInException) null
            else classOfT.newInstance()
        }

    fun Any?.toJson(): String {
        return Gson().toJson(this)
    }

    //===== VISIBILITY =====//

    fun View.goVisible() {
        if (visibility != View.VISIBLE)
            visibility = View.VISIBLE
    }

    fun View.goInvisible() {
        if (visibility != View.INVISIBLE)
            visibility = View.INVISIBLE
    }

    fun View.goGone() {
        if (visibility != View.GONE)
            visibility = View.GONE
    }

    fun View.visibleOrGone(condition: Boolean) = if (condition) goVisible() else goGone()
    fun View.visibleOrInvisible(condition: Boolean) = if (condition) goVisible() else goInvisible()

    fun String?.toIntWithSafety(): Int {
        return if (this != null) {
            if (this.isEmpty()) {
                0
            } else {
                try {
                    this.toInt()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    0
                }
            }
        } else {
            0
        }
    }

    fun Context.newChip(text: String?, chipId: Int) = Chip(this).apply {
        id = chipId
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        setPadding(4, 0, 4, 0)
        this.text = text?:""
        setTextSize(14f)
        setChipBackgroundColorResource(R.color.light_gray)
        isCheckable  = true
        checkedIcon = null
        setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                setChipBackgroundColorResource(R.color.gray)
                setTextColor(ContextCompat.getColor(context, R.color.white))
            }else {
                setChipBackgroundColorResource(R.color.light_gray)
                setTextColor(ContextCompat.getColor(context, R.color.black))
            }
        }
    }

    /**
     * [chipListJson] - string of json array of chip list source
     * [nameObj] - Object name from [chipListJson] for chip text, default : "name"
     * [idObj] - Object name from [chipListJson] for chip id. The result of this object will be
     * converted to integer, default : "id"
     * [prefixChipName] - add extra chip in the first index. Default : "All" | noPrefix : null
     * */
    fun ChipGroup.populateChip(chipJsonArray: String, nameObj: String = "name", idObj: String = "id",
                               prefixChipName: String? = context.getString(R.string.semua)){
        val chipJson = JSONArray(chipJsonArray)
        Log.d("PopulateChip","chipJson : $chipJson")
        clearCheck()
        removeAllViews()
        prefixChipName?.let { addView(context.newChip(it, 0)) }
        for (i in 0 until chipJson.length()){
            val obj = chipJson.getJSONObject(i)
            Log.d("PopulateChip","[$i] name[$nameObj] : ${obj.optString(nameObj)} | id[$idObj] : ${obj.opt(idObj)?.toString().toIntWithSafety()}")
            addView(context.newChip(obj.optString(nameObj), obj.opt(idObj)?.toString().toIntWithSafety()))
        }
    }
    fun ChipGroup.populateChip(arrString: ArrayList<String>, prefixChipName: String? = context.getString(R.string.semua)){
        clearCheck()
        removeAllViews()
        prefixChipName?.let { addView(context.newChip(it, 0)) }
        for (i in arrString.indices){
            addView(context.newChip(arrString[i], i+1))
        }
    }
}