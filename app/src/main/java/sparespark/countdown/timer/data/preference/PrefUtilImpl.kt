package sparespark.countdown.timer.data.preference

import android.content.Context
import android.preference.PreferenceManager
import sparespark.countdown.timer.core.TimerState


private const val TIMER_LENGTH_ID = "timer_length"
private const val PREVIOUS_TIMER_LENGTH_SECONDS_ID = "previous_timer_length_seconds"
private const val TIMER_STATE_ID = "timer_state"
private const val SECONDS_REMAINING_ID = "seconds_remaining"
private const val ALARM_SET_TIME_ID = "backgrounded_time"
private const val NOTIFICATION_ALLOWANCE = "notification_allowance"


class PrefUtilImpl : PrefUtil {

    /*
    * Timer length
    *
    * */
    override fun getTimerLength(context: Context): Int {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getInt(TIMER_LENGTH_ID, 10)
    }

    override fun getPreviousTimerLengthSeconds(context: Context): Long {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, 0)
    }

    override fun setPreviousTimerLengthSeconds(seconds: Long, context: Context) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, seconds)
        editor.apply()
    }

    /*
    * timer current state
    *
    * */
    override fun getTimerState(context: Context): TimerState {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val ordinal = preferences.getInt(TIMER_STATE_ID, 0)
        return TimerState.values()[ordinal]
    }

    override fun setTimerState(state: TimerState, context: Context) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        val ordinal = state.ordinal
        editor.putInt(TIMER_STATE_ID, ordinal)
        editor.apply()
    }

    /*
    *
    * Second remaining..
    * */
    override fun getSecondsRemaining(context: Context): Long {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getLong(SECONDS_REMAINING_ID, 0)
    }

    override fun setSecondsRemaining(seconds: Long, context: Context) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putLong(SECONDS_REMAINING_ID, seconds)
        editor.apply()
    }

    /*
    * alarm time
    *
    * */
    override fun getAlarmSetTime(context: Context): Long {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return  preferences.getLong(ALARM_SET_TIME_ID, 0)
    }

    override fun setAlarmSetTime(time: Long, context: Context) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putLong(ALARM_SET_TIME_ID, time)
        editor.apply()
    }

    /*
    * notify
    * */
    override fun isNotificationAllowed(context: Context): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean(NOTIFICATION_ALLOWANCE, true)
    }
}