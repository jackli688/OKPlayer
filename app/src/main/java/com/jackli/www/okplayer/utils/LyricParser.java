package com.jackli.www.okplayer.utils;

import com.jackli.www.okplayer.model.bean.Lyric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.okplayer.utils
 * @description: description
 * @date: 2018/2/7
 * @time: 0:24
 */
public class LyricParser {

    public static List<Lyric> parseFile(File lyricFile) {
        ArrayList<Lyric> arrayList = new ArrayList<>();
        if (lyricFile == null || !lyricFile.exists()) {
            arrayList.add(new Lyric(0, "歌词文件不存在"));
            return arrayList;
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(lyricFile), "GBK"));
            String line = reader.readLine();
            while (line != null) {
                ArrayList<Lyric> lineLyrics = parseLine(line);
                arrayList.addAll(lineLyrics);
                //继续读取下一行歌词
                line = reader.readLine();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(arrayList);
        return arrayList;
    }

    //    解析一行歌词
    private static ArrayList<Lyric> parseLine(String line) {
        ArrayList<Lyric> lineLyrics = new ArrayList<>();
        String[] arr = line.split("]");
        String content = arr[arr.length - 1];
        for (int i = 0; i < arr.length - 1; i++) {
            int startPoint = parseStartPoint(arr[i]);
            Lyric lyric = new Lyric(startPoint, content);
            lineLyrics.add(lyric);
        }
        return lineLyrics;
    }

    //    解析歌词
    private static int parseStartPoint(String time) {
        String regularExpression = ":";
        String[] arr = time.split(regularExpression);

        String minStr = arr[0].substring(1, 3);

        String[] arr2 = arr[1].split("\\.");
        String secStr = arr2[0];
        String msecStr = arr2[1];

        //将字符串转换为int值
        int min = Integer.parseInt(minStr);
        int sec = Integer.parseInt(secStr);
        int msec = Integer.parseInt(msecStr);

        //计算歌词起始时间
        int startPoint = min * 60 * 1000 + sec * 1000 + msec * 10;
        return startPoint;
    }
}
