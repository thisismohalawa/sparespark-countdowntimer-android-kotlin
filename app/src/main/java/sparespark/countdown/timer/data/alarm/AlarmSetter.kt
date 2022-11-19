package sparespark.countdown.timer.data.alarm

import android.content.Context

interface AlarmSetter {

    fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long

    fun removeAlarm(context: Context)

}