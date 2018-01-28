package com.jackli.www.servers.ui.activities

import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.BatteryManager
import android.os.Handler
import android.support.v4.view.ViewCompat
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import com.jackli.www.servers.R
import com.jackli.www.servers.model.bean.VideoItem
import com.jackli.www.servers.ui.activities.base.BaseActivity
import com.jackli.www.servers.ui.widgets.VideoView
import com.jackli.www.servers.utils.LogUtils
import com.jackli.www.servers.utils.StringUtils

class VideoPlayerActivity : BaseActivity() {
    override val contentId: Int
        get() = R.layout.activity_video_player

    private var mHandler: Handler? = null

    private var mVideoView: VideoView? = null
    private var mLl_top: LinearLayout? = null
    private var mLv_pause: ImageView? = null
    private var mTv_title: TextView? = null
    private var mLv_battery: ImageView? = null
    private var mLv_system_time: TextView? = null
    private var mSk_volume: SeekBar? = null
    private var mIv_mute: ImageView? = null
    private var mAlpha_cover: View? = null
    private var mTv_position: TextView? = null
    private var mSk_position: SeekBar? = null
    private var mTv_duration: TextView? = null
    private var mIv_pre: ImageView? = null
    private var mIv_next: ImageView? = null
    private var mLl_bottom: LinearLayout? = null
    private var mVideoItems: ArrayList<VideoItem>? = null
    private var mPosition: Int = 0
    private var mAudioManager: AudioManager? = null
    private var isControlShowing: Boolean = false
    private var mCurrentVolume: Int = 0
    private var mGestureDetector: GestureDetector? = null
    private var mStartY: Float = 0.toFloat()
    private var mStartVolume: Int = 0
    private var startAlpha: Float = 0.toFloat()
    private var mVideoReceiver: VideoReceiver? = null
    private var mBack: View? = null
    private var mFull_screen: ImageView? = null
    private var isCompletion: Boolean? = false

