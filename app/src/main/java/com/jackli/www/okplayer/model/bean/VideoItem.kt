package com.jackli.www.okplayer.model.bean

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore

import java.util.ArrayList


/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.okplayer.model.bean
 * @description: description
 * @date: 2017/12/20
 * @time: 19:46
 */
class VideoItem : Parcelable {

    // 成员变量
    var title: String? = null
    var path: String? = null
    var size: Long = 0
    var duration: Int = 0


    override fun describeContents(): Int {
        return 0
    }


    override fun writeToParcel(dest: Parcel, flags: Int) {
        //1.必须按成员变量声明的数序封装数据，不然会出现获取数据错误
        //2.序列化对象
        dest.writeString(title)
        dest.writeString(path)
        dest.writeLong(size)
        dest.writeInt(duration)
    }

    override fun toString(): String {
        return "VideoItem{" +
                "title='" + title + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", duration=" + duration +
                '}'
    }

    companion object {


        fun parseCursor(cursor: Cursor?): VideoItem? {
            //健壮性检查
            if (cursor == null || cursor.count == 0) {
                return null
            }
            val IvideoItem = VideoItem()
            IvideoItem.title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
            IvideoItem.path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
            IvideoItem.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
            IvideoItem.duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
            return IvideoItem
        }

        fun parseListFromCursor(cursor: Cursor?): ArrayList<VideoItem> {
            val videoItems = ArrayList<VideoItem>(150)
            //健壮性检查
            if (cursor == null || cursor.count == 0) {
                return videoItems
            }
            //将游标移动到数据的最前面，才能解析出整个列表
            cursor.moveToPosition(-1)
            while (cursor.moveToNext()) {
                videoItems.add(this!!.parseCursor(cursor)!!)
            }
            return videoItems
        }

        /**
         * 1.必选实现Parcelable.Creator接口，否则在获取VideoItem数据的时候，会报错，如下:
         * android.os.BadParcelableException;
         * Parcelable protocol requires a Parcelable.Creator object called CREATOR on class com.jackli.www.okplayer.model.bean.VideoItem
         * 2.这个接口实现了从Parcelable容器读取VideoItem数据,并返回VideoItem对象给逻辑层使用
         * 3.实现Parcelable.Creator接口对象名必须为CRETOR,不如同样会报错上面所提到的错
         * 4.在读取Parcelable容器里的数据时，必须按成员变量声明的顺序读取数据，不然会出现获取数据出错
         * 5.反序列化对象
         */
        @JvmField
        val CREATOR = object : Parcelable.Creator<VideoItem> {

            override fun createFromParcel(source: Parcel): VideoItem {
                //必须按照成员变量声明的顺序读取数据,不然会出现获取数据出错
                val videoItem = VideoItem()
                videoItem.title = source.readString()
                videoItem.path = source.readString()
                videoItem.size = source.readLong()
                videoItem.duration = source.readInt()
                return videoItem
            }

            override fun newArray(size: Int): Array<VideoItem?> {
                return arrayOfNulls(size)
            }
        }
    }

}
