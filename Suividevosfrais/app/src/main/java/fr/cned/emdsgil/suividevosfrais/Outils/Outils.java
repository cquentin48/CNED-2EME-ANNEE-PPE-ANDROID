package fr.cned.emdsgil.suividevosfrais.Outils;

/**
 * Created by Caesar01 on 06/02/2018.
 */

public abstract class Outils {

    /**
     * Conversion du mois au format String au format Integer
     * @param monthAndDate la date au format (YYYYMM) au format string
     * @return Integer le mois au format integer
     */
    public static int convertMonthToDigital(String monthAndDate){
        //Extraction du mois dans la date
        String mois = monthAndDate.substring(4,5);

        return Integer.parseInt(mois);
    }

    public static int convertYearToDigital(String monthAndDate){
        //Extraction du mois dans la date
        String mois = monthAndDate.substring(0,3);

        return Integer.parseInt(mois);
    }
}
