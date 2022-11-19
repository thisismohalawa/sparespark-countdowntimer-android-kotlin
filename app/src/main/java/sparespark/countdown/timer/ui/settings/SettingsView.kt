package sparespark.countdown.timer.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import sparespark.countdown.timer.R

class SettingsView : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)

    }
}