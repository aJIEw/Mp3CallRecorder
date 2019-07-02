package com.ajiew.mp3callrecorder.library

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.ajiew.mp3callrecorder.library.helper.DebugLog

/**
 * @author aJIEw
 * Created on: 2019-06-18 15:56
 */
class PhoneCallService : Service() {

    private lateinit var mp3CallRecorder: Mp3CallRecorder

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        DebugLog.d("onCreate()")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        DebugLog.d("onStartCommand()")

        mp3CallRecorder = Mp3CallRecorder.Builder(this)
//            .setRecordFilePath()
//            .setRecordFileName()
//            .setShowSource()
//            .setShowNumber()
//            .setShowTime()
            .build()

        mp3CallRecorder.startPhoneCallReceiver()

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()

        DebugLog.d("onDestroy()")
        mp3CallRecorder.stopPhoneCallReceiver()
    }
}