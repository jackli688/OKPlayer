package com.jackli.www.okplayer;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

/**
 * 获取服务器的url,然后调用本地的okPlayer来进行播放
 */
public class MainActivity extends AppCompatActivity {
//    private static final String URL = "http://192.168.31.123:8080/player_test/video/video1.mp4";
    private static final String URL = "http://192.168.43.97:8080/player_test/video/oppo.mp4";
    String videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        final TextView url = findViewById(R.id.url);
//        final ClipboardManager cmb = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
//        cmb.setText(content.trim()); //将内容放入粘贴管理器,在别的地方长按选择"粘贴"即可
//        cm.getText();//获取粘贴信息
        url.setTextIsSelectable(true);
//        url.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                Toast.makeText(MainActivity.this, "订单号已复制到剪切板，快去粘贴吧~", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        });
        final Button play = findViewById(R.id.play);
        final VideoView videoView = findViewById(R.id.video);
//        videoView.setVideoURI(Uri.parse(URL));
        url.setText("视频的url:" + URL);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setDataAndType(Uri.parse(URL), "video/mp4");
                startActivity(intent);
                playVideo(videoView, URL);
//                Log.d("clipboard", "onclick is run");
//                ClipData clip = clipboard.getPrimaryClip();
//                if (clip == null) {
//                    Log.d("clipboard", "剪切中的数据为空");
//                    return;
//                } else {
//                    ClipData.Item itemAt = clip.getItemAt(0);
//                    CharSequence text = itemAt.getText();
//                    Log.d("clipboard", "text:" + text);
//                }
            }
        });

        //video设置点击事件无效
        findViewById(R.id.video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("clipboard", "videoview was click");
            }
        });
    }


    public void playVideo(final VideoView videoViewPlayer, String videoPath) {
        try {
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoViewPlayer);
            mediaController.setMediaPlayer(videoViewPlayer);
            videoViewPlayer.setMediaController(mediaController);
            videoViewPlayer.setVideoURI(Uri.parse(videoPath));
            videoViewPlayer.requestFocus();
            videoViewPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                    videoViewPlayer.start();
                }
            });
            videoViewPlayer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
