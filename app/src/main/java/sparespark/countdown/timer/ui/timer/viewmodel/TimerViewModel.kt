package sparespark.countdown.timer.ui.timer.viewmodel

import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch
import sparespark.countdown.timer.core.BaseViewModel
import sparespark.countdown.timer.core.TimerState
import sparespark.countdown.timer.data.alarm.AlarmSetter
import sparespark.countdown.timer.data.notification.NotificationHandler
import sparespark.countdown.timer.data.preference.PrefUtil
import sparespark.countdown.timer.ui.timer.TimerViewEvent
import java.util.*
import kotlin.coroutines.CoroutineContext

val nowSeconds: Long
    get() = Calendar.getInstance().timeInMillis / 1000

class TimerViewModel(
    uiContext: CoroutineContext,
    private var prefUtil: PrefUtil,
    private var alarmSetter: AlarmSetter,
    private var notificationHandler: NotificationHandler
) : BaseViewModel<TimerViewEvent>(uiContext) {

    // logic
    private lateinit var timer: CountDownTimer
    private var timerState = TimerState.Stopped // initial

    // timer seconds
    private var timerLengthSeconds = 0L // long
    private var secondsRemaining = 0L

    // view states
    internal val progressMaxValue = MutableLiveData<Int>()
    internal val progressValue = MutableLiveData<Int>()
    internal val timerCountDownTextValue = MutableLiveData<String>()
    internal val timerStateTextValue = MutableLiveData<String>()

    //  button enabled
    internal val startButtonEnabledStatus = MutableLiveData<Boolean>()
    internal val stopButtonEnabledStatus = MutableLiveData<Boolean>()
    internal val pauseButtonEnabledStatus = MutableLiveData<Boolean>()


    override fun handleEvent(event: TimerViewEvent) {
        when (event) {
            // timer
            is TimerViewEvent.PausedTimer -> pauseTimerLogic()
            is TimerViewEvent.StartTimer -> event.context?.let { startTimerLogic(it) }
            is TimerViewEvent.CancelTimer -> event.context?.let { stopTimerLogic(it) }
            // view
            is TimerViewEvent.OnViewPaused -> event.context?.let { onViewPaused(it) }
            is TimerViewEvent.OnViewResumed -> event.context?.let { initTimerMainLogic(it) }
        }
    }

    private fun pauseTimerLogic() = launch {
        timer.cancel()
        timerState = TimerState.Paused
        updateButtonsState()
    }

    private fun startTimerLogic(context: Context) = launch {
        startTimer(context)
        timerState = TimerState.Running
        updateButtonsState()
    }

    private fun stopTimerLogic(context: Context) {
        if (this::timer.isInitialized) {
            timer.cancel()
            onTimerFinished(context)
        }
    }

    private fun onViewPaused(context: Context) {
        /*
        * Running...
        *
        * */
        if (timerState == TimerState.Running) {
            timer.cancel()
            val currentNowSeconds = nowSeconds
            val wakeUpTime = alarmSetter.setAlarm(context, currentNowSeconds, secondsRemaining)

            prefUtil.setAlarmSetTime(currentNowSeconds, context)
            if (prefUtil.isNotificationAllowed(context))
                notificationHandler.showTimerRunning(context, wakeUpTime)

            /*
            * Paused..
            * */
        } else if (timerState == TimerState.Paused) {
            if (prefUtil.isNotificationAllowed(context))
                notificationHandler.showTimerPaused(context)
        }

        /*
        *
        * Update preference
        * */
        prefUtil.apply {
            setPreviousTimerLengthSeconds(timerLengthSeconds, context)
            setSecondsRemaining(secondsRemaining, context)
            setTimerState(timerState, context)
        }

    }

    private fun initTimerMainLogic(context: Context) {
        initTimer(context)
        alarmSetter.removeAlarm(context)
        prefUtil.setAlarmSetTime(0, context)
        notificationHandler.hideTimerNotification(context)
    }

    private fun initTimer(context: Context) {
        timerState = prefUtil.getTimerState(context)
        /*
        * set timer length & second remaining
        *
        * */
        if (timerState == TimerState.Stopped)
            setNewTimerLength(context)
        else
            setPreviousTimerLength(context)

        secondsRemaining =
            if (timerState == TimerState.Running || timerState == TimerState.Paused)
                prefUtil.getSecondsRemaining(context)
            else
                timerLengthSeconds
        /*
        * set alarm time
        *
        * */
        val alarmSetTime = prefUtil.getAlarmSetTime(context)
        if (alarmSetTime > 0)
            secondsRemaining -= nowSeconds - alarmSetTime
        /*
        *
        * */
        if (secondsRemaining <= 0)
            onTimerFinished(context)
        else if (timerState == TimerState.Running)
            startTimer(context)
        /*
        * update UI
        * */
        updateButtonsState()
        updateCountdownValues()
    }

    /*
    *
    *
    *
    *
    *
    *
    *
    *
    *
    * */
    private fun startTimer(context: Context) {
        timerState = TimerState.Running
        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() = onTimerFinished(context)

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownValues()
            }
        }.start()
    }

    private fun onTimerFinished(context: Context) {
        timerState = TimerState.Stopped
        //set the length of the timer to be the one set in SettingsActivity
        //if the length was changed when the timer was running
        setNewTimerLength(context)
        progressValue.value = 0

        prefUtil.setSecondsRemaining(timerLengthSeconds, context)
        secondsRemaining = timerLengthSeconds

        updateButtonsState()
        updateCountdownValues()
    }


    private fun setNewTimerLength(context: Context) {
        val lengthInMinutes = prefUtil.getTimerLength(context)
        timerLengthSeconds = (lengthInMinutes * 60L)
        progressMaxValue.value = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength(context: Context) {
        timerLengthSeconds = prefUtil.getPreviousTimerLengthSeconds(context)
        progressMaxValue.value = timerLengthSeconds.toInt()
    }

    /*
    * Update UI
    *
    *
    * */
    private fun updateCountdownValues() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()

        timerCountDownTextValue.value =
            "$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr else "0$secondsStr"}"
        progressValue.value = (timerLengthSeconds - secondsRemaining).toInt()

    }

    private fun updateButtonsState() {
        when (timerState) {
            TimerState.Running -> {
                startButtonEnabledStatus.value = false
                pauseButtonEnabledStatus.value = true
                stopButtonEnabledStatus.value = true
                timerStateTextValue.value = "timer is running ..."
            }
            TimerState.Paused -> {
                startButtonEnabledStatus.value = true
                pauseButtonEnabledStatus.value = false
                stopButtonEnabledStatus.value = true
                timerStateTextValue.value = "timer is paused !"

            }
            TimerState.Stopped -> {
                startButtonEnabledStatus.value = true
                pauseButtonEnabledStatus.value = false
                stopButtonEnabledStatus.value = false
                timerStateTextValue.value = "timer is stopped ."

            }
        }
    }
}