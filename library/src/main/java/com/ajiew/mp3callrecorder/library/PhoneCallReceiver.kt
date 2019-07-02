package com.ajiew.mp3callrecorder.library

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.widget.Toast
import com.ajiew.mp3callrecorder.library.helper.PrefsHelper
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Override this to manage phone call and change the recording behaviour
 *
 * @author aJIEw
 * */
open class PhoneCallReceiver : BroadcastReceiver() {

    private lateinit var context: Context

    // call in or call out number, not always available
    private var callNumber: String? = null

    private var isIncoming: Boolean = false
    private var callStartTime: Date? = null
    private var answerTime: Date? = null

    private lateinit var mp3Recorder: Mp3Recorder
    private var isRecordStarted = false
    private var recordFile: File? = null

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context

        if (recordFile == null) {
            recordFile = File(context.externalCacheDir, "tmp_record")
        }

        if (intent.action == ACTION_OUT) {
            callNumber = intent.extras!!.getString(EXTRA_PHONE_NUMBER)
        } else {
            val stateStr = intent.extras!!.getString(TelephonyManager.EXTRA_STATE)
            val number = intent.extras!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
            callNumber = number
            var state = 0

            if (stateStr == TelephonyManager.EXTRA_STATE_IDLE) {
                state = TelephonyManager.CALL_STATE_IDLE
            } else if (stateStr == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                state = TelephonyManager.CALL_STATE_OFFHOOK
            } else if (stateStr == TelephonyManager.EXTRA_STATE_RINGING) {
                state = TelephonyManager.CALL_STATE_RINGING
            }
            onCallStateChanged(state)
        }
    }

    private fun onCallStateChanged(state: Int) {
        if (state == lastState) return

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> { // incoming call
                isIncoming = true
                callStartTime = Date()

                onIncomingCallReceived(callNumber, callStartTime)
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> { // triggered when you answer or start a call

                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    onIncomingCallAnswered(callNumber, Date())
                } else {
                    isIncoming = false
                    callStartTime = Date()

                    onOutgoingCallStarted(callNumber, callStartTime)
                }
            }
            TelephonyManager.CALL_STATE_IDLE -> { // triggered when call is ended

                // Previous state is ringing means this call is either missed or rejected
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    onIncomingCallMissedOrRejected(callNumber, callStartTime, Date())
                } else if (isIncoming) {
                    onIncomingCallEnded(callNumber, callStartTime, Date())
                } else {
                    onOutgoingCallEnded(callNumber, callStartTime, Date())
                }
            }
        }

        lastState = state
    }

    open fun onIncomingCallReceived(number: String?, startTime: Date?) {
        Toast.makeText(context, "received", Toast.LENGTH_SHORT).show()
        startRecording()
    }

    open fun onIncomingCallAnswered(number: String?, answerTime: Date?) {
        this.answerTime = answerTime
    }

    open fun onIncomingCallEnded(number: String?, startTime: Date?, endTime: Date?) {
        stopRecording(startTime, endTime)
    }

    open fun onIncomingCallMissedOrRejected(number: String?, startTime: Date?, missOrRejectTime: Date?) {
        stopRecording(startTime, missOrRejectTime)
    }

    open fun onOutgoingCallStarted(number: String?, startTime: Date?) {
        startRecording()
        Toast.makeText(context, "out started", Toast.LENGTH_SHORT).show()
    }

    open fun onOutgoingCallEnded(number: String?, startTime: Date?, endTime: Date?) {
        stopRecording(startTime, endTime)
    }

    /**
     * Called when record started, you can override this to do your own work if you need to.
     * */
    open fun onRecordStarted() {
    }

    /**
     * Called when record finished.
     * */
    open fun onRecordFinished() {
    }

    private fun startRecording() {
        if (isRecordStarted) {
            mp3Recorder.stop()
            mp3Recorder.release()
            recordFile?.delete()

            isRecordStarted = false
        } else {
            try {
                mp3Recorder = Mp3Recorder()
                mp3Recorder.prepare(recordFile!!)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                Thread { mp3Recorder.start() }.start()
                isRecordStarted = true
                onRecordStarted()
            }
        }
    }

    private fun stopRecording(startTime: Date?, endTime: Date?) {
        if (isRecordStarted) {
            mp3Recorder.stop()
            mp3Recorder.release()

            isRecordStarted = false
            onRecordFinished()

            var fileName = PrefsHelper.readPrefString(
                context,
                Mp3CallRecorder.PREF_FILE_NAME
            )
            val dirPath = PrefsHelper.readPrefString(
                context,
                Mp3CallRecorder.PREF_FILE_PATH
            )
            val showSource = PrefsHelper.readPrefBool(
                context,
                Mp3CallRecorder.PREF_SHOW_SOURCE
            )
            val showNumber = PrefsHelper.readPrefBool(
                context,
                Mp3CallRecorder.PREF_SHOW_NUMBER
            )
            val showTime = PrefsHelper.readPrefBool(
                context,
                Mp3CallRecorder.PREF_SHOW_TIME
            )

            if (showSource) {
                fileName += if (isIncoming) "_incoming" else "_outgoing"
            }

            if (showNumber) {
                fileName += "_$callNumber"
            }

            if (showTime) {
                fileName += "_$startTime"
                fileName += if (answerTime != null) "_" + getDataTime("yyyyMMddHHmmss", answerTime!!) else ""
                fileName += "_$endTime"
            }

            fileName += ".mp3"

            val dest = File(dirPath, fileName)
            recordFile?.renameTo(dest)
            recordFile?.delete()
        }

        callNumber = null
        isIncoming = false
        callStartTime = null
        answerTime = null
    }

    private fun getDataTime(format: String, date: Date): String {
        val df = SimpleDateFormat(format, Locale.CHINA)
        return df.format(date)
    }

    companion object {

        private var lastState = TelephonyManager.CALL_STATE_IDLE

        const val ACTION_IN = "android.intent.action.PHONE_STATE"
        const val ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL"
        const val EXTRA_PHONE_NUMBER = "android.intent.extra.PHONE_NUMBER"
    }
}