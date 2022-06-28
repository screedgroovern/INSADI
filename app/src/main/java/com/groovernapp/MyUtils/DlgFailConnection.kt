package com.groovernapp.MyUtils

import android.content.DialogInterface
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.groovernapp.insadi.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.groovernapp.data.RESTClient
import com.groovernapp.insadi.databinding.DlgInsertIpBinding
import com.pixplicity.easyprefs.library.Prefs

class DlgFailConnection(var act: AppCompatActivity? = null) {
    private val TAG = this::class.java.simpleName
    private var dlg: BottomSheetDialog? = if (act == null) null else BottomSheetDialog(act!!)
    private var odlRetry: DialogInterface.OnDismissListener? = null
    private var Istatuscode = 0

    fun setContext(actvt: AppCompatActivity) {
        act = actvt
        buildDlg()
    }
    fun onRequestFailed(istatuscode: Int, odlretry: DialogInterface.OnDismissListener? = null){
        onRequestFailed(null, istatuscode, odlretry)
    }
    fun onRequestFailed(msg: String?, istatuscode: Int? = null, odlretry: DialogInterface.OnDismissListener? = null){
        Log.e(TAG, "msg : $msg")
        Handler(Looper.getMainLooper()).post{
            if (msg != null) Toast.makeText(act, msg, Toast.LENGTH_LONG).show()
            else showDlg(istatuscode!!, odlretry)
        }
    }

    private fun buildDlg(){
        dlg = gvaDialogs.alertDialog(act!!)
            .tittleText(run{
                if (Istatuscode != 0){
                    "Sedang pemeliharaan jaringan"
                }else {
                    "Oops! Koneksi Anda terputus"
                }
            })
            .alertText(run{
                if (Istatuscode != 0){
                    "Server kami sedang dalam pemeliharaan. Silahkan coba beberapa waktu lagi.\nMohon maaf atas ketidaknyamannannya"
                }else {
                    "Coba cari koneksi WiFi lain atau periksa paket data Anda"
                }
            })
            .setBTnoListener{
                act!!.startActivity(Intent(Settings.ACTION_SETTINGS))//Settings -> Settings Provider
            }
            .setBTnoText(("Lihat Settings"), 14)
            .setBTyesText((if (odlRetry == null) act!!.getString(R.string.ok) else "Coba Lagi"), 14)
            .activeIpEditorAccess(true)
            .build().also {
                it.setCanceledOnTouchOutside(false)
                if (odlRetry != null) it.setOnDismissListener(odlRetry)

                it.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                it.behavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback(){
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_DRAGGING){
                            it.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {

                    }
                })
            }
    }
    private fun showDlg(istatuscode: Int, odlretry: DialogInterface.OnDismissListener? = null) {
        if (!dlg!!.isShowing) {
            Istatuscode = istatuscode
            odlRetry = odlretry
            buildDlg()
            dlg?.show()
        }
    }

}