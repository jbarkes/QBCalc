/*************************************************************************************************
 * Android QB Rating App
 * Jason Barkes - http://jbarkes.blogspot.com
 * November 11, 2013
 *************************************************************************************************/
package barkes.football.qbcalc.adapters;

import android.util.Log;
import android.widget.BaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import barkes.football.qbcalc.BuildConfig;
import barkes.util.SmartLog;
import barkes.util.StaticStrings;

/**
 * Created by Jason Barkes on 11/21/2013.
 */
public abstract class JsonAdapterBase extends BaseAdapter {

    private JSONArray _array;

    @Override
    public int getCount() {
        return _array == null ? 0 : _array.length();
    }

    @Override
    public Object getItem(int position) {
        if (_array == null | _array.length() < position) {
            return null;
        }
        try {
            return _array.get(position);
        }
        catch (final JSONException e) {
            if (BuildConfig.DEBUG) {
                SmartLog.w(StaticStrings.LOG_TAG, "Exception caught: " + e.getMessage());
                SmartLog.d(StaticStrings.LOG_TAG, Log.getStackTraceString(e));
            }
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public JSONObject getObject(int position) {
        return (JSONObject) getItem(position);
    }

    public JSONArray getJSONArray() {
        return _array;
    }

    public void setData(JSONArray data) {
        _array = data;
        notifyDataSetChanged();
    }
}
