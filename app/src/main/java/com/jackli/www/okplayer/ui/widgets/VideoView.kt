package com.jackli.www.okplayer.ui.widgets

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnErrorListener
import android.media.MediaPlayer.OnInfoListener
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.MediaController

import java.io.IOException

/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.okplayer.ui.widgets
 * @description: description
 * @date: 2017/12/31
 * @time: 0:15
 */
class VideoView : SurfaceView, MediaController.MediaPlayerControl {
    private val TAG = "VideoView"
    // settable by the client
    private var mUri: Uri? = null
    private var mHeaders: Map<String, String>? = null

    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private var mCurrentState = STATE_IDLE
    private var mTargetState = STATE_IDLE

    // All the stuff we need for playing and showing a video
    private var mSurfaceHolder: SurfaceHolder? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var mAudioSession: Int = 0
    private var mVideoWidth: Int = 0
    private var mVideoHeight: Int = 0
    private var mSurfaceWidth: Int = 0
    private var mSurfaceHeight: Int = 0
    private var mMediaController: MediaController? = null
    private var mOnCompletionListener: MediaPlayer.OnCompletionListener? = null
    private var mOnPreparedListener: MediaPlayer.OnPreparedListener? = null
    private var mCurrentBufferPercentage: Int = 0
    private var mOnErrorListener: OnErrorListener? = null
    private var mOnInfoListener: OnInfoListener? = null
    private var mSeekWhenPrepared: Int = 0  // recording the seek position while preparing
    private val mCanPause: Boolean = false
    private val mCanSeekBack: Boolean = false
    private val mCanSeekForward: Boolean = false

    // 自定义的变量
    private var mContext: Context? = null
    private var mDefaultH: Int = 0
    private var mDefaultW: Int = 0
    private var mScreenH: Int = 0
    private var mScreenW: Int = 0
    /**
     * 如果为true，则说明当前是全屏状态
     */
    /**
     * 如果返回 true 说明当前是全屏状态
     */
    var isFullSreen = false
        private set

    internal var mSizeChangedListener: MediaPlayer.OnVideoSizeChangedListener = MediaPlayer.OnVideoSizeChangedListener { mp, width, height ->
        Log.d(TAG, "onVideoSizeChanged:mp.width:" + mp.videoWidth +
                ",mp.height:" + mp.videoHeight + ",width:" + width + ",height:" + height)
        mVideoWidth = mp.videoWidth
        mVideoHeight = mp.videoHeight
        //                    mVideoWidth = 960;
        //                    mVideoHeight = 540;
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            holder.setFixedSize(mVideoWidth, mVideoHeight)
            requestLayout()
        }
    }

    internal var mPreparedListener: MediaPlayer.OnPreparedListener = MediaPlayer.OnPreparedListener { mp ->
        mCurrentState = STATE_PREPARED

        // Get the capabilities of the player for this stream
        //            Metadata data = mp.getMetadata(MediaPlayer.METADATA_ALL,
        //                                      MediaPlayer.BYPASS_METADATA_FILTER);
        //
        //            if (data != null) {
        //                mCanPause = !data.has(Metadata.PAUSE_AVAILABLE)
        //                        || data.getBoolean(Metadata.PAUSE_AVAILABLE);
        //                mCanSeekBack = !data.has(Metadata.SEEK_BACKWARD_AVAILABLE)
        //                        || data.getBoolean(Metadata.SEEK_BACKWARD_AVAILABLE);
        //                mCanSeekForward = !data.has(Metadata.SEEK_FORWARD_AVAILABLE)
        //                        || data.getBoolean(Metadata.SEEK_FORWARD_AVAILABLE);
        //            } else {
        //                mCanPause = mCanSeekBack = mCanSeekForward = true;
        //            }

        if (mOnPreparedListener != null) {
            mOnPreparedListener!!.onPrepared(mMediaPlayer)
        }
        if (mMediaController != null) {
            mMediaController!!.isEnabled = true
        }
        Log.d(TAG, "onPrepared:mp.getWidth:" + mp.videoWidth + ",mp.getVideoHeight:" + mp.videoHeight)
        mVideoWidth = mp.videoWidth
        mVideoHeight = mp.videoHeight

        val seekToPosition = mSeekWhenPrepared  // mSeekWhenPrepared may be changed after seekTo() call
        if (seekToPosition != 0) {
            seekTo(seekToPosition)
        }
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
            holder.setFixedSize(mVideoWidth, mVideoHeight)
            if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                // We didn't actually change the size (it was already at the size
                // we need), so we won't get a "surface changed" callback, so
                // start the video here instead of in the callback.
                if (mTargetState == STATE_PLAYING) {
                    start()
                    if (mMediaController != null) {
                        mMediaController!!.show()
                    }
                } else if (!isPlaying && (seekToPosition != 0 || currentPosition > 0)) {
                    if (mMediaController != null) {
                        // Show the media controls when we're paused into a video and make 'em stick.
                        mMediaController!!.show(0)
                    }
                }
            }
        } else {
            // We don't know the video size yet, but should start anyway.
            // The video size might be reported to us later.
            if (mTargetState == STATE_PLAYING) {
                start()
            }
        }
    }

    private val mCompletionListener = OnCompletionListener {
        mCurrentState = STATE_PLAYBACK_COMPLETED
        mTargetState = STATE_PLAYBACK_COMPLETED
        if (mMediaController != null) {
            mMediaController!!.hide()
        }
        if (mOnCompletionListener != null) {
            mOnCompletionListener!!.onCompletion(mMediaPlayer)
        }
    }

    private val mErrorListener = OnErrorListener { mp, framework_err, impl_err ->
        Log.d(TAG, "Error: $framework_err,$impl_err")
        mCurrentState = STATE_ERROR
        mTargetState = STATE_ERROR
        if (mMediaController != null) {
            mMediaController!!.hide()
        }

        /* If an error handler has been supplied, use it and finish. */
        if (mOnErrorListener != null) {
            if (mOnErrorListener!!.onError(mMediaPlayer, framework_err, impl_err)) {
                return@OnErrorListener true
            }
        }

        /* Otherwise, pop up an error dialog so the user knows that
             * something bad has happened. Only try and pop up the dialog
             * if we're attached to a window. When we're going away and no
             * longer have a window, don't bother showing the user an error.
             */
        //            if (getWindowToken() != null) {
        //                Resources r = mContext.getResources();
        //                int messageId;
        //
        //                if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
        //                    messageId = com.android.internal.R.string.VideoView_error_text_invalid_progressive_playback;
        //                } else {
        //                    messageId = com.android.internal.R.string.VideoView_error_text_unknown;
        //                }
        //
        //                new AlertDialog.Builder(mContext)
        //                        .setMessage(messageId)
        //                        .setPositiveButton(com.android.internal.R.string.VideoView_error_button,
        //                                new DialogInterface.OnClickListener() {
        //                                    public void onClick(DialogInterface dialog, int whichButton) {
        //                                        /* If we get here, there is no onError listener, so
        //                                         * at least inform them that the video is over.
        //                                         */
        //                                        if (mOnCompletionListener != null) {
        //                                            mOnCompletionListener.onCompletion(mMediaPlayer);
        //                                        }
        //                                    }
        //                                })
        //                        .setCancelable(false)
        //                        .show();
        //            }
        true
    }

    //    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
    //        new MediaPlayer.OnBufferingUpdateListener() {
    //        public void onBufferingUpdate(MediaPlayer mp, int percent) {
    //            mCurrentBufferPercentage = percent;
    //        }
    //    };

    // 允许从外部设置缓冲监听
    private var mBufferingUpdateListener: MediaPlayer.OnBufferingUpdateListener? = null

    internal var mSHCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder, format: Int,
                                    w: Int, h: Int) {
            mSurfaceWidth = w
            mSurfaceHeight = h
            val isValidState = mTargetState == STATE_PLAYING
            val hasValidSize = mVideoWidth == w && mVideoHeight == h
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared)
                }
                start()
            }
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            mSurfaceHolder = holder
            openVideo()
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            // after we return from this we can't use the surface any more
            mSurfaceHolder = null
            if (mMediaController != null) mMediaController!!.hide()
            release(true)
        }
    }

    private val isInPlaybackState: Boolean
        get() = mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING

    constructor(context: Context) : super(context) {
        initVideoView(context)
    }

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0) {
        initVideoView(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initVideoView(context)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //Log.i("@@@@", "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", "
        //        + MeasureSpec.toString(heightMeasureSpec) + ")");
        Log.d(TAG, "/////////////////////////////////////////////////////")
        var width = View.getDefaultSize(mVideoWidth, widthMeasureSpec)
        var height = View.getDefaultSize(mVideoHeight, heightMeasureSpec)
        Log.d(TAG, "屏幕默宽高：$width,$height")
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            Log.d(TAG, "mVideoWidth:$mVideoWidth,mVideoHeight:$mVideoHeight")
            val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
            val widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec)
            val heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec)

            if (widthSpecMode == View.MeasureSpec.EXACTLY && heightSpecMode == View.MeasureSpec.EXACTLY) {
                Log.d(TAG, "宽高的值都为精准值,测量的控件的宽:" + width + "高度:" + height)
                // the size is fixed
                width = widthSpecSize
                height = heightSpecSize

                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight
                    Log.d(TAG, "image too wide, correcting,width:" + width)
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth
                    Log.d(TAG, "image too tall, correcting,height:" + height)
                }
            } else if (widthSpecMode == View.MeasureSpec.EXACTLY) {
                Log.d(TAG, "只有宽度是精确值")
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize
                height = width * mVideoHeight / mVideoWidth
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize
                }
            } else if (heightSpecMode == View.MeasureSpec.EXACTLY) {
                Log.d(TAG, "只有高度是精确值")
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize
                width = height * mVideoWidth / mVideoHeight
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize
                }
            } else {
                Log.d(TAG, "宽高都都是wrapContent")
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth
                height = mVideoHeight
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize
                    width = height * mVideoWidth / mVideoHeight
                }
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize
                    height = width * mVideoHeight / 1
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
            //            Log.d(TAG, "measure width:" + width + ",height:" + height);
            Log.d(TAG, "*************************************")
            Log.d(TAG, "video的宽高为0")
            Log.d(TAG, "**************************************")
            //            width = 960;
            //            height = 540;
        }
        setMeasuredDimension(width, height)
        Log.d(TAG, "/////////最终的宽高是,width:$width,height:$height")
        // 记录原始的控件大小
        mDefaultH = height
        mDefaultW = width
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = VideoView::class.java.name
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            info.className = VideoView::class.java.name
        }
    }

    fun resolveAdjustedSize(desiredSize: Int, measureSpec: Int): Int {
        return View.getDefaultSize(desiredSize, measureSpec)
    }

    private fun initVideoView(context: Context) {
        mVideoWidth = 0
        mVideoHeight = 0
        holder.addCallback(mSHCallback)
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
        mCurrentState = STATE_IDLE
        mTargetState = STATE_IDLE


        // 初始化自定义的变量
        mContext = context
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mScreenW = manager.defaultDisplay.width
        mScreenH = manager.defaultDisplay.height
    }

    fun setVideoPath(path: String) {
        setVideoURI(Uri.parse(path))
    }

    /**
     * @hide
     */
    @JvmOverloads
    fun setVideoURI(uri: Uri, headers: Map<String, String>? = null) {
        mUri = uri
        mHeaders = headers
        mSeekWhenPrepared = 0
        openVideo()
        requestLayout()
        invalidate()
    }

    fun stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
            mCurrentState = STATE_IDLE
            mTargetState = STATE_IDLE
        }
    }

    private fun openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return
        }
        // Tell the music playback service to pause
        // TODO: these constants need to be published somewhere in the framework.
        val i = Intent("com.android.music.musicservicecommand")
        i.putExtra("command", "pause")
        mContext!!.sendBroadcast(i)

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false)
        try {
            mMediaPlayer = MediaPlayer()
            if (mAudioSession != 0) {
                mMediaPlayer!!.audioSessionId = mAudioSession
            } else {
                mAudioSession = mMediaPlayer!!.audioSessionId
            }
            mMediaPlayer!!.setOnPreparedListener(mPreparedListener)
            mMediaPlayer!!.setOnVideoSizeChangedListener(mSizeChangedListener)
            mMediaPlayer!!.setOnCompletionListener(mCompletionListener)
            mMediaPlayer!!.setOnErrorListener(mErrorListener)
            mMediaPlayer!!.setOnInfoListener(mOnInfoListener)
            mMediaPlayer!!.setOnBufferingUpdateListener(mBufferingUpdateListener)
            mCurrentBufferPercentage = 0
            mMediaPlayer!!.setDataSource(mContext!!, mUri!!/*, mHeaders*/)
            mMediaPlayer!!.setDisplay(mSurfaceHolder)
            mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mMediaPlayer!!.setScreenOnWhilePlaying(true)
            mMediaPlayer!!.prepareAsync()
            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING
            attachMediaController()
        } catch (ex: IOException) {
            Log.w(TAG, "Unable to open content: " + mUri!!, ex)
            mCurrentState = STATE_ERROR
            mTargetState = STATE_ERROR
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
            return
        } catch (ex: IllegalArgumentException) {
            Log.w(TAG, "Unable to open content: " + mUri!!, ex)
            mCurrentState = STATE_ERROR
            mTargetState = STATE_ERROR
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
            return
        }

    }

    fun setMediaController(controller: MediaController) {
        if (mMediaController != null) {
            mMediaController!!.hide()
        }
        mMediaController = controller
        attachMediaController()
    }

    private fun attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController!!.setMediaPlayer(this)
            val anchorView = if (this.parent is View)
                this.parent as View
            else
                this
            mMediaController!!.setAnchorView(anchorView)
            mMediaController!!.isEnabled = isInPlaybackState
        }
    }

    fun setBufferingUpdateListener(mBufferingUpdateListener: MediaPlayer.OnBufferingUpdateListener) {
        this.mBufferingUpdateListener = mBufferingUpdateListener
    }

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    fun setOnPreparedListener(l: MediaPlayer.OnPreparedListener) {
        mOnPreparedListener = l
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    fun setOnCompletionListener(l: OnCompletionListener) {
        mOnCompletionListener = l
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    fun setOnErrorListener(l: OnErrorListener) {
        mOnErrorListener = l
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    fun setOnInfoListener(l: OnInfoListener) {
        mOnInfoListener = l
    }

    /*
     * release the media player in any state
     */
    private fun release(cleartargetstate: Boolean) {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.reset()
            mMediaPlayer!!.release()
            mMediaPlayer = null
            mCurrentState = STATE_IDLE
            if (cleartargetstate) {
                mTargetState = STATE_IDLE
            }
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (isInPlaybackState && mMediaController != null) {
            toggleMediaControlsVisiblity()
        }
        return false
    }

    override fun onTrackballEvent(ev: MotionEvent): Boolean {
        if (isInPlaybackState && mMediaController != null) {
            toggleMediaControlsVisiblity()
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL
        if (isInPlaybackState && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer!!.isPlaying) {
                    pause()
                    mMediaController!!.show()
                } else {
                    start()
                    mMediaController!!.hide()
                }
                return true
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer!!.isPlaying) {
                    start()
                    mMediaController!!.hide()
                }
                return true
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer!!.isPlaying) {
                    pause()
                    mMediaController!!.show()
                }
                return true
            } else {
                toggleMediaControlsVisiblity()
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun toggleMediaControlsVisiblity() {
        if (mMediaController!!.isShowing) {
            mMediaController!!.hide()
        } else {
            mMediaController!!.show()
        }
    }

    override fun start() {
        if (isInPlaybackState) {
            mMediaPlayer!!.start()
            mCurrentState = STATE_PLAYING
        }
        mTargetState = STATE_PLAYING
    }

    override fun pause() {
        if (isInPlaybackState) {
            if (mMediaPlayer!!.isPlaying) {
                mMediaPlayer!!.pause()
                mCurrentState = STATE_PAUSED
            }
        }
        mTargetState = STATE_PAUSED
    }

    fun suspend() {
        release(false)
    }

    fun resume() {
        openVideo()
    }

    override fun getDuration(): Int {
        return if (isInPlaybackState) {
            mMediaPlayer!!.duration
        } else -1

    }

    override fun getCurrentPosition(): Int {
        return if (isInPlaybackState) {
            mMediaPlayer!!.currentPosition
        } else 0
    }

    override fun seekTo(msec: Int) {
        if (isInPlaybackState) {
            mMediaPlayer!!.seekTo(msec)
            mSeekWhenPrepared = 0
        } else {
            mSeekWhenPrepared = msec
        }
    }

    override fun isPlaying(): Boolean {
        return isInPlaybackState && mMediaPlayer!!.isPlaying
    }

    override fun getBufferPercentage(): Int {
        return if (mMediaPlayer != null) {
            mCurrentBufferPercentage
        } else 0
    }

    override fun canPause(): Boolean {
        return mCanPause
    }

    override fun canSeekBackward(): Boolean {
        return mCanSeekBack
    }

    override fun canSeekForward(): Boolean {
        return mCanSeekForward
    }

    override fun getAudioSessionId(): Int {
        if (mAudioSession == 0) {
            val foo = MediaPlayer()
            mAudioSession = foo.audioSessionId
            foo.release()
        }
        return mAudioSession
    }


    // 如果当前是全屏状态则切换到默认的大小，否则切换到全屏大小
    fun switchFullScreen() {
        if (isFullSreen) {
            // 全屏状态，切换到默认大小
            layoutParams.width = mDefaultW
            layoutParams.height = mDefaultH
            Log.d(TAG, "***********全屏切换到非全屏，width:$mDefaultW,height:$mDefaultH**********************")
        } else {
            //            LogUtils.e(TAG,"VideoView.switchFullScreen,mDefaultW="+mDefaultW+";mScreenW="+mScreenW+";isFullSreen="+isFullSreen);
            // 非全屏状态，切换到全屏大小
            layoutParams.width = mScreenW
            layoutParams.height = mScreenH
            Log.d(TAG, "********非全屏切换到全屏，width:$mScreenW,height:$mScreenH******************************")
        }
        // 刷新控件大小
        requestLayout()
        isFullSreen = !isFullSreen
    }

    companion object {

        // all possible internal states
        private val STATE_ERROR = -1
        private val STATE_IDLE = 0
        private val STATE_PREPARING = 1
        private val STATE_PREPARED = 2
        private val STATE_PLAYING = 3
        private val STATE_PAUSED = 4
        private val STATE_PLAYBACK_COMPLETED = 5
    }
}