package fr.cned.emdsgil.suividevosfrais.Vue;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import fr.cned.emdsgil.suividevosfrais.Controleur.Global;
import fr.cned.emdsgil.suividevosfrais.Modele.AccesDistant;
import fr.cned.emdsgil.suividevosfrais.Outils.Serializer;
import fr.cned.emdsgil.suividevosfrais.R;

public class ConnexionActivity extends AppCompatActivity {
    private Global controle = Global.getInstance(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);
    }

    private void btnConnexion(){
        Button connection = (Button)findViewById(R.id.connexionButton);
        connection.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                AccesDistant accesDistant = new AccesDistant();

                //Récupération des identifiants
                String usernameEntered = ((EditText)findViewById(R.id.pseudoEditText)).getText().toString();
                String pwdEntered = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();

                //Création de la liste JSON pour vérification
                List connexionInfo = new ArrayList();
                connexionInfo.add(0,usernameEntered);
                connexionInfo.add(1,pwdEntered);
                JSONArray connexionInfoJSONArray = new JSONArray(connexionInfo);
                //Envoi de la requête
                accesDistant.envoi("connexion",connexionInfoJSONArray);
            }
        }) ;
    }
}
