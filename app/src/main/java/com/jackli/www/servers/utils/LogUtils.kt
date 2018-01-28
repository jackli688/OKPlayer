package com.jackli.www.servers.utils

import android.util.Log

object LogUtils {

    private val ENABLE = false

    /** 打印一个 debug 等级的 log  */
    @JvmStatic
    fun d(tag: String, msg: String) {
        if (ENABLE) {
            Log.d(tag, msg)
        }
    }

    /** 打印一个 error 等级的 log  */
    @JvmStatic
    fun e(tag: String, msg: String) {
        if (ENABLE) {
            Log.e(tag, msg)
        }
    }

    /** 打印一个 error 等级的 log  */
    @JvmStatic
    fun e(tag: Class<*>, msg: String) {
        if (ENABLE) {
            Log.e(tag.simpleName, msg)
        }
    }
}
