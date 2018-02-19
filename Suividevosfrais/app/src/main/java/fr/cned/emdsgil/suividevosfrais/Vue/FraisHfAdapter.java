package fr.cned.emdsgil.suividevosfrais.Vue;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;

import fr.cned.emdsgil.suividevosfrais.Controleur.Global;
import fr.cned.emdsgil.suividevosfrais.Modele.FraisHf;
import fr.cned.emdsgil.suividevosfrais.R;

public class FraisHfAdapter extends BaseAdapter {

	private final Hashtable<Integer, FraisHf> lesFrais ; // liste des frais du mois
	private final LayoutInflater inflater ;

    /**
	 * Constructeur de l'adapter pour valoriser les propriétés
     * @param context Accès au contexte de l'application
     * @param lesFrais Liste des frais hors forfait
     */
	public FraisHfAdapter(Context context, Hashtable<Integer, FraisHf> lesFrais) {
		inflater = LayoutInflater.from(context) ;
		this.lesFrais = lesFrais ;
    }
	
	/**
	 * retourne le nombre d'éléments de la listview
	 */
	@Override
	public int getCount() {
		return lesFrais.size() ;
	}

	/**
	 * retourne l'item de la listview à un index précis
	 */
	@Override
	public Object getItem(int index) {
		return lesFrais.get(index) ;
	}

	/**
	 * retourne l'index de l'élément actuel
	 */
	@Override
	public long getItemId(int index) {
		return index;
	}

	/**
	 * structure contenant les éléments d'une ligne
	 */
	private class ViewHolder {
		TextView txtListJour ;
		TextView txtListMontant ;
		TextView txtListMotif ;
		ImageButton imgSuppr;
	}
	
	/**
	 * Affichage dans la liste
	 */
	@Override
	public View getView(final int index, View convertView, ViewGroup parent) {
		Log.d("Index",index+"");
		ViewHolder holder ;
		if (convertView == null) {
			holder = new ViewHolder() ;
			convertView = inflater.inflate(R.layout.layout_liste, parent, false) ;
			holder.txtListJour = convertView.findViewById(R.id.txtListJour);
			holder.txtListMontant = convertView.findViewById(R.id.txtListMontant);
			holder.txtListMotif = convertView.findViewById(R.id.txtListMotif);
			holder.imgSuppr = convertView.findViewById(R.id.cmdSuppHf);
			convertView.setTag(holder) ;
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		holder.txtListJour.setText(String.format(Locale.FRANCE, "%d", lesFrais.get(index).getJour()));
		holder.txtListMontant.setText(String.format(Locale.FRANCE, "%.2f", lesFrais.get(index).getMontant())) ;
		holder.txtListMotif.setText(lesFrais.get(index).getMotif()) ;
		holder.imgSuppr.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// code a exécuter sur le clic d'un bouton suppr
				// récupère l'indice de la ligne concernée
				int position = (Integer)v.getTag() ;
				Global.deleteFraisHorsForfait(index, position);
				// rafraichi la liste visuelle
				notifyDataSetChanged() ;
			}
		}) ;
		return convertView ;
	}
	
}
