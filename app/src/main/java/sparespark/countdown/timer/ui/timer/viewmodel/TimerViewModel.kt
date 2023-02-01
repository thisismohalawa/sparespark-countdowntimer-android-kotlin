package sparespark.countdown.timer.ui.timer.viewmodel

import android.app.Activity
import android.app.Application
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.launch
import sparespark.countdown.timer.core.*
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
    private var notificationHandler: NotificationHandler,
    private val app: Application,
) : BaseViewModel<TimerViewEvent>(uiContext, app) {

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


    // check for updates
    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(app) }

    override fun handleEvent(event: TimerViewEvent) {
        when (event) {
            // timer
            is TimerViewEvent.PausedTimer -> pauseTimerLogic()
            is TimerViewEvent.StartTimer -> startTimerLogic()
            is TimerViewEvent.CancelTimer -> stopTimerLogic()
            // view
            is TimerViewEvent.OnViewPaused -> onViewPaused()
            is TimerViewEvent.OnViewResumed -> initTimerMainLogic()
            // update
            is TimerViewEvent.CheckForAppUpdates -> checkForAppUpdates()
            is TimerViewEvent.CheckIfAppUpdatesInProgress -> checkIfAppUpdatesInProgress()
        }
    }

    private fun pauseTimerLogic() = launch {
        timer.cancel()
        timerState = TimerState.Paused
        updateButtonsState()
    }

    private fun startTimerLogic() = launch {
        startTimer()
        timerState = TimerState.Running
        updateButtonsState()
    }

    private fun stopTimerLogic() {
        if (this::timer.isInitialized) {
            timer.cancel()
            onTimerFinished()
        }
    }

    private fun onViewPaused() {
        /*
        * Running...
        *
        * */
        if (timerState == TimerState.Running) {
            timer.cancel()
            val currentNowSeconds = nowSeconds
            val wakeUpTime = alarmSetter.setAlarm(app, currentNowSeconds, secondsRemaining)

            prefUtil.setAlarmSetTime(currentNowSeconds, app)
            if (prefUtil.isNotificationAllowed(app))
                notificationHandler.showTimerRunning(app, wakeUpTime)

            /*
            * Paused..
            * */
        } else if (timerState == TimerState.Paused) {
            if (prefUtil.isNotificationAllowed(app))
                notificationHandler.showTimerPaused(app)
        }

        /*
        *
        * Update preference
        * */
        prefUtil.apply {
            setPreviousTimerLengthSeconds(timerLengthSeconds, app)
            setSecondsRemaining(secondsRemaining, app)
            setTimerState(timerState, app)
        }

    }

    private fun initTimerMainLogic() {
        initTimer()
        alarmSetter.removeAlarm(app)
        prefUtil.setAlarmSetTime(0, app)
        notificationHandler.hideTimerNotification(app)
    }

    private fun initTimer() {
        timerState = prefUtil.getTimerState(app)
        /*
        * set timer length & second remaining
        *
        * */
        if (timerState == TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()

        secondsRemaining =
            if (timerState == TimerState.Running || timerState == TimerState.Paused)
                prefUtil.getSecondsRemaining(app)
            else
                timerLengthSeconds
        /*
        * set alarm time
        *
        * */
        val alarmSetTime = prefUtil.getAlarmSetTime(app)
        if (alarmSetTime > 0)
            secondsRemaining -= nowSeconds - alarmSetTime
        /*
        *
        * */
        if (secondsRemaining <= 0)
            onTimerFinished()
        else if (timerState == TimerState.Running)
            startTimer()
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
    private fun startTimer() {
        timerState = TimerState.Running
        try {
            timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
                override fun onFinish() = onTimerFinished()

                override fun onTick(millisUntilFinished: Long) {
                    secondsRemaining = millisUntilFinished / 1000
                    updateCountdownValues()
                }
            }.start()

        } catch (ex: Exception) {
            errorState.value = "Error ${ex.message}, Try Again.."
        }
    }

    private fun onTimerFinished() {
        timerState = TimerState.Stopped
        //set the length of the timer to be the one set in SettingsActivity
        //if the length was changed when the timer was running
        setNewTimerLength()
        progressValue.value = 0

        prefUtil.setSecondsRemaining(timerLengthSeconds, app)
        secondsRemaining = timerLengthSeconds

        updateButtonsState()
        updateCountdownValues()
    }


    private fun setNewTimerLength() {
        val lengthInMinutes = prefUtil.getTimerLength(app)
        timerLengthSeconds = (lengthInMinutes * 60L)
        progressMaxValue.value = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength() {
        timerLengthSeconds = prefUtil.getPreviousTimerLengthSeconds(app)
        progressMaxValue.value = timerLengthSeconds.toInt()
    }

    private fun checkIfAppUpdatesInProgress() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        it,
                        AppUpdateType.IMMEDIATE,
                        app.activity() as Activity,
                        UPDATE_REQUEST_CODE
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            } else
                Log.d(DEBUG_TAG, "checkIfUpdateInProgress...false. ")
        }
    }

    private fun checkForAppUpdates() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        it,
                        AppUpdateType.IMMEDIATE,
                        app.activity() as Activity,
                        UPDATE_REQUEST_CODE
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

            } else Log.d(DEBUG_TAG, "checkForAppUpdates...latest version. ")
        }.addOnFailureListener {
            Log.d(
                DEBUG_TAG, "App update exception : ${it.message} \n" +
                        "cause : ${it.cause.toString()} "
            )
        }
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