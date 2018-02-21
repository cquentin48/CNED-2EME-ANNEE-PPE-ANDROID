package fr.cned.emdsgil.suividevosfrais.Vue;

import android.os.Bundle;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import fr.cned.emdsgil.suividevosfrais.Controleur.Global;
import fr.cned.emdsgil.suividevosfrais.Modele.FraisMois;
import fr.cned.emdsgil.suividevosfrais.Outils.Serializer;
import fr.cned.emdsgil.suividevosfrais.R;

public class HfActivity extends AppCompatActivity {
	private Global controle;
	private Button ajouterButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hf);
        setTitle("GSB : Frais HF");
        ajouterButton = (Button)findViewById(R.id.cmdHfAjouter);
		controle.getInstance(this);
        // modification de l'affichage du DatePicker
        Global.changeAfficheDate((DatePicker) findViewById(R.id.datHf), true) ;
		// mise à 0 du montant
		((EditText)findViewById(R.id.txtHf)).setText("0") ;
        // chargement des méthodes événementielles
		imgReturn_clic() ;
		cmdAjouter_clic() ;
		dat_clic();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_actions, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals(getString(R.string.retour_accueil))) {
            retourActivityPrincipale() ;
        }
        return super.onOptionsItemSelected(item);
    }

	/**
	 * Sur la selection de l'image : retour au menu principal
	 */
    private void imgReturn_clic() {
    	findViewById(R.id.imgHfReturn).setOnClickListener(new ImageView.OnClickListener() {
    		public void onClick(View v) {
    			retourActivityPrincipale() ;    		
    		}
    	}) ;
    }

	/**
	 * Sur le changement de date : mise à jour de l'affichage de la qte
	 */
	private void dat_clic() {
		final DatePicker uneDate = (DatePicker) findViewById(R.id.datHf);
		uneDate.init(uneDate.getYear(), uneDate.getMonth(), uneDate.getDayOfMonth(), new DatePicker.OnDateChangedListener(){
			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				valoriserPropriete() ;
			}
		});
	}

    /**
     * Sur le clic du bouton ajouter : enregistrement dans la liste et sérialisation
     */
    private void cmdAjouter_clic() {
    	findViewById(R.id.cmdHfAjouter).setOnClickListener(new Button.OnClickListener() {
    		public void onClick(View v) {

    			//Récupération des données en provenance de la fenêtre
				Integer annee = ((DatePicker)findViewById(R.id.datHf)).getYear() ;
				Integer mois = ((DatePicker)findViewById(R.id.datHf)).getMonth() + 1 ;
				Integer jour = ((DatePicker)findViewById(R.id.datHf)).getDayOfMonth() ;

				//Récupération des index de la fiche de frais et du frais hf (valeur par défault)
				String keyText = String.valueOf(annee)+((mois<10)?0:"")+String.valueOf(mois)+"";
				int key = Integer.parseInt(keyText);
				int id = Global.getMaxIndiceFraisHF()+1;

				//Affichage logcat
				Log.d("Frais HF","Chargement du frais du mois"+key);

				//Récupération du montant et du libellé entrés dans la fenêtre
				String montantEntree = (((EditText)findViewById(R.id.txtHf)).getText().toString());
				EditText libelleEditText = (EditText)findViewById(R.id.txtHfMotif);

				//Conversion du montant au format float sous la forme suivante : entier+montant sous la virgule/100
				Float montant = Float.valueOf(montantEntree.substring(0,montantEntree.length()-3))
						+(Float.valueOf(montantEntree.substring(montantEntree.length()-1))/100);


				//enregListe() ;
    			//Serializer.serialize(Global.getListeFraisMois(), HfActivity.this) ;

				//Parcours de la table des frais HF de la fiche de frais
    			for(int i = 0;i<Global.getListeFraisMois().get(key).getLesFraisHf().size();i++){
    				//Si le frais HF est déjà existant
    				if(Global.getListeFraisMois().get(key).getLesFraisHf().get(i).getJour() == jour){
    					id = i;
					}
				}

				//Récupération du motif
				String motif = libelleEditText.getText().toString() ;

				//Ajout dans la table
				Global.updateUpdateFraisHorsForfaitTable(id, mois, annee, motif, jour, montant);

				//Retour au menu principal
				retourActivityPrincipale() ;
			}
		}) ;
    }

	private void valoriserPropriete(){
		//Propriété pour la fenêtre
		int annee, mois,jour;
		float qte;
		String libelle;

		//Récupération de la date
		annee = ((DatePicker)findViewById(R.id.datHf)).getYear() ;
		mois = ((DatePicker)findViewById(R.id.datHf)).getMonth() + 1 ;
		jour = ((DatePicker)findViewById(R.id.datHf)).getDayOfMonth();

		// récupération de la qte correspondant au mois actuel
		qte = 0 ;
		libelle = "";

		//Récupération de la clé
		String keyText = String.valueOf(annee)+((mois<10)?0:"")+String.valueOf(mois)+"";
		int key = Integer.parseInt(keyText);
		Log.d("Clé",key+"");
		if(controle.getListeFraisMois().containsKey(201710)){
			Log.d("Mois", "Mois présent");
		}

		//S'il existe une fiche de frais du mois
		if (controle.getListeFraisMois().containsKey(key)) {
			Log.d("Opération","Bon Mois");
			//Récupération de la fiche
			FraisMois uneFiche = Global.getListeFraisMois().get(key);

			//Parcours de la fiche
			for(int i = 0;i<uneFiche.getLesFraisHf().size();i++){
				//Si le frais HF existe pour ce jour-ci
				Log.d("variable i",i+"");
				if(uneFiche.getLesFraisHf().get(i).getJour() == jour){
					qte = uneFiche.getLesFraisHf().get(i).getMontant();
					libelle = uneFiche.getLesFraisHf().get(i).getMotif();
					if(!(ajouterButton.getText().toString() == "Modifier")){
						ajouterButton.setText("Modifier");
					}
				}else{
					if(!(ajouterButton.getText().toString() == "Ajouter")){
						ajouterButton.setText("Ajouter");
					}
				}
			}
		}

		//Mise à jour des libellé des frais hors-forfait dans la fenêtre
		((TextView)findViewById(R.id.txtHf)).setText(String.format(Locale.FRANCE, "%.2f", qte)) ;
		((TextView)findViewById(R.id.txtHfMotif)).setText(libelle) ;
	}

	/**
	 * Retour à l'activité principale (le menu)
	 */
	private void retourActivityPrincipale() {
		Intent intent = new Intent(HfActivity.this, MainActivity.class) ;
		startActivity(intent) ;   					
	}
}
