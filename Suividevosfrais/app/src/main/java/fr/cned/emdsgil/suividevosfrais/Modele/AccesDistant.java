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
 * Updated by Quentin CHAPEL
 */

public class AccesDistant implements AsyncResponse {

    // constantes
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
                Log.d("Retour du tableau JSON", message[1]);
                if (message[2].equals("succes")) {
                    try {
                        //Affichage dans la console
                        Log.d("Opération", message[1]);



                        //Objet JSON pour le retour de l'opération SQL de connection
                        JSONObject returnArrayInfo = new JSONObject(message[4]);

                        //Déclaration et initialisation des variables pour le compte
                        String username = returnArrayInfo.getString("login");
                        Boolean comptable = (returnArrayInfo.getInt("comptable")==1)?true:false;
                        String id = returnArrayInfo.getString("id");
                        Compte account = new Compte(username, comptable, id);

                        //Initialisation du compte dans le contrôleur
                        if(controle.getCompte() == null) {
                            Log.d("Résultat","Connection réussie!");
                            controle.setCompte(account);
                        }
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
                    FraisMois uneFiche;

                    //Le mois et l'année de la fiche
                    int mois;
                    int annee;

                    //Frais forfaitisés
                    int nui;
                    int km;
                    int etp;
                    int rep;

                    //Définition du frais HF
                    int jour;
                    int id;
                    float montant;
                    String libelle;

                    //Récupération des informations de la fiche
                    for(int i = 0;i<fichesMoisJSONArray.length();i++){

                        //Déclaration des variables pour l'initialisations des fiches de frais
                            //Info Fiche => Mois
                            JSONObject ficheMoisJSONObject = new JSONObject(fichesMoisJSONArray.get(i)+"");
                            Log.d("Fiche mois",ficheMoisJSONObject.toString());

                            //Récupération du mois et de l'année de la fiche

                                //Mois
                                mois = Integer.parseInt(ficheMoisJSONObject.getString("mois").substring(4));
                                //Affichage logcat
                                Log.d("Mois de la fiche",mois+"");

                                //Année
                                annee = Integer.parseInt(ficheMoisJSONObject.getString("mois").substring(0,4));
                                //Affichage logcat
                                Log.d("Annee de la fiche",annee+"");

                        //La clé aura pour valeur : YYYYMM si le mois est supérieur ou égal à 10 || Si le mois est plus inférieur à 10 : YYYY0M
                        String key = String.valueOf(annee)+((mois<10)?0:"")+String.valueOf(mois)+"";

                        //Initialisation de la fiche
                        uneFiche = new FraisMois(annee,mois);
                        uneFiche.setAnnee(annee);
                        uneFiche.setMois(mois);

                        //Affichage logcat
                        Log.d("Initialisation", "Initialistion de la fiche");

                        //Récupération des frais forfaitisés
                            //Nuitée
                            nui = ficheMoisJSONObject.getInt("nui");
                            //Affichage logcat
                            Log.d("Nuitée",nui+"");

                            //Kilométrage
                            km = ficheMoisJSONObject.getInt("km");
                            //Affichage logcat
                            Log.d("Forfait kilométrique",km+"");

                            //Forfait étape
                            etp = ficheMoisJSONObject.getInt("etp");
                            //Affichage logcat
                            Log.d("Nombre d'étapes",etp+"");

                            //Repas
                            rep = ficheMoisJSONObject.getInt("rep");
                            //Affichage logcat
                            Log.d("Repas midi",rep+"");

                        //Ajout des frais forfaitisés dans la fiche
                            //Message introduction logcat
                            Log.d("Ajout des frais", "Ajout des frais forfaitisés dans la fiche du mois");

                            uneFiche.setEtape(etp);
                            uneFiche.setEtp(km);
                            uneFiche.setRepas(rep);
                            uneFiche.setNuitee(nui);

                        //Récupération du nombre de frais hors-forfait
                        int nbFraisHF = ficheMoisJSONObject.getInt("nbFraisHF");
                        //Message introduction logcat
                        Log.d("Nombre de frais HF", ""+nbFraisHF);

                        //Import des frais hors-forfait
                        for(int j = 0; j<nbFraisHF;j++){
                            //Affichage dans la console
                            Log.d("Import","Import des données du frais Hors-forfait n°"+j);

                            //Import des données (jour, libellé, mois, montant, identifiant)
                                //Jour
                                jour = ficheMoisJSONObject.getInt("jour"+j);
                                //Affichage dans la console
                                Log.d("Jour du frais HF",jour+"");

                                //Libellé
                                libelle = ficheMoisJSONObject.getString("libelle"+j);
                                //Affichage dans la console
                                Log.d("Libellé du frais HF",libelle+"");

                                //Montant
                                montant = (float) ficheMoisJSONObject.getDouble("montant"+j);
                                //Affichage dans la console
                                Log.d("Montant du frais HF",montant+"");

                                //Identifiant
                                id = ficheMoisJSONObject.getInt("id"+j);
                                //Affichage dans la console
                                Log.d("Identifiant du frais HF",id+"");

                            //Ajout du frais à la liste
                            uneFiche.addFraisHf(montant,libelle,jour,id,j);
                            //Affichage dans la console
                            Log.d("Ajout","Ajout du frais Hors-forfait n°"+j+" dans la liste des frais hors-forfait pour le frais de "+annee+mois+".");
                        }

                        //Affichage dans la console
                        Log.d("Nombre de frais HF ajouté"+((controle.getListeFraisMois().size()>=2)?"s":"")+" pour le mois "+key,
                              controle.getListeFraisMois().size()+" fiche"+((controle.getListeFraisMois().size()>=2)?"s.":"."));

                        Log.d("Clé mois",Integer.parseInt(key)+"");
                        //Ajout de la fiche à la liste
                        controle.addFicheFrais(Integer.parseInt(key),uneFiche);
                        //Affichage dans la console
                        Log.d("Ajout","Ajout du frais du mois "+key+" à la liste.");
                        controle.setLoadedData(true);
                    }

                  //En cas de problème
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("Nombre de fiches ajoutée"+((controle.getListeFraisMois().size()>=2)?"s":""), controle.getListeFraisMois().size()+" fiche"+((controle.getListeFraisMois().size()>=2)?"s ont été ajoutées.":"a été ajoutée."));
                //Cas d'opération de mise à jour d'insertion ou de suppression de frais forfaitisés/hors-forfait
            }else if(message[1].equals("majFrais")){
                for(int i = 3; i<message.length; i++){
                    //Affichage des messages
                    //Messages d'erreur
                    if(message[i].contains("Erreur : ")){
                        Log.e("Erreur : ",message[i].replace("Erreur :", ""));
                    }

                    //Message d'avertissements
                    else if(message[i].contains("Avertissement : ")){
                        Log.w("Avertissement", message[i].replace("Avertissement : ",""));
                    }

                    //Message d'informations
                    else if(message[i].contains("Information : ")){
                        Log.d("Information", message[i].replace("Information : ", ""));
                    }

                    //Requête SQL
                    else if(message[i].contains("SQL : ")){
                        Log.d("Requête SQL", message[i].replace("SQL : ",""));
                    }
                }
            }

            else if(message[1].equals("Erreur !")){
                // retour suite à une erreur
                Log.e("Erreur", ""+message[1]);
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
        if(operation == "majFrais"){
            accesDonnees.addParam("userId",controle.getCompte().getUserId());
        }
        // paramètres POST pour l'envoi vers le serveur distant
        accesDonnees.addParam("operation", operation);
        accesDonnees.addParam("lesdonnees", lesDonneesJSON.toString());
        // appel du serveur
        accesDonnees.execute(SERVERADDR);
    }
}