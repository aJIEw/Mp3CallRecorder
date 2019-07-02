package com.ajiew.mp3callrecorder.library

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Environment
import com.ajiew.mp3callrecorder.library.PhoneCallReceiver.Companion.ACTION_IN
import com.ajiew.mp3callrecorder.library.PhoneCallReceiver.Companion.ACTION_OUT
import com.ajiew.mp3callrecorder.library.helper.DebugLog
import com.ajiew.mp3callrecorder.library.helper.PrefsHelper

/**
 * Controller for recording phone call
 *
 * @author aJIEw
 */
class Mp3CallRecorder private constructor(private val context: Context) {

    private var phoneCallReceiver: PhoneCallReceiver? = null

    /**
     * Start phone call service and listening for phone calls and recording it, recommended starting by this.
     * */
    fun startPhoneCallService() {
        val intent = Intent()
        intent.setClass(context, PhoneCallService::class.java)

        context.startService(intent)
        DebugLog.d("startService()")
    }

    /**
     * Start phone call receiver and listening for phone calls.
     * */
    fun startPhoneCallReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_IN)
        intentFilter.addAction(ACTION_OUT)
        intentFilter.priority = 100

        if (phoneCallReceiver == null) {
            phoneCallReceiver = PhoneCallReceiver()
        }
        context.registerReceiver(phoneCallReceiver, intentFilter)
    }

    /**
     * Unregister receiver.
     * */
    fun stopPhoneCallReceiver() {
        try {
            if (phoneCallReceiver != null) {
                context.unregisterReceiver(phoneCallReceiver)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Change receiver to your customized phone call receiver
     *
     * Please call this before calling startPhoneCallReceiver() or startPhoneCallService()
     * */
    fun changeReceiver(receiver: PhoneCallReceiver) {
        phoneCallReceiver = receiver
    }

    class Builder(private val context: Context) {

        init {
            PrefsHelper.writePrefString(context,
                PREF_FILE_NAME, "Record")
            PrefsHelper.writePrefString(context,
                PREF_FILE_PATH, Environment.getExternalStorageDirectory().path)
            PrefsHelper.writePrefBool(context,
                PREF_SHOW_SOURCE, true)
            PrefsHelper.writePrefBool(context,
                PREF_SHOW_NUMBER, true)
            PrefsHelper.writePrefBool(context,
                PREF_SHOW_TIME, true)
        }

        fun build(): Mp3CallRecorder {
            return Mp3CallRecorder(context)
        }

        fun setRecordFilePath(filePath: String): Builder {
            PrefsHelper.writePrefString(context,
                PREF_FILE_PATH, filePath)
            return this
        }

        fun setRecordFileName(fileName: String): Builder {
            PrefsHelper.writePrefString(context,
                PREF_FILE_NAME, fileName)
            return this
        }

        fun setShowSource(showSource: Boolean): Builder {
            PrefsHelper.writePrefBool(context,
                PREF_SHOW_SOURCE, showSource)
            return this
        }

        fun setShowNumber(showNumber: Boolean): Builder {
            PrefsHelper.writePrefBool(context,
                PREF_SHOW_NUMBER, showNumber)
            return this
        }

        fun setShowTime(showTime: Boolean): Builder {
            PrefsHelper.writePrefBool(context,
                PREF_SHOW_TIME, showTime)
            return this
        }
    }

    companion object {
        /**
         * Record file path
         * */
        const val PREF_FILE_PATH = "PrefFilePath"

        /**
         * Record file name
         * */
        const val PREF_FILE_NAME = "PrefFileName"

        /**
         * Show source of the phone call, incoming or outgoing
         * */
        const val PREF_SHOW_SOURCE = "PrefShowSource"

        /**
         * Show phone number, incoming number or dialing out number
         * */
        const val PREF_SHOW_NUMBER = "PrefShowNumber"

        /**
         * Show call start time, answer time (incoming call), end time
         * */
        const val PREF_SHOW_TIME = "PrefShowTime"
    }
}