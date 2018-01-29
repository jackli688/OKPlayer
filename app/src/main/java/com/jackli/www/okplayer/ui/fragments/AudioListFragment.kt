package com.jackli.www.okplayer.ui.fragments

import android.content.Intent
import android.database.Cursor
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ListView

import com.jackli.www.okplayer.R
import com.jackli.www.okplayer.model.bean.AudioItem
import com.jackli.www.okplayer.model.db.MyAsyncQueryHandler
import com.jackli.www.okplayer.ui.activities.AudioPlayerActivity
import com.jackli.www.okplayer.ui.adapters.AudioListAdapter
import com.jackli.www.okplayer.ui.fragments.base.BaseFragment

/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.okplayer.ui.fragments
 * @description: description
 * @date: 2017/12/20
 * @time: 18:29
 */
class AudioListFragment : BaseFragment() {
    private var mListView: ListView? = null
    private var mAdapter: AudioListAdapter? = null
    override val contentId: Int
        get() = R.layout.fragment_audio_list

    override fun initView() {
        mListView = findViewById(R.id.listview)
    }

    override fun initListener() {
        mListView!!.onItemClickListener = MyOnItemClickListener()
        mAdapter = AudioListAdapter(context = activity, c = null, autoRequery = true)
        mListView!!.adapter = mAdapter
    }

    override fun initData() {
        initDatas()
    }

    private fun initDatas() {
        //获取手机里的视频数据
        val resolver = activity.contentResolver
        //开启子线程查询
        val asyncQueryHandler = MyAsyncQueryHandler(resolver)
        asyncQueryHandler
                .startQuery(
                        1,
                        mAdapter,
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME,
                                MediaStore.Audio.Media.ARTIST),
                        null,
                        null,
                        null
                )
    }

    override fun subscribeClick(view: View) {

    }

    private inner class MyOnItemClickListener : android.widget.AdapterView.OnItemClickListener {

        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            //获取被点击条目的数据
            val cursor = mAdapter!!.getItem(position) as Cursor
            //Cursor对象是不稳定的，应该将其转换成bean在传递给下一个界面
            //            VideoItem videoItem = VideoItem.parseCursor(cursor);
            val audioItems = AudioItem.parseListFromCursor(cursor)

            //将数据传递到播放界面
            val intent = Intent(activity, AudioPlayerActivity::class.java)
            intent.putExtra("audioItems", audioItems)
            intent.putExtra("position", position)
            startActivity(intent)
        }
    }
}
