package com.ajiew.mp3callrecorder.library.helper

import android.util.Log

/**
 * @author aJIEw
 */
object DebugLog {

    var IS_DEBUG = true

    fun d(msg: String) {
        debugLog("", msg)
    }

    fun d(packName: String, msg: String) {
        debugLog(packName, msg)
    }

    fun d(msg: String, vararg format: Any) {
        if (IS_DEBUG) {
            Log.i("debug", String.format(msg, *format))
        }
    }

    private fun debugLog(packName: String, msg: String) {
        if (IS_DEBUG) {
            Log.d("debug", "$packName-->$msg")
        }
    }

    fun e(msg: String, t: Throwable?) {
        if (IS_DEBUG) {
            Log.e("error", msg, t)
        }
    }
}