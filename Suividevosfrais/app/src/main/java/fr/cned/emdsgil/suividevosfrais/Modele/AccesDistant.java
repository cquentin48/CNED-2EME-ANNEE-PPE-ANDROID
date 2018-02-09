package fr.cned.emdsgil.suividevosfrais.Modele;

import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.cned.emdsgil.suividevosfrais.Controleur.Global;
import fr.cned.emdsgil.suividevosfrais.Outils.AccesHTTP;
import fr.cned.emdsgil.suividevosfrais.Outils.AsyncResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Caesar01 on 12/01/2018.
 */

public class AccesDistant implements AsyncResponse {
    //Adresse du script de maj mysql
    public final static String SERVADRESS = "http://192.168.1.20/android-suivifrais/mysqlHandling.php";
    public Global global;

    public AccesDistant(){
        global = Global.getInstance(null);
    }

    @Override
    /**
     * Traitement des données en provenance du serveur distant
     * @param output Contenu du retour du serveur
     */
    public HashMap<Object, Object> processFinish(String output){
        Log.d("serveur", output+"\n");
        String[] message = output.split("%");
        HashMap<Object, Object> outputData = new HashMap<>();
        if(message.length>1){
            if(message[1] == "succes"){
                Log.d("Serveur",message[1].toString());
                try{
                    JSONArray connectionReturnArray = new JSONArray(message[2]);
                    switch(message[0]){
                        //Cas de connection
                        case "connection":
                            JSONObject connectionReturn = new JSONObject(""+connectionReturnArray.get(2));
                            String username = connectionReturn.getString("login");
                            String pwd = connectionReturn.getString("mpd");
                            Boolean comptable = connectionReturn.getBoolean("comptable");
                            int id = connectionReturn.getInt("id");
                            Compte account = new Compte(username, pwd, comptable, id);
                            global.setCompte(account);
                        break;

                        case "chargementFrais":
                            HashMap<String, FraisMois> fraisMoisTab = new HashMap<>();
                            JSONArray fraisMoisArray = new JSONArray(""+connectionReturnArray.get(2));

                            for(int i = 0;i<fraisMoisArray.length();i+=4){
                                JSONObject uneFiche = new JSONObject(""+fraisMoisArray.get(0));
                                int annee = Integer.parseInt(uneFiche.getString("mois").substring(0,3));
                                int mois = Integer.parseInt(uneFiche.getString("mois").substring(4,5));
                                FraisMois fraisMois = new FraisMois(annee, mois);
                                int nuitee = new JSONObject(""+fraisMoisArray.get(i)).getInt("quantite");
                                int repas = new JSONObject(""+fraisMoisArray.get(i+1)).getInt("quantite");
                                int km = new JSONObject(""+fraisMoisArray.get(i+2)).getInt("quantite");
                                int etape = new JSONObject(""+fraisMoisArray.get(i+3)).getInt("quantite");
                                fraisMois.setEtape(etape);
                                fraisMois.setNuitee(nuitee);
                                fraisMois.setEtp(km);
                                fraisMois.setRepas(repas);
                                fraisMoisTab.put("fraisMois",fraisMois);
                            }
                        break;
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }else{
                //En cas d'erreur, on l'affiche
                Log.d("Serveur", message[1].toString());
            }
        }
        return outputData;
    }

    public void writeToMysql(HashMap<Integer, Object> updateData, String operationType){
        AccesDistant ecriture = new AccesDistant();
        List liste = new ArrayList();

        for(int i = 0; i<updateData.size(); i++){
            liste.add(i,updateData.get(i));
        }

        JSONArray tab = new JSONArray(liste);
        ecriture.envoi(operationType, tab);
    }

    /**
     * Envoi de données vers le serveur distant
     * @param operation le type d'opération réalisé
     * @param mysqlDonneesJSON les données pour la réalisation de l'opération Mysql
     */
    public void envoi(String operation, JSONArray mysqlDonneesJSON) {
        AccesHTTP accesHTTP = new AccesHTTP();

        //On envoi le type d'opération par la méthode POST
        accesHTTP.addParam("operation", operation);

        //On envoi les données au format JSON dans le format post
        accesHTTP.addParam("donneesMysql", mysqlDonneesJSON.toString());

        //Execution de la requête SQL
        accesHTTP.execute(SERVADRESS);
    }
}
