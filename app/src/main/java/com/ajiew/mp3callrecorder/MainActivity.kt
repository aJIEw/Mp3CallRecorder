package com.ajiew.mp3callrecorder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_PHONE_NUMBER
import android.content.IntentFilter
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ajiew.mp3callrecorder.library.PhoneCallReceiver.Companion.ACTION_IN
import com.ajiew.mp3callrecorder.library.PhoneCallReceiver.Companion.ACTION_OUT

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val mp3CallRecorder = Mp3CallRecorder.Builder(this)
//            .setRecordFileName("test")
//            .build()
//
//        mp3CallRecorder.changeReceiver(MyPhoneCallReceiver())
//        mp3CallRecorder.startPhoneCallReceiver()

        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_IN)
        intentFilter.addAction(ACTION_OUT)
        intentFilter.priority = 100

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                var callNumber: String? = null
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
                    Log.d(javaClass.canonicalName, "onReceive: $state")
                }
            }
        }, intentFilter)
    }

}
