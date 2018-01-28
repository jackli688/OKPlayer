package com.jackli.www.servers.model.db

import android.content.AsyncQueryHandler
import android.content.ContentResolver
import android.database.Cursor
import android.widget.CursorAdapter

/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.okplayer.model.db
 * @description: description
 * @date: 2017/12/20
 * @time: 19:31
 */
class MyAsyncQueryHandler(resolver: ContentResolver) : AsyncQueryHandler(resolver) {

    override fun onQueryComplete(token: Int, cookie: Any, cursor: Cursor) {
        super.onQueryComplete(token, cookie, cursor)
        val adapter = cookie as CursorAdapter
        adapter.swapCursor(cursor)
    }
}
