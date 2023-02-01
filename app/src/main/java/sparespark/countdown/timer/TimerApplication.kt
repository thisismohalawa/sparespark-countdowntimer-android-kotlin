package sparespark.countdown.timer

import android.app.Application
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import sparespark.countdown.timer.data.alarm.AlarmSetter
import sparespark.countdown.timer.data.alarm.AlarmSetterImpl
import sparespark.countdown.timer.data.notification.NotificationHandler
import sparespark.countdown.timer.data.notification.NotificationHandlerImpl
import sparespark.countdown.timer.data.preference.PrefUtil
import sparespark.countdown.timer.data.preference.PrefUtilImpl
import sparespark.countdown.timer.ui.timer.viewmodel.TimerViewModelFactory

class TimerApplication : Application(), KodeinAware {

    override val kodein = Kodein.lazy {
        import(androidXModule(this@TimerApplication))

        // logic
        bind<PrefUtil>() with singleton { PrefUtilImpl() }
        bind<AlarmSetter>() with singleton { AlarmSetterImpl() }
        bind<NotificationHandler>() with singleton { NotificationHandlerImpl() }
        // viewModel
        bind() from provider {
            TimerViewModelFactory(
                instance(),
                instance(),
                instance(),
                instance()
            )
        }

    }
}