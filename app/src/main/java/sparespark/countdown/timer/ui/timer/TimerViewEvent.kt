package sparespark.countdown.timer.ui.timer

import android.content.Context

sealed class TimerViewEvent {

    /*
    * Timer..
    * */
    object PausedTimer : TimerViewEvent()
    data class StartTimer(val context: Context?) : TimerViewEvent()
    data class CancelTimer(val context: Context?) : TimerViewEvent()
    /*
    * View..
    * */
    data class OnViewResumed(val context: Context?) : TimerViewEvent()
    data class OnViewPaused(val context: Context?) : TimerViewEvent()
}
