package com.jackli.www.servers.ui.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ListView

import com.jackli.www.servers.R
import com.jackli.www.servers.model.bean.VideoItem
import com.jackli.www.servers.model.db.MyAsyncQueryHandler
import com.jackli.www.servers.ui.activities.VideoPlayerActivity
import com.jackli.www.servers.ui.adapters.VideoListAdapter
import com.jackli.www.servers.ui.fragments.base.BaseFragment
import com.jackli.www.servers.utils.LogUtils

@Suppress("UNREACHABLE_CODE")
/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.okplayer.ui.f
 * @description: description
 * @date: 2017/12/20
 * @time: 18:29
 */
class VideoListFragment : BaseFragment() {
    private var mListView: ListView? = null
    private var mAdapter: VideoListAdapter? = null

    override val contentId: Int
        get() = R.layout.fragment_video_list

    override fun initView() {
        mListView = findViewById(R.id.list_view) as ListView
    }

    override fun initListener() {
        mListView!!.onItemClickListener = MyOnItemClickListener()

        mAdapter = VideoListAdapter(context = activity, c = null, autoRequery = true)
        mListView!!.adapter = mAdapter
    }

    override fun initData() {
        initDatas()
    }

    override fun subscribeClick(view: View) {

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION -> for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    LogUtils.d(javaClass.getSimpleName(), "permission Granted:" + permissions[i])
                    initDatas()
                } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    LogUtils.d(javaClass.getSimpleName(), "permission Denied:" + permissions[i])
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun initDatas() {
        //获取手机里的视频数据
        val resolver = activity.contentResolver
        //开启子线程查询
        val asyncQueryHandler = MyAsyncQueryHandler(resolver)
        asyncQueryHandler
                .startQuery(1, mAdapter, MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.TITLE), "", null, null)
    }

    private inner class MyOnItemClickListener : android.widget.AdapterView.OnItemClickListener {

        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            //获取被点击条目的数据
            val cursor = mAdapter!!.getItem(position) as Cursor
            //Cursor对象是不稳定的，应该将其转换成bean在传递给下一个界面
            //            VideoItem videoItem = VideoItem.parseCursor(cursor);
            val videoItems = VideoItem.parseListFromCursor(cursor)

            //将数据传递到播放界面
            val intent = Intent(activity, VideoPlayerActivity::class.java)
            intent.putExtra("videoItems", videoItems)
            intent.putExtra("position", position)
            startActivity(intent)
        }
    }

    companion object {

        private val REQUEST_PERMISSION = 0
    }
}
