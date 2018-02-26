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
import java.util.Map;

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

    /**
     * Constructeur par défault
     */
    private Global(){
        super();
    }

    /**
     * Création de l'instance Global
     * @param contexte le contexte de la fenêtre
     * @return l'instance
     */
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
     * Mise à jour/Insertion du frais Hors-Forfait dans l'index de mise à jour des données pour l'envoi vers la base de données MYSQL
     * @param id l'identifiant du frais
     * @param mois le mois du frais
     * @param libelle la description du frais
     * @param montant le montant du frais
     */
    public static void updateUpdateFraisHorsForfaitTable(int id, int mois, int annee, String libelle, int jour, float montant){

        //Création de la clé de parcours à partir du mois et de l'année sous la forme : YYYYMM
        String key = String.valueOf(annee)+((mois<10)?0:"")+String.valueOf(mois)+"";
        //Passage au format entier
        int ficheMoisKey = Integer.parseInt(key);
        Global.getListeFraisMois().get(ficheMoisKey).setModifType("MODIFIE");

        //Si le frais forfaitisé fait parti d'une fiche déjà existante
        Log.d("Index fiche mois",ficheMoisKey+"");
        if(listeFraisMois.containsKey(ficheMoisKey) == true){
            Log.d("Operation","Ajout d'un frais hors-forfait à la liste de maj");
            Log.d("Info","Fiche déjà existante");
            //Si le frais HF est déjà existant
            if(listeFraisMois.get(ficheMoisKey).getLesFraisHf().containsKey(id)){
                Log.d("Info", "Frais Hors-forfait déjà existant => maj du frais => Modification dans la base");

                //On récupère le frais hors-forfait
                FraisHf unFraisMaj = listeFraisMois.get(ficheMoisKey).getLesFraisHf().get(id);
                Log.d("Opération", "Récupération de la fiche du mois"+mois);
                Log.d("Opération", "Index de la fiche de mois"+ficheMoisKey);

                //Maj des libellés
                unFraisMaj.setMontant(montant);
                unFraisMaj.setMotif(libelle);
                unFraisMaj.setModified("MODIFIE");

                //On ajoute la fiche de frais à la liste de maj
                listeFraisMoisMaj.put(ficheMoisKey,listeFraisMois.get(ficheMoisKey));
                Log.d("Opération", "Ajout de la fiche à la table de mise à jour");

                //On ajoute le frais Hors-forfait à la liste du mois
                listeFraisMoisMaj.get(ficheMoisKey).getLesFraisHf().put(id,unFraisMaj);
                Log.d("Opération", "Ajout du frais Hors-forfait à la fiche présente dans la table de mise à jour");
            }else{
                //On incrémente l'indice maximum de frais hors-forfait
                maxIndiceFraisHF++;

                //Affichage dans la console
                Log.d("Info", "Frais Hors-forfait non existant => création du frais => Clause INSERT dans la base");

                //On récupère le frais hors-forfait
                FraisHf unFraisMaj = listeFraisMois.get(ficheMoisKey).getLesFraisHf().get(id);
                Log.d("Opération", "Récupération de la fiche du mois"+mois);

                //On ajoute la fiche de frais à la liste de maj
                listeFraisMoisMaj.put(ficheMoisKey,listeFraisMois.get(mois));

                //On ajoute la fiche de frais à la liste de maj
                listeFraisMoisMaj.get(ficheMoisKey).addFraisHf(montant,libelle,jour,maxIndiceFraisHF,listeFraisMoisMaj.get(mois).getLesFraisHf().size()+1);
                Log.d("Opération", "Ajout du frais hors-forfait à la table de mise à jour");
                listeFraisMoisMaj.get(ficheMoisKey).getLesFraisHf().get(listeFraisMoisMaj.get(mois).getLesFraisHf().get(maxIndiceFraisHF)).setModified("CREE");
            }
            //Sinon
        }else{
            Log.d("Operation","Ajout d'un frais hors-forfait à la liste de maj");
            Log.d("Info","Fiche non existante => Création de la fiche");
            //On transforme l'integer du mois en string

            //On crée une fiche de mois pour l'incorporer à la liste des données
            listeFraisMoisMaj.put(ficheMoisKey, new FraisMois(mois,annee));
            listeFraisMoisMaj.get(ficheMoisKey).setModifType("CREE");
            //On incrémente l'indice maximum de frais hors-forfait
            maxIndiceFraisHF++;

            //Affichage dans la console
            Log.d("Info", "Frais Hors-forfait non existant => création du frais => Clause INSERT dans la base");
            Log.d("Opération", "Récupération de la fiche du mois"+mois);

            //On ajoute la fiche de frais à la liste de maj
            listeFraisMoisMaj.put(ficheMoisKey,listeFraisMois.get(ficheMoisKey));

            //On ajoute la fiche de frais à la liste de maj
            listeFraisMoisMaj.get(ficheMoisKey).addFraisHf(montant,libelle,jour,maxIndiceFraisHF,listeFraisMoisMaj.get(ficheMoisKey).getLesFraisHf().size()+1);
            Log.d("Opération", "Ajout de la fiche à la table de mise à jour");
            Log.d("Opération", "Ajout du frais Hors-forfait à la table de mise à jour");
            listeFraisMoisMaj.get(ficheMoisKey).getLesFraisHf().get(ficheMoisKey).setModified("CREE");
        }
        Log.d("Taille table maj",Global.getListeFraisMoisMaj().size()+"");
    }

    /**
     * Gestion de la mise à jour des frais forfaitises pour l'opération de mise à jour mysql
     * @param mois le mois du frais
     * @param libelle le libelle du frais (id)
     * @param quantite la nouvelle quantite
     */
    public static void updateUpdateFraisForfaitTable(int mois, String libelle, int quantite){
            Log.d("Type de modification",listeFraisMois.get(mois).isModified());
            //Récupération de la clé et de la fiche du mois
            FraisMois value = listeFraisMois.get(mois);

            //Récupération du type de modification
            String modifType = value.isModified();

            //Parcours des types de modifications
            switch(modifType){
                case "CREE":
                    //Affichage console
                    Log.d("Operation","Ajout d'un frais forfaitisé à la liste de maj");
                    Log.d("Info","Fiche inexistante dans la base de donnée => Création de la fiche");
                    break;

                case "MODIFIE":

                    //Affichage console
                    Log.d("Operation", "Ajout d'un frais forfaitisé à la liste de maj");
                    Log.d("Info", "Fiche déjà existante");
                    Log.d("Info", "Mise à jour de la fiche du mois " + mois + ".");
                    break;
            }
            //On ajoute le frais créé dans la mise à jour
            listeFraisMoisMaj.put(mois,value);
    }

    /**
     * Opération pour la mise à jour mysql
     * @param updateTableHashTable la table de mise à jour
     */
    public static void UpdateFrais(Hashtable<Integer, FraisMois> updateTableHashTable){
        JSONArray updateTableJSONArray = new JSONArray();
        //Ajout de l'identifiant utilisateur
        updateTableJSONArray.put(Global.getCompte().getUserId());

        //Parcours de la table de mise à jour
        for ( Hashtable.Entry<Integer, FraisMois> entry : updateTableHashTable.entrySet() ) {
            //Instance du tableau JSON
            JSONArray ficheFraisMoisJSONArray = new JSONArray();

            //Récupération de la valeur
            FraisMois value = entry.getValue();

            //Ajout du mois et de l'année au tableau
            ficheFraisMoisJSONArray.put(value.getAnnee());
            ficheFraisMoisJSONArray.put(value.getMois());

            //Ajout des frais forfaitisés au tableau
            ficheFraisMoisJSONArray.put(value.getEtape());
            ficheFraisMoisJSONArray.put(value.getNuitee());
            ficheFraisMoisJSONArray.put(value.getKm());
            ficheFraisMoisJSONArray.put(value.getRepas());

            //Ajout du cas de modification du tableau
            ficheFraisMoisJSONArray.put(value.isModified());
            JSONArray listeFraisHFJSONArray = new JSONArray();

            //Parcours des frais HF du mois
            for(Map.Entry<Integer,FraisHf> entryFraisHF : value.getLesFraisHf().entrySet()){
                //Récupération du frais Hors-Forfait
                FraisHf fraisHfValue = entryFraisHF.getValue();

                //Création du tableau de mise à jour JSONArray
                JSONArray fraisHfJSONArray = new JSONArray();

                //Ajout des éléments montant, motif, clé mysql et jour au tableau de frais hors-forfait du mois JSONArray
                fraisHfJSONArray.put(fraisHfValue.getMontant());
                fraisHfJSONArray.put(fraisHfValue.getMotif());
                fraisHfJSONArray.put(fraisHfValue.getMySQlKey());

                //Ajout du cas de modification du tableau
                fraisHfJSONArray.put(fraisHfValue.isModified());

                //Ajout du tableau de frais hors-forfait du mois key au tableau JSONArray de mise à jour
                listeFraisHFJSONArray.put(fraisHfJSONArray);
            }
            //Ajout du tableau des frais hf du mois à la fiche de frais
            ficheFraisMoisJSONArray.put(listeFraisHFJSONArray);

            //Ajout de la fiche au tableau JSON
            updateTableJSONArray.put(ficheFraisMoisJSONArray);
        }
        Log.d("Opération", "Envoi des données du tableau de mis à jour vers le serveur mysql");
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
