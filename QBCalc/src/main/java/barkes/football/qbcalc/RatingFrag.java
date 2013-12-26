/*************************************************************************************************
 * Android QB Rating App
 * Jason Barkes - http://jbarkes.blogspot.com
 * 11/13/2013
 *************************************************************************************************/
package barkes.football.qbcalc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import barkes.football.qbcalc.adapters.FilteredArrayAdapter;
import barkes.football.qbcalc.adapters.LeagueAdapter;
import barkes.football.qbrating.NCAAQBRating;
import barkes.football.qbrating.NFLQBRating;
import barkes.football.qbrating.QBRatingBase;
import barkes.football.qbrating.QBRatingList;
import barkes.util.SmartLog;
import barkes.util.StaticStrings;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

/**
 * QB Rating base class for calculating a QB's passing efficiency rating.
 */
public class RatingFrag {
    public static class QBFragImpl extends Fragment {

        /** Callbacks */
        private OnRatingCalculatedListener mCalcCallback;

        /** Controls */
        private View mLayoutRootView = null;
        private Spinner mLeagueSpinner = null;
        private TextView mCompletionPercentageTextView = null;
        private TextView mYardsPerAttemptTextView = null;
        private TextView mInterceptionPercentageTextView = null;
        private TextView mTouchdownPercentageTextView = null;
        private EditText mInterceptionsEditText = null;
        private EditText mTouchdownsEditText = null;
        private EditText mYardsEditText = null;
        private EditText mAttemptsEditText = null;
        private EditText mCompletionsEditText = null;
        private EditText mRatingEditText = null;
        private AutoCompleteTextView mTeamTextView = null;
        private AutoCompleteTextView mPlayerTextView = null;
        private ImageView mRatingInfoImage = null;
        private Button mCalcButton = null;
        private Button mClearButton = null;
        private TableRow mButtonsRow = null;

        /** Adapters */
        private FilteredArrayAdapter mTeamsAdapter = null;
        private FilteredArrayAdapter mPlayersAdapter = null;
        private ArrayList<StatTextWatcher> mEditWatchers = null;
        private QBRatingBase mQB = null;
        private QBRatingList mQBList = new QBRatingList();

        /** Constants */
        public static final int MAX_MRU = 20;
        public static final int DEFAULT_LIST_LIMIT = 20;
        private int mListLimit = DEFAULT_LIST_LIMIT;

        public static QBFragImpl newInstance() {
            QBFragImpl fragment = new QBFragImpl();
            return fragment;
        }

        /**
         * Container Activity must implement the QB Rating calculator interface.
         */
        public interface OnRatingCalculatedListener {
            public void onRatingCalculated(QBRatingBase qbRating);
        }

        /**
         * Internal class to handle required EditText controls.
         */
        private static class StatTextWatcher implements TextWatcher {
            private final EditText mEditSrc;
            private final String mErrMsg;
            private boolean mClearFlag;

            public StatTextWatcher(EditText editSource, String errMsg) {
                this.mEditSrc = editSource;
                mClearFlag = false;
                mErrMsg = errMsg;
            }

            public void setClear(boolean clearFlag) {
                this.mClearFlag = clearFlag;
            }

            public  boolean getClear() {
                return mClearFlag;
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if( !mClearFlag && charSequence.toString().length() == 0 ) {
                    String msg = mErrMsg + " is required";
                    mEditSrc.setError(msg);
                }
            }
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

