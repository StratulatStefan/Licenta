package model;

import java.io.Serializable;

/**
 * <ul>
 *  <li>Clasa va contine toate datele corespunzatoare unei versiuni a unui fisier</li>
 *  <li>Va fi folosita in contextul unor mesaje trimise prin retea. Din acest motiv, va trebui sa fie serializabila</li>
 * </ul>
 */
public class FileVersionData implements Serializable {
    /**
     * Suma de control.
     */
    private long crc;
    /**
     * Numarul versiunii
     */
    private String versionNo;
    /**
     * Descierea versiunii
     */
    private String versionDescription;
    /**
     * Dimensiunea actuala a fisierului in versiunea actuala.
     */
    private Long size;


    /**
     * Constructor vid
     */
    public FileVersionData(){}
    /**
     * <ul>
     *  <li>Constructor cu argumente</li>
     *  <li>Va instantia fiecare membru al clasei, in functie de parametrii furnizati</li>
     * </ul>
     */
    public FileVersionData(long crc, String versionNo, String versionDescription, long size){
        this.crc = crc;
        this.versionNo = versionNo;
        this.versionDescription = versionDescription;
        this.size = size;
    }


    /**
     * Getter pentru numarul versiunii.
     */
    public String getVersionNo() {
        return versionNo;
    }
    /**
     * Setter pentru numarul versiunii.
     */
    public void setVersionNo(String versionNo) {
        this.versionNo = versionNo;
    }

    /**
     * Getter pentru descrierea versiunii.
     */
    public String getVersionDescription() {
        return versionDescription;
    }
    /**
     * Setter pentru descrierea versiunii.
     */
    public void setVersionDescription(String versionDescription) {
        this.versionDescription = versionDescription;
    }

    /**
     * Getter pentru suma de control.
     */
    public long getCrc() {
        return crc;
    }
    /**
     * Setter pentru suma de control.
     */
    public void setCrc(long crc) {
        this.crc = crc;
    }

    /**
     * Getter pentru dimensiunea fisierului.
     */
    public Long getSize() {
        return size;
    }
    /**
     * Setter pentru dimensiunea fisierului.
     */
    public void setSize(Long size) {
        this.size = size;
    }
}