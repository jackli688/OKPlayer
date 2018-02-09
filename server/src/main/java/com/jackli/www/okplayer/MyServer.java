package com.jackli.www.okplayer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * @author: jackli
 * @version: V1.0
 * @project: OkPlayer
 * @package: com.jackli.www.servers
 * @description: description
 * @date: 2018/1/29
 * @time: 22:03
 */
public class MyServer extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
