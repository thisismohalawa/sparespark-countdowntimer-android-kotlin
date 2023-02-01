package sparespark.countdown.timer.ui.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.timer_view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import sparespark.countdown.timer.R
import sparespark.countdown.timer.core.makeToast
import sparespark.countdown.timer.ui.timer.viewmodel.TimerViewModel
import sparespark.countdown.timer.ui.timer.viewmodel.TimerViewModelFactory

class TimerView : Fragment(), View.OnClickListener, KodeinAware {
    // inject
    override val kodein by closestKodein()

    // viewModel
    private lateinit var viewModel: TimerViewModel
    private val viewModelFactory: TimerViewModelFactory by instance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.timer_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[TimerViewModel::class.java]
        /*
        *
        * UI
        * */
        viewmodelObserver()
        fab_start.setOnClickListener(this)
        fab_pause.setOnClickListener(this)
        fab_stop.setOnClickListener(this)

    }

    private fun viewmodelObserver() {
        with(viewModel) {
            /*
            * view state
            *
            * */
            error.observe(viewLifecycleOwner) {
                if (it.isNotBlank()) makeToast(it)
            }
            progressValue.observe(viewLifecycleOwner) {
                if (it != null)
                    progress_countdown.progress = it
            }
            progressMaxValue.observe(viewLifecycleOwner) {
                if (it != null)
                    progress_countdown.max = it
            }
            timerCountDownTextValue.observe(viewLifecycleOwner) {
                if (it != null) txt_countdown.text = it
            }
            timerStateTextValue.observe(viewLifecycleOwner) {
                if (it.isNotBlank()) txt_state.text = it
            }
            /*
            * button enabled
            *
            * */
            stopButtonEnabledStatus.observe(viewLifecycleOwner) {
                fab_stop.isEnabled = it
            }
            startButtonEnabledStatus.observe(viewLifecycleOwner) {
                fab_start.isEnabled = it
            }
            pauseButtonEnabledStatus.observe(viewLifecycleOwner) {
                fab_pause.isEnabled = it
            }
            /*
            * app update
            * */
            handleEvent(TimerViewEvent.CheckForAppUpdates)
        }
    }

    override fun onClick(view: View?) {
        if (view != null)
            when (view.id) {
                R.id.fab_pause ->
                    viewModel.handleEvent(TimerViewEvent.PausedTimer)
                R.id.fab_start ->
                    viewModel.handleEvent(TimerViewEvent.StartTimer(context))
                R.id.fab_stop ->
                    viewModel.handleEvent(TimerViewEvent.CancelTimer(context))
            }
    }

    override fun onResume() {
        super.onResume()
        /*
        *  init timer...
        *  remove background timer, hide notification...
        *
        * */
        viewModel.apply {
            handleEvent(TimerViewEvent.OnViewResumed(context))
            handleEvent(TimerViewEvent.CheckIfAppUpdatesInProgress)
        }
    }

    override fun onPause() {
        super.onPause()
        /*
        * is called just before activity goes in background.
        *
        * if timer is running then cancel it, then start background timer
        * and show notification...
        *
        * if timer is paused them show notification only...
        *
        *
        * */
        viewModel.handleEvent(TimerViewEvent.OnViewPaused(context))
    }
}