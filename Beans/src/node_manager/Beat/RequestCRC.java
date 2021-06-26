package node_manager.Beat;

import java.io.Serializable;

/**
 * <ul>
 *  <li>Clasa folosita pentru mecanismul de verificare a integritatii stocarii nodurilor;</li>
 *  <li>Cand primeste acest obiect, nodul intern va sti ca trebuie sa calculeze CRC-ul fiecarui fisier
 *      si sa il trimita atasat in urmatorul beat dupa finalizarea calcularii, astfel incat nodul general
 *      sa verifice daca fisierele sunt valide</li>
 *  <li>Obiectul ce instantiaza aceasta clasa va fi trimis prin retea; Din acest motiv, clasa trebuie sa fie
 *      serializabila</li>
 * </ul>
 */
public class RequestCRC implements Serializable {
    /** -------- Constructor -------- **/
    /**
     * Constructor vid
     */
    public RequestCRC(){}
}
