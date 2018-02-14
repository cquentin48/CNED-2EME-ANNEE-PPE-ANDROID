package fr.cned.emdsgil.suividevosfrais.Outils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    // propriété de création d'une table dans la base de données
    public String connexion = "";
    public String isComptable = "";
    public String loadFrais = "";
    public String writeFrais = "";

    /**
     * Construction de l'accès à une base de données locale
     * @param context
     * @param name
     * @param version
     */
    public MySQLiteOpenHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    /**
     * méthode redéfinie appelée automatiquement par le constructeur
     * uniquement si celui-ci repère que la base n'existe pas encore
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("");
    }

    /**
     * méthode redéfinie appelée automatiquement s'il y a changement de version de la base
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
