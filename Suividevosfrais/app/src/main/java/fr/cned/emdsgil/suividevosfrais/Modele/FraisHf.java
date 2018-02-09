package fr.cned.emdsgil.suividevosfrais.Modele;

import java.io.Serializable;

/**
 * Classe métier contenant la description d'un frais hors forfait
 *
 */
public class FraisHf  implements Serializable {

	private final Float montant ;
	private final String motif ;
	private final Integer jour ;
	private boolean modified;
	
	public FraisHf(Float montant, String motif, Integer jour) {
		this.montant = montant ;
		this.motif = motif ;
		this.jour = jour ;
		this.modified = false;
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

	public boolean isModified() { return modified; }
}
