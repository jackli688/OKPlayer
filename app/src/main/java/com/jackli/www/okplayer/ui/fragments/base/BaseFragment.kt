package com.jackli.www.okplayer.ui.fragments.base

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast

import com.jackli.www.okplayer.ui.interfaces.UiInterface
import com.jackli.www.okplayer.utils.LogUtils

/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.okplayer.ui.fragments.base
 * @description: description
 * @date: 2017/12/19
 * @time: 22:07
 */
abstract class BaseFragment : Fragment(), View.OnClickListener, UiInterface {

    private lateinit var mRootView: View

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater!!.inflate(contentId, container, false)
        initView()
        initListener()
        initData()
        registerCommonButton()
        return mRootView
    }

    private fun registerCommonButton() {
        val view: View? = null
        //                = findViewById(R.id.back);
        view?.setOnClickListener(this)

    }


    protected fun findViewById(viewId: Int): ListView? {
        return mRootView.findViewById(viewId)
    }

    override fun onClick(v: View) {

    }

    protected fun logD(msg: String) {
        LogUtils.d(javaClass.getName(), msg)
    }

    protected fun toast(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }

}