    //获取当前的音量
    private val currentVolume: Int
        get() = mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)

    override fun initView() {
        val mainLooper = mainLooper
        mHandler = Handler(mainLooper, Handler.Callback { msg ->
            when (msg.what) {
                MSG_UPDATE_SYSTEM_TIME -> updateSystemTime()
                MSG_UPDATE_POSITION -> startUpdatePosition()
                MSG_SHOW_FULLSCREEN -> {
                    isControlShowing = true
                    switchControl()
                }
            }
            false
        })
        mVideoView = findViewById(R.id.videoview)
        mLl_top = findViewById(R.id.video_ll_top)
        mLl_bottom = findViewById(R.id.video_ll_bottom)

        //顶部面板
        mLv_pause = findViewById(R.id.video_iv_pause)
        mTv_title = findViewById(R.id.video_tv_title)
        mLv_battery = findViewById(R.id.video_iv_battery)
        mLv_system_time = findViewById(R.id.video_tv_system_time)
        mSk_volume = findViewById(R.id.video_sk_volume)
        mIv_mute = findViewById(R.id.video_iv_mute)
        mAlpha_cover = findViewById(R.id.alpha_cover)

        //底部面板
        mTv_position = findViewById(R.id.video_tv_position)        //当前视频播放的进度
        mSk_position = findViewById(R.id.video_sk_position)
        mTv_duration = findViewById(R.id.video_tv_duration)       //视频总时长
        mIv_pre = findViewById(R.id.video_iv_pre)
        mIv_next = findViewById(R.id.video_iv_next)
        mFull_screen = findViewById(R.id.video_iv_fullscreen)     //屏幕大小切换

        //返回键
        mBack = findViewById(R.id.back)
    }

    override fun initListener() {
        //注册点击监听
        mLv_pause!!.setOnClickListener(this)
        mIv_mute!!.setOnClickListener(this)
        mIv_pre!!.setOnClickListener(this)
        mIv_next!!.setOnClickListener(this)
        mBack!!.setOnClickListener(this)

        //注册手势监听
        mGestureDetector = GestureDetector(this, OnVideoGestureListener())

        //注册进度条监听
        val onSeekBarChangeListener = OnVideoSeekBarChangeListener()
        mSk_volume!!.setOnSeekBarChangeListener(onSeekBarChangeListener)
        mSk_position!!.setOnSeekBarChangeListener(onSeekBarChangeListener)

        //注册视频监听
        mVideoView!!.setOnPreparedListener(OnVideoPreparedListener())
        mVideoView!!.setOnCompletionListener(OnVideoCompletionListener())
        //设置缓冲背景监听
        mVideoView!!.setBufferingUpdateListener(OnBufferingUpdateListener())

        //注册广播,获取系统电量
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        mVideoReceiver = VideoReceiver()
        registerReceiver(mVideoReceiver, filter)

        //屏幕大小显示监听
        mFull_screen!!.setOnClickListener(this)
    }

    private inner class OnBufferingUpdateListener : MediaPlayer.OnBufferingUpdateListener {

        override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
            LogUtils.d("buffer", "percent:" + percent)
            val bufferPercent = percent / 100.0f
            val bufferTime = mSk_position!!.max * bufferPercent
            mSk_position!!.secondaryProgress = bufferTime.toInt()
        }

    }


    override fun initData() {
        val data = intent.data
        LogUtils.d(TAG, "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%url:" + data)
        if (data != null) {
            //播放视频
            val mediator = MediaController(this)
            mediator.visibility = View.INVISIBLE
            mediator.setAnchorView(mVideoView)
//            val parse = Uri.parse(data.toString())
            mVideoView!!.setMediaController(mediator)
            mVideoView!!.setVideoPath(data.toString())
            var path = data.path.trim()
            var paths = path.split('/')
            mTv_title!!.text = paths[paths.size - 1]
        } else {
            //获取数据
            mVideoItems = intent.getParcelableArrayListExtra("videoItems")
            LogUtils.d(TAG, "VideoPlayerActivity.initData," + mVideoItems!!)
            mPosition = intent.getIntExtra("position", -1)

            //播放用户选中的视频
            if (playItem()) return
        }

        //开启系统时间更新
        updateSystemTime()

        //初始化音量进度
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (mAudioManager == null) throw AssertionError()
        val maxVolume = mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        // setMax 的时候，会使用布局文件的初始化 progress 修改进度
        mSk_volume!!.max = maxVolume

        val currentVolume = currentVolume
        LogUtils.d(TAG, "VideoPlayerActivity.initData,")
        mSk_volume!!.progress = currentVolume
        // 初始的屏幕亮度为完全透明
        mAlpha_cover!!.alpha = 0f
        if (mVideoView!!.isPlaying) {
            mVideoView!!.pause()
        } else {
            mVideoView!!.start()
            updatePauseStatus()
        }

        //隐藏控制面板
        hideContrlorOnInit()
    }

    /**
     * 在初始化界面饿时候，隐藏在控制面板
     */
    private fun hideContrlorOnInit() {
        //获取顶部面板高度，并隐藏面板
        LogUtils.d(TAG, "VideoPlayerActivity.hideContrlorOnInit,=" + mLl_top!!.height)
        mLl_top!!.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                //界面初始化完成时会调用这个方法
                mLl_top!!.viewTreeObserver.removeOnGlobalLayoutListener(this)

                //隐藏面板
                val topH = mLl_top!!.height
                LogUtils.d(TAG, "VideoPlayerActivity.onGlobalLayout,=" + topH)
                ViewCompat.animate(mLl_top).translationY((-topH).toFloat()).setDuration(200).start()
            }
        })

        //获取底部面板高度，并隐藏面板
        mLl_bottom!!.measure(0, 0)
        LogUtils.d(TAG, "VideoPlayerActivity.hideContrlorOnInit,=" + mLl_bottom!!.measuredHeight)
        val bottomH = mLl_bottom!!.measuredHeight.toFloat()
        ViewCompat.animate(mLl_bottom).translationY(bottomH).setDuration(200).start()
        isControlShowing = false
    }

    /**
     * 更新系统时间,并稍后再次更新
     */
    private fun updateSystemTime() {
        LogUtils.d(TAG, "VideoPlayerActivity.updateSystemTime,=" + System.currentTimeMillis())
        mLv_system_time!!.text = StringUtils.formatSystemTime()

        mHandler!!.sendEmptyMessageDelayed(MSG_UPDATE_SYSTEM_TIME, 500)
    }


    private fun playItem(): Boolean {
        //健壮性检查
        if (mVideoItems!!.size == 0 || mPosition == -1)
            return true
        val videoItem = mVideoItems!![mPosition]
        LogUtils.d(TAG, "VideoPlayerActivity.playItem,=" + videoItem)

        //播放视频
        mVideoView!!.setVideoPath(videoItem.path!!)

        //添加系统自带的视频进度条
        val mediaController = MediaController(this)
        mediaController.visibility = View.INVISIBLE
        mVideoView!!.setMediaController(mediaController)

        //初始化标题
        mTv_title!!.text = videoItem.title
        return false
    }


    override fun onResume() {
        super.onResume()
        if (!mVideoView!!.isPlaying) {
            mVideoView!!.start()
            startUpdatePosition()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mVideoView!!.isPlaying && !(isCompletion!!)) {
            mVideoView!!.pause()
            mHandler!!.removeMessages(MSG_UPDATE_POSITION)
        }
    }

    override fun subscribeClick(view: View) {
        when (view.id) {
            R.id.video_iv_pause -> updatePauseStatus()
            R.id.video_iv_mute -> updateMuteStatus()
            R.id.video_iv_pre -> playPre()
            R.id.video_iv_next -> playNext()
            R.id.video_iv_fullscreen -> {
                mVideoView!!.switchFullScreen()
            }
        }
    }


    /**
     * 播放上一个视频
     */
    private fun playPre() {
        if (mPosition > 0) {
            mPosition--
            playItem()
        }
        updatePreAndNextBtn()
    }

    /**
     * 播放下一个视频
     */
    private fun playNext() {
        if (mPosition < mVideoItems!!.size - 1) {
            mPosition++
            playItem()
        }
        updatePreAndNextBtn()
    }

    /**
     * 如果当前不是静音状态，保存当前音量,将声音设置为0;如果当前音量为0，则将林良恢复到之前的大小
     */
    private fun updateMuteStatus() {
        if (currentVolume != 0) {
            //音量不是0，记录音量，并设为0
            mCurrentVolume = currentVolume
            updateVolume(0)
        } else {
            //音量为0,恢复音量
            updateVolume(mCurrentVolume)
        }
    }

    /**
     * 更新音量
     */
    private fun updateVolume(volume: Int) {
        //Flag为1，则修改音量会显示系统提示框，为0则不显示
        mAudioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
        mSk_volume!!.progress = volume
    }

    /**
     * 根据当前视频是否有上一曲和下一曲更新按钮的可用状态
     */
    private fun updatePreAndNextBtn() {
        mIv_pre!!.isEnabled = mPosition > 0
        mIv_next!!.isEnabled = mPosition < mVideoItems!!.size - 1

    }

    /**
     * 切换暂停状态，并更新暂停按钮的图片
     */
    private fun updatePauseStatus() {
        if (mVideoView!!.isPlaying) {
            mVideoView!!.pause()
            mHandler!!.removeMessages(MSG_UPDATE_POSITION)
        } else {
            mVideoView!!.start()
            startUpdatePosition()
        }

        updatePauseBtn()
    }

    private inner class OnVideoGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            LogUtils.d(TAG, "OnVideoGestureListener.onSingleTapConfirmed,")
            if (isControlShowing && e.action != MotionEvent.ACTION_UP) {
            } else {
                switchControl()
            }
            return super.onSingleTapConfirmed(e)
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            LogUtils.d(TAG, "onDoubleTap")
            mVideoView!!.switchFullScreen()
            return super.onDoubleTap(e)
        }

        override fun onLongPress(e: MotionEvent?) {
            updatePauseStatus()
        }
    }

    /**
     * 切换控制面板的显示状态
     */
    private fun switchControl() {
        mHandler!!.removeMessages(MSG_SHOW_FULLSCREEN)
        if (isControlShowing) {
            //显示状态，将面板隐藏
            var values = floatArrayOf(0.0f, -mLl_top!!.height.toFloat())
            startAnimator(mLl_top!!, values)
            values = floatArrayOf(0.0f, mLl_bottom!!.height.toFloat())
            startAnimator(mLl_bottom!!, values)
        } else {
            //隐藏状态,将面板显示出来
            var values = floatArrayOf(-mLl_top!!.height.toFloat(), 0.0f)
            startAnimator(mLl_top!!, values)
            values = floatArrayOf(-mLl_bottom!!.height.toFloat(), 0.0f)
            startAnimator(mLl_bottom!!, values)
            //开始计时，到时间后，自动隐藏，并把标记为恢复
            mHandler!!.sendEmptyMessageDelayed(MSG_SHOW_FULLSCREEN, 5000)
        }
        isControlShowing = !isControlShowing
    }

    private fun startAnimator(obj: View, values: FloatArray) {
        val topAnimator = ObjectAnimator.ofFloat(obj, "translationY", *values)
        topAnimator.repeatMode = ObjectAnimator.REVERSE
        topAnimator.duration = 500
        topAnimator.start()
    }

    private inner class OnVideoSeekBarChangeListener : SeekBar.OnSeekBarChangeListener {

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            LogUtils.d(TAG, "OnVideoSeekBarChangeListener.onProgressChanged,progress=$progress,fromUser=$fromUser")
            //如果不是用户发起的变更，则不处理
            if (!fromUser) {
                return
            }
            when (seekBar.id) {
                R.id.video_sk_volume -> updateVolume(progress)
                R.id.video_sk_position -> {
//                    updatePauseStatus()
                    mHandler!!.removeMessages(MSG_UPDATE_POSITION)
                    mVideoView!!.seekTo(progress)
                    startUpdatePosition()
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            LogUtils.d(TAG, "OnVideoSeekBarChangeListener.onStartTrackingTouch,")
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            LogUtils.d(TAG, "OnVideoSeekBarChangeListener.onStopTrackingTouch,")
        }
    }

    private inner class OnVideoPreparedListener : MediaPlayer.OnPreparedListener {

        override fun onPrepared(mp: MediaPlayer) {
            //视频资源已经准备好，可以开始播放
            mVideoView!!.start()
            //更新暂停按钮的图片
            updatePauseBtn()

            //开始更新播放进度
            val duration = mVideoView!!.duration
            mTv_duration!!.text = StringUtils.formatDuration(duration)
            mSk_position!!.max = duration
            startUpdatePosition()
        }
    }

    private fun startUpdatePosition() {
        val position = mVideoView!!.currentPosition
        updatePosition(position)
        mHandler!!.sendEmptyMessageDelayed(MSG_UPDATE_POSITION, 900)
    }

    /**
     * 根据播放进度，修改界面
     *
     * @param position
     */
    private fun updatePosition(position: Int) {
        LogUtils.d(TAG, "VideoPlayerActivity.updatePosition,=" + position)
        mTv_position!!.text = StringUtils.formatDuration(position)
        mSk_position!!.progress = position
    }

    /**
     * 更新暂停按钮的图片
     */
    private fun updatePauseBtn() {
        if (mVideoView!!.isPlaying) {
            //播放状态，放置暂停按钮
            mLv_pause!!.setImageResource(R.drawable.btn_video_pause)
        } else {
            //暂停状态,放置播放按钮
            mLv_pause!!.setImageResource(R.drawable.btn_video_play)
        }
    }

    private inner class OnVideoCompletionListener : MediaPlayer.OnCompletionListener {

        override fun onCompletion(mp: MediaPlayer) {
            mHandler!!.removeMessages(MSG_UPDATE_POSITION)
            mp.pause()
            isCompletion = true
        }
    }

    private inner class VideoReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            //获取系统电量
            LogUtils.d(TAG, "VideoReceiver.onReceive,intent=" + intent)
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            updateBatteryPic(level)
        }
    }

    /**
     * 根据当前的系统电量，更新电池图片
     *
     * @param level
     */
    private fun updateBatteryPic(level: Int) {
        LogUtils.d(TAG, "VideoPlayerActivity.updateBatteryPic,level=" + level)
        when {
            level < 10 -> mLv_battery!!.setImageResource(R.mipmap.ic_battery_0)
            level < 20 -> mLv_battery!!.setImageResource(R.mipmap.ic_battery_10)
            level < 40 -> mLv_battery!!.setImageResource(R.mipmap.ic_battery_40)
            level < 60 -> mLv_battery!!.setImageResource(R.mipmap.ic_battery_60)
            level < 80 -> mLv_battery!!.setImageResource(R.mipmap.ic_battery_80)
            else -> mLv_battery!!.setImageResource(R.mipmap.ic_battery_100)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //手势分析器需要产生结果，就需要将touch事件交给他分析
        mGestureDetector!!.onTouchEvent(event)

        // 最终使用的音量 = 起始音量 + 偏移音量
        //偏移音量 = 最大音量 * 划过屏幕的百分比
        //划过屏幕的百分比 = 手指划过屏幕的距离 / 屏幕高度
        //手指划过屏幕的距离 = 手指当前位置 - 手指起始位置

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mHandler!!.removeMessages(MSG_SHOW_FULLSCREEN)
                //手指起始位置
                mStartY = event.y
                mStartVolume = currentVolume
                startAlpha = mAlpha_cover!!.alpha
            }
            MotionEvent.ACTION_MOVE -> {
                //手指当前位置
                val currentY = event.y
                //手指划过屏幕的距离
                val offsetY = currentY - mStartY
                val displayMetrics = DisplayMetrics()
                //获取屏幕参数
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                val halfScreenH = displayMetrics.heightPixels / 2
                //划过屏幕参数
                val movePercent = offsetY / halfScreenH

                // 根据是在屏幕的左侧还是右侧，决定修改亮点还是音量
                val halfScreenW = displayMetrics.widthPixels / 2
                if (event.x < halfScreenW) {
                    //屏幕左半侧，修改亮度
                    turnAlpha(movePercent)
                } else {
                    //屏幕右半侧,修改音量
                    turnVolume(-movePercent)
                }
            }
            MotionEvent.ACTION_UP -> {
                mHandler!!.sendEmptyMessageDelayed(MSG_SHOW_FULLSCREEN, 5000)
            }
        }
        return true
    }

    //   根据划过屏幕的百分比修改音量
    private fun turnVolume(movePercent: Float) {
        // 偏移音量
        val offsetVolume = mSk_volume!!.max * movePercent
        //最终使用的音量
        val finalVolume = (mStartVolume + offsetVolume).toInt()
        LogUtils.d(TAG, "VideoPlayerActivity.turnVolume,=" + finalVolume)
        //更新音量
        updateVolume(finalVolume)
    }

    //  根据划过屏幕的百分比修改音量
    private fun turnAlpha(movePercent: Float) {
        //最终使用的透明度
        val finalAlpha = startAlpha + movePercent
        LogUtils.d(TAG, "VideoPlayerActivity.turnAlpha,=" + finalAlpha)
        //系统没有对透明度做上下限的纠正，需要我们自己来处理边界
        if (0.0 <= finalAlpha && finalAlpha < 1.0) {
            //修改透明度
            mAlpha_cover!!.alpha = finalAlpha
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mVideoReceiver)
        mHandler!!.removeCallbacksAndMessages(null)
    }

    companion object {

        private val MSG_UPDATE_SYSTEM_TIME = 0
        private val MSG_UPDATE_POSITION = 1
        private val MSG_SHOW_FULLSCREEN = 2

    }

}
