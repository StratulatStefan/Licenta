package client_manager.data;

/**
 * Clasa folosita pentru a incapsula cererea de eliminare a unui fisier, trimisa de la client la managerul general.
 * Va mosteni clasa ClientRequestManager, care incapsuleaza o cerere dintre client si managerul general.
 * Cererea de eliminare a fisierului are nevoie doar de id-ul utilizatorului si de numele fisierului, atribute care
 * deja se afla in clasa parinte; Asadar, aceasta clasa nu va avea niciun membru.
 * **/
public class DeleteFileRequest extends ClientManagerRequest {
}
