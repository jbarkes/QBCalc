/*************************************************************************************************
 * Android QB Rating App
 * Jason Barkes - http://jbarkes.blogspot.com
 * 11/13/2013
 *************************************************************************************************/
package barkes.football.qbcalc;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {

    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    @SuppressWarnings("deprecation")
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();

        /** EULA handler */
        final Context context = this;
        Preference eula = (Preference) findPreference(getString(R.string.pref_key_eula));
        if (eula != null) {
            eula.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Dialog dlg = new Dialog(context);
                    dlg.setContentView(R.layout.frag_eula);
                    dlg.setCancelable(true);
                    dlg.setTitle("QB Calculator EULA");
                    dlg.setCanceledOnTouchOutside(true);
                    dlg.show();

                    return true;
                }
            });
        }
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        /**
         * In the simplified UI, fragments are not used at all and we instead use the older
         * PreferenceActivity APIs.
         */

        /** Add 'reports' preferences and corresponding header. */
        addPreferencesFromResource(R.xml.pref_reports);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_report_name)));

        /** Add 'recents' preferences. */
        addPreferencesFromResource(R.xml.pref_recents);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_recents_limit)));

        /** Add 'about' header and preferences. */
        addPreferencesFromResource(R.xml.pref_about);
        setVersionPreferenceSummary();
    }

    /** {@inheritDoc} */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
            & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
            || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
            || !isXLargeTablet(context);
    }

    /** {@inheritDoc} */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * A preference value change listener that updates the preference's summary to reflect its
     * new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            /** For list preferences, look up the correct display value in the preference's 'entries' list. */
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            /** Set the summary to reflect the new value. */
            preference.setSummary(
                index >= 0
                    ? listPreference.getEntries()[index]
                    : null);

        } else {
            /** For all others, set the summary to the value's simple string representation. */
            preference.setSummary(stringValue);
        }
        return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        /** Set the listener to watch for value changes. */
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        /** Trigger the listener immediately with the preference's current value. */
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @SuppressWarnings("deprecation")
    private void setVersionPreferenceSummary() {
        String version = "";
        EditTextPreference preference = (EditTextPreference)
                findPreference(getString(R.string.pref_key_version));

        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // Do nothing
        }

        preference.setSummary(version);
    }

    /**
     * This fragment shows reports preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ReportsPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_reports);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_report_name)));
        }
    }

    /**
     * This fragment shows recent QB rating preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class RecentsPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_recents);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_recents_limit)));
        }
    }

    /**
     * This fragment shows the 'About' preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AboutPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_about);

            /** EULA handler */
            final Context context = getActivity();
            Preference eula = (Preference) findPreference(getString(R.string.pref_key_eula));
            if (eula != null) {
                eula.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Dialog dlg = new Dialog(context);
                        dlg.setContentView(R.layout.frag_eula);
                        dlg.setCancelable(true);
                        dlg.setTitle("QB Calculator EULA");
                        dlg.setCanceledOnTouchOutside(true);
                        dlg.show();

                        return true;
                    }
                });
            }

            //setVersionPreferenceSummary();
            String version = "";
            EditTextPreference preference = (EditTextPreference)
                    findPreference(getString(R.string.pref_key_version));

            try {
                version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                // Do nothing
            }

            preference.setSummary(version);
        }
    }

}
