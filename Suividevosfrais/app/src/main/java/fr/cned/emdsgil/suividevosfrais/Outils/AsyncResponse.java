package fr.cned.emdsgil.suividevosfrais.Outils;

import java.util.HashMap;

/**
 * Created by Caesar01 on 12/01/2018.
 */

public interface AsyncResponse {
    HashMap<Object, Object> processFinish(String output);
}
