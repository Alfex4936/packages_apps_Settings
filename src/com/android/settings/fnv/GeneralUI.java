
package com.android.settings.fnv;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class GeneralUI extends SettingsPreferenceFragment {

    private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String PREF_USE_ALT_RESOLVER = "use_alt_resolver";
    private static final String PREF_STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
    private static final String PREF_SHOW_OVERFLOW = "show_overflow";

    private Preference mCustomLabel;
    private CheckBoxPreference mUseAltResolver;
    private CheckBoxPreference mStatusBarNotifCount;
    private CheckBoxPreference mShowActionOverflow;

    String mCustomLabelText = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.general_ui_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        mCustomLabel = prefSet.findPreference(PREF_CUSTOM_CARRIER_LABEL);
        updateCustomLabelTextSummary();

        mUseAltResolver = (CheckBoxPreference) prefSet.findPreference(PREF_USE_ALT_RESOLVER);
        mUseAltResolver.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.ACTIVITY_RESOLVER_USE_ALT, false));

        mShowActionOverflow = (CheckBoxPreference) findPreference(PREF_SHOW_OVERFLOW);
        mShowActionOverflow.setChecked((Settings.System.getInt(getActivity().
                        getApplicationContext().getContentResolver(),
                        Settings.System.UI_FORCE_OVERFLOW_BUTTON, 0) == 1));

        mStatusBarNotifCount = (CheckBoxPreference) prefSet.findPreference(PREF_STATUS_BAR_NOTIF_COUNT);
        mStatusBarNotifCount.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(), 
                Settings.System.STATUS_BAR_NOTIF_COUNT, false));      

    }

    private void updateCustomLabelTextSummary() {
        mCustomLabelText = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.CUSTOM_CARRIER_LABEL);
        if (mCustomLabelText == null || mCustomLabelText.length() == 0) {
            mCustomLabel.setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomLabel.setSummary(mCustomLabelText);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mCustomLabel) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

            alert.setTitle(R.string.custom_carrier_label_title);
            alert.setMessage(R.string.custom_carrier_label_explain);

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(mCustomLabelText != null ? mCustomLabelText : "");
            alert.setView(input);

            alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = ((Spannable) input.getText()).toString();
                    Settings.System.putString(getActivity().getContentResolver(),
                            Settings.System.CUSTOM_CARRIER_LABEL, value);
                    updateCustomLabelTextSummary();
                    Intent i = new Intent();
                    i.setAction("com.aokp.romcontrol.LABEL_CHANGED");
                    mContext.sendBroadcast(i);
                }
            });
            alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
        } else if (preference == mUseAltResolver) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.ACTIVITY_RESOLVER_USE_ALT, checked ? true : false);
            return true;
        } else if (preference == mShowActionOverflow) {
            boolean enabled = mShowActionOverflow.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.UI_FORCE_OVERFLOW_BUTTON,
                    enabled ? 1 : 0);
            // Show toast appropriately
            if (enabled) {
                Toast.makeText(getActivity(), R.string.show_overflow_toast_enable,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), R.string.show_overflow_toast_disable,
                        Toast.LENGTH_LONG).show();
            }
            return true;
        } else if (preference == mStatusBarNotifCount) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_NOTIF_COUNT, checked ? true : false);
            return true;
        }
        
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}