package com.groovernapp.MyUtils

import android.content.DialogInterface
import com.groovernapp.MyUtils.Enums.Request
import com.groovernapp.MyUtils.Enums.Status
import org.json.JSONObject

data class Resource<out T>(val status: Status, val data: T? = null, val msg: String? = null, val statusCode: Int? = null,
                           val retryAction: DialogInterface.OnDismissListener? = null, val todo: String? = null, val todoData: JSONObject? = null,
                           val request: Request = Request.GET) {

    companion object {
        fun <T> success(data: T?, msg: String? = "", todo: String? = null, request: Request = Request.GET): Resource<T> =
            Resource(Status.SUCCESS, data, msg, request = request)
        fun <T> failed(statusCode: Int, todo: String? = null, retry:DialogInterface.OnDismissListener? = null): Resource<T> =
            Resource(Status.FAILED, statusCode = statusCode, todo = todo, retryAction = retry)
        fun <T> failed(msg: String?, todo: String? = null, retry:DialogInterface.OnDismissListener? = null): Resource<T> =
            Resource(Status.FAILED, msg = msg, todo = todo, retryAction = retry)
        fun <T> failed(data: T?, msg: String?, todo: String? = null, retry:DialogInterface.OnDismissListener? = null): Resource<T> =
            Resource(Status.FAILED, data, msg = msg, todo = todo, retryAction = retry)
        fun <T> loading(msg: String? = null): Resource<T> = Resource(Status.LOADING, msg = msg)
    }
}
