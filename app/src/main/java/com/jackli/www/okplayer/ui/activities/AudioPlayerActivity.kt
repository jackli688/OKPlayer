package com.jackli.www.okplayer.ui.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.AnimationDrawable
import android.os.IBinder
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.jackli.www.okplayer.R
import com.jackli.www.okplayer.model.bean.AudioItem
import com.jackli.www.okplayer.services.AudioService
import com.jackli.www.okplayer.services.AudioService.AudioBinder
import com.jackli.www.okplayer.ui.activities.base.BaseActivity


/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.servers.ui.activities
 * @description: description
 * @date: 2018/1/29
 * @time: 10:48
 */
class AudioPlayerActivity : BaseActivity() {
    override val contentId: Int
        get() = R.layout.activity_audio

    var title: TextView? = null
    var wave: ImageView? = null
    var artist: TextView? = null
    var playedTime: TextView? = null
    var skBar: SeekBar? = null
    var order: ImageView? = null
    var preAudio: ImageView? = null
    var pause: ImageView? = null
    var nextAudio: ImageView? = null
    var audioList: ImageView? = null
    var back: View? = null

    var audioItems: ArrayList<AudioItem>? = null
    var position: Int? = null

    var mAudioBinder: AudioBinder? = null
    private var connection: AudioServiceConnection? = null


    override fun initView() {
        title = findViewById(R.id.audio_tv_title)
        wave = findViewById(R.id.audio_iv_wave)
        artist = findViewById(R.id.audio_tv_artist)
        playedTime = findViewById(R.id.audio_tv_position)
        skBar = findViewById(R.id.audio_sk_position)
        order = findViewById(R.id.order)
        preAudio = findViewById(R.id.audio_iv_pre)
        pause = findViewById(R.id.audio_iv_pause)
        nextAudio = findViewById(R.id.audio_iv_next)
        audioList = findViewById(R.id.audioList)
        back = findViewById(R.id.back)
    }

    override fun initListener() {
        skBar?.setOnSeekBarChangeListener(ISeekBarChangeListener)
        order?.setOnClickListener(this@AudioPlayerActivity)
        preAudio?.setOnClickListener(this@AudioPlayerActivity)
        pause?.setOnClickListener(this@AudioPlayerActivity)
        nextAudio?.setOnClickListener(this@AudioPlayerActivity)
        audioList?.setOnClickListener(this@AudioPlayerActivity)
        back?.setOnClickListener(this@AudioPlayerActivity)
    }

    //初始化数据
    override fun initData() {
        val data = intent.data
        if (data != null) {

        } else {
            //获取数据
            audioItems = intent.getParcelableArrayListExtra<AudioItem>("audioItems")
            position = intent.getIntExtra("position", -1)
            if (palyItem()) return
        }
    }


    //点击事件的详细处理过程
    override fun subscribeClick(view: View) {

    }

    private fun palyItem(): Boolean {
        if (position == -1 || audioItems?.size == 0) {
            return true
        } else {
            // 开启服务来播放音乐
            val intent = Intent(intent)
            intent.setClass(this, AudioService::class.java!!)
            startService(intent)

            connection = AudioServiceConnection()
            bindService(intent, connection, Context.BIND_AUTO_CREATE)

            // 开启示波器动画
            val animationDrawable = wave?.drawable as AnimationDrawable
            animationDrawable.start()
        }
        return false
    }

    //进度条滑动处理
    object ISeekBarChangeListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            TODO("not implemented")
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            TODO("not implemented")
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            TODO("not implemented")
        }
    }

    inner class AudioServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mAudioBinder = service as AudioService.AudioBinder
        }

        override fun onServiceDisconnected(name: ComponentName) {

        }
    }

}