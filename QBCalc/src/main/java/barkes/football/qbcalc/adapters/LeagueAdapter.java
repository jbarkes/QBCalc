/*************************************************************************************************
 * Android QB Rating App
 * Jason Barkes - http://jbarkes.blogspot.com
 * November 11, 2013
 *************************************************************************************************/
package barkes.football.qbcalc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import barkes.football.qbcalc.R;
import barkes.football.qbrating.QBRatingBase;

public class LeagueAdapter extends ArrayAdapter<String> {
    private Context mContext = null;
    /*private final static String[] LEAGUES = {
        "NFL",
        "NCAA",
        "AFL",
        "CFL",
        "HS"
    };*/

    public LeagueAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        mContext = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        //return super.getDropDownView(position, convertView, parent);
        return getCustomView(position, convertView, parent, R.layout.league_spinner_dropdown);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);
        return getCustomView(position, convertView, parent, R.layout.league_spinner_item);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent, int viewID) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        View item = inflater.inflate(viewID, parent, false);
        TextView label = (TextView)item.findViewById(R.id.tv_league);
        label.setText(QBRatingBase.LEAGUE.values()[position].toString());

        ImageView icon = (ImageView)item.findViewById(R.id.icon);

        if (QBRatingBase.LEAGUE.values()[position] == QBRatingBase.LEAGUE.NFL) {
            icon.setImageResource(R.drawable.ic_helmet_blue_small);
        }
        else if (QBRatingBase.LEAGUE.values()[position] == QBRatingBase.LEAGUE.NCAA) {
            icon.setImageResource(R.drawable.ic_helmet_black_small);
        }
        else{
            icon.setImageResource(R.drawable.ic_helmet_silver_small);
        }

        return item;
    }

}
