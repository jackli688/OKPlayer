package com.jackli.www.okplayer.utils

import android.database.Cursor

/**
 * Created by Administrator on 2016/8/7.
 */
object CursorUtils {

    private val TAG = "CursorUtils"

    // 打印 cursor 里的所有内容
    fun printCursor(cursor: Cursor) {
        LogUtils.e(TAG, "CursorUtils.printCursor,count=" + cursor.count)
        // 打印查询到的数据
        while (cursor.moveToNext()) {
            LogUtils.e(TAG, "CursorUtils.printCursor,=======================")
            for (i in 0 until cursor.columnCount) {
                LogUtils.e(TAG, "CursorUtils.printCursor,name=" + cursor.getColumnName(i) + ";value=" + cursor.getString(i))
            }
        }
    }
}
