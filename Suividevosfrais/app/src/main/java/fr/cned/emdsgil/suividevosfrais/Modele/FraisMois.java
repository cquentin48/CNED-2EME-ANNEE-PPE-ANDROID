package fr.cned.emdsgil.suividevosfrais.Modele;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Classe métier contenant les informations des frais d'un mois
 */
public class FraisMois implements Serializable {

    private Integer mois; // mois concerné
    private Integer annee; // année concernée
    private Integer etape; // nombre d'étapes du mois
    private Integer km; // nombre de km du mois
    private Integer nuitee; // nombre de nuitées du mois
    private Integer repas; // nombre de repas du mois
    private final Hashtable<Integer, FraisHf> lesFraisHf; // liste des frais hors forfait du mois

    public FraisMois(Integer annee, Integer mois) {
        this.annee = annee;
        this.mois = mois;
        this.etape = 0;
        this.km = 0;
        this.nuitee = 0;
        this.repas = 0;
        lesFraisHf = new Hashtable<>();
    }

    /**
     * Ajout d'un frais hors forfait
     *
     * @param montant Montant en euros du frais hors forfait
     * @param motif Justification du frais hors forfait
     */
    public void addFraisHf(Float montant, String motif, Integer jour, Integer key) {
        lesFraisHf.put(key,new FraisHf(montant, motif, jour));
    }

    /**
     * Suppression d'un frais hors forfait
     *
     * @param fraisHfIndex L'index frais hors-forfait à supprimer
     */
    public void supprFraisHf(int fraisHfIndex) {lesFraisHf.remove(fraisHfIndex);}

    public Integer getMois() {
        return mois;
    }

    public void setMois(Integer mois) {
        this.mois = mois;
    }

    public Integer getAnnee() {
        return annee;
    }

    public void setAnnee(Integer annee) {
        this.annee = annee;
    }

    public Integer getEtape() {
        return etape;
    }

    public void setEtape(Integer etape) {
        this.etape = etape;
    }

    public Integer getEtp() {
        return km;
    }

    public void setEtp(Integer km) {
        this.km = km;
    }

    public Integer getNuitee() {
        return nuitee;
    }

    public void setNuitee(Integer nuitee) {
        this.nuitee = nuitee;
    }

    public Integer getRepas() {
        return repas;
    }

    public void setRepas(Integer repas) {
        this.repas = repas;
    }

    public Hashtable<Integer, FraisHf> getLesFraisHf() {
        return lesFraisHf;
    }

    /**
     * Calcul le taux total des frais du mois
     * @return double le montant total des frais du mois
     */
    private double getTauxFinal(){
        double taux = 0;
        taux = this.etape*110+this.km*0.62+this.repas*25.00+this.nuitee*80.00;
        for(int i = 0; i<lesFraisHf.size(); i++){
            taux+=lesFraisHf.get(i).getMontant();
        }
        return taux;
    }
}
