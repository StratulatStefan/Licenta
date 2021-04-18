package node_manager.Beat;

import java.io.Serializable;

/**
 * Clasa folosita pentru mecanismul de verificare a sanatatii stocarii nodurilor;
 * Cand primeste acest obiect, nodul intern va sti ca trebuie sa calculeze CRC-ul fiecarui fisier
 * si sa il trimita atasat in urmatorul beat, astfel incat nodul general sa verifice daca fisierele sunt valide
 */
public class RequestCRC implements Serializable {
}
