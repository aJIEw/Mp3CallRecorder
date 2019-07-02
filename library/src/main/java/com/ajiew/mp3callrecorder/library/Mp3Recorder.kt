package com.ajiew.mp3callrecorder.library

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.NonNull
import com.ajiew.mp3callrecorder.library.helper.DebugLog
import net.junzz.lib.mp3lame.LameNative
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile

/**
 * Record phone call audios as mp3 format
 *
 * @author aJIEw
 */
internal class Mp3Recorder {

    private val AUDIO_SAMPLE_RATE = 32000

    @NonNull
    private val lame = LameNative()
    private var audioRecorder: AudioRecord? = null
    private var fileOutputStream: FileOutputStream? = null
    private var randomAccessFile: RandomAccessFile? = null


    @Throws(IOException::class)
    fun prepare(@NonNull file: File) {
        if (file.exists()) file.delete()
        // will create file if not exists
        randomAccessFile = RandomAccessFile(file, "rws")
        fileOutputStream = FileOutputStream(randomAccessFile!!.fd)

        val minBuffSize = AudioRecord.getMinBufferSize(
            AUDIO_SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioRecorder = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(AUDIO_SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build()
                )
                .setBufferSizeInBytes(2 * minBuffSize)
                .build()
        } else {
            audioRecorder = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                2 * minBuffSize)
        }

        // initiate Lame
        val initResult = lame.init(audioRecorder!!.sampleRate, audioRecorder!!.channelCount)
        if (initResult != 0) {
            throw ExceptionInInitializerError("Lame init error.")
        } else {
            DebugLog.d("Lame init success.")
        }
    }


    fun start() {
        val capacity = audioRecorder!!.sampleRate * audioRecorder!!.channelCount
        val recordBuffer = ShortArray(capacity)
        val lameBuffer = ByteArray(capacity)

        audioRecorder!!.startRecording()
        while (audioRecorder!!.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            val recorderSample = audioRecorder!!.read(recordBuffer, 0, capacity)
//            DebugLog.d(TAG, "CallRecorder sample size: $recorderSample")

            if (recorderSample > 0) {
                val lameSample = lame.buffer(recordBuffer, recordBuffer, recorderSample, lameBuffer)
//                DebugLog.d(TAG, "Lame sample size: $lameSample")

                if (lameSample > 0) {
                    try {
                        fileOutputStream!!.write(lameBuffer, 0, lameSample)
                    } catch (e: IOException) {
                        DebugLog.e("Lame audio stream write failure.", e)
                        audioRecorder!!.stop()
                    }

                }
            }
        }

        val flushSample = lame.flush(lameBuffer)
        DebugLog.d("Lame flush size: $flushSample")
        if (flushSample > 0) {
            try {
                fileOutputStream!!.write(lameBuffer, 0, flushSample)
            } catch (e: IOException) {
                DebugLog.e("Lame audio stream write failure.", e)
            }

        }

        try {
            fileOutputStream!!.flush()
        } catch (e: IOException) {
            DebugLog.e("Lame audio stream flush failure.", e)
        }

        try {
            fileOutputStream!!.close()
            fileOutputStream = null
        } catch (e: IOException) {
            DebugLog.e("Lame audio stream close failure.", e)
        }

        try {
            randomAccessFile!!.close()
            randomAccessFile = null
        } catch (e: IOException) {
            DebugLog.e("Audio 'RandomAccessFile' close failure.", e)
        }
    }


    fun stop() {
        if (audioRecorder!!.recordingState != AudioRecord.RECORDSTATE_STOPPED) {
            audioRecorder!!.stop()
        }
    }


    fun release() {
        audioRecorder!!.release()
        audioRecorder = null

        val initResult = lame.close()
        // return 0 if closed without any trouble
        if (initResult != 0) {
            DebugLog.e("Lame close result: $initResult", null)
        } else {
            DebugLog.d("Lame close result: $initResult")
        }
    }
}