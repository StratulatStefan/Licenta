package com.rest_api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * <ul>
 * 	<li>Clasa care va contine toate atributele corespunzatoare reprezentarii tipului unui utilizator.</li>
 * 	<li> Clasa va fi adnotata corespunzator, astfel incat sa se poata realiza <strong>maparea obiectual-relational</strong> prin intermediul <strong>JPA</strong>.</li>
 * </ul>
 */
@Entity
@Table(name="user_type")
public class UserType {
    /**
     * <ul>
     * 	<li>Tipul de utilizator</li>
     * 	<li> Va contine si adnotarea <strong>@Id</strong> pentru a sugera ca la nivel relational, acest camp trebuie sa fie <strong>PK</strong> pentru tabela.</li>
     * </ul>
     */
    @Id
    @Column(name="user_type")
    String user_type;

    /**
     * Factorul de replicare corespunzator tipului de utilizator.
     */
    @Column(name="replication_factor")
    int replication_factor;

    /**
     * Cantitatea de memorie disponibila, corespunzatoare tipului de utilizator.
     */
    @Column(name="available_storage")
    long available_storage;

    /**
     * Pretul abonamentului pentru a avea tipul curent de utilizator.
     */
    @Column(name="price_dollars")
    float price_dollars;

    /**
     * Constructor vid
     */
    public UserType(){}

    /**
     * Constructor cu parametri, care instantiaza membrii clasei
     */
    public UserType(String user_type, int replication_factor, long available_storage, float price_dollars){
        this.user_type = user_type;
        this.replication_factor = replication_factor;
        this.available_storage = available_storage;
        this.price_dollars = price_dollars;
    }

    /**
     * Getter pentru tipul utilizatorului.
     */
    public String getUser_type() {
        return user_type;
    }
    /**
     * Setter pentru tipul utilizatorului.
     */
    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    /**
     * Getter pentru factorul de replicare.
     */
    public int getReplication_factor() {
        return replication_factor;
    }
    /**
     * Setter pentru factorul de replicare.
     */
    public void setReplication_factor(int replication_factor) {
        this.replication_factor = replication_factor;
    }

    /**
     * Getter pentru cantitatea de memorie totala disponibila pentru utilizator.
     */
    public long getAvailable_storage() {
        return available_storage;
    }
    /**
     * Setter pentru cantitatea de memorie totala disponibila pentru utilizator.
     */
    public void setAvailable_storage(long available_storage) {
        this.available_storage = available_storage;
    }

    /**
     * Getter pentru pretul abonamentului.
     */
    public float getPrice_dollars() {
        return price_dollars;
    }
    /**
     * Setter pentru pretul abonamentului.
     */
    public void setPrice_dollars(float price_dollars) {
        this.price_dollars = price_dollars;
    }
}
