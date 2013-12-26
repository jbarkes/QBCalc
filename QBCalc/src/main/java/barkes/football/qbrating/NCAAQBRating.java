/*************************************************************************************************
 * Android QB Rating App
 * Jason Barkes - http://jbarkes.blogspot.com
 * November 11, 2013
 *************************************************************************************************/
package barkes.football.qbrating;

import barkes.util.StaticStrings;
import barkes.util.FormatHelper;

/**
 * Created by Jason Barkes on 11/16/2013.
 */
public class NCAAQBRating extends QBRatingBase {

    public NCAAQBRating(int completions, int attempts, int yards, int tds, int interceptions,
                        LEAGUE league, String date, String team, String player) {
        super(completions, attempts, yards, tds, interceptions, league, date, team, player);
    }

    @Override
    public double getRating() {

        final double yac = getYardsPerAttempt() * 8.4;
        final double tpc = getTouchdownPercentage() * 3.3;
        final double ipc = getInterceptionPercentage() * 2;

        this.rating = FormatHelper.quickRound(getCompletionPercentage() + yac + tpc - ipc, StaticStrings.FMT_SGL_PREC);
        return this.rating;
    }

}
