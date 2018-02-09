package fr.cned.emdsgil.suividevosfrais.Controleur;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import org.json.JSONArray;

import java.lang.reflect.Field;
import java.util.Hashtable;

import fr.cned.emdsgil.suividevosfrais.Modele.AccesDistant;
import fr.cned.emdsgil.suividevosfrais.Modele.Compte;
import fr.cned.emdsgil.suividevosfrais.Modele.FraisMois;

public class Global {

    // tableau d'informations mémorisées
    public static Hashtable<Integer, FraisMois> listFraisMois = new Hashtable<>();
    private static Global instance = null;
    private static Context contexte;
    private static AccesDistant accesDistant;
    private Compte compte;

    /* Retrait du type de l'Hashtable (Optimisation Android Studio)
     * Original : Typage explicit =
	 * public static Hashtable<Integer, FraisMois> listFraisMois = new Hashtable<Integer, FraisMois>();
	*/

    // fichier contenant les informations sérialisées
    public static final String filename = "save.fic";

    public static final Global getInstance(Context contexte) {
        if (Global.instance == null) {//Si aucune instance n'a été créée
            Global.contexte = contexte;
            Global.instance = new Global();
        }
        return Global.instance ;
    }


    /**
     * Modification de l'affichage de la date (juste le mois et l'année, sans le jour)
     */
    public static void changeAfficheDate(DatePicker datePicker, boolean afficheJours) {
        try {
            Field f[] = datePicker.getClass().getDeclaredFields();
            for (Field field : f) {
                int daySpinnerId = Resources.getSystem().getIdentifier("day", "id", "android");
                datePicker.init(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), null);
                if (daySpinnerId != 0)
                {
                    View daySpinner = datePicker.findViewById(daySpinnerId);
                    if (!afficheJours)
                    {
                        daySpinner.setVisibility(View.GONE);
                    }
                }
            }
        } catch (SecurityException | IllegalArgumentException e) {
            Log.d("ERROR", e.getMessage());
        }
    }

    /**
     * Supprime un frais hors-forfait
     * @param fraisHfIndex l'index du frais hors-forfait à supprimer
     * @param monthIndex l'index du mois des frais hors-forfait
     */
    public static void deleteFraisHorsForfait(int fraisHfIndex, int monthIndex){
        listFraisMois.get(monthIndex).supprFraisHf(fraisHfIndex);
    }


    public Compte getCompte() {
        return compte;
    }

    public void setCompte(Compte compte) {
        this.compte = compte;
    }
}
