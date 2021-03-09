package client_manager;

import java.io.Serializable;

/**
 * Clasa folosita in momentul in care clientul doreste efectuarea unei noi operatii de catre nodul general
 * De exemplu, clientul solicita stocarea unui nou
 * fisier in sistem; In acest moment, frontend-ul va trebui sa faca o cerere
 * catre managerul general, pentr un token care va cuprinde nodurile ce vor stoca fisierul;
 * Clientul trebuie sa furnizeze toate datele necesare
 */
public class ClientManagerRequest implements Serializable {
    /**
     * Id-ul utilizatorului curent, care face solicitarea
     */
    private String userId = "";

    /**
     * Operatia pe care clientul o solicita de la nodul general
     */
    private String operation = "";

    /**
     * Numele fisierului afectat (la nevoie; exemplu newfile)
     */
    private String filename = "";

    /**
     * Dimensiunea fisierului (la nevoie; exemplu newfile)
     */
    private int filesize = 0;

    /**
     * Factorul de replicare (la nevoie; exemplu newfile)
     */
    private int replication_factor = 1;


    /**
     * Getter pentru id-ul utilizatorului
     * @return Id-ul utilizatorului
     */
    public String getUserId() {
        return userId;
    }
    /**
     * Setter pentru id-ul utilizatorului.
     * @param userId Id-ul utilizatorului
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter pentru operatia dorita
     * @return Operatia dorita
     */
    public String getOperation() {
        return operation;
    }
    /**
     * Setter pentru operatia dorita
     * @param operation Operatia dorita
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getFilesize() {
        return filesize;
    }
    public void setFilesize(int filesize) {
        this.filesize = filesize;
    }

    public int getReplication_factor() {
        return replication_factor;
    }
    public void setReplication_factor(int replication_factor) {
        this.replication_factor = replication_factor;
    }
}

