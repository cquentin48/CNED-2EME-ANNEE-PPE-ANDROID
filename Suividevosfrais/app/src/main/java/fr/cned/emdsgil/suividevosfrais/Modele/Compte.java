package fr.cned.emdsgil.suividevosfrais.Modele;

/**
 * Created by Caesar01 on 17/01/2018.
 */

public class Compte {
    private String username;

    private boolean isComptable;
    private String userId;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isComptable() { return isComptable; }

    public String getUserId() { return userId; }

    public Compte(String username, boolean isComptable, String userId){
        this.username = username;
        this.isComptable = isComptable;
        this.userId = userId;
    }

    public void setComptable(boolean comptable) {
        isComptable = comptable;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
