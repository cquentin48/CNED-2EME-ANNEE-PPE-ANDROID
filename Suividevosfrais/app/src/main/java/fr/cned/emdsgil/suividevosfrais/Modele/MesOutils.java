package fr.cned.emdsgil.suividevosfrais.Modele;

import java.util.ArrayList;

/**
 * Created by Caesar01 on 12/01/2018.
 */

public class MesOutils {

    /**
     * Retourne la liste des frais hors-forfait du modifiés pour la maj MYSQL
     * @param listeFraisHorsForfait la liste des frais hors-forfait des mois
     * @return la liste modifiée nécessitant une maj
     */
    public ArrayList<FraisHf> extractFraisHorsForfaitModifies(ArrayList<FraisHf> listeFraisHorsForfait){
        ArrayList<FraisHf> listeFraisHorsForfaitModifies = new ArrayList<>();
        for (FraisHf leFrais: listeFraisHorsForfait)
        {
            //En cas de modifications
            if(leFrais.isModified() == true){
                listeFraisHorsForfaitModifies.add(leFrais);
            }
        }
        return listeFraisHorsForfaitModifies;
    }

    /**
     * Retourne la liste des frais du modifiés pour la maj MYSQL
     * @param listeFraisForfait la liste des frais des mois
     * @return la liste modifiée nécessitant une maj
     */
    public ArrayList<FraisMois> extractFraisModifies(ArrayList<FraisMois> listeFraisForfait){
        ArrayList<FraisMois> listeFraisModifies = new ArrayList<>();
        for (FraisMois leFrais: listeFraisForfait)
        {
            //En cas de modifications
            if(leFrais.isModified() == true){
                listeFraisModifies.add(leFrais);
            }
        }
        return listeFraisModifies;
    }
}
