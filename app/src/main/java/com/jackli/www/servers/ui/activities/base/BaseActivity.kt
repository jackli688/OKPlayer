package com.jackli.www.servers.ui.activities.base

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.View

import com.jackli.www.servers.R
import com.jackli.www.servers.ui.interfaces.UiInterface

/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.okplayer.ui.activities.base
 * @description: description
 * @date: 2017/12/19
 * @time: 21:17
 */
abstract class BaseActivity : FragmentActivity(), View.OnClickListener, UiInterface {
    protected val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentId)
        initView()
        initListener()
        registerCommonButton()
        initData()
    }

    override fun onClick(v: View) {
        //通过id判断是不是同一个操作
        if (v.id == R.id.back) {
            finish()
        } else {
            //每个子Activity自己要实现的点击
            subscribeClick(v)
        }
    }

    /**
     * 如返回按钮等具有相同操作的,注册相同的监听
     */
    private fun registerCommonButton() {

    }

}
