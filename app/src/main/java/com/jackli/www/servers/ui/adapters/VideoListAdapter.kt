package com.jackli.www.servers.ui.adapters

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView

import com.jackli.www.servers.R
import com.jackli.www.servers.utils.StringUtils

/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.okplayer.ui.adapters
 * @description: description
 * @date: 2017/12/20
 * @time: 19:32
 */
class VideoListAdapter(context: Context, c: Cursor?, autoRequery: Boolean) : CursorAdapter(context, c, autoRequery) {

    /**
     * 创建新的view和viewholder
     *
     * @param context
     * @param cursor
     * @param parent
     * @return
     */
    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        val iRootView = LayoutInflater.from(context).inflate(R.layout.video_list_item, parent, false)
        val holder = ViewHolder()
        holder.iTitle = iRootView.findViewById(R.id.video_name)
        holder.iDuration = iRootView.findViewById(R.id.video_time)
        holder.iSize = iRootView.findViewById(R.id.video_size)
        iRootView.tag = holder
        return iRootView
    }

    /**
     * 填充条目内容,此时view不可能为空，cursor已经移动到了指定的位置，只要解析内容即可
     *
     * @param view
     * @param context
     * @param cursor
     */
    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val iHolder = view.tag as ViewHolder
        iHolder.iTitle!!.text = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
        val duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
        iHolder.iDuration!!.text = StringUtils.formatDuration(duration)
        val size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
        iHolder.iSize!!.text = Formatter.formatFileSize(context, size)
    }

    private inner class ViewHolder {
        internal var iTitle: TextView? = null
        internal var iDuration: TextView? = null
        internal var iSize: TextView? = null
    }

}
