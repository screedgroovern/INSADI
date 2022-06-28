package com.groovernapp.MyUtils

import android.util.Log
import org.json.JSONObject

class toDos (type: String, val todoData: JSONObject? = null){
    enum class Type (val stringVal: String){
        LOGIN("login"),
        ACTIVATE_TELP("activateTelp");
    }

    private val TAG = this::class.java.simpleName
    val todoType = getEnumName(type)
    val context = MyUtils.dlgFailConnection.act!!.baseContext

    private fun getEnumName(value: String): Type? = Type.values().associateBy(Type::stringVal)[value]
    fun execute(){
        Log.e(TAG, "ToDo : $todoType")
        when(todoType){
//            Type.LOGIN -> LoginRepo.manageLoginResponse(context, todoData!!)
//            Type.ACTIVATE_TELP -> MemberRepo.register(JSONObject().apply {
//                put("KodeMember", todoData!!.optString("KodeMember"))
//                put("TelpConf", 1)
//            })
            else -> Log.e(TAG, "Unknown todo-Type")
        }
    }
}