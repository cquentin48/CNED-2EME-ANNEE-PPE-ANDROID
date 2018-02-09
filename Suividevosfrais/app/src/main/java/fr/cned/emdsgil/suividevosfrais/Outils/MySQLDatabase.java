package fr.cned.emdsgil.suividevosfrais.Outils;

/**
 * Created by Caesar01 on 11/01/2018.
 */

public class MySQLDatabase {
    public static String username = "";
    public static String password = "";

    public static String databaseAdress = "";
    public static String databaseName = "gsb_frais";

    private MySQLDatabase instance;

    private MySQLDatabase(){
        super();
    }

    /**
     * Crée une seule instance de connection MYSQL
     * @return
     */
    public MySQLDatabase getInstance(){
        if(this.instance == null){
            return new MySQLDatabase();
        }else{
            return this.instance;
        }
    }

    public boolean MySQLOperation(String operationName, String tableName){
        //Afficher ici opération mysql en rapport avec php
        return true;
    }
}
