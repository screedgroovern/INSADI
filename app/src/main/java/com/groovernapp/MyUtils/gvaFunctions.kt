package com.groovernapp.MyUtils

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.ParseException
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.model.LatLng
import com.groovernapp.insadi.R
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object gvaFunctions {
    enum class DisplayMetricSide { WIDTH, HEIGHT }

    private val TAG = javaClass.simpleName
    fun logoutAccount(con: Context){
//        SQLiteDB(con).clearLogin(MyUtils.userData?.wa)
//        con.startActivity(Intent(con, Login::class.java))
        Handler(Looper.getMainLooper()).postDelayed({
            con.sendBroadcast(Intent().apply { action = "LOGOUT" })
        }, 1000)
    }

    fun callNotif(con: Context, title: String?, message: String?, intent: Intent) {
        Log.e(TAG, "notifcalled")
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        val PI = PendingIntent.getActivity(con, 11, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val mBuilder =
            NotificationCompat.Builder(con, "notastats")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                    .bigText(message)
                )
                .setContentIntent(PI)
                .setAutoCancel(true)
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        val notificationManager = con.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mchanel = NotificationChannel("notastats", "Nota", NotificationManager.IMPORTANCE_HIGH)
            mBuilder.setChannelId("notastats")
            notificationManager.createNotificationChannel(mchanel)
        }
        notificationManager.notify(1, mBuilder.build())
    }

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

    fun sendWhatsAppMsg(act: Activity, telp: String, msg: String = "") = Intent(Intent.ACTION_VIEW).run {
        try {
            val url ="https://api.whatsapp.com/send?phone=${
                if (telp.substring(0, 2) != "62") "62${telp.substring(1, telp.length)}" else telp
            }&text=" + URLEncoder.encode(msg,"UTF-8")
            Log.e("WhatsApp", url)
            setPackage("com.whatsapp")
            data = Uri.parse(url)
            if (resolveActivity(act.packageManager) != null) act.startActivity(this)
            else Toast.makeText(act, "WhatsApp not Installed", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stringDatetoMilliseconds(strDate: String, dateFormat: String): Long? =
        try {
            SimpleDateFormat(dateFormat, Locale.getDefault()).parse(strDate)?.time
        } catch (e: Exception) {
            Log.e(TAG, "ParseDateError -> ${e.message}")
            null
        }

    fun showKeyboard(act: Activity, show: Boolean) {
        val imm: InputMethodManager =
            act.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (show) {
            imm.showSoftInput(act.currentFocus, 0)
        } else {
            imm.hideSoftInputFromWindow(act.currentFocus?.windowToken, 0)
        }
    }

    fun String?.toLatLng() = this?.run {
        val arrLatLng = split(",")
        LatLng(arrLatLng[0].trim().toDouble(), arrLatLng[1].trim().toDouble())
    }

    fun String?.intentToGmaps(con: Context) = toLatLng().intentToGmaps(con)
    fun LatLng?.intentToGmaps(con: Context) =
        try {
            Log.e(TAG, "coordinate : $this")
            this?.let {
                Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${it.latitude},${it.longitude})")).apply {
                    setPackage("com.google.android.apps.maps")
                    con.startActivity(this)
                }
            }
            true
        }catch (e: Exception){
            Log.e(TAG, "IntentGmaps Exception : $e")
            Toast.makeText(con, "IntentGmaps Exception : $e", Toast.LENGTH_SHORT).show()
            false
        }

    fun animateCL(cl: ConstraintLayout, cset: ConstraintSet, iduration: Long) {
        TransitionManager.beginDelayedTransition(cl, CLAnimation(iduration))
        cset.applyTo(cl)
    }
    fun typedvalueToDp(value: Int, con: Context?): Int = typedvalueToDp(value.toFloat(), con)
    fun typedvalueToDp(value: Float, con: Context?): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, con?.resources?.displayMetrics).toInt()
    fun pixelsToSp(act: Activity, px: Float): Float = px/act.resources.displayMetrics.scaledDensity

    @Suppress("DEPRECATION")
    fun dynamicTextSize(act: Activity, textSize: Int): Float = DisplayMetrics().run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            act.display?.getRealMetrics(this)
        } else {
            act.windowManager.defaultDisplay.getMetrics(this)
        }
        val density: Float = act.resources.displayMetrics.density
        val width = widthPixels / density

        width * textSize * 0.0025f
    }


    @Suppress("DEPRECATION")
    fun getDisplayMetric(act: Activity, DisplayMetricSide: DisplayMetricSide, inDp: Boolean = false) = DisplayMetrics().run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            act.display?.getRealMetrics(this)
        } else {
            act.windowManager.defaultDisplay.getMetrics(this)
        }
        (if (inDp) act.resources.displayMetrics.density else 1f).let { density ->
            when(DisplayMetricSide){
                gvaFunctions.DisplayMetricSide.WIDTH -> widthPixels / density
                gvaFunctions.DisplayMetricSide.HEIGHT -> heightPixels / density
            }
        }
    }

    class CLAnimation(iduration: Long) : TransitionSet() {
        init {
            duration = iduration
            ordering = ORDERING_TOGETHER
            addTransition(ChangeBounds().setInterpolator(DecelerateInterpolator()))
            addTransition(Fade(Fade.OUT))
            addTransition(Fade(Fade.IN))
        }
    }
}