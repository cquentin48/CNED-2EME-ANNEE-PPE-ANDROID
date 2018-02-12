package fr.cned.emdsgil.suividevosfrais.Modele;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import fr.cned.emdsgil.suividevosfrais.Controleur.Global;
import fr.cned.emdsgil.suividevosfrais.Outils.AccesHTTP;
import fr.cned.emdsgil.suividevosfrais.Outils.AsyncResponse;
import fr.cned.emdsgil.suividevosfrais.Outils.Outils;

/**
 * Created by emds on 12/01/2017.
 */

public class AccesDistant implements AsyncResponse {

    // constante
    private static final String SERVERADDR = "http://192.168.56.1/android-suivifrais/mysqlHandling.php";
    private Global controle ;

    /**
     * Constructeur
     */
    public AccesDistant(){
        controle = Global.getInstance(null);
    }


    /**
     * Traitement des informations qui viennent du serveur distant
     * @param output le retour de l'opération PHP par le serveur SQL
     */
    @Override
    public void processFinish(String output) {
        // contenu du retour du serveur, pour contrôle dans la console
        Log.d("serveur", "************" + output+"\n");

        // découpage du message reçu
        String[] message = output.split("%");

        // contrôle si le serveur a retourné une information
        if(message.length>1){
            if(message[1].equals("connection")){
                // retour suite à un enregistrement distant d'un profil
                Log.d("retour", "************enreg="+message[1]);
                if (message[2].equals("succes")) {
                    try {
                        //Objet JSON pour le retour de l'opération SQL de connection
                        JSONObject returnArrayInfo = new JSONObject(message[4]);

                        //Déclaration et initialisation des variables pour le compte
                        String username = returnArrayInfo.getString("login");
                        Boolean comptable = (returnArrayInfo.getInt("comptable")==1)?true:false;
                        String id = returnArrayInfo.getString("id");
                        Compte account = new Compte(username, comptable, id);

                        //Initialisation du compte dans le contrôleur
                        controle.setCompte(account);
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                }
            }else if(message[1].equals("chargementFrais")){
                // retour suite à la récupération du dernier profil
                Log.d("retour du serveur", "Chargement des fiches frais pour l'utilisateur "+controle.getCompte().getUsername());
                try {
                    //Récupération du tableau des fiches de frais
                    JSONArray fichesMoisJSONArray = new JSONArray(message[4]);

                    //Variables métier
                    Hashtable<Integer, FraisMois> listeFicheFrais = new Hashtable<>();

                    //Récupération des informations de la fiche
                    for(int i = 0;i<fichesMoisJSONArray.length();i++){

                        //Déclaration des variables pour l'initialisations des fiches de frais
                            //Info Fiche => Mois
                        JSONObject ficheMoisJSONObject = new JSONObject(fichesMoisJSONArray.get(0)+"");
                        System.out.println(ficheMoisJSONObject.toString());
                        JSONArray ficheMoisJSONArray = new JSONArray(ficheMoisJSONObject.toString());
                        Log.d("Fiche Mois Array", ficheMoisJSONArray.toString());
                        /*JSONObject infoFiche = new JSONObject(fichesMoisJSONArray.get(0)+"");
                        JSONArray infoFicheMoisJSONArray = new JSONArray(infoFiche.toString());
                        Log.d("Info fiche Mois",infoFicheMoisJSONArray.toString());
                            //fraisForfaitisesJSONArray => Liste des frais Forfaitisés
                        JSONArray fraisForfaitisesJSONArray = new JSONArray(fichesMoisJSONArray.get(1)+"");
                        //Log.d("Frais forfaitisés",fraisForfaitisesJSONArray.toString());
                            //fraisHorsForfaitsJSONArray => Liste des frais Hors-Forfait
                        JSONArray fraisHorsForfaitsJSONArray = new JSONArray(fichesMoisJSONArray.get(2)+"");
                        //Log.d("Frais Hors-Forfait",fraisHorsForfaitsJSONArray.toString());

                        //Variables métiers
                            //Objet métier pour la liste des frais du mois
                        FraisMois unMois;

                        //On extrait le mois et on le converti au format digital
                        int mois = Outils.convertMonthToDigital(infoFiche.getString("mois"));
                        int annee = Outils.convertYearToDigital(infoFiche.getString("mois"));

                        //Initialisation du mois
                        unMois = new FraisMois(annee, mois);

                        //Parcours de la table JSON des frais Forfaitisés
                        for(int j = 0;j<fraisForfaitisesJSONArray.length();j++){
                            //Déclaration de l'objet JSON
                            JSONObject unFraisForfaitisee = new JSONObject(fraisForfaitisesJSONArray.get(j)+"");

                            //Déclaration des entités du frais forfaitisé : libellé et quantité
                            String libelle = unFraisForfaitisee.getString("idfraisforfait");
                            int quantite = unFraisForfaitisee.getInt("quantite");

                            switch(libelle){
                                //Etape
                                case "ETP":
                                    unMois.setEtape(quantite);
                                    break;
                                //Kilométrage
                                case "KM":
                                    unMois.setEtp(quantite);
                                    break;
                                //Nuitée
                                case "NUI":
                                    unMois.setNuitee(quantite);
                                    break;
                                //Repas
                                case "REP":
                                    unMois.setRepas(quantite);
                                    break;
                            }

                        }

                        //Parcours de la table JSON pour implémenter les frais hors-forfait
                        for(int j = 0;j<fichesMoisJSONArray.length();j++){
                            //Création de l'objet JSON pour la récupération du frais Hors-Forfait à l'indice j
                            JSONObject unFraisJSON = new JSONObject(fraisHorsForfaitsJSONArray.get(j)+"");

                            //Récupération des données de l'objet JSON se situant à l'indice j
                            String libelle = unFraisJSON.getString("libelle");
                            float montant = unFraisJSON.getLong("montant");
                            int jour = unFraisJSON.getInt("jour");
                            int key = unFraisJSON.getInt("id");

                            //Ajout du frais dans la liste
                            listeFicheFrais.get(i).addFraisHf(montant,libelle,jour,j);
                        }
*/
                    }

                    //On met à jour la liste des fiches de frais
                    controle.setListeFraisMois(listeFicheFrais);

                  //En cas de problème
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Cas d'opération de mise à jour d'insertion ou de suppression de frais forfaitisés/hors-forfait
            }else if(message[0].equals("deleteFraisHF") || message[0].equals("mySQLDeleteFraisForfaitisee") || message[0].equals("mySQLSetFraisForfaitisee")){
                for(int i = 2; i<message.length; i++){
                    Log.d("Opération Serveur MYSQL",message[i]);
                }
            }

            else if(message[0].equals("Erreur !")){
                // retour suite à une erreur
                Log.d("retour", "************erreur="+message[1]);
            }
        }
    }

    /**
     * Envoi d'informations vers le serveur distant
     * @param operation
     * @param lesDonneesJSON
     */
    public void envoi(String operation, JSONArray lesDonneesJSON){
        AccesHTTP accesDonnees = new AccesHTTP();
        // permet de faire le lien asynchrone avec AccesHTTP
        accesDonnees.delegate = this;
        // paramètres POST pour l'envoi vers le serveur distant
        accesDonnees.addParam("operation", operation);
        accesDonnees.addParam("lesdonnees", lesDonneesJSON.toString());
        // appel du serveur
        accesDonnees.execute(SERVERADDR);
    }
}