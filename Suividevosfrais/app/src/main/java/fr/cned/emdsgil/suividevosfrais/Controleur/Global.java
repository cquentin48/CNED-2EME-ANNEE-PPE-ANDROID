package fr.cned.emdsgil.suividevosfrais.Controleur;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import fr.cned.emdsgil.suividevosfrais.Modele.AccesDistant;
import fr.cned.emdsgil.suividevosfrais.Modele.Compte;
import fr.cned.emdsgil.suividevosfrais.Modele.FraisHf;
import fr.cned.emdsgil.suividevosfrais.Modele.FraisMois;

public final class Global{

    // tableau d'informations mémorisées
    private static Global instance = null;
    private static Context contexte;
    private static AccesDistant accesDistant;
    private static boolean loadedData = false;
    private static int maxIndiceFraisHF = 0;

    //Modèles de données
    private static Compte compte;
    private static Hashtable<Integer, FraisMois> listeFraisMois = new Hashtable<>();
    private static Hashtable<Integer, FraisMois> listeFraisMoisMaj = new Hashtable<>();


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

    public static int getMaxIndiceFraisHF() {
        return maxIndiceFraisHF;
    }

    public static void setMaxIndiceFraisHF(int maxIndiceFraisHF) {
        Global.maxIndiceFraisHF = maxIndiceFraisHF;
    }

    public static Hashtable<Integer, FraisMois> getListeFraisMoisMaj() {
        return listeFraisMoisMaj;
    }

