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
public class NFLQBRating extends QBRatingBase {
    private static final double NFL_MAX = 2.375;

    public NFLQBRating(int completions, int attempts, int yards, int tds, int interceptions,
                       LEAGUE league, String date, String team, String player) {
        super(completions, attempts, yards, tds, interceptions, league, date, team, player);
    }

    @Override
    public double getRating() {

        /** Completion percentage */
        double cpc = (getCompletionPercentage() - 30) * 0.05;
        if (cpc < 0) {
            cpc = 0;
        } else if (cpc > NFL_MAX) {
            cpc = NFL_MAX;
        }

        /** Yards per attempt */
        double yac = (getYardsPerAttempt() - 3) * 0.25;
        if (yac < 0) {
            yac = 0;
        } else if (yac > NFL_MAX) {
            yac = NFL_MAX;
        }

        /** TD percentage */
        double tpc = getTouchdownPercentage() * 0.2;
        if (tpc > NFL_MAX) {
            tpc = NFL_MAX;
        }

        /** Interception percentage */
        double ipc = NFL_MAX - (getInterceptionPercentage() * 0.25);
        if (ipc < 0) {
            ipc = 0;
        }

        /** Final rating */
        this.rating = FormatHelper.quickRound(((cpc + yac + tpc + ipc) / 6) * 100, StaticStrings.FMT_SGL_PREC);
        return this.rating;
    }
}
