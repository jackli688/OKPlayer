package com.jackli.www.okplayer.utils

import android.util.Log

object LogUtils {
    private val ENABLE = true

    /** 打印一个 debug 等级的 log  */
    fun d(tag: String, msg: String) {
        if (ENABLE) {
            Log.d(tag, msg)
        }
    }

    /** 打印一个 error 等级的 log  */
    fun e(tag: String, msg: String) {
        if (ENABLE) {
            Log.e(tag, msg)
        }
    }

    /** 打印一个 error 等级的 log  */
    fun e(tag: Class<*>, msg: String) {
        if (ENABLE) {
            Log.e(tag.simpleName, msg)
        }
    }
}
