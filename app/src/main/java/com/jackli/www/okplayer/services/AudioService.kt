package com.jackli.www.okplayer.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder

import com.jackli.www.okplayer.model.bean.AudioItem
import com.jackli.www.okplayer.utils.LogUtils

import java.io.IOException
import java.util.ArrayList

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
    private var position: Int = 0
    private var mAudioBinder: AudioBinder? = null

    override fun onCreate() {
        super.onCreate()
        mAudioBinder = AudioBinder()
    }

    override fun onBind(intent: Intent): IBinder? {
        return mAudioBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        //获取当前要播放的音乐
        audioItems = intent.getParcelableArrayListExtra("audioItems")
        position = intent.getIntExtra("position", -1)
        LogUtils.e(TAG, "AudioService.onStartCommand,position=$position;audioItems=$audioItems")
        mAudioBinder!!.playItem()
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
                //获取当前正在播放的歌曲
                val audioItem = audioItems!![position]

                //发送广播，通知界面，歌曲已经开始播放播放了
                val intent = Intent("")
                intent.putExtra("audioItem", audioItem)
                sendBroadcast(intent)
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
                mediaPlayer!!.setDataSource(audioItem.path)
                mediaPlayer!!.setOnPreparedListener(OnAudioPreparedListener())
                mediaPlayer!!.prepareAsync()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        /**
         * 如果歌曲正在播放则暂停，否则开启播放
         */
        fun swtichPauseStatus() {
            if (mediaPlayer!!.isLooping)
                mediaPlayer!!.pause()
            else
                mediaPlayer!!.start()
        }

        /**
         * 跳转到指定毫秒处播放
         */
        fun seekTo(msec: Int) {
            mediaPlayer!!.seekTo(msec)
        }
    }

    companion object {
        private val TAG = "AudioService"
    }
}
