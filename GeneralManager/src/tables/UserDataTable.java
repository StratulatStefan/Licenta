package tables;

import model.UserData;

import java.util.ArrayList;
import java.util.List;


/**
 * Clasa care contine o tabela cu datele de interes ale utilizatorilor
 */
public class UserDataTable {
    /** -------- Attribute -------- **/
    /**
     * Lista de utilizatori.
     */
    private final List<UserData> users;


    /** -------- Constructori -------- **/
    /**
     * Constructor; Initializeaza lista de utilizatori
     */
    public UserDataTable(){
        this.users = new ArrayList<>();
    }


    /** -------- Functii de prelucrare -------- **/
    /**
     * Functie care adauga un utilizator in lista
     * (Daca nu exista deja)
     */
    public void addUser(String userId, String userType) throws Exception{
        synchronized (this.users) {
            if (this.getUserIndexById(userId) >= 0) {
                throw new Exception("Utilizatorul exista deja!");
            }
            this.users.add(new UserData(userId, userType));
        }
    }

    /**
     * Functie care elimina un utilizator din lista
     * (Daca acesta exista)
     */
    public void deleteUser(String userId) throws Exception{
        synchronized (this.users) {
            if (this.getUserIndexById(userId) == -1) {
                throw new Exception("Utilizatorul nu exista!");
            }
            this.users.removeIf(user -> user.getUserId().equals(userId));
        }
    }

    /**
     * Functie care actualizeaza tipul unui utilizator (Standard/Premium)
     */
    public void updateUserType(String userId, String userType) throws Exception{
        synchronized (this.users) {
            int userIndex = this.getUserIndexById(userId);
            if (userIndex == -1) {
                throw new Exception("Utilizatorul nu exista!");
            }
            this.users.get(userIndex).setUserType(userType);
        }
    }


    /** -------- Gettere -------- **/
    /**
     * Functie care verifica daca un utilizator exista in lista;
     * Daca exista, returneaza indexul acestuia in lista
     */
    public int getUserIndexById(String userId){
        for(UserData user : this.users){
            if(user.getUserId().equals(userId)){
                this.users.indexOf(user);
            }
        }
        return -1;
    }

    /**
     * Functie care returneaza tipul unui utilizator
     */
    public String getUserType(String userId) throws Exception{
        synchronized (this.users){
            int userIndex = getUserIndexById(userId);
            if(userIndex == -1){
                throw new Exception("Utilizatorul nu exista!");
            }
            return this.users.get(userIndex).getUserType();
        }

    }

}
