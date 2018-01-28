package com.jackli.www.servers.ui.interfaces

import android.view.View

/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.okplayer.ui.interfaces
 * @description: description
 * @date: 2017/12/20
 * @time: 16:05
 */
interface UiInterface {

    val contentId: Int

    fun initView()

    fun initListener()

    fun initData()

    /**
     * 订阅点击事件
     * @param view
     */
    fun subscribeClick(view: View)
}
