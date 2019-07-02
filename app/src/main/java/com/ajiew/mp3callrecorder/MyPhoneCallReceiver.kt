package com.ajiew.mp3callrecorder

import com.ajiew.mp3callrecorder.library.PhoneCallReceiver
import com.ajiew.mp3callrecorder.library.helper.DebugLog
import java.util.*

class MyPhoneCallReceiver : PhoneCallReceiver() {

    override fun onIncomingCallReceived(number: String?, startTime: Date?) {
        super.onIncomingCallReceived(number, startTime)
        DebugLog.d("onIncomingCallReceived: ")
    }

    override fun onOutgoingCallStarted(number: String?, startTime: Date?) {
        super.onOutgoingCallStarted(number, startTime)
        DebugLog.d("onOutgoingCallStarted: ")
    }
}