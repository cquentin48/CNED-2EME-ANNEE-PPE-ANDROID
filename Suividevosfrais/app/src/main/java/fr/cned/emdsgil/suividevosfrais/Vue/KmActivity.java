package fr.cned.emdsgil.suividevosfrais.Vue;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.DatePicker.OnDateChangedListener;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import fr.cned.emdsgil.suividevosfrais.Controleur.Global;
import fr.cned.emdsgil.suividevosfrais.Modele.FraisMois;
import fr.cned.emdsgil.suividevosfrais.Outils.Serializer;
import fr.cned.emdsgil.suividevosfrais.R;

public class KmActivity extends AppCompatActivity {

    // informations affichées dans l'activité
    private Integer annee ;
    private Integer mois ;
    private Integer qte ;

    private Global controle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        controle.getInstance(this);
        setContentView(R.layout.activity_km);
        setTitle("GSB : Frais Km");
        // modification de l'affichage du DatePicker
        Global.changeAfficheDate((DatePicker) findViewById(R.id.datKm), false) ;
        // valorisation des propriétés
        valoriseProprietes() ;
        // chargement des méthodes événementielles
        imgReturn_clic() ;
        cmdValider_clic() ;
        cmdPlus_clic() ;
        cmdMoins_clic() ;
        dat_clic() ;
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
     * Valorisation des propriétés avec les informations affichées
     */
    private void valoriseProprietes() {
        annee = ((DatePicker)findViewById(R.id.datKm)).getYear() ;
        mois = ((DatePicker)findViewById(R.id.datKm)).getMonth() + 1 ;
        // récupération de la qte correspondant au mois actuel
        qte = 0 ;
        String keyText = String.valueOf(annee)+((mois<10)?0:"")+String.valueOf(mois)+"";
        int key = Integer.parseInt(keyText);
        if (Global.getListeFraisMois().containsKey(key)) {
            qte = Global.getListeFraisMois().get(key).getEtape() ;
            Global.getListeFraisMois().get(key).setModifType("CREE");
        }else if(!(Global.getListeFraisMois().get(key).isModified() == "CREE")){
            Global.getListeFraisMois().get(key).setModifType("MODIFIE");
        }
        ((TextView)findViewById(R.id.txtKm)).setText(String.format(Locale.FRANCE, "%d", qte)) ;
        TextView txtKm = (TextView)findViewById(R.id.txtKm);
        txtKm.invalidate();
    }

    /**
     * Sur la selection de l'image : retour au menu principal
     */
    private void imgReturn_clic() {
        findViewById(R.id.imgKmReturn).setOnClickListener(new ImageView.OnClickListener() {
            public void onClick(View v) {
                retourActivityPrincipale() ;
            }
        }) ;
    }

    /**
     * Sur le clic du bouton valider : sérialisation
     */
    private void cmdValider_clic() {
        findViewById(R.id.cmdKmValider).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Serializer.serialize(Global.getListeFraisMois(), KmActivity.this) ;
                retourActivityPrincipale() ;
            }
        }) ;
    }

    /**
     * Sur le clic du bouton plus : ajout de 10 dans la quantité
     */
    private void cmdPlus_clic() {
        findViewById(R.id.cmdKmPlus).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                qte+=10 ;
                enregNewQte() ;
            }
        }) ;
    }

    /**
     * Sur le clic du bouton moins : enlève 10 dans la quantité si c'est possible
     */
    private void cmdMoins_clic() {
        findViewById(R.id.cmdKmMoins).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                qte = Math.max(0, qte-10) ; // suppression de 10 si possible
                enregNewQte() ;
            }
        }) ;
    }

    /**
     * Sur le changement de date : mise à jour de l'affichage de la qte
     */
    private void dat_clic() {
        final DatePicker uneDate = (DatePicker) findViewById(R.id.datKm);
        uneDate.init(uneDate.getYear(), uneDate.getMonth(), uneDate.getDayOfMonth(), new OnDateChangedListener(){
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                valoriseProprietes() ;
            }
        });
    }

    /**
     * Enregistrement dans la zone de texte et dans la liste de la nouvelle qte, à la date choisie
     */
    private void enregNewQte() {
        // enregistrement dans la zone de texte
        ((TextView)findViewById(R.id.txtKm)).setText(String.format(Locale.FRANCE, "%d", qte)) ;
        // enregistrement dans la liste
        String keyText = String.valueOf(annee)+((mois<10)?0:"")+String.valueOf(mois)+"";
        int key = Integer.parseInt(keyText);
        if (!Global.getListeFraisMois().containsKey(key)) {
            // creation du mois et de l'annee s'ils n'existent pas déjà
            Global.getListeFraisMois().put(key, new FraisMois(annee, mois)) ;
        }
        Global.getListeFraisMois().get(key).setEtape(qte);
        controle.updateUpdateFraisForfaitTable(key,"KM",qte);
    }

    /**
     * Retour à l'activité principale (le menu)
     */
    private void retourActivityPrincipale() {
        Intent intent = new Intent(KmActivity.this, MainActivity.class) ;
        startActivity(intent) ;
    }
}
