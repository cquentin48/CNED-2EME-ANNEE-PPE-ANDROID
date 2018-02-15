package fr.cned.emdsgil.suividevosfrais.Controleur;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import org.json.JSONArray;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import fr.cned.emdsgil.suividevosfrais.Modele.AccesDistant;
import fr.cned.emdsgil.suividevosfrais.Modele.Compte;
import fr.cned.emdsgil.suividevosfrais.Modele.FraisMois;

public final class Global{

    // tableau d'informations mémorisées
    private static Global instance = null;
    private static Context contexte;
    private static AccesDistant accesDistant;
    private static boolean loadedData = false;

    //Modèles de données
    private Compte compte;
    public static Hashtable<Integer, FraisMois> listeFraisMois = new Hashtable<>();


    // fichier contenant les informations sérialisées
    public static final String filename = "save.fic";

    private Global(){
        super();
    }

    public static final Global getInstance(Context contexte) {
        if (Global.instance == null) {//Si aucune instance n'a été créée
            Global.contexte = contexte;
            Global.instance = new Global();
            accesDistant = new AccesDistant();
        }
        return Global.instance ;
    }

    public static boolean isLoadedData() {
        return loadedData;
    }

    public static void setLoadedData(boolean loadedData) {
        Global.loadedData = loadedData;
    }

    /**
     * Ajoute une fiche de frais à la liste
     * @param uneFiche la liste à ajouter
     * @param key l'index de la fiche
     */
    public static void addFicheFrais(Integer key,FraisMois uneFiche){
        listeFraisMois.put(key,uneFiche);
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
        listeFraisMois.get(monthIndex).supprFraisHf(fraisHfIndex);
    }


    public Compte getCompte() {
        return compte;
    }

    public void setCompte(Compte compte) {
        this.compte = compte;
    }

    /**
     * Connection au service GSB Info
     * @param username le pseudonyme entré
     * @param password le mdp entré
     * @return le compte crée, sinon il retourne null
     */
    public void connection(String username, String password){
        List connexionInfo = new ArrayList();
        connexionInfo.add(0,username);
        connexionInfo.add(1,password);
        JSONArray connexionInfoJSONArray = new JSONArray(connexionInfo);
        accesDistant.envoi("connexion",connexionInfoJSONArray);
    }

    /**
     * Suppression du frais Hors-Forfait
     * @param id l'identifiant du frais hors-forfait
     */
    public void deleteFraisHF(int id){
        List connexionInfo = new ArrayList();
        connexionInfo.add(0, id);
        JSONArray connectionInfoJSONArray = new JSONArray(connexionInfo);
        accesDistant.envoi("deleteFraisHF", connectionInfoJSONArray);
    }

    /**
     * Mise à jour/Insertion du frais Hors-Forfait dans la base de donnée MYSQL
     * @param id l'identifiant du frais
     * @param mois le mois du frais
     * @param idVisiteur le matricule du visiteur
     * @param libelle la description du frais
     * @param date la date du frais
     * @param montant le montant du frais
     */
    public void mySQLSetFraisHorsForfait(int id, String mois, String idVisiteur, String libelle, String date, String montant){
        List connexionInfo = new ArrayList();

        //Renseignements des champs pour la requête SQL
        connexionInfo.add(0,id);
        connexionInfo.add(1,mois);
        connexionInfo.add(2,idVisiteur);
        connexionInfo.add(3,libelle);
        connexionInfo.add(3,date);
        connexionInfo.add(3,montant);

        //Création de la table JSON
        JSONArray connexionInfoJSONArray = new JSONArray(connexionInfo);

        //Envoi de la requête
        accesDistant.envoi("mySQLSetFraisHorsForfait",connexionInfoJSONArray);
    }

    /**
     * Mise à jour du frais dans la base de donnée Mysql
     * @param mois le mois dans lequel le frais se situe
     * @param libelle le libelle du frais forfaitisé (se trouvant au format raccourci)
     * @param idVisiteur le marticul du visiteur
     * @param quantite le quantite du frais
     */
    public void mySQLSetFraisForfaitisee(String mois, String libelle, String idVisiteur, int quantite){
        List connexionInfo = new ArrayList();

        //Renseignements des champs pour la requête SQL
        connexionInfo.add(0,mois);
        connexionInfo.add(1,libelle);
        connexionInfo.add(2,idVisiteur);
        connexionInfo.add(3,quantite);

        //Création de la table JSON
        JSONArray connexionInfoJSONArray = new JSONArray(connexionInfo);

        //Envoi de la requête
        accesDistant.envoi("mySQLSetFraisForfaitisee",connexionInfoJSONArray);
    }

    /**
     * Chargemen des fiches de frais du mois
     * @param userId le matricule du visiteur
     */
    public void chargementFrais(String userId){
        List fraisHFMoisInfo = new ArrayList();
        fraisHFMoisInfo.add(0,userId);
        JSONArray fraisHFMoisInfoJSONArray = new JSONArray(fraisHFMoisInfo);
        System.out.println(fraisHFMoisInfoJSONArray.toString());
        accesDistant.envoi("chargementFrais",fraisHFMoisInfoJSONArray);
    }

    /**
     * Retourne la liste des fiches de frais
     * @return liste des fiches de frais
     */
    public static Hashtable<Integer, FraisMois> getListeFraisMois() {
        return listeFraisMois;
    }

    /**
     * Setteur de la fiche de frais du mois
     * @param listeFraisMois la nouvelle fiche de frais du mois
     */
    public static void setListeFraisMois(Hashtable<Integer, FraisMois> listeFraisMois) {
        Global.listeFraisMois = listeFraisMois;
    }
}
