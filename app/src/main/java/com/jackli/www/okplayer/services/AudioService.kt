package com.jackli.www.okplayer.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel.DEFAULT_CHANNEL_ID
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.widget.RemoteViews
import android.widget.Toast
import com.jackli.www.okplayer.R
import com.jackli.www.okplayer.model.bean.AudioItem
import com.jackli.www.okplayer.ui.activities.AudioPlayerActivity
import com.jackli.www.okplayer.utils.LogUtils
import java.io.IOException
import java.util.*

/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.servers.services
 * @description: description
 * @date: 2018/1/29
 * @time: 18:56
 */
class AudioService : Service() {
    private var audioItems: ArrayList<AudioItem>? = null
    private var position: Int = -1
    private var mAudioBinder: AudioBinder? = null
    private var mPlayMode: Int = PLAYMODE_ALL_REPEAT

    companion object {
        private val TAG = "AudioService"
        //播放模式
        val PLAYMODE_ALL_REPEAT = 0
        val PLAYMODE_SINGLE_REPEAT = 1
        val PLAYMODE_RANDOM = 2
        //启动模式
        val NOTIFY_PRE = 0
        val NOTIFY_NEXT = 1
        val NOTIFY_CONTENT = 2
    }

    override fun onCreate() {
        super.onCreate()
        mAudioBinder = AudioBinder()

        val preferences = getSharedPreferences("config", MODE_PRIVATE)
        mPlayMode = preferences.getInt("playmode", PLAYMODE_ALL_REPEAT)
    }

    override fun onBind(intent: Intent): IBinder? {
        return mAudioBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        LogUtils.d(TAG, "AudioService.onStartCommand,intent=" + intent)
        val notify_type = intent.getIntExtra("notify_type", -1)
        when (notify_type) {
            NOTIFY_PRE -> {
                //通知栏上一首
                mAudioBinder?.playPre()
            }
            NOTIFY_NEXT -> {
                //通知栏下一首
                mAudioBinder?.playNext()
            }
            NOTIFY_CONTENT -> {
                //通知栏正文
                notifyUI()
            }
            else -> {
                //不是从通知栏启动，也就是从播放列表启动了播放界面，正常播放音乐就行
                val position = intent.getIntExtra("position", -1)
                if (position == this@AudioService.position) {
                    //重复打开了同一首歌,不需要从头播放
                    notifyUI()
                } else {
                    //打开了一首新歌，需要开始播放
                    //获取当前要播放的音乐
                    //获取当前要播放的音乐
                    audioItems = intent.getParcelableArrayListExtra("audioItems")
                    LogUtils.e(TAG, "AudioService.onStartCommand,position=$position;audioItems=$audioItems")
                    this@AudioService.position = position  //播放不同的歌曲，需要更i性能歌曲的位置
                    mAudioBinder!!.playItem()
                }

            }
        }
        return super.onStartCommand(intent, flags, startId)

    }


