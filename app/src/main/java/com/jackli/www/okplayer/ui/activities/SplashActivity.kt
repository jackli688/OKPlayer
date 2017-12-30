package com.jackli.www.okplayer.ui.activities

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.jackli.www.okplayer.R
import com.jackli.www.okplayer.R.layout.activity_splash
import com.jackli.www.okplayer.ui.activities.base.BaseActivity
import com.jackli.www.okplayer.utils.LogUtils
import java.util.*


class SplashActivity : BaseActivity() {

    override val contentId: Int
        get() = activity_splash

    private var mTextView: TextView? = null
    private var mParent: ViewGroup? = null


    override fun initView() {
        mTextView = findViewById(R.id.tv1)
        mParent = findViewById(R.id.splash_parent)
    }

    @SuppressLint("ObjectAnimatorBinding")
    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    override fun initListener() {
        mTextView!!.setOnClickListener(this)
        val animator = ObjectAnimator.ofFloat(mParent, "alpha", 1.0f, 0.9f, 0.8f, 0.6f, 0.8f, 0.9f, 1.0f)
        animator.duration = 5000
        animator.repeatMode = ValueAnimator.REVERSE
        animator.start()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initData() {
        //动态的获取权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val hasReadPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)

            val permissions = ArrayList<String>()
            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            if (!permissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE),
                        0)
            } else {
                turnHomeActivity(2000)
            }

        } else {
            //低版本
            turnHomeActivity(2000)
        }
    }

    private fun turnHomeActivity(delayM: Long) {
        val handler = Handler(this.mainLooper)
        handler.postDelayed({
            startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
            finish()
        }, delayM)
    }

    override fun subscribeClick(view: View) {
        when (view.id) {
            R.id.tv1 -> {
            }
            else -> {
            }
        }//                Intent intent = new Intent(Intent.ACTION_VIEW);
        //                intent.setDataAndType(Uri.parse("http://192.168.31.123:8080/player_test/video/oppo.mp4"), "video/mp4");
        //                startActivity(intent);
        //动态的获取权限
        //                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //                    int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //                    int hasReadPermission =  checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        //
        //                    ArrayList<String> permissions = new ArrayList();
        //                    if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
        //                        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //                    }
        //
        //                    if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
        //                        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        //                    }
        //
        //                    if (!permissions.isEmpty()) {
        //                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
        //                                0);
        //                    } else {
        //                    }
        //
        //                } else {
        //                    //低版本
        //                }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION -> for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    LogUtils.d(javaClass.getSimpleName(), "permission Granted:" + permissions[i])
                    turnHomeActivity(0)
                } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    LogUtils.d(javaClass.getSimpleName(), "permission Denied:" + permissions[i])
                    finish()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {

        private val REQUEST_PERMISSION = 0
    }
}