    public static void setListeFraisMoisMaj(Hashtable<Integer, FraisMois> listeFraisMoisMaj) {
        Global.listeFraisMoisMaj = listeFraisMoisMaj;
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
            Log.e("ERROR", e.getMessage());
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


    public static Compte getCompte() {
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
     * Mise à jour/Insertion du frais Hors-Forfait dans la base de donnée MYSQL
     * @param id l'identifiant du frais
     * @param mois le mois du frais
     * @param idVisiteur le matricule du visiteur
     * @param libelle la description du frais
     * @param montant le montant du frais
     */
    public void updateUpdateFraisHorsForfaitTable(int id, int mois, String idVisiteur, String libelle, int jour, float montant){
        //Si le frais forfaitisé fait parti d'une fiche déjà existante
        if(listeFraisMois.containsKey(mois) == true){
            Log.d("Operation","Ajout d'un frais hors-forfait à la liste de maj");
            Log.d("Info","Fiche déjà existante");
            //Si le frais HF est déjà existant
            if(listeFraisMois.get(mois).getLesFraisHf().containsKey(id)){
                Log.d("Info", "Frais Hors-forfait déjà existant => maj du frais => Modification dans la base");

                //On récupère le frais hors-forfait
                FraisHf unFraisMaj = listeFraisMois.get(mois).getLesFraisHf().get(id);
                Log.d("Opération", "Récupération de la fiche du mois"+mois);

                //On ajoute la fiche de frais à la liste de maj
                listeFraisMoisMaj.put(mois,listeFraisMois.get(mois));
                Log.d("Opération", "Ajout de la fiche à la table de mise à jour");

                //On ajoute le frais Hors-forfait à la liste du mois
                listeFraisMoisMaj.get(id).getLesFraisHf().put(id,unFraisMaj);
                Log.d("Opération", "Ajout du frais Hors-forfait à la fiche présente dans la table de mise à jour");
            }else{
                //On incrémente l'indice maximum de frais hors-forfait
                this.maxIndiceFraisHF++;

                //Affichage dans la console
                Log.d("Info", "Frais Hors-forfait non existant => création du frais => Clause INSERT dans la base");

                //On récupère le frais hors-forfait
                FraisHf unFraisMaj = listeFraisMois.get(mois).getLesFraisHf().get(id);
                Log.d("Opération", "Récupération de la fiche du mois"+mois);

                //On ajoute la fiche de frais à la liste de maj
                listeFraisMoisMaj.put(mois,listeFraisMois.get(mois));

                //On ajoute la fiche de frais à la liste de maj
                listeFraisMoisMaj.get(mois).addFraisHf(montant,libelle,jour,this.maxIndiceFraisHF,listeFraisMoisMaj.get(mois).getLesFraisHf().size()+1);
                Log.d("Opération", "Ajout du frais hors-forfait à la table de mise à jour");
                listeFraisMoisMaj.get(mois).getLesFraisHf().get(listeFraisMoisMaj.get(mois).getLesFraisHf().get(this.maxIndiceFraisHF)).setModified("CREE");
            }
            //Sinon
        }else{
            Log.d("Operation","Ajout d'un frais hors-forfait à la liste de maj");
            Log.d("Info","Fiche non existante => Création de la fiche");
            //On transforme l'integer du mois en string
            String moisString = mois+"";

            //On extrait le mois et l'année
            int moisInt = Integer.parseInt(moisString.substring(4));
            int anneeInt = Integer.parseInt(moisString.substring(0,3));

            //On crée une fiche de mois pour l'incorporer à la liste des données
            listeFraisMoisMaj.put(mois, new FraisMois(moisInt,anneeInt));
            listeFraisMoisMaj.get(mois).setModifType("CREE");
            //On incrémente l'indice maximum de frais hors-forfait
            this.maxIndiceFraisHF++;

            //Affichage dans la console
            Log.d("Info", "Frais Hors-forfait non existant => création du frais => Clause INSERT dans la base");

            //On récupère le frais hors-forfait
            FraisHf unFraisMaj = listeFraisMois.get(mois).getLesFraisHf().get(id);
            Log.d("Opération", "Récupération de la fiche du mois"+mois);

            //On ajoute la fiche de frais à la liste de maj
            listeFraisMoisMaj.put(mois,listeFraisMois.get(mois));

            //On ajoute la fiche de frais à la liste de maj
            listeFraisMoisMaj.get(mois).addFraisHf(montant,libelle,jour,this.maxIndiceFraisHF,listeFraisMoisMaj.get(mois).getLesFraisHf().size()+1);
            Log.d("Opération", "Ajout de la fiche à la table de mise à jour");
            Log.d("Opération", "Ajout du frais Hors-forfait à la table de mise à jour");
            listeFraisMoisMaj.get(mois).setModifType("CREE");
        }
    }

    /**
     * Gestion de la mise à jour des frais forfaitises pour l'opération de mise à jour mysql
     * @param mois le mois du frais
     * @param libelle le libelle du frais (id)
     * @param quantite la nouvelle quantite
     */
    public static void updateUpdateFraisForfaitTable(int mois, String libelle, int quantite){
        //Si le frais forfaitisé fait parti d'une fiche déjà existante
        if(listeFraisMois.containsKey(mois) == true){
            Log.d("Operation","Ajout d'un frais forfaitisé à la liste de maj");
            Log.d("Info","Fiche déjà existante");
            Log.d("Info","Mise à jour de la fiche du mois "+mois+".");
            //On met à jour le frais forfaitisé
            switch(libelle){
                //Kilométrage
                case "KM":
                    Log.d("Info","Element de maj : kilométrage");
                    listeFraisMois.get(mois).setEtp(quantite);
                    break;

                //Forfait Etape
                case "ETP":
                    Log.d("Info","Element de maj : forfait étape");
                    listeFraisMois.get(mois).setEtape(quantite);
                    break;

                //Nuitée
                case "NUI":
                    Log.d("Info","Element de maj : nuitée");
                    listeFraisMois.get(mois).setNuitee(quantite);
                    break;

                //Repas
                case "REP":
                    Log.d("Info","Element de maj : repas de midi");
                    listeFraisMois.get(mois).setRepas(quantite);
                    break;
            }
            listeFraisMoisMaj.put(mois,listeFraisMois.get(mois));
            listeFraisMoisMaj.get(mois).setModifType("MODIFIE");
            //Sinon
        }else{
            Log.d("Operation","Ajout d'un frais forfaitisé à la liste de maj");
            Log.d("Info","Fiche inexistante => Création de la fiche");
            //On transforme l'integer du mois en string
            String moisString = mois+"";

            //On extrait le mois et l'année
            int moisInt = Integer.parseInt(moisString.substring(4));
            int anneeInt = Integer.parseInt(moisString.substring(0,3));

            //On crée une fiche de mois pour l'incorporer à la liste des données
            listeFraisMoisMaj.put(mois, new FraisMois(moisInt,anneeInt));
            listeFraisMoisMaj.get(mois).setModifType("CREE");
            switch(libelle){
                case "KM":
                    Log.d("Info","Element de maj : kilométrage");
                    listeFraisMois.get(mois).setEtp(quantite);
                    break;


                case "ETP":
                    Log.d("Info","Element de maj : forfait étape");
                    listeFraisMois.get(mois).setEtape(quantite);
                    break;


                case "NUI":
                    Log.d("Info","Element de maj : nuitée");
                    listeFraisMois.get(mois).setNuitee(quantite);
                    break;


                case "REP":
                    Log.d("Info","Element de maj : repas de midi");
                    listeFraisMois.get(mois).setRepas(quantite);
                    break;
            }
            //On ajoute le frais créé à la liste de mois
            listeFraisMois.put(mois,listeFraisMoisMaj.get(mois));
        }
    }

    /**
     * Opération pour la mise à jour mysql
     * @param updateTableHashTable la table de mise à jour
     */
    public static void UpdateFrais(Hashtable<Integer, FraisMois> updateTableHashTable){
        List<FraisMois> listeMaj = new ArrayList<>();
        for(int i = 0;i<updateTableHashTable.size();i++){
            listeMaj.add(updateTableHashTable.get(i));
        }
        JSONArray updateTableJSONArray = new JSONArray(listeMaj);
        accesDistant.envoi("majFrais",updateTableJSONArray);
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

    public static void estDefini(){
        System.out.println("Est défini!");
    }
}
