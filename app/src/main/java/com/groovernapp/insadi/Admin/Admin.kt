package com.groovernapp.insadi.Admin

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.groovernapp.data.models.UserData
import com.groovernapp.MyUtils.Enums.JenisJabatan
import com.groovernapp.MyUtils.Enums.Status
import com.groovernapp.MyUtils.MyUtils
import com.groovernapp.ViewModels.VMAdmin
import com.groovernapp.insadi.R
import com.groovernapp.insadi.databinding.ActAdminBinding
import com.groovernapp.insadi.databinding.DlgAddadminBinding

class Admin: AppCompatActivity() {
    private val TAG = this::class.java.simpleName
    private lateinit var Vbind: ActAdminBinding
    val VMadmin: VMAdmin by viewModels()
    private var dlgAdmin: BottomSheetDialog? = null
    private val adminAdapter = AdminRVAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Vbind = ActAdminBinding.inflate(layoutInflater)
        setContentView(Vbind.root)

        setUI()
        setObserver()
    }

    override fun onResume() {
        super.onResume()
        MyUtils.dlgFailConnection.setContext(this)
    }

    private fun setUI() = Vbind.run {
        AdminIMGback.setOnClickListener { finish() }
        AdminEDsearch.setOnEditorActionListener { tv, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                VMadmin.listAdmin(tv.text.toString())
                true
            } else false
        }
        AdminRVlistadmin.apply {
            adapter = adminAdapter
            addItemDecoration(DividerItemDecoration(this@Admin, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(this@Admin, R.drawable.divider_rv_10dp)!!)
            })
        }
        AdminSRlistadmin.apply {
            setOnRefreshListener {
                Handler(Looper.getMainLooper()).post {
                    AdminEDsearch.setText("")
                    VMadmin.listAdmin()
                    isRefreshing = false
                }
            }
        }
        AdminFABaddadmin.setOnClickListener {
            showDlgAdmin()
        }
        VMadmin.listAdmin("")
    }

    private fun setObserver() = VMadmin.run {
        MLDlistAdmin.observe(this@Admin){
            when(it?.status){
                Status.LOADING -> {}
                Status.FAILED -> {}
                Status.SUCCESS -> Vbind.run {
                    adminAdapter.submitList(it.data?.apply { add(UserData()) }?.toList())
                    dlgAdmin?.dismiss()
                }
                else -> {}
            }
        }
    }

    fun showDlgAdmin(data: UserData? = UserData()) = BottomSheetDialog(this, R.style.transparentsheetdialog).let { dlg ->
        DlgAddadminBinding.inflate(LayoutInflater.from(this@Admin), Vbind.root, false).run {
            dlgAdmin = dlg
            dlg.setContentView(root)
            dlg.behavior.apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback(){
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_DRAGGING){
                            state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {  }
                })
            }
            data?.apply {
                if (uid == 0){
                    DlgRegAdminTVttl.text = getString(R.string.tambah_admin)
                    DlgRegAdminTILpwd.visibility = VISIBLE
                    DlgRegAdminTILpwdconf.visibility = VISIBLE
                }else {
                    DlgRegAdminTVttl.text = getString(R.string.edit_admin)
                    DlgRegAdminTILpwd.visibility = GONE
                    DlgRegAdminTILpwdconf.visibility = GONE
                }
                DlgRegAdminEDusername.apply {
                    doAfterTextChanged { data.username = text.toString() }
                    setText(username?:"")
                }
                DlgRegAdminEDnama.apply {
                    doAfterTextChanged { data.nama = text.toString() }
                    setText(nama ?: "")
                }
                DlgRegAdminEDtelp.apply {
                    doAfterTextChanged { data.telp = text.toString() }
                    setText(telp ?: "")
                }
                DlgRegAdminEDalamat.apply {
                    doAfterTextChanged { data.alamat = text.toString() }
                    setText(alamat ?: "")
                }
                DlgRegAdminRGjabatan.setOnCheckedChangeListener { _, i ->
                    when(i){
                        R.id.DlgRegAdmin_RBTadmin -> data.jabatan = JenisJabatan.ADMIN
                        R.id.DlgRegAdmin_RBTmaster -> data.jabatan = JenisJabatan.MASTER
                    }
                }
                when(jabatan){
                    JenisJabatan.ADMIN -> DlgRegAdminRBTadmin.isChecked = true
                    JenisJabatan.MASTER -> DlgRegAdminRBTmaster.isChecked = true
                    else -> DlgRegAdminRBTadmin.isChecked = true
                }
            }

            DlgRegAdminBTyes.setOnClickListener {
                DlgRegAdminTILusername.apply {
                    if (editText?.text.toString().isEmpty()){
                        error = "Username harus diisi"
                    }else isErrorEnabled = false
                }
                DlgRegAdminTILnama.apply {
                    if (editText?.text.toString().isEmpty()){
                        error = "Nama harus diisi"
                    }else isErrorEnabled = false
                }
                DlgRegAdminTILtelp.apply {
                    if (editText?.text.toString().isEmpty()){
                        error = "No. Handphone harus diisi"
                    }else isErrorEnabled = false
                }
                DlgRegAdminTILalamat.apply {
                    if (editText?.text.toString().isEmpty()){
                        error = "Alamat harus diisi"
                    }else isErrorEnabled = false
                }
                DlgRegAdminTILpwd.apply {
                    if (visibility == VISIBLE && editText?.text.toString().isEmpty()){
                        error = "Password harus diisi"
                    }else isErrorEnabled = false
                }
                if (DlgRegAdminEDpwd.visibility == VISIBLE && DlgRegAdminEDpwdconf.visibility == VISIBLE
                    && DlgRegAdminEDpwd.text.toString() != DlgRegAdminEDpwdconf.text.toString()){
                    DlgRegAdminTILpwd.error = "Password dan Konfirmasi Password tidak sama"
                    DlgRegAdminTILpwdconf.error = "Password dan Konfirmasi Password tidak sama"
                }else {
                    DlgRegAdminTILpwd.isErrorEnabled = false
                    DlgRegAdminTILpwdconf.isErrorEnabled = false
                }
                if (!(DlgRegAdminTILusername.isErrorEnabled || DlgRegAdminTILnama.isErrorEnabled || DlgRegAdminTILtelp.isErrorEnabled
                    || DlgRegAdminTILalamat.isErrorEnabled || DlgRegAdminTILpwd.isErrorEnabled || DlgRegAdminTILpwdconf.isErrorEnabled)){
                    VMadmin.addAdmin(data!!, DlgRegAdminEDpwd.text.toString())
                }
            }
            DlgRegAdminBTno.setOnClickListener {
                dlg.dismiss()
            }
        }
        if (dlgAdmin?.isShowing != true){
            dlg.show()
        }
    }
}