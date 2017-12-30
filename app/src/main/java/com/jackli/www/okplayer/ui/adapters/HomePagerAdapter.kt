package com.jackli.www.okplayer.ui.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.view.View

import java.util.ArrayList

/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.okplayer.ui.adapters
 * @description: description
 * @date: 2017/12/20
 * @time: 18:25
 */
class HomePagerAdapter(fm: FragmentManager, fragmentList: ArrayList<Fragment>) : FragmentPagerAdapter(fm) {

    internal var fragmentList: List<Fragment>

    init {
        this.fragmentList = fragmentList
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }
}
