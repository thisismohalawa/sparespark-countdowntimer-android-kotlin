package sparespark.countdown.timer.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel<T>(
    private val uiContext: CoroutineContext
) : ViewModel(),
    CoroutineScope {

    abstract fun handleEvent(event: T)
    private var jobTracker: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = uiContext + jobTracker

    protected val errorState = MutableLiveData<String>()
    val error: LiveData<String> get() = errorState

}