package com.jackli.www.okplayer.utils;

import android.os.Environment;

import java.io.File;

/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.okplayer.utils
 * @description: description
 * @date: 2018/2/7
 * @time: 21:16
 */
public class LyricsLoader {

    static File ROOT = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Download/audio/");

    public static File loadLyricFile(String title) {
        if (ROOT.isDirectory())
            LogUtils.e("lyrics", "文件夹存在");
        if (ROOT.exists()) {
            LogUtils.e("lyrics", "audio所在目录正确：" + ROOT.getPath());
        }
//        查找本地的lrc文件
//        File file = new File(ROOT, title);
        File file = new File(ROOT, title + ".lrc");
        LogUtils.e("RootPath", ROOT.getPath() + '\n' + ROOT.getAbsolutePath());
        if (file.exists()) {
            LogUtils.e("lyrics", "歌词文件存在");
            return file;
        }
        //查找本地的txt文件
        file = new File(ROOT, title + ".txt");
        if (file.exists()) {
            return file;
        }
        //到lrc文件夹下查找
        //没有的话，去服务器下载
        return null;
    }
}
