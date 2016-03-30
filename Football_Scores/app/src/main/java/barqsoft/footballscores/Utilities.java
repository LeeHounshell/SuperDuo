package barqsoft.footballscores;

import android.content.res.Resources;

import org.apache.commons.lang3.StringUtils;


class Utilities {
    //private final static String TAG = "LEE: <" + Utilities.class.getSimpleName() + ">";

    private final static Resources resources = FootballScoresApplication.getAppContext().getResources();

    public static String getLeague(int league_num) {
        //Log.v(TAG, "getLeague");
        // FIXED: updated league info - see League.java
        switch (league_num) {
            case League.SERIE_A:
            case League.SERIE_A1:
                return resources.getString(R.string.football_seria_a);
            case League.PREMIER_LEGAUE:
            case League.PREMIER_LEAGUE:
            case League.PRIMERA_LIGA:
                return resources.getString(R.string.football_premier_league);
            case League.CHAMPIONS_LEAGUE:
                return resources.getString(R.string.football_champions_league);
            case League.PRIMERA_DIVISION:
            case League.PRIMERA_DIVISION1:
                return resources.getString(R.string.football_primera_division);
            case League.SEGUNDA_DIVISION :
                return resources.getString(R.string.football_segunda_division);
            case League.BUNDESLIGA:
            case League.BUNDESLIGA1:
            case League.BUNDESLIGA2:
            case League.BUNDESLIGA3:
                return resources.getString(R.string.football_bundesliga);
            case League.LIGUE1:
                return resources.getString(R.string.football_ligue_1);
            case League.LIGUE2:
                return resources.getString(R.string.football_ligue_2);
            case League.EREDIVISIE:
                return resources.getString(R.string.football_eredivisie);
            case League.AYSO425:
                return resources.getString(R.string.football_ayso425);
            default:
                return resources.getString(R.string.football_unknown) + league_num;
        }
    }

    public static String getMatchDay(int matchDay, int leagueNum) {
        //Log.v(TAG, "getMatchDay");
        if (leagueNum == League.CHAMPIONS_LEAGUE) {
            if (matchDay <= 6) {
                return resources.getString(R.string.football_stages_6);
            }
            else if (matchDay == 7) {
                return resources.getString(R.string.football_first_knockout);
            }
            else if (matchDay == 8) {
                return resources.getString(R.string.football_knockout);
            }
            else if (matchDay == 9 || matchDay == 10) {
                return resources.getString(R.string.football_quarterfinal);
            }
            else if (matchDay == 11) {
                return resources.getString(R.string.football_semifinal);
            }
            else if (matchDay == 12) {
                return resources.getString(R.string.football_final);
            }
            else {
                return resources.getString(R.string.football_show_matchday) + String.valueOf(matchDay);
            }
        }
        else {
            return resources.getString(R.string.football_show_matchday) + String.valueOf(matchDay);
        }
    }

    public static String getScores(int homeGoals, int awayGoals) {
        //Log.v(TAG, "getScores");
        if (homeGoals < 0 || awayGoals < 0) {
            return resources.getString(R.string.future);
        }
        else {
            return String.valueOf(homeGoals) + resources.getString(R.string.football_separator) + String.valueOf(awayGoals);
        }
    }

    public static int getTeamCrestByTeamName(String teamName) {
        //Log.v(TAG, "getTeamCrestByTeamName");
        if (teamName == null) {
            return R.drawable.unknown;
        }
        //This is the set of icons that are currently in the app. Feel free to find and add more
        if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_arsenal_london), teamName)) {
            return R.drawable.arsenal;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_manchester_united), teamName)) {
            return R.drawable.manchester_united;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_swansea_city), teamName)) {
            return R.drawable.swansea_city_afc;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_leicester_city), teamName)) {
            return R.drawable.leicester_city_fc_hd_logo;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_everton), teamName)) {
            return R.drawable.everton_fc_logo1;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_west_ham_united), teamName)) {
            return R.drawable.west_ham;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_tottenham_hotspur), teamName)) {
            return R.drawable.tottenham_hotspur;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_west_bromwich_albion), teamName)) {
            return R.drawable.west_bromwich_albion_hd_logo;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_sunderland), teamName)) {
            return R.drawable.sunderland;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_stoke_city), teamName)) {
            return R.drawable.stoke_city;
        }
        // FIXED: added missing teams
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_aston_villa), teamName)) {
            return R.drawable.aston_villa;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_burney), teamName)) {
            return R.drawable.burney_fc_hd_logo;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_chelsea), teamName)) {
            return R.drawable.chelsea;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_crystal_palace), teamName)) {
            return R.drawable.crystal_palace_fc;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_hull_city), teamName)) {
            return R.drawable.hull_city_afc_hd_logo;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_liverpool), teamName)) {
            return R.drawable.liverpool;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_manchester_city), teamName)) {
            return R.drawable.manchester_city;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_newcastle_united), teamName)) {
            return R.drawable.newcastle_united;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_queens_park_rangers), teamName)) {
            return R.drawable.queens_park_rangers_hd_logo;
        }
        else if (StringUtils.containsIgnoreCase(resources.getString(R.string.football_southampton), teamName)) {
            return R.drawable.southampton_fc;
        }
        else {
            return R.drawable.unknown;
        }
    }

}