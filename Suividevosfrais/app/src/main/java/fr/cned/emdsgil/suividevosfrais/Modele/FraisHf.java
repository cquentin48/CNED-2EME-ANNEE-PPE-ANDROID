package fr.cned.emdsgil.suividevosfrais.Modele;

import java.io.Serializable;

/**
 * Classe métier contenant la description d'un frais hors forfait
 *
 */
public class FraisHf  implements Serializable {

	private Float montant ;
	private String motif ;
	private Integer jour ;
	private Integer mySQLKey;
	private String modified;
	
	public FraisHf(Float montant, String motif, Integer jour, Integer mySQLKey) {
		this.montant = montant ;
		this.motif = motif ;
		this.jour = jour ;
		this.mySQLKey = mySQLKey;
		this.modified = "";
	}

	public void setModified(String newModif){
		this.modified = newModif;
	}

	public Float getMontant() {
		return montant;
	}

	public String getMotif() {
		return motif;
	}

	public Integer getJour() {
		return jour;
	}

	public void setMontant(Float nveauMontant){this.montant = nveauMontant;}

	public void setMotif(String nveauMotif){this.motif = nveauMotif;}

	public void setMySQLKey(Integer mySQLKey){this.mySQLKey = mySQLKey;}

	public Integer getMySQlKey(){return this.mySQLKey;}

	public String isModified(){return this.modified;}
}
