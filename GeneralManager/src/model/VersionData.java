package model;

import java.io.Serializable;

/**
 * <ul>
 * 	<li>Clasa care descrie toate caracteristicile unei versiuni a unui fisier.</li>
 * 	<li> Va fi folosita in contextul definirii tabelelor de stare, care vor fitrimise in retea catre aplicatia de tip client pentru monitorizare, motiv pentru care clasa este serializabila.</li>
 * </ul>
 */
public class VersionData implements Serializable {
    /**
     * Momentul de timp la care a fost inregistrata noua versiune.
     */
    private String timestamp;
     /**
     * Numele versiuni [v<x>]
     */
    private String versionName;
    /**
     * Suma de control a fisierului inregistrata in cadrul noii versiuni
     */
    private long hash;
    /**
     * Descrierea versiunii
     */
    private String description;


    /**
     * Constructorul cu parametri, care va initializa membrii clasei.
     */
    public VersionData(String timestamp, String versionName, long hash, String description){
        this.timestamp = timestamp;
        this.versionName = versionName;
        this.hash = hash;
        this.description = description;
    }

    /**
     * Constructorul vid.
     */
    public VersionData(){}

    /**
     * <ul>
     * 	<li>Constructorul care va crea un obiect pe baza unei inregistrari din fisierul de <strong>metadate</strong>.</li>
     * 	<li>Se va face parsarea sirului de caractere citit din fisier astfel incat sa se obtina valoarea fiecarui membru al clasei.</li>
     * </ul>
     * @param metadataFileRegister versiune extrasa din fisier, sub forma unui sir de caractere.
     */
    public VersionData(String metadataFileRegister){
        String[] fields = metadataFileRegister.split(" ");
        this.timestamp = fields[0];
        this.versionName = fields[1];
        this.hash = Long.parseLong(fields[2]);
        StringBuilder stringBuilder = new StringBuilder();
        for(String field : fields) {
            stringBuilder.append(field).append(" ");
        }
        this.description = stringBuilder.toString().replace("{", "").replace("}","");
    }


    /**
     * Getter pentru suma de control.
     */
    public long getHash() {
        return hash;
    }
    /**
     * Setter pentru suma de control.
     */
    public void setHash(long hash) {
        this.hash = hash;
    }

    /**
     * Getter pentru momentul de timp.
     */
    public String getTimestamp() {
        return timestamp;
    }
    /**
     * Setter pentru momentul de timp.
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Getter pentru numele versiunii.
     */
    public String getVersionName() {
        return versionName;
    }

    /**
     * Getter pentru numarul versiunii.
     */
    public int getVersionNumber(){
        return Integer.parseInt(versionName.substring(1));
    }

    /**
     * Setter pentru numele versiunii.
     */
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    /**
     * Setter pentru numarul versiunii.
     */
    public void setVersionNumber(int versionNumber){
        this.versionName = "v" + versionNumber;
    }

    /**
     * Getter pentru descrierea versiunii.
     */
    public String getDescription() {
        return description;
    }
    /**
     * Setter pentru descrierea versiunii.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return timestamp + " " + versionName + " " + hash + " {" + description + "}";
    }
}
