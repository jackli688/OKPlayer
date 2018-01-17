package com.jackli.www.okplayer.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.widget.MediaController;

/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.okplayer.ui.widgets
 * @description: description
 * @date: 2017/12/31
 * @time: 0:15
 */
public class VideoView extends SurfaceView implements MediaController.MediaPlayerControl {

    public VideoView(Context context) {
        super(context);
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public void seekTo(int pos) {

    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }
}
