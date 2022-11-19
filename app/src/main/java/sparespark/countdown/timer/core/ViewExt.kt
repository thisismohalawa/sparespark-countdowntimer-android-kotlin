package sparespark.countdown.timer.core

import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun View.enable(enabled: Boolean) {
    isEnabled = enabled
    alpha = if (enabled) 1f else 0.5f
}

fun View.preventDoubleClick() {
    this.isEnabled = false
    this.postDelayed({ this.isEnabled = true }, 1000)
}

internal fun Fragment.makeToast(value: String) {
    Toast.makeText(activity, value, Toast.LENGTH_SHORT).show()
}
