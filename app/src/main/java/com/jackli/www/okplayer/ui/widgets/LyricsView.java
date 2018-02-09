package com.jackli.www.okplayer.ui.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.jackli.www.okplayer.R;
import com.jackli.www.okplayer.model.bean.Lyric;
import com.jackli.www.okplayer.utils.LyricParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.servers.ui.widgets
 * @description: description
 * @date: 2018/2/6
 * @time: 21:26
 */
@SuppressLint("AppCompatCustomView")
public class LyricsView extends TextView {

    private Paint mPaint;
    private int mHighlightColor = Color.GREEN;
    private int mNormalColor = Color.WHITE;
    private int mHighLightSize;
    private int mNormalSize;
    private int mViewWeight;
    private int mViewHeight;
    private int mMiddleLine;
    private List<Lyric> mLyricList;
    private int mLineH;
    private int mDuration;
    private int mPosition;

    public LyricsView(Context context) {
        this(context, null);
    }


    public LyricsView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // 初始化绘制的对象
    private void init() {
        //从资源文件，获取字体大小
        mHighLightSize = getResources().getDimensionPixelSize(R.dimen.lyrics_highlight);
        mNormalSize = getResources().getDimensionPixelSize(R.dimen.lyrics_normal);
        mLineH = getResources().getDimensionPixelOffset(R.dimen.lyrics_line_height);

        //初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mHighlightColor);
        mPaint.setTextSize(mHighLightSize);
//        initData();  //临时填充数据


    }

    @Keep
    private void initData() {
        //模拟歌词数据
        mLyricList = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            mLyricList.add(new Lyric(i * 20000, "当前歌词的行数是:" + i));
        }
        mMiddleLine = 16;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mViewWeight = w;
        mViewHeight = h;
    }

    @Override
    public boolean onPreDraw() {
        return super.onPreDraw();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLyricList == null || mLyricList.size() == 0) {
            drawSingleLineText(canvas);
        } else {
//            LogUtils.e("lyrics", "多行歌词绘制");
            drawMulctLineText(canvas);
        }
    }

    //绘制一行文本
    private void drawMulctLineText(Canvas canvas) {
//        获取居中行的歌词
        Lyric middleLyric = mLyricList.get(mMiddleLine);
//        居中行Y偏移 = 行高* 偏移百分比
//        偏移百分比=行已用时间/行可用百分时间
//        行已用时间= position-行起始时间
//        行可用时间=下一行起始时间-行起始时间
        //下一行起始时间
        int nextStartPoint;
        if (mMiddleLine == mLyricList.size() - 1) {
            //最后一行
            nextStartPoint = mDuration;
        } else {
            Lyric nextLyric = mLyricList.get(mMiddleLine + 1);
            nextStartPoint = nextLyric.getStartPoint();
        }
        //行可用时间
        int lineTime = nextStartPoint - middleLyric.getStartPoint();
        //行已用时间
        int pastTime = mPosition - middleLyric.getStartPoint();
        //偏移百分比
        float pastPercent = pastTime * 1.0f / lineTime;
        //居中行Y偏移
        float offsetY = mLineH * pastPercent;

        //计算居中行的Y位置
        Rect bounds = new Rect();
        String content = middleLyric.getContent();
        mPaint.getTextBounds(content, 0, content.length(), bounds);
        float centerY = mViewHeight / 2 + bounds.height() / 2 - offsetY;

        //绘制所有行的歌词，并相对于居中进行偏移
        for (int i = 0; i < mLyricList.size(); i++) {
            //只有居中行使用高亮色
            if (i == mMiddleLine) {
                mPaint.setColor(mHighlightColor);
            } else {
                mPaint.setColor(mNormalColor);
            }
            Lyric drawLyric = mLyricList.get(i);
            //计算正在绘制行的位置
            float drawY = centerY + (i - mMiddleLine) * mLineH;
            drawTextHorizontal(canvas, drawLyric.getContent(), drawY);
        }
    }


    //    绘制整个歌词列表
    private void drawSingleLineText(Canvas canvas) {
        //绘制一行文本
        String text = "正在加载歌词.....";
        //X = view的一半宽度 - 文字的一半宽度
        //Y = view的一半高度 +  文字的一半高度
        Rect bounds = new Rect();
        mPaint.getTextBounds(text, 0, text.length(), bounds);
        float drawY = mViewHeight / 2 + bounds.height() / 2;
        drawTextHorizontal(canvas, text, drawY);
    }

    //    在指定高度上水平橘红绘制文本
    private void drawTextHorizontal(Canvas canvas, String content, float drawY) {
        float textWidth = mPaint.measureText(content);//测量文本宽度
        float drawX = mViewHeight / 2 - textWidth / 2;
        canvas.drawText(content, drawX, drawY, mPaint);
    }

    //根据当前播放进度，计算居中行位置
    public void computeMiddleLine(int position, int duration) {
        mDuration = duration;
        mPosition = position;
        //遍历所有行歌词，如果某行的时间比position小,同时下一行的时间比position大
        for (int i = 0; i < mLyricList.size(); i++) {
            Lyric currentLyric = mLyricList.get(i);
            int nextStartPoint;
            if (i == mLyricList.size() - 1) {
                //最后一行，取得的可用时间截止到歌曲长度
                nextStartPoint = duration;
            } else {
                Lyric nextLyric = mLyricList.get(i + 1);
                nextStartPoint = nextLyric.getStartPoint();
            }

//            根据position是否在当前歌曲的播放区间内,来确定是否是居中行
            if (currentLyric.getStartPoint() <= position && nextStartPoint > position) {
                mMiddleLine = i;
                break;
            }
        }
        //已经找到当前行了，刷新界面即可
        invalidate();
    }

    @Keep
    public void setLyricFile(File lyricFile) {
        mLyricList = LyricParser.parseFile(lyricFile);
        mMiddleLine = 0;
    }
}
