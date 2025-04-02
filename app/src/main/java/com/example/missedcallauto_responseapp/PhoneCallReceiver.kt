package com.example.missedcallauto_responseapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log

class PhoneCallReceiver : BroadcastReceiver() {

    private var ringingNumber: String? = null
    private var isRinging = false
    private val TAG = "PhoneCallReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            Log.d(TAG, "Phone State: $state, Number: $number")

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    // Phone is ringing, save the number
                    if (!number.isNullOrEmpty()) {
                        ringingNumber = number
                        isRinging = true
                        Log.d(TAG, "Incoming call from: $ringingNumber")
                    }
                }

                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // Call ended
                    if (isRinging) {
                        // This was a missed call (went from RINGING to IDLE without OFFHOOK)
                        ringingNumber?.let { phoneNumber ->
                            Log.d(TAG, "Missed call detected from: $phoneNumber")
                            sendSMS(context, phoneNumber)
                        }
                    }
                    isRinging = false
                    ringingNumber = null
                }

                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // Call was answered
                    isRinging = false
                    ringingNumber = null
                }
            }
        }
    }

    private fun sendSMS(context: Context, phoneNumber: String) {
        try {
            val smsManager = SmsManager.getDefault()
            val message = "Xin lỗi, tôi đang bận và không thể trả lời cuộc gọi của bạn. Tôi sẽ gọi lại sau."

            Log.d(TAG, "Sending SMS to: $phoneNumber")
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d(TAG, "SMS sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS", e)
        }
    }
}