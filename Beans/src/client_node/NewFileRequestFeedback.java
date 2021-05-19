package client_node;

import java.io.Serializable;

/**
 * Clasa care inglobeaza raspunsul trimis de catre nodul intern catre frontend, ca raspuns la cererea
 * de stocare a unui fisier.
 * Obiectul care va instantia aceasta clasa va fi trimis prin retea, deci va trebui sa fie serializabil.
 */
public class NewFileRequestFeedback implements Serializable {
    /** -------- Atribute -------- **/
    /**
     * Adresa noului care trimite feedback-ul
     */
    private String nodeAddress;
    /**
     * Id-ul utilizatorului care detine fisierul
     */
    private String userId;
    /**
     * Numele fisierului
     */
    private String filename;
    /**
     * CRC fisier
     */
    private long crc;
    /**
     * status operatie (folosit de la frontend la nodul general
     */
    private String status;

    /** -------- Gettere & Settere -------- **/
    /**
     * Getter pentru id-ul utilizatorului
     */
    public String getUserId(){return this.userId;}
    /**
     * Setter pentru id-ul userului care va detine fisierul
     * @param userId Id-ul utilizatorului
     */
    public void setUserId(String userId){ this.userId = userId;}

    /**
     * Getter pentru numele fisierului
     * @return numele fisierului
     */
    public String getFilename(){
        return this.filename;
    }
    /**
     * Setter pentru numele fisierului.
     * Nu pastreaza calea absoluta, ci doar numele efectiv al fisierului.
     * @param filename numele fisierului (poate contine si calea)
     */
    public void setFilename(String filename){
        String[] filePathItems = filename.split("\\\\");
        this.filename = filePathItems[filePathItems.length - 1];
    }

    /**
     * Getter pentru adresa nodului.
     */
    public String getNodeAddress() {
        return nodeAddress;
    }
    /**
     * Setter pentru adresa nodului.
     */
    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    /**
     * Getter pentru CRC
     */
    public long getCrc() {
        return crc;
    }
    /**
     * Setter pentru CRC
     */
    public void setCrc(long crc) {
        this.crc = crc;
    }

    /**
     * Getter pentru status
     */
    public String getStatus() {
        return status;
    }
    /**
     * Setter pentru status
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
