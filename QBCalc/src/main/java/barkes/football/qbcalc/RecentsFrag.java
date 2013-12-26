/*************************************************************************************************
 * Android QB Rating App
 * Jason Barkes - http://jbarkes.blogspot.com
 * November 11, 2013
 *************************************************************************************************/
package barkes.football.qbcalc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import barkes.football.qbcalc.adapters.RecentsAdapter;
import barkes.football.qbrating.QBRatingBase;
import barkes.util.SmartLog;
import barkes.util.StaticStrings;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Recents ListFragment that represents a history of the QB rating calculations that have
 * been performed. The list is persisted as a JSON array string in Shared Preferences.
 */
public class RecentsFrag extends ListFragment {

    /** Callback interface definition */
    public interface OnRecentsActionsListener {
        public void onRecentsCleared();
        public void onRatingOpen(QBRatingBase qb);
    }

    /** Callbacks */
    private OnRecentsActionsListener mRecentsActionsCallback;

    private View mLayoutRootView = null;
    private TextView mNoRecentsTextView = null;

    /** Adapters */
    private RecentsAdapter mRecentsAdapter = null;
    private String mJsonString = null;

    public void updateData(QBRatingBase qbRating) {
        mNoRecentsTextView.setVisibility(View.INVISIBLE);
        mJsonString = getJsonFromPreferences();
        setAdapterData(mJsonString);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        /** Make sure the container Activity has implemented the callback interface. */
        try {
            mRecentsActionsCallback = (OnRecentsActionsListener) activity;
        }
        catch (ClassCastException e) {
            if (BuildConfig.DEBUG) {
                throw new ClassCastException(activity.toString()
                    + " must implement OnRecentsActionsListener");
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        /** Currently not doing anything on single item click. */
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mJsonString = getJsonFromPreferences();
        setAdapterData(mJsonString);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayoutRootView = inflater.inflate(R.layout.frag_recents, container, false);

        mNoRecentsTextView = (TextView) mLayoutRootView.findViewById(R.id.tv_no_recents);
        if (mRecentsAdapter.getCount() == 0) {
            mNoRecentsTextView.setVisibility(View.VISIBLE);
        }
        else {
            mNoRecentsTextView.setVisibility(View.INVISIBLE);
        }

        //final LayoutInflater inflaterContext = inflater;
        //ListView lv = (ListView) mLayoutRootView.findViewById(R.id.list);
        /*mLayoutRootView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) inflaterContext
                        .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(v, InputMethodManager.HIDE_IMPLICIT_ONLY);

                    // This was taken from MainActivity
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    //imm.showSoftInput(tab.getCustomView(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                    imm.hideSoftInputFromWindow(tab.getCustomView().getWindowToken(), 0);
                    //imm.hideSoftInputFromWindow(mLayoutRootView.getWindowToken(), 0);
                }
            }
        });*/

        return mLayoutRootView;
    }

    /*@Override
    public void onResume() {
        super.onResume();

        InputMethodManager imm = (InputMethodManager) getActivity()
            .getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mLayoutRootView.getWindowToken(), 0); // InputMethodManager.HIDE_IMPLICIT_ONLY);
    }*/

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        /** Get the info on the selected item */
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

        /** Get the selected rating */
        QBRatingBase qb = QBRatingBase.fromJSONObject((JSONObject) mRecentsAdapter.getItem(info.position));

        /** Set the menu title */
        menu.setHeaderTitle(qb.getLeague().toString() + ": " + qb.getRating());

        /** Add the menu items */
        String[] menuItems = getResources().getStringArray(R.array.recent_context_menu);
        for (int i = 0; i < menuItems.length; i++) {
            menu.add(Menu.NONE, i, i, menuItems[i]);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        /** Get the info on the selected item */
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        /** Get the selected rating */
        QBRatingBase qb = QBRatingBase.fromJSONObject((JSONObject) mRecentsAdapter.getItem(info.position));

        switch (item.getItemId()) {
            /** Open */
            case 0:
                mRecentsActionsCallback.onRatingOpen(qb);
                return true;
            /** Clear all */
            case 1:
                clearRecents();
                return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        /** Reset menu item */
        MenuItem menuClearRecents = menu.add(Menu.NONE, R.id.action_clear_recents, Menu.NONE,
                R.string.action_clear_recents);
        {
            menuClearRecents.setAlphabeticShortcut('d');
            menuClearRecents.setIcon(R.drawable.ic_trash);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                menuClearRecents.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        /** Export CSV menu item */
        MenuItem menuExportCSV = menu.add(Menu.NONE, R.id.action_export_csv, Menu.NONE,
                R.string.action_export_csv);
        {
            menuExportCSV.setAlphabeticShortcut('s');
            menuExportCSV.setIcon(R.drawable.ic_save);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                menuExportCSV.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_recents:
                clearRecents();
                return true;
            case R.id.action_export_csv:
                exportAndEmailCSV();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearRecents() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Confirm");
        builder.setMessage("Are you sure?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                clearRecentsJsonPreference();
                mJsonString = getJsonFromPreferences();
                setAdapterData(mJsonString);
                mRecentsActionsCallback.onRecentsCleared();
                mNoRecentsTextView.setVisibility(View.VISIBLE);

                dialog.dismiss();
            }

        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });


        AlertDialog alert = builder.create();
        alert.show();
    }

    private void clearRecentsJsonPreference() {
        SharedPreferences settings = getActivity().getSharedPreferences(getString(R.string.pref_key_recents), 0);
        SharedPreferences.Editor e = settings.edit();
        e.putString(getString(R.string.pref_key_recents_json), "");
        e.commit();
    }

    private String getJsonFromPreferences() {
        String jsonRaw = null;
        SharedPreferences settings = getActivity().getSharedPreferences(getString(R.string.pref_key_recents), 0);
        jsonRaw = settings.getString(getString(R.string.pref_key_recents_json), "");
        return jsonRaw;
    }

    private void setAdapterData(String jsonString) {
        mRecentsAdapter = new RecentsAdapter(getActivity());

        JSONObject jsonObj = null;
        JSONArray jsonArray = null;
        try {
            jsonObj = new JSONObject(jsonString);
            jsonArray = jsonObj.getJSONArray(StaticStrings.JSON_KEY_RECENTS);
        }
        catch (JSONException e) {
            if (BuildConfig.DEBUG) {
                SmartLog.w(StaticStrings.LOG_TAG, "Exception caught: " + e.getMessage());
                SmartLog.d(StaticStrings.LOG_TAG, Log.getStackTraceString(e));
            }
        }

        mRecentsAdapter.setData(jsonArray);
        setListAdapter(mRecentsAdapter);
    }

    private void exportAndEmailCSV() {

        /** Make sure there's something to export */
        if (mRecentsAdapter.getCount() == 0) {
            Toast toast = Toast.makeText(getActivity(), "There is nothing to export", LENGTH_LONG);
            toast.show();
            return;
        }

        final String CSV_SEPARATOR = ",";
        final String CSV_NEWLINE = "\r\n";
        final File sdDir = Environment.getExternalStorageDirectory();

        JSONObject jsonObj = null;
        JSONArray jsonArray = null;
        try {
            jsonObj = new JSONObject(mJsonString);
            jsonArray = jsonObj.getJSONArray(StaticStrings.JSON_KEY_RECENTS);

            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(sdDir + "/" + StaticStrings.CSV_FILENAME), StaticStrings.MSG_TYPE_UTF8));

            /** Write the CSV header columns */
            StringBuffer csvHeader = new StringBuffer();
            for (QBRatingBase.CSV_HEADER hdr : QBRatingBase.CSV_HEADER.values()) {
                csvHeader.append(hdr.name());
                csvHeader.append(CSV_SEPARATOR);
            }
            bw.write(csvHeader.toString());
            bw.write(CSV_NEWLINE);

            /** Write the CSV data */
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject child = jsonArray.getJSONObject(i);
                QBRatingBase qb = QBRatingBase.fromJSONObject(child);

                StringBuffer csvLine = new StringBuffer();
                csvLine.append(qb.getLeague().name()); csvLine.append(CSV_SEPARATOR);
                csvLine.append(qb.team); csvLine.append(CSV_SEPARATOR);
                csvLine.append(qb.player); csvLine.append(CSV_SEPARATOR);
                csvLine.append(String.valueOf(qb.completions)); csvLine.append(CSV_SEPARATOR);
                csvLine.append(String.valueOf(qb.attempts)); csvLine.append(CSV_SEPARATOR);
                csvLine.append(String.valueOf(qb.getCompletionPercentage())); csvLine.append(CSV_SEPARATOR);
                csvLine.append(String.valueOf(qb.yards)); csvLine.append(CSV_SEPARATOR);
                csvLine.append(String.valueOf(qb.getYardsPerAttempt())); csvLine.append(CSV_SEPARATOR);
                csvLine.append(String.valueOf(qb.tds)); csvLine.append(CSV_SEPARATOR);
                csvLine.append(String.valueOf(qb.getTouchdownPercentage())); csvLine.append(CSV_SEPARATOR);
                csvLine.append(String.valueOf(qb.interceptions)); csvLine.append(CSV_SEPARATOR);
                csvLine.append(String.valueOf(qb.getInterceptionPercentage())); csvLine.append(CSV_SEPARATOR);
                csvLine.append(String.valueOf(qb.getRating())); bw.write(csvLine.toString());
                bw.write(CSV_NEWLINE);
            }

            bw.flush();
            bw.close();

            /** Send the email with CSV file attached */
            String date = new SimpleDateFormat(StaticStrings.FMT_SHORT_DT).format(new Date());
            String appUrl = getString(R.string.app_url);
            String subject = "QB Calculator App - CSV Export (" + date + ")";
            String htmlBody =
                "<p><b>" + subject + "</b></p>" +
                "<p>This is a CSV export of the recently calculated QB ratings from the " +
                "Android <a href=\"" + appUrl + "\">QB Calculator</a>.</p><br />" +
                "<p>&copy; 2013 <a href=\"" + appUrl + "\">Jason Barkes</a></p>";

            Intent csv = new Intent(Intent.ACTION_SEND);
            csv.putExtra(Intent.EXTRA_SUBJECT, subject);
            csv.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(htmlBody));
            csv.setType(StaticStrings.MSG_TYPE_CSV);
            csv.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + sdDir + "/" + StaticStrings.CSV_FILENAME));
            startActivity(Intent.createChooser(csv, StaticStrings.DLG_TITLE_SELECT_APP));

        } catch (FileNotFoundException e) {
            if (BuildConfig.DEBUG) {
                SmartLog.w(StaticStrings.LOG_TAG, "Export CSV Exception: " + e.getMessage());
                SmartLog.d(StaticStrings.LOG_TAG, Log.getStackTraceString(e));
            }
            Toast.makeText(getActivity(), "Unable to export CSV: " + e.getMessage(), LENGTH_LONG).show();
        } catch (UnsupportedEncodingException e) {
            if (BuildConfig.DEBUG) {
                SmartLog.w(StaticStrings.LOG_TAG, "Export CSV Exception: " + e.getMessage());
                SmartLog.d(StaticStrings.LOG_TAG, Log.getStackTraceString(e));
            }
            Toast.makeText(getActivity(), "Unable to export CSV: " + e.getMessage(), LENGTH_LONG).show();
        } catch (JSONException e) {
            if (BuildConfig.DEBUG) {
                SmartLog.w(StaticStrings.LOG_TAG, "Export CSV Exception: " + e.getMessage());
                SmartLog.d(StaticStrings.LOG_TAG, Log.getStackTraceString(e));
            }
            Toast.makeText(getActivity(), "Unable to export CSV: " + e.getMessage(), LENGTH_LONG).show();
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                SmartLog.w(StaticStrings.LOG_TAG, "Export CSV Exception: " + e.getMessage());
                SmartLog.d(StaticStrings.LOG_TAG, Log.getStackTraceString(e));
            }
            Toast.makeText(getActivity(), "Unable to export CSV: " + e.getMessage(), LENGTH_LONG).show();
        }
    }
}
