package fr.cned.emdsgil.suividevosfrais.Vue;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import fr.cned.emdsgil.suividevosfrais.Controleur.Global;
import fr.cned.emdsgil.suividevosfrais.R;

public class ConnexionActivity extends AppCompatActivity {
    private Global controle;
    private static Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContext();
        this.setTitle(this.getTitle()+" - Connexion");
        this.controle = Global.getInstance(this);
        setContentView(R.layout.activity_connexion);
        this.btnConnexion();
    }

    /**
     * Gestion de l'évenement sur le bouton de connection
     */
    private void btnConnexion(){
        Button connection = (Button)findViewById(R.id.connexionButton);
        connection.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //Récupération des identifiants
                String usernameEntered = ((EditText)findViewById(R.id.pseudoEditText)).getText().toString();
                String pwdEntered = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();

                //Si un des champs n'a pas été renseigné
                if(pwdEntered == null || usernameEntered == null){
                    Toast.makeText(mContext, "Avez-vous bien renseigné tous les champs?", Toast.LENGTH_SHORT);
                }else {
                    //On envoie la requête SQL
                    controle.connection(usernameEntered, pwdEntered);

                    //Si la connection est réussie
                    if (controle.getCompte() != null) {
                        //On change de fenêtre
                        changeActivity();
                    }
                }
            }
        }) ;
    }

    /**
     * Changement d'activitée
     */
    public void changeActivity(){
        if(controle.getCompte() != null){
            Log.d("Résultat connection","Connection réussie!");
            Intent intent = new Intent(this.mContext, MainActivity.class);
            mContext.startActivity(intent);
        }else{
            Log.d("Résultat connection", "Erreur dans la connection!");
        }
    }

    public void setContext(){
        this.mContext = this.getBaseContext();
    }
}
