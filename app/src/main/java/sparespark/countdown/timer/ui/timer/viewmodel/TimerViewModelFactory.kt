package sparespark.countdown.timer.ui.timer.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import sparespark.countdown.timer.data.alarm.AlarmSetter
import sparespark.countdown.timer.data.notification.NotificationHandler
import sparespark.countdown.timer.data.preference.PrefUtil

class TimerViewModelFactory(
    private var prefUtil: PrefUtil,
    private var alarmSetter: AlarmSetter,
    private var notificationHandler: NotificationHandler,
    private val app: Application
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TimerViewModel(
            Dispatchers.Main,
            prefUtil,
            alarmSetter,
            notificationHandler,
            app
        ) as T
    }
}