package com.groovernapp.insadi

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.groovernapp.MyUtils.Enums.JenisJabatan
import com.groovernapp.MyUtils.Enums.Status
import com.groovernapp.MyUtils.MyUtils
import com.groovernapp.MyUtils.MyUtils.toJson
import com.groovernapp.ViewModels.VMLogin
import com.groovernapp.data.RESTClient
import com.groovernapp.insadi.databinding.ActLoginBinding
import com.groovernapp.insadi.databinding.DlgInsertIpBinding
import com.pixplicity.easyprefs.library.Prefs

class Login : AppCompatActivity() {
    private val TAG = this::class.java.simpleName
    private lateinit var Vbind: ActLoginBinding
    private val VMlogin: VMLogin by viewModels()
    private var countShowIp = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Vbind = ActLoginBinding.inflate(layoutInflater)
        setContentView(Vbind.root)

        setUI()
        setObserver()
    }

    override fun onResume() {
        super.onResume()
        MyUtils.dlgFailConnection.setContext(this)
    }

    private fun setUI() = Vbind.run {
        LoginCV.setOnClickListener {
            countShowIp++
            if (countShowIp == 5){
                countShowIp = 0
                showIpEditorDialog()
            }else Handler(mainLooper).postDelayed({
                countShowIp = 0
            }, 5000)
        }
        LoginEDpwd.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                LoginBTlogin.performClick()
                true
            } else false
        }
        LoginBTlogin.setOnClickListener {
            LoginTILusername.apply {
                if (editText?.text.toString().isEmpty()){
                    error = "Username harus diisi"
                }else isErrorEnabled = false
            }
            LoginTILpwd.apply {
                if (editText?.text.toString().isEmpty()){
                    error = "Password harus diisi"
                }else isErrorEnabled = false
            }
            if (!(LoginTILusername.isErrorEnabled || LoginTILpwd.isErrorEnabled)){
                VMlogin.login(LoginEDusername.text.toString(), LoginEDpwd.text.toString())
            }

        }
    }
    private fun setObserver() = VMlogin.run {
        MyUtils.MLDuserdata.observe(this@Login){
            when(it?.status){
                Status.LOADING -> loading(true)
                Status.FAILED -> loading(false)
                Status.SUCCESS -> {
                    if (it.data?.jabatan != JenisJabatan.PENGUNJUNG) {
                        loading(false)
                        it.data?.let{ data-> Prefs.putString("userData", data.toJson ()) }
                        startActivity(Intent(this@Login, MainView::class.java))
                        finish()
                        Toast.makeText(this@Login, it.msg ?: "", Toast.LENGTH_LONG).show()
                    }
                }
                else -> {}
            }
        }
    }

    private fun loading(isloading: Boolean) = Vbind.run {
        LoginBTlogin.visibility = if (isloading) INVISIBLE else VISIBLE
        LoginPB.visibility = if (isloading) VISIBLE else INVISIBLE
    }

    private fun showIpEditorDialog() = BottomSheetDialog(this).apply{
        DlgInsertIpBinding.inflate(LayoutInflater.from(this@Login), Vbind.root, false).run {
            setContentView(root)

            btSet.setOnClickListener {
                val ip = tilIp.editText?.text.toString()
                RESTClient.url = "http://$ip/insadi/"
                Prefs.putString("IP", ip)
                Log.d("showIpEditorDialog","Set IP to -> ${RESTClient.url}")
                this@apply.dismiss()
            }
        }
    }.show()
}