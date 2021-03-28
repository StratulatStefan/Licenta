package node_manager;

/**
 * Clasa folosita pentru a incapsula cererea de eliminare a unui fisier, trimisa de la managerul general la nodul intern.
 * Va mosteni clasa ClientRequestManager, care incapsuleaza o cerere dintre managerul general si nodul intern.
 * Cererea de eliminare a fisierului are nevoie doar de id-ul utilizatorului si de numele fisierului, atribute care
 * deja se afla in clasa parinte; Asadar, aceasta clasa nu va avea niciun membru.
 * **/
public class DeleteRequest extends EditRequest {
}