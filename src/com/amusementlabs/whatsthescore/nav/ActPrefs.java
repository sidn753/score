package com.amusementlabs.whatsthescore.nav;

import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.RingtonePreference;
import android.support.v4.app.NavUtils;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.amusementlabs.whatsthescore.R;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;

@EActivity
public class ActPrefs extends SherlockPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        updateSummaries();
    }

    @OptionsItem
    boolean homeSelected() {
        NavUtils.navigateUpFromSameTask(this);
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummaries();
    }

    @Override
    protected void onResume() {
        super.onResume();


        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);


        // A patch to overcome OnSharedPreferenceChange not being called by RingtonePreference bug
        RingtonePreference pref = (RingtonePreference) findPreference(getString(R.string.prefs_title_timer_ringtone));
        pref.setOnPreferenceChangeListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void updateSummaries() {

        //set input summary
        String key = getString(R.string.prefs_title_input);
        String value = getPreferenceScreen().getSharedPreferences().getString(key, "");
        Preference inputPref = findPreference(key);

        if (value.equals(getString(R.string.pref_input_keyboard))) {
            inputPref.setSummary(getString(R.string.pref_input_summ_keyboard));
        } else {
            inputPref.setSummary(getString(R.string.pref_input_summ_plusone));
        }

        //set sorting summary
        key = getString(R.string.prefs_title_sorting);
        value = getPreferenceScreen().getSharedPreferences().getString(key, "");
        findPreference(key).setSummary(getString(R.string.prefs_summ_sorting) + " " + value);


        //set ringtone summary (here to set summary on create, updates handles by onPrefereneChange)
        key = getString(R.string.prefs_title_timer_ringtone);
        value = getPreferenceScreen().getSharedPreferences().getString(key, "");
        updateRingtoneSummary((RingtonePreference) findPreference(key), Uri.parse(value));

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        //this method needed to to bug in android
        updateRingtoneSummary((RingtonePreference) preference, Uri.parse((String) newValue));
        return true;
    }

    private void updateRingtoneSummary(RingtonePreference preference, Uri ringtoneUri) {
        Ringtone ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
        if (ringtone != null && !ringtone.getTitle(this).equals("Unknown ringtone"))
            preference.setSummary(ringtone.getTitle(this));
        else
            preference.setSummary("Silent");
    }


}