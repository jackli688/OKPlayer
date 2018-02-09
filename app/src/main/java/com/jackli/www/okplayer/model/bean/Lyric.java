package com.jackli.www.okplayer.model.bean;

import android.support.annotation.NonNull;

/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.okplayer.model.bean
 * @description: description
 * @date: 2018/2/6
 * @time: 23:34
 */
public class Lyric implements Comparable<Lyric> {

    private int startPoint;
    private String content;


    public Lyric(int startPoint, String content) {
        this.startPoint = startPoint;
        this.content = content;
    }

    public void setStartPoint(int startPoint) {
        this.startPoint = startPoint;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStartPoint() {
        return startPoint;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Lyric{" +
                "startPoint=" + startPoint +
                ", content='" + content + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NonNull Lyric o) {
        return 0;
    }
}
