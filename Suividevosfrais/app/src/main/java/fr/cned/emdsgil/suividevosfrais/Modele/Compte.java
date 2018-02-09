package fr.cned.emdsgil.suividevosfrais.Modele;

/**
 * Created by Caesar01 on 17/01/2018.
 */

public class Compte {
    private String username;

    private String password;
    private boolean isComptable;
    private int userId;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isComptable() { return isComptable; }

    public int getUserId() { return userId; }

    public Compte(String username, String password, boolean isComptable, int userId){
        this.username = username;
        this.password = password;
        this.isComptable = isComptable;
        this.userId = userId;
    }
}
