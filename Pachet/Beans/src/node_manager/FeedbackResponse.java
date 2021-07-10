package node_manager;

import java.io.Serializable;

/**
 * <ul>
 * 	<li>Clasa va reprezenta un raspuns trimis de la nodurile interne catre nodul general, in urma efectuarii unei operatii solicitate.</li>
 * 	<li>Obiectul care va instantia aceasta clasa va fi trimis prin retea, motiv pentru care clasa va fi serializabila.</li>
 * 	<li>Va contine doar informatii despre statusul operatiei.</li>
 * 	<li> Pentru a include si alte informatii referitoare la efectuarea cererii,clasa se va mosteni si se vor adauga structuri de date.</li>
 * </ul>
 */
public class FeedbackResponse implements Serializable {
    /**
     * Flag pentru succesul operatiei
     */
    private boolean success;


    /**
     * Constructor vid
     */
    public FeedbackResponse(){super();}


    /**
     * Getter pentru flag-ul de succes
     */
    public boolean isSuccess() {
        return success;
    }
    /**
     * Setter pentru flag-ul de succes.
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
}
