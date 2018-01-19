package com.jackli.www.okplayer.utils

import android.annotation.SuppressLint

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Created by Administrator on 2016/8/7.
 */
object StringUtils {

    private val TAG = "StringUtils"

    // 将时间戳转换为 00:11, 01:00:11
    @SuppressLint("DefaultLocale")
    fun formatDuration(duration: Int): String {
        val hours = duration / 1000 / 3600
        val minutes = duration / 1000 / 60 % 60
        val seconds = duration / 1000 % 60
        return if (hours < 1) {
            // 小于一小时 00:11
            String.format("%02d:%02d", minutes, seconds)
        } else {
            // 大于一小时 01:11:22
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    // 获取当前系统时间的格式化字符串 23:59:59
    fun formatSystemTime(): String {
        val format = SimpleDateFormat("hh:mm:ss", Locale.CHINA)
        return format.format(Date())
    }
}
