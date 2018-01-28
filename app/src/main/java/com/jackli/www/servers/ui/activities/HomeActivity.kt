package com.jackli.www.servers.ui.activities

import android.graphics.Point
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.TextView
import com.jackli.www.servers.R
import com.jackli.www.servers.ui.activities.base.BaseActivity
import com.jackli.www.servers.ui.adapters.HomePagerAdapter
import com.jackli.www.servers.ui.fragments.AudioListFragment
import com.jackli.www.servers.ui.fragments.VideoListFragment
import java.util.*

class HomeActivity : BaseActivity() {

    override val contentId: Int
        get() = R.layout.activity_home

    private var mVideo: TextView? = null
    private var mAudio: TextView? = null
    private var mIndicate: View? = null
    private var mViewPager: ViewPager? = null
    private var fragmentList: ArrayList<Fragment>? = null
    private var mAdapter: HomePagerAdapter? = null


    /**
     * 专门用来处理findViewById操作
     */
    override fun initView() {
        mVideo = findViewById(R.id.video)
        mAudio = findViewById(R.id.audio)
        mIndicate = findViewById(R.id.indicate)
        mViewPager = findViewById(R.id.home_viewpager)
    }

    /**
     * 用来注册监听器和适配器，注册广播接收者
     */
    override fun initListener() {
        //注册监听
        mVideo!!.setOnClickListener(this)
        mAudio!!.setOnClickListener(this)
        mViewPager!!.addOnPageChangeListener(OnHomePageChangeListener())

        //设置适配器
        fragmentList = ArrayList()
        mAdapter = HomePagerAdapter(supportFragmentManager, fragmentList!!)
        mViewPager!!.adapter = mAdapter
    }

    /**
     * 获取数据,初始化界面
     */
    override fun initData() {
        //填充Viewpager的界面
        fragmentList!!.add(VideoListFragment())
        fragmentList!!.add(AudioListFragment())
        mAdapter!!.notifyDataSetChanged()

        //选择默认的页面
        mViewPager!!.setCurrentItem(0, false)
        updateTabs(0)

        //更新指示器的宽度
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        val screenWidth = point.x
        val indicateWidth = screenWidth / fragmentList!!.size
        mIndicate!!.layoutParams.width = indicateWidth
        mIndicate!!.requestLayout()
    }

    override fun subscribeClick(view: View) {
        when (view.id) {
            R.id.video -> mViewPager!!.setCurrentItem(0, false)
            R.id.audio -> mViewPager!!.setCurrentItem(1, false)
            else -> {
            }
        }

    }

    private inner class OnHomePageChangeListener : ViewPager.OnPageChangeListener {

        //当界面滑动的时候被回调，position是当前选中的界面，positionOffset界面展开的百分比，positionOffsetPixels界面展开的大小
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            //            LogUtils.d(this.getClass().getName(), "position:" + position + ",positionOffset:"
            //                    + positionOffset + ",positionOffsetPixels:" + positionOffsetPixels);
            /**
             * 处理指示器平移
             * 最终要使用的位置 = 起始位置 + 偏移位置
             * 起始位置 = positon * 指示器的宽度
             * 偏移位置 = 页面展开的百分比 *  指示器宽度
             */
            val offsetX = (positionOffset * mIndicate!!.width).toInt()
            val startX = position * mIndicate!!.width
            val translateX = startX + offsetX
            mIndicate!!.translationX = translateX.toFloat()
        }

        //当选中的界面发生变化后回调这个方法
        override fun onPageSelected(position: Int) {
            //将选中的标题变大且高亮，没选中的标题变小且变暗
            updateTabs(position)
        }

        //当界面滚动状态反生变化时回调
        override fun onPageScrollStateChanged(state: Int) {


        }
    }

    private fun updateTabs(position: Int) {
        updateTab(position, mVideo, 0)
        updateTab(position, mAudio, 1)

    }

    private fun updateTab(position: Int, tab: TextView?, tabPosition: Int) {

        //变色
        tab!!.isSelected = position == tabPosition

        //缩放
        if (tab.isSelected) {
            ViewCompat.animate(tab).scaleX(1.1f).scaleY(1.1f).setDuration(200).start()
        } else {
            ViewCompat.animate(tab).scaleY(0.9f).scaleY(0.9f).setDuration(200).start()
        }

    }

    private fun requestPermission() {

    }
}
