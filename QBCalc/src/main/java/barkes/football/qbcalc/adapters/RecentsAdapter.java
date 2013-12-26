/*************************************************************************************************
 * Android QB Rating App
 * Jason Barkes - http://jbarkes.blogspot.com
 * November 11, 2013
 *************************************************************************************************/
package barkes.football.qbcalc.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import barkes.football.qbcalc.BuildConfig;
import barkes.football.qbcalc.R;
import barkes.football.qbrating.QBRatingBase;
import barkes.util.StaticStrings;
import barkes.util.SmartLog;

/**
 * Created by Jason Barkes on 11/21/2013.
 */
public class RecentsAdapter extends JsonAdapterBase {

    private final Context _context;
    private final LayoutInflater _inflater;

    public RecentsAdapter(Context context) {
        this._context = context;
        this._inflater = LayoutInflater.from(context);
    }

    /** View caching pattern */
    static class ViewHolder {
        TextView tvLine;
        TextView tvSub;
        ImageView ivIcon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        JSONObject recentItem = this.getObject(position);

        if(convertView == null) {
            convertView = _inflater.inflate(R.layout.item_recents, parent, false);
            holder = new ViewHolder();

            holder.tvLine = (TextView) convertView.findViewById(R.id.tv_item);
            holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_item);
            holder.tvSub = (TextView) convertView.findViewById(R.id.tv_subitem);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            String line = recentItem.getString(StaticStrings.KEY_LEAGUE)
                    + ": " + recentItem.getString(StaticStrings.KEY_RATING)
                    + " (" + recentItem.getString(StaticStrings.KEY_DATE) + ")";

            /** Include the player name (if supplied) or the team name (if supplied) **/
            String sub = recentItem.getString(StaticStrings.KEY_PLAYER).length() > 0
                    ? recentItem.getString(StaticStrings.KEY_PLAYER) + ", "
                    : recentItem.getString(StaticStrings.KEY_TEAM).length() > 0
                    ? recentItem.getString(StaticStrings.KEY_TEAM) + ", " : "";

            sub = sub + recentItem.getString(StaticStrings.KEY_COMPLETIONS)
                    + "/" + recentItem.getString(StaticStrings.KEY_ATTEMPTS)
                    + ", " + recentItem.getString(StaticStrings.KEY_YARDS)
                    + " YDS, " + recentItem.getString(StaticStrings.KEY_TDS)
                    + " TD, " + recentItem.getString(StaticStrings.KEY_INTERCEPTIONS) + " INT";
            holder.tvLine.setText(line);
            holder.tvSub.setText(sub);

            /** Set the rating helmet icon **/
            String leagueString = recentItem.getString(StaticStrings.KEY_LEAGUE);
            QBRatingBase.LEAGUE league = QBRatingBase.LEAGUE.HS;
            if (leagueString != null && leagueString.length() > 0) {
                league = QBRatingBase.LEAGUE.valueOf(leagueString);
                switch (league) {
                    case NFL:
                        holder.ivIcon.setImageResource(R.drawable.ic_helmet_blue);
                        break;
                    case NCAA:
                        holder.ivIcon.setImageResource(R.drawable.ic_helmet_black);
                        break;
                    case AFL:
                    case CFL:
                    case HS:
                        holder.ivIcon.setImageResource(R.drawable.ic_helmet_silver);
                        break;
                }
            }
        }
        catch (JSONException e) {
            if (BuildConfig.DEBUG) {
                SmartLog.w(StaticStrings.LOG_TAG, "Exception caught: " + e.getMessage());
                SmartLog.d(StaticStrings.LOG_TAG, Log.getStackTraceString(e));
            }
        }

        return convertView;
    }
}
