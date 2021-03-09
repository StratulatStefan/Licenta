package client_node;

import java.io.Serializable;
import java.util.Collections;

/**
 * Clasa folosita in comunicarea de date cu nodurile adiacente. Inglobeaza toate
 * datele caracteristice header-ului fisierului ce se va trimite. Prin header intelegem
 * token-ul, numele fisierului, dimensiunea fisierului si alte date caracteristice utilizatorului.
 */
public class FileHeader implements Serializable {
    /**
     * Token-ul ce defineste chain-ul de comunicare.
     */
    private String token;
    /**
     * Numele fisierului
     */
    private String filename;
    /**
     * Dimensiunea fisierului. Aparent, nu avem nevoie de ea.
     */
    private long filesize;
    /**
     * ID-ul userului care va detine fisierul
     */
    private String userId;

    /**
     * Constructor vid pentru clasa.
     */
    public FileHeader(){}


    /**
     * Getter pentru token
     * @return token-ul.
     */
    public String getToken(){
        return this.token;
    }

    /**
     * Setter pentru token
     * @param token token-ul
     */
    public void setToken(String token){
        this.token = token;
    }


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
        String[] filePathItems = filename.split("\\/");
        this.filename = filePathItems[filePathItems.length - 1];
    }


    /**
     * Getter pentru dimensiunea fisierului
     * @return dimensiunea fisierului
     */
    public long getFilesize(){
        return this.filesize;
    }

    /**
     * Setter pentru dimensiunea fisierului. Se face parsare de la String la long,
     * deci se poate genera o exceptie;
     * @param filesize
     */
    public void setFilesize(String filesize) throws Exception{
        this.filesize = Long.parseLong(filesize);
    }

    /**
     * Setter pentru dimensiunea fisierului, ce primeste direct dimensiunea in formatul necesar
     * @param filesize dimensiunea fisierului in format long
     */
    public void setFilesize(long filesize){
        this.filesize = filesize;
    }


    public String getUserId(){return this.userId;}

    /**
     * Setter pentru id-ul userului care va detine fisierul
     * @param userId Id-ul utilizatorului
     */
    public void setUserId(String userId){ this.userId = userId;}
}