    inner class AudioBinder : Binder() {
        private var mediaPlayer: MediaPlayer? = null

        /**
         * 返回true,表示正在播放nag
         */
        val isPlaying: Boolean
            get() = mediaPlayer!!.isPlaying

        /**
         * 返回音乐的总时长
         */
        val duration: Int
            get() = mediaPlayer!!.duration

        /**
         * 返回当前的播放位置
         */
        val currentPosition: Int
            get() = mediaPlayer!!.currentPosition

        private inner class OnAudioPreparedListener : MediaPlayer.OnPreparedListener {

            override fun onPrepared(mp: MediaPlayer) {
                //音乐准备完毕，开始播放
                mediaPlayer!!.start()

                //通知界面更新
                notifyUI()

                //显示通知
                showNotification()

            }
        }

        fun playItem() {
            if (audioItems!!.size == 0 || position == -1) {
                return
            }
            val audioItem = audioItems!![position]
            if (mediaPlayer != null) {
                mediaPlayer!!.reset()
            } else {
                mediaPlayer = MediaPlayer()
            }
            //播放音乐
            try {
                mediaPlayer?.setDataSource(audioItem.path)
                mediaPlayer?.setOnPreparedListener(OnAudioPreparedListener())
                mediaPlayer?.setOnCompletionListener(OnAudioCompletionListener())
                mediaPlayer?.prepareAsync()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        inner class OnAudioCompletionListener : MediaPlayer.OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer?) {
                //当前歌曲播放结束
                autoPlayNext()
            }

        }

        /**
         * 如果歌曲正在播放则暂停，否则开启播放
         */
        fun switchPauseStatus() {
            if (this.getPlayStatus()!!)
                mediaPlayer?.pause()
            else
                mediaPlayer?.start()
        }

        /**
         * 获取当前播放器的状态
         */
        fun getPlayStatus(): Boolean? = mediaPlayer?.isPlaying

        /**
         * 跳转到指定毫秒处播放
         */
        fun seekTo(msec: Int) {
            mediaPlayer!!.seekTo(msec)
        }

        fun getPlayMode(): Int {
            return mPlayMode
        }

        @SuppressLint("ApplySharedPref")
        fun switchPlayMode() {
            when (mPlayMode) {
                PLAYMODE_ALL_REPEAT -> mPlayMode = PLAYMODE_SINGLE_REPEAT
                PLAYMODE_SINGLE_REPEAT -> mPlayMode = PLAYMODE_RANDOM
                PLAYMODE_RANDOM -> mPlayMode = PLAYMODE_ALL_REPEAT
            }
            val preferences = getSharedPreferences("config", Context.MODE_PRIVATE)
            preferences.edit().putInt("playmode", mPlayMode).commit()
        }

        /**播放上一首*/
        fun playPre() = if (position > 0) {
            position--
            playItem()
        } else Toast.makeText(this@AudioService, "已经是第一首歌了!!", Toast.LENGTH_SHORT).show()

        /**播放下一首*/
        fun playNext() {
            if (position < audioItems?.size!! - 1) {
                position++
                playItem()
            } else Toast.makeText(this@AudioService, "已经是最后一首歌了", Toast.LENGTH_SHORT).show()
        }


        /**根据当前播放模式，自动选择下一首进行播放*/
        private fun autoPlayNext() {
            when (mPlayMode) {
                PLAYMODE_ALL_REPEAT -> {
                    //列表循环，如果已经是最后首歌曲则跳转到第一首歌曲，否则播放下一首歌曲
                    if (position == audioItems?.size!! - 1)
                        position = 0
                    else position++
                }
                PLAYMODE_SINGLE_REPEAT -> {
                    //单曲循环，保持当前位置，重新播放
                }
                PLAYMODE_RANDOM -> {
                    //随机播放，在列表范围内生成随机位置，播放
                    position = Random(System.currentTimeMillis()).nextInt(audioItems?.size!!)
                }
            }
            //播放当前的选中的歌曲
            playItem()
        }
    }


    /**显示通知*/
    private fun showNotification() {
        val notification: Notification = getCustomNotificationByNewApi()
        val manager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(0, notification)
    }

    /**使用新API 来生成通知对象*/
    /** 使用新 API 来生成通知对象  */
    private fun getCustomNotificationByNewApi(): Notification =
            NotificationCompat.Builder(this@AudioService, DEFAULT_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.icon)
                    .setTicker("正在播放:" + getCurrentItem()?.title)
                    .setContent(getRemotesView())
                    .setOngoing(true)
                    .mNotification


    private fun getRemotesView(): RemoteViews? {
        val remoteViews = RemoteViews(packageName, R.layout.audio_notification)
        //设置文本
        remoteViews.setTextViewText(R.id.audio_notify_tv_title, getCurrentItem()?.title)
        remoteViews.setTextViewText(R.id.audio_notify_tv_artist, getCurrentItem()?.artist)
        //设置点击事件
        remoteViews.setOnClickPendingIntent(R.id.audio_notify_iv_pre, getPreIntent())
        remoteViews.setOnClickPendingIntent(R.id.audio_notify_iv_next, getNextIntent())
        remoteViews.setOnClickPendingIntent(R.id.audio_notify_layout, getContentIntent())
        return remoteViews
    }

    /**生成正文的点击响应*/
    private fun getContentIntent(): PendingIntent? {
        val intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra("notify_type", NOTIFY_CONTENT)
        return PendingIntent.getActivity(this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**店家下一曲时使用的PendingIntent*/
    private fun getNextIntent(): PendingIntent? {
        val intent = Intent(this, AudioService::class.java)
        intent.putExtra("notify_type", NOTIFY_NEXT)
        return PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**点击上一曲时使用的PendingIntent*/
    private fun getPreIntent(): PendingIntent? {
        val intent = Intent(this, AudioService::class.java)
        intent.putExtra("notify_type", NOTIFY_PRE)
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getCurrentItem(): AudioItem? {
        return audioItems?.get(position)
    }

    private fun notifyUI() {
        //获取当前正在播放的歌曲
        val audioItem = audioItems!![position]

        //发送广播，通知界面，歌曲已经开始播放播放了
        val intent = Intent("com.jackli.prepared")
        intent.putExtra("audioItem", audioItem)
        sendBroadcast(intent)
    }

}
