package com.groovernapp.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.groovernapp.MyUtils.Enums.Status
import com.groovernapp.MyUtils.MyUtils
import com.groovernapp.MyUtils.Resource
import com.groovernapp.MyUtils.toDos
import com.pixplicity.easyprefs.library.Prefs
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object RESTClient {
    private val TAG = javaClass.simpleName
    private val Client = OkHttpClient.Builder()
        .connectTimeout(3000, TimeUnit.MILLISECONDS)
        .readTimeout(10000, TimeUnit.MILLISECONDS)
        .addInterceptor {
            val response = it.proceed(it.request())

            if (!response.isSuccessful){
                Log.e(TAG, "failed -> ${response.message} || code : ${response.code}")
                MyUtils.dlgFailConnection.onRequestFailed(response.message, response.code){ _-> it.proceed(it.request()) }
            }
            response
        }
        .build()
    private var jso = JSONObject()
    private var jsoparam = JSONObject()
//    private val url = "http://192.168.240.250:8080/insadi/"
//    private val url = "http://192.168.31.133/insadi/"
    var url = Prefs.getString("IP").let {
        if (it.isNullOrEmpty()) "http://192.168.240.250:8080/insadi/"
        else "http://$it/insadi/"
    }
    private val request = Request.Builder().apply {
        url(url)
    }

    fun command(command: String) = apply {
        jso = JSONObject().apply { put("cmd", command) }
        jsoparam = JSONObject()
    }
    fun addParam(key: String, value: Any) = apply { jsoparam.put(key, value.toString()) }
    fun addParam(jso: JSONObject) = apply { jsoparam = jso }

    fun <T> post(functionTAG: String, mld: MutableLiveData<Resource<T>>, action: (respData: JSONObject)-> Resource<T>?){
        Client.newCall(request
            .post(jso.put("params", jsoparam).toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build().also {
                Log.e(TAG, "url : $url\nparams : $jso || $it")
            }).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "$functionTAG -> error = ${e.message}")
                MyUtils.dlgFailConnection.onRequestFailed(0){ call.clone().enqueue(this) }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { resp ->
                    Log.e(TAG, "$functionTAG -> sent_time : ${response.sentRequestAtMillis}ms || " +
                            "response_time : ${response.receivedResponseAtMillis}ms\n" +
                            "header : ${response.headers}\n" +
                            "response : ${resp.trim()}")
                    if (response.isSuccessful) {
                        try {
                            JSONObject(resp).let{ respdata ->
                                action(respdata)?.let {
                                    if (it.status == Status.FAILED && it.data == null){
                                        MyUtils.dlgFailConnection.onRequestFailed(it.msg)
                                    }
                                }
                                mld.postValue(action(respdata))
                                (respdata.optString("todo")?:"").let { todo ->
                                    if (todo.isNotEmpty()) {
                                        toDos(todo, respdata.optJSONObject("todoData")).execute()
                                    }
                                }
                            }
                        }catch (e: JSONException){
                            Log.e(TAG, "$functionTAG -> error = ${e.message}")
                            MyUtils.dlgFailConnection.onRequestFailed("JSONexception : ${e.message}")
                        }
                    }
                }
            }

        })
    }
}