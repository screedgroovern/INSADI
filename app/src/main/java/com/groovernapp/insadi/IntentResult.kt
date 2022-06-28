package com.groovernapp.insadi

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class IntentResult(private val registry : ActivityResultRegistry): DefaultLifecycleObserver {
    private lateinit var intentResult : ActivityResultLauncher<Intent>
    //    private var intentType: Intent? = null
    private var UnitAction: (ActivityResult) -> Unit = {  }

    override fun onCreate(owner: LifecycleOwner) {
        intentResult = registry.register("key", owner, ActivityResultContracts.StartActivityForResult()) { result ->
            UnitAction(result)
        }
    }

    fun launchActivityResult(intnt: Intent, option: ActivityOptionsCompat? = null, unitAction: (ActivityResult) -> Unit) {
        UnitAction = unitAction
        intentResult.launch(intnt, option)
    }
}