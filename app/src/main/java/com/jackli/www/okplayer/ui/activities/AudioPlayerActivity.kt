package com.jackli.www.okplayer.ui.activities

import android.annotation.SuppressLint
import android.content.*
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.jackli.www.okplayer.R
import com.jackli.www.okplayer.model.bean.AudioItem
import com.jackli.www.okplayer.services.AudioService
import com.jackli.www.okplayer.services.AudioService.AudioBinder
import com.jackli.www.okplayer.ui.activities.base.BaseActivity
import com.jackli.www.okplayer.ui.widgets.LyricsView
import com.jackli.www.okplayer.utils.LogUtils
import com.jackli.www.okplayer.utils.LyricsLoader
import com.jackli.www.okplayer.utils.StringUtils


@Suppress("UNREACHABLE_CODE")
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
    var mLyric: LyricsView? = null
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
    private var animationDrawable: AnimationDrawable? = null

    private var onAudioReceiver: OnAudioReceiver? = null

    private val MSG_UPDATE_POSTITION = 0
    private val MSG_LYRIC_ROLLING = 1
    private var flag = false

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_UPDATE_POSTITION -> startUpdatePosition()
                MSG_LYRIC_ROLLING -> startRollLyric()
            }
        }

    }


    override fun initView() {
        title = findViewById(R.id.audio_tv_title)
        wave = findViewById(R.id.audio_iv_wave)
        artist = findViewById(R.id.audio_tv_artist)
        mLyric = findViewById(R.id.lyrics)
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
        skBar?.setOnSeekBarChangeListener(ISeekBarChangeListener())
        order?.setOnClickListener(this@AudioPlayerActivity)
        preAudio?.setOnClickListener(this@AudioPlayerActivity)
        pause?.setOnClickListener(this@AudioPlayerActivity)
        nextAudio?.setOnClickListener(this@AudioPlayerActivity)
        audioList?.setOnClickListener(this@AudioPlayerActivity)
        back?.setOnClickListener(this@AudioPlayerActivity)

        registerReceiver()
    }

    private fun registerReceiver() {
        flag = true
        //注册广播
        val intentFilter = IntentFilter("com.jackli.prepared")
        onAudioReceiver = OnAudioReceiver()
        registerReceiver(onAudioReceiver, intentFilter)
        LogUtils.e("onAudioReceiver", "广播注册了")
    }

    //初始化数据
    override fun initData() {
        if (intent == null) return
        val data = intent.data
        if (data != null) {

        } else {
            //获取数据
            // 开启服务来播放音乐
            val intent = Intent(intent)
            intent.setClass(this, AudioService::class.java)
            startService(intent)

            connection = AudioServiceConnection()
            bindService(intent, connection, Context.BIND_AUTO_CREATE)

            // 开启示波器动画
            animationDrawable = wave?.drawable as AnimationDrawable
            animationDrawable?.start()
        }
    }


    //点击事件的详细处理过程
    override fun subscribeClick(view: View) {
        when (view.id) {
            R.id.order -> {
                switchPlayMode()
            }
            R.id.audio_iv_pre -> {
                mAudioBinder?.playPre()
            }
            R.id.audio_iv_pause -> {
                switchPauseStatus()
            }
            R.id.audio_iv_next -> {
                mAudioBinder?.playNext()
            }
            R.id.audioList -> {
                showAudioList()
            }
        }
    }

    private fun showAudioList() {

    }

    private fun switchPauseStatus() {
        mAudioBinder?.switchPauseStatus()
//        startUpdatePosition()
        updatePauseBtn()
    }

    /**切换播放模式*/
    private fun switchPlayMode() {
        mAudioBinder?.switchPlayMode()
        updatePlayModeBtn()
    }

    //进度条滑动处理
    inner class ISeekBarChangeListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            //跳转到播放进度
            if (fromUser) mAudioBinder?.seekTo(progress)

        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }

    inner class AudioServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mAudioBinder = service as AudioService.AudioBinder
        }

        override fun onServiceDisconnected(name: ComponentName) {

        }
    }

    inner class OnAudioReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            //音乐已经开始播放了
            updatePauseBtn()
            //获取正在播放的歌曲

            val audioItem: AudioItem = intent?.getParcelableExtra("audioItem") as AudioItem

            //更新标题
            title?.text = audioItem.title
            artist?.text = audioItem.artist

            //开启进度更新
            startUpdatePosition()

            //更新播放模式
            updatePlayModeBtn()

            //开始更新歌词
            updateLyrics(audioItem)
        }

    }

    private fun updateLyrics(audioItem: AudioItem) {
//        开始更新歌词
        val split = audioItem.title?.split(".")
        val get = split?.get(0)
        val lyricFile = LyricsLoader.loadLyricFile(get)
        mLyric?.setLyricFile(lyricFile)
        startRollLyric()
    }

    private fun updatePlayModeBtn() {
        when (mAudioBinder?.getPlayMode()) {
            AudioService.PLAYMODE_ALL_REPEAT -> order?.setImageResource(R.drawable.btn_audio_playmode_allrepeat)
            AudioService.PLAYMODE_SINGLE_REPEAT -> order?.setImageResource(R.drawable.btn_audio_playmode_singlerepeat)
            AudioService.PLAYMODE_RANDOM -> order?.setImageResource(R.drawable.btn_audio_playmode_random)
        }
    }


    /**根据当前的播放状态，切换不同的暂停按钮图片*/
    private fun updatePauseBtn() {
        when {
            mAudioBinder?.isPlaying!! -> {
                pause?.setImageResource(R.drawable.btn_audio_pause)
                animationDrawable?.start()
            }
            else -> {
                pause?.setImageResource(R.drawable.btn_audio_play)
                animationDrawable?.stop()
            }
        }
    }


    /**更新播放进度，并且延迟一段时间后再次更新*/
    private fun startUpdatePosition() {
//        if (mAudioBinder?.getPlayStatus()!!) {
        LogUtils.d("update", "播放进度的位置还在执行")
        //获取当前的播放位置
        var duration: Int? = mAudioBinder?.duration
        var currentPosition = mAudioBinder?.currentPosition
        var positionStr = StringUtils.formatDuration(currentPosition!!)
        var durationStr = StringUtils.formatDuration(duration!!)
        playedTime?.text = (positionStr + "/" + durationStr)

        skBar?.max = duration
        skBar?.progress = currentPosition

        //发送延时消息，准备再次更新界面
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_POSTITION, 500)
//        } else {
//            //清楚发送延时消息，停止更新界面
//            mHandler.removeMessages(MSG_UPDATE_POSTITION)
//        }

    }

    /**开始滚动歌词，根据当前播放进度，计算歌词的高亮行，并稍后再次更新*/
    private fun startRollLyric() {
        mLyric?.computeMiddleLine(mAudioBinder?.currentPosition!!, mAudioBinder?.duration!!)
        mHandler.sendEmptyMessage(MSG_LYRIC_ROLLING)
    }

    override fun onDestroy() {
        if (onAudioReceiver != null && flag) {
            flag = false
            unregisterReceiver(onAudioReceiver)
            LogUtils.e("onAudio", "广播注销了")
        }
        if (connection != null) {
            unbindService(connection)
        }
        mHandler?.removeCallbacksAndMessages(null)
        super.onDestroy()
        LogUtils.e("onAudioReceiver", "播放界面销毁了")
    }
}