            /** Make sure the container Activity has implemented the callback interface. */
            try {
                mCalcCallback = (OnRatingCalculatedListener) activity;
            }
            catch (ClassCastException e) {
                if (BuildConfig.DEBUG) {
                    throw new ClassCastException(activity.toString()
                        + " must implement OnRatingCalculatedListener");
                }
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
            mEditWatchers = new ArrayList<StatTextWatcher>();

            String currentListJson = getRecentsJsonPreference(getString(R.string.pref_key_recents_json));
            Gson gson = new Gson();
            mQBList = gson.fromJson(currentListJson, QBRatingList.class);
            if(mQBList == null) {
                mQBList = new QBRatingList();
            }

            mListLimit = getListLimitPref();
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            mLayoutRootView = inflater.inflate(R.layout.frag_rating, container, false);
            mLeagueSpinner = (Spinner) mLayoutRootView.findViewById(R.id.spinner_league);
            mCompletionPercentageTextView = (TextView) mLayoutRootView.findViewById(R.id.text_completion_percentage);
            mYardsPerAttemptTextView = (TextView) mLayoutRootView.findViewById(R.id.text_yards_per_attempt);
            mInterceptionPercentageTextView = (TextView) mLayoutRootView.findViewById(R.id.text_interception_percentage);
            mTouchdownPercentageTextView = (TextView) mLayoutRootView.findViewById(R.id.text_td_percentage);
            mInterceptionsEditText = (EditText) mLayoutRootView.findViewById(R.id.edit_ints);
            mTouchdownsEditText = (EditText) mLayoutRootView.findViewById(R.id.edit_tds);
            mYardsEditText = (EditText) mLayoutRootView.findViewById(R.id.edit_yards);
            mAttemptsEditText = (EditText) mLayoutRootView.findViewById(R.id.edit_attempts);
            mCompletionsEditText = (EditText) mLayoutRootView.findViewById(R.id.edit_completions);
            mRatingEditText = (EditText) mLayoutRootView.findViewById(R.id.edit_rating);
            mTeamTextView = (AutoCompleteTextView) mLayoutRootView.findViewById(R.id.atv_team);
            mPlayerTextView = (AutoCompleteTextView) mLayoutRootView.findViewById(R.id.atv_player);
            mRatingInfoImage = (ImageView) mLayoutRootView.findViewById(R.id.iv_rating_info);
            mCalcButton = (Button) mLayoutRootView.findViewById(R.id.btn_calc);
            mClearButton = (Button) mLayoutRootView.findViewById(R.id.btn_clear);
            mButtonsRow = (TableRow) mLayoutRootView.findViewById(R.id.row_buttons);

            // Show the calc button for Android versions older than Honeycomb (API level 11)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                mButtonsRow.setVisibility(View.VISIBLE);
                mCalcButton.setVisibility(View.VISIBLE);
                mClearButton.setVisibility(View.VISIBLE);

                mCalcButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListLimit = getListLimitPref();
                        calcRating(true);
                    }
                });

                mClearButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        resetControls();
                    }
                });
            }

            /** Hide the info icon until a rating is calculated */
            mRatingInfoImage.setVisibility(View.INVISIBLE);

            createTextWatchers();
            //makeHintsTransparent();
            createEditListeners(inflater);
            bindTeamsAdapter();
            bindPlayersAdapter();
            setupImeOptions();

            /** Bind the league spinner to the custom array adapter */
            mLeagueSpinner.setAdapter(new LeagueAdapter(getActivity(),
                    R.layout.league_spinner_item, QBRatingBase.LEAGUES));

            /** Calc rating when spinner selection is changed */
            mLeagueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (mQB != null) {
                        mListLimit = getListLimitPref();
                        calcRating(true);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

            /** Show the info icon, when appropriate */
            mRatingInfoImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mQB == null) {
                        return;
                    }

                    double perfect = QBRatingBase.getPerfectRating(mQB.getLeague());
                    String msg = "A perfect " + mQB.getLeague().name() + " rating is " + String.valueOf(perfect);
                    Toast toast = Toast.makeText(getActivity(), msg, LENGTH_LONG);
                    toast.show();
                }
            });

            /** Set focus to the attempts control */
            mCompletionsEditText.requestFocus();

            return mLayoutRootView;
        }

        /**
         * Create the action menu items.
         */
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            /** Reset menu item */
            MenuItem menuReset = menu.add(Menu.NONE, R.id.action_reset, Menu.NONE,
                    R.string.action_reset);
            {
                menuReset.setAlphabeticShortcut('r');
                menuReset.setIcon(R.drawable.ic_refresh);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    menuReset.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }

            /** Calculate menu item */
            MenuItem menuCalc = menu.add(Menu.NONE, R.id.action_calculate, Menu.NONE,
                    R.string.action_calculate);
            {
                menuCalc.setAlphabeticShortcut('c');
                menuCalc.setIcon(R.drawable.ic_calculator);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    menuCalc.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }

            /** Share menu item */
            MenuItem menuEmail = menu.add(Menu.NONE, R.id.action_email, Menu.NONE,
                    R.string.action_email);
            {
                menuEmail.setAlphabeticShortcut('e');
                menuEmail.setIcon(R.drawable.ic_email);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    menuEmail.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }

        /**
         * Action menu event handler.
         */
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_calculate:
                    mListLimit = getListLimitPref();
                    calcRating(true);
                    return true;
                case R.id.action_reset:
                    resetControls();
                    return true;
                case R.id.action_email:
                    sendReport();
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void bindTeamsAdapter() {
            try {
                String[] teams = getArrayPreference(getString(R.string.pref_key_recent_teams), getString(R.string.key_teams_json));
                mTeamsAdapter = new FilteredArrayAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, teams);
                mTeamTextView.setThreshold(1);
                mTeamTextView.setAdapter(mTeamsAdapter);
            } catch (NullPointerException e) {
                if (BuildConfig.DEBUG) {
                    SmartLog.w(StaticStrings.LOG_TAG, "Missing teams JSON preference: " + e.getMessage());
                    SmartLog.d(StaticStrings.LOG_TAG, Log.getStackTraceString(e));
                }
            }
        }

        private void bindPlayersAdapter() {
            try {
                String[] players = getArrayPreference(getString(R.string.pref_key_recent_players), getString(R.string.key_players_json));
                mPlayersAdapter = new FilteredArrayAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, players);
                mPlayerTextView.setThreshold(1);
                mPlayerTextView.setAdapter(mPlayersAdapter);
            } catch (NullPointerException e) {
                if (BuildConfig.DEBUG) {
                    SmartLog.w(StaticStrings.LOG_TAG, "Missing players JSON preference: " + e.getMessage());
                    SmartLog.d(StaticStrings.LOG_TAG, Log.getStackTraceString(e));
                }
            }
        }

        private void saveTeams() {
            String team = mTeamTextView.getText().toString();
            String[] teams;
            ArrayList<String> teamsArrayList = new ArrayList<String>();

            if (mTeamsAdapter != null) {
                for (int i = 0; i < mTeamsAdapter.getCount(); i++) {
                    String t = mTeamsAdapter.getItem(i);
                    if (!t.contentEquals(team)) {
                        teamsArrayList.add(mTeamsAdapter.getItem(i));
                    }
                }
            }

            teamsArrayList.add(0, team);

            /** Limit recents teams */
            if (teamsArrayList.size() > MAX_MRU) {
                teamsArrayList.remove(teamsArrayList.size());
            }

            teams = teamsArrayList.toArray(new String[0]);
            setArrayPreference(getString(R.string.pref_key_recent_teams), getString(R.string.key_teams_json), teams);
            bindTeamsAdapter();
        }

        private void savePlayers() {
            String player = mPlayerTextView.getText().toString();
            String[] players;
            ArrayList<String> playersArrayList = new ArrayList<String>();

            if (mPlayersAdapter != null) {
                for (int i = 0; i < mPlayersAdapter.getCount(); i++) {
                    String t = mPlayersAdapter.getItem(i);
                    if (!t.contentEquals(player)) {
                        playersArrayList.add(mPlayersAdapter.getItem(i));
                    }
                }
            }

            playersArrayList.add(0, player);

            /** Limit recent players */
            if (playersArrayList.size() > MAX_MRU) {
                playersArrayList.remove(playersArrayList.size());
            }

            players = playersArrayList.toArray(new String[0]);
            setArrayPreference(getString(R.string.pref_key_recent_players), getString(R.string.key_players_json), players);
            bindPlayersAdapter();
        }

        private void setArrayPreference(String name, String key, String[] values) {
            Gson gson = new Gson();
            String json = gson.toJson(values);
            setPreference(name, key, json);
        }

        private void setPreference(String name, String key, String value) {
            SharedPreferences settings = getActivity().getSharedPreferences(name, 0);
            SharedPreferences.Editor e = settings.edit();
            e.putString(key, value);
            e.commit();
        }

        private String[] getArrayPreference(String name, String key) {
            String[] prefArray;
            String json = getPreference(name, key, "");

            Gson gson = new Gson();
            prefArray = gson.fromJson(json, String[].class);

            return prefArray;
        }

        private String getPreference(String name, String key, String defaultValue) {
            SharedPreferences settings = getActivity().getSharedPreferences(name, 0);
            String value = settings.getString(key, defaultValue);

            return value;
        }

        /**
         * Set text hint color transparent, since we get the validator text from the hint, but the
         * EditTexts are too short to display the hints properly.
         */
        /*private void makeHintsTransparent() {
            Iterator itr = mEditWatchers.iterator();
            while(itr.hasNext()) {
                StatTextWatcher stw = (StatTextWatcher) itr.next();
                stw.mEditSrc.setHintTextColor(getResources().getColor(R.color.transparent));
            }

            mRatingEditText.setHintTextColor(getResources().getColor(R.color.transparent));
        }*/

        /**
         * Clears the results controls and rating result object.
         */
        private void clearResults() {
            mCompletionPercentageTextView.setText("");
            mYardsPerAttemptTextView.setText("");
            mInterceptionPercentageTextView.setText("");
            mTouchdownPercentageTextView.setText("");
            mRatingEditText.setText("");
            mRatingInfoImage.setVisibility(View.INVISIBLE);

            mQB = null;
        }

        /**
         * Create the EditText IME options and navigation.
         */
        private void setupImeOptions() {
            mCompletionsEditText.setNextFocusDownId(R.id.edit_attempts);
            mAttemptsEditText.setNextFocusDownId(R.id.edit_yards);
            mYardsEditText.setNextFocusDownId(R.id.edit_tds);
            mTouchdownsEditText.setNextFocusDownId(R.id.edit_ints);

            mCompletionsEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            mAttemptsEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            mYardsEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            mTouchdownsEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);

            mInterceptionsEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }

        /**
         * This method is used to create the onFocusChangeListeners on the various stat EditTexts.
         */
        private void createEditListeners(final LayoutInflater inflater) {
            ArrayList<EditText> controls = new ArrayList<EditText>();
            controls.add(mCompletionsEditText);
            controls.add(mAttemptsEditText);
            controls.add(mYardsEditText);
            controls.add(mTouchdownsEditText);
            controls.add(mInterceptionsEditText);

            Iterator itr = controls.iterator();
            while (itr.hasNext()) {
                final EditText ctl = (EditText) itr.next();
                ctl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            InputMethodManager imm = (InputMethodManager) inflater
                                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                        }
                    }
                });

                ctl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InputMethodManager imm = (InputMethodManager) inflater
                            .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                    }
                });

                ctl.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
                    @Override public void afterTextChanged(Editable editable) { }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                        clearResults();
                    }
                });
            }

            /** Hide the IME when the rating is selected */
            mRatingEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mRatingEditText.getWindowToken(), 0);
                }
            });

            /** Hide the IME when the rating receives focus */
            mRatingEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mRatingEditText.getWindowToken(), 0);
                    }
                }
            });
        }

        /**
         * Method to create a QB rating report and launch available intents.
         */
        private void sendReport() {
            if(mQB == null) {
                Toast toast = makeText(getActivity(), StaticStrings.ERR_CALC_REQUIRED, LENGTH_LONG);
                toast.show();
                return;
            }

            /** Get reports-related preferences */
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            String reportTitle = prefs.getString(getString(R.string.pref_key_report_name), getString(R.string.pref_default_report_name));

            boolean includeTeam = prefs.getBoolean(getString(R.string.pref_key_include_team), false);
            String teamName = mQB.team;

            boolean includePlayer = prefs.getBoolean(getString(R.string.pref_key_include_player), false);
            String playerName = mQB.player;

            boolean includeDate = prefs.getBoolean(getString(R.string.pref_key_include_date), false);

            String completions = String.valueOf(mQB.completions);
            String attempts = String.valueOf(mQB.attempts);
            String yards = String.valueOf(mQB.yards);
            String tds = String.valueOf(mQB.tds);
            String interceptions = String.valueOf(mQB.interceptions);
            String cp = String.valueOf(mQB.getCompletionPercentage());
            String ya = String.valueOf(mQB.getYardsPerAttempt());
            String tp = String.valueOf(mQB.getTouchdownPercentage());
            String ip = String.valueOf(mQB.getInterceptionPercentage());
            String rating = String.valueOf(mQB.getRating());
            String formula = mQB.getDispName();

            String date = new SimpleDateFormat(StaticStrings.FMT_SHORT_DT).format(new Date());
            String subject = "QB Rating Report (" + date + ")"; // TODO: get report title from shared pref
            String appUrl = getString(R.string.app_url);

            String htmlBody =
                "<p><b>" + reportTitle + "</b></p>" +
                "<p>This is a QB passer efficiency rating report, generated by the " +
                "Android <a href=\"" + appUrl + "\">QB Calculator</a>.</p>" +
                "<p>" + "<b><u>QB STAT LINE</u></b><br>" +
                completions + "/" + attempts + ", " + yards + " YDS, " + tds + " TD, " + interceptions + " INT<br><br>" +
                "<b><u>QB STATISTICS</u></b><br>";
                if(includeTeam) htmlBody += "Team: " + teamName + "<br>";
                if(includePlayer) htmlBody += "Player: " + playerName + "<br>";
                if(includeDate) htmlBody += "Date: " + date + "<br>";
                htmlBody += "Formula: " + formula + "<br>" +
                "Completions: " + completions + "<br>" +
                "Attempts: " + attempts + "<br>" +
                "Passing Yards: " + yards + "<br>" +
                "Passing TDs: " + tds + "<br>" +
                "Interceptions: " + interceptions + "<br>" +
                "Completion %: " + cp + "<br>" +
                "Yards Per Attempt: " + ya + "<br>" +
                "TD %: " + tp + "<br>" +
                "Interception %: " + ip + "<br>" +
                "QB Rating: " + rating +
                "</p>" +
                "<p>&copy; 2013 <a href=\"" + appUrl + "\">Jason Barkes</a></p>";

            final Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(Intent.EXTRA_SUBJECT, subject);
            email.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(htmlBody));
            email.setType(StaticStrings.MSG_TYPE_RFC822);
            startActivity(Intent.createChooser(email, StaticStrings.DLG_TITLE_SELECT_APP));
        }

        /**
         * Method to set the stats externally, given a QB rating object.
         */
        public void setRating(QBRatingBase qb) {
            removeTextWatchers();

            /** Clear the current rating */
            mQB = null;

            /** Set the stat control values */
            int index = qb.getLeague().ordinal();
            mLeagueSpinner.setSelection(index, false);

            mInterceptionsEditText.setText(String.valueOf(qb.interceptions));
            mTouchdownsEditText.setText(String.valueOf(qb.tds));
            mYardsEditText.setText(String.valueOf(qb.yards));
            mAttemptsEditText.setText(String.valueOf(qb.attempts));
            mCompletionsEditText.setText(String.valueOf(qb.completions));
            mTeamTextView.setText(qb.team);
            mPlayerTextView.setText(qb.player);

            createTextWatchers();
        }

        /**
         * Method to clear EditText and TextView controls.
         */
        private void resetControls() {
            removeTextWatchers();

            ArrayList<TextView> ctls = new ArrayList<TextView>();

            ctls.add(mCompletionPercentageTextView);
            ctls.add(mYardsPerAttemptTextView);
            ctls.add(mInterceptionPercentageTextView);
            ctls.add(mTouchdownPercentageTextView);
            ctls.add(mInterceptionsEditText);
            ctls.add(mTouchdownsEditText);
            ctls.add(mYardsEditText);
            ctls.add(mAttemptsEditText);
            ctls.add(mCompletionsEditText);
            ctls.add(mRatingEditText);
            ctls.add(mTeamTextView);
            ctls.add(mPlayerTextView);

            Iterator itr = ctls.iterator();
            while (itr.hasNext()) {
                TextView ctl = (TextView) itr.next();
                ctl.setText("");
            }

            mRatingInfoImage.setVisibility(View.INVISIBLE);

            createTextWatchers();

            /** Set focus to the attempts control */
            mCompletionsEditText.requestFocus();

            mQB = null;
        }

        /**
         * Creates required field TextWatchers on our QB stats EditTexts.
         */
        private void createTextWatchers() {
            createTextWatcher(mCompletionsEditText, getResources().getText(R.string.label_completions).toString());
            createTextWatcher(mAttemptsEditText, getResources().getText(R.string.label_attempts).toString());
            createTextWatcher(mYardsEditText, getResources().getText(R.string.label_yards).toString());
            createTextWatcher(mTouchdownsEditText, getResources().getText(R.string.label_tds).toString());
            createTextWatcher(mInterceptionsEditText, getResources().getText(R.string.label_ints).toString());
        }

        /**
         * Creates an individual required field TextWatcher for the specified EditText.
         */
        private void createTextWatcher(EditText editText, String errMsg) {
            StatTextWatcher stw = new StatTextWatcher(editText, errMsg);
            mEditWatchers.add(stw);
            editText.addTextChangedListener(stw);
        }

        /**
         * Removes associated TextWatchers from known EditTexts.
         */
        private void removeTextWatchers() {
            mCompletionsEditText.setError(null);
            mCompletionsEditText.removeTextChangedListener(mEditWatchers.get(0));
            mAttemptsEditText.removeTextChangedListener(mEditWatchers.get(1));
            mAttemptsEditText.setError(null);
            mYardsEditText.removeTextChangedListener(mEditWatchers.get(2));
            mYardsEditText.setError(null);
            mTouchdownsEditText.removeTextChangedListener(mEditWatchers.get(3));
            mTouchdownsEditText.setError(null);
            mInterceptionsEditText.removeTextChangedListener(mEditWatchers.get(4));
            mInterceptionsEditText.setError(null);

            mEditWatchers.clear();
        }

        /**
         * Disables associated TextWatchers - typically to be used before resetting required
         * EditTexts to avoid a visual indicator after the reset.
         */
        private void disableTextWatchers(boolean disableFlag) {
            Iterator itr = mEditWatchers.iterator();
            while(itr.hasNext()) {
                StatTextWatcher stw = (StatTextWatcher) itr.next();
                stw.setClear(disableFlag);
            }
        }

        /**
         * Triggers the associated TextWatchers by setting the specified text; which may or may
         * not cause a visual indicator, depending on the text that is set and the TextWatchers'
         * configuration.
         */
        private void triggerTextWatchers(String txtToSet) {
            Iterator itr = mEditWatchers.iterator();
            while(itr.hasNext()) {
                StatTextWatcher stw = (StatTextWatcher) itr.next();
                if(stw.mEditSrc.getText().length() == 0) {
                    stw.onTextChanged(txtToSet, 0, 0, 0);
                }
            }
        }

        /**
         * Method that performs the QB rating calculation.
         */
        public void calcRating(boolean logRecent) {
            try {
                QBRatingBase.LEAGUE league = QBRatingBase.LEAGUE.values()[mLeagueSpinner.getSelectedItemPosition()];

                int completions = Integer.valueOf(mCompletionsEditText.getText().toString());
                int attempts = Integer.valueOf(mAttemptsEditText.getText().toString());
                int yards = Integer.valueOf(mYardsEditText.getText().toString());
                int tds = Integer.valueOf(mTouchdownsEditText.getText().toString());
                int interceptions = Integer.valueOf(mInterceptionsEditText.getText().toString());
                String date = new SimpleDateFormat(StaticStrings.FMT_SHORT_DT).format(new Date());
                String team = mTeamTextView.getText().toString();
                String player = mPlayerTextView.getText().toString();

                /** Create the appropriate rating object */
                switch (league) {
                    case NFL:
                    case AFL:
                    case CFL:
                        mQB = new NFLQBRating(completions, attempts, yards, tds, interceptions,
                                league, date, team, player);
                        break;
                    case NCAA:
                    case HS:
                        mQB = new NCAAQBRating(completions, attempts, yards, tds, interceptions,
                                league, date, team, player);
                        break;
                }

                /** Perform the calculation */
                double rating = mQB.getRating();

                /** Show the results */
                mRatingEditText.setText(String.valueOf(rating));
                mCompletionPercentageTextView.setText("(" + String.valueOf(mQB.getCompletionPercentage()) + "%)");
                mYardsPerAttemptTextView.setText("(" + String.valueOf(mQB.getYardsPerAttempt()) + " YPA)");
                mInterceptionPercentageTextView.setText("(" + String.valueOf(mQB.getInterceptionPercentage()) + "%)");
                mTouchdownPercentageTextView.setText("(" + String.valueOf(mQB.getTouchdownPercentage()) + "%)");

                 /** Add to the recents JSON preference array */
                if (logRecent) {
                    addRecentJsonPreference(mQB);
                    mCalcCallback.onRatingCalculated(mQB);
                }

                mRatingInfoImage.setVisibility(View.VISIBLE);

                mRatingEditText.requestFocus();
                mRatingEditText.setSelected(true);

                /** Hide the soft keyboard */
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.showSoftInput(mRatingEditText, InputMethodManager.HIDE_IMPLICIT_ONLY);
                imm.hideSoftInputFromWindow(mRatingEditText.getWindowToken(), 0);

                /** Update teams and players json shared preferences */
                saveTeams();
                savePlayers();
            }
            catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    SmartLog.w(StaticStrings.LOG_TAG, "Exception caught: " + e.getMessage());
                    SmartLog.d(StaticStrings.LOG_TAG, Log.getStackTraceString(e));
                }

                /** One of the required EditTexts is empty, so trigger the validators. */
                triggerTextWatchers("");
            }
        }

        /**
         * Gets the recents QB Ratings array JSON from shared preferences.
         */
        private String getRecentsJsonPreference(String prefName) {
            SharedPreferences settings = getActivity().getSharedPreferences(getString(R.string.pref_key_recents), 0);
            String value = settings.getString(prefName, "");

            return value;
        }

        /**
         * Adds a new QB Rating calculation to the stored JSON preference.
         */
        private void addRecentJsonPreference(QBRatingBase qbRating) {

            mQBList.recents.add(0, qbRating);
            int itemCount = mQBList.recents.size();

            if (itemCount > mListLimit) {
                for (int i = itemCount - 1; i > mListLimit - 1; --i) {
                    mQBList.recents.remove(i);
                }
            }

            Gson gson = new Gson();
            String jsonString = gson.toJson(mQBList);

            /** Save the JSON */
            SharedPreferences settings = getActivity().getSharedPreferences(getString(R.string.pref_key_recents), 0);
            SharedPreferences.Editor e = settings.edit();
            e.putString(getString(R.string.pref_key_recents_json), jsonString);
            e.commit();
        }

        /**
         * Resets the recents history list.
         */
        public void resetRecentsHistory() {
            final String currentListJson = getRecentsJsonPreference(getString(R.string.pref_key_recents_json));
            Gson gson = new Gson();

            mQBList = gson.fromJson(currentListJson, QBRatingList.class);
            if(mQBList == null) {
                mQBList = new QBRatingList();
            }
        }

        /**
         * Returns the recents list limit preference.
         */
        private int getListLimitPref() {
            int limit = Integer.valueOf(DEFAULT_LIST_LIMIT);
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

            try {
                limit = Integer.valueOf(settings.getString(getString(R.string.pref_key_recents_limit), String.valueOf(DEFAULT_LIST_LIMIT)));
            } catch (NumberFormatException e) {
                limit = Integer.valueOf(DEFAULT_LIST_LIMIT);
            }

            return limit;
        }
    }
}
