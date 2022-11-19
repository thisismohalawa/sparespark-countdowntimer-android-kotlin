package sparespark.countdown.timer.data.preference

import android.content.Context
import sparespark.countdown.timer.core.TimerState


interface PrefUtil {

    // Timer length
    fun getTimerLength(context: Context): Int
    fun getPreviousTimerLengthSeconds(context: Context): Long
    fun setPreviousTimerLengthSeconds(seconds: Long, context: Context)

    // timerState
    fun getTimerState(context: Context): TimerState
    fun setTimerState(state: TimerState, context: Context)

    // SecondsRemaining
    fun getSecondsRemaining(context: Context): Long
    fun setSecondsRemaining(seconds: Long, context: Context)

    // alarm
    fun getAlarmSetTime(context: Context): Long
    fun setAlarmSetTime(time: Long, context: Context)

    // notify
    fun isNotificationAllowed(context: Context): Boolean

}