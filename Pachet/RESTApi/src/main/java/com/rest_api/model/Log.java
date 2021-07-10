package com.rest_api.model;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * <ul>
 * 	<li>Clasa care va contine toate atributele corespunzatoare reprezentarii  unui eveniment.</li>
 * 	<li> Clasa va fi adnotata corespunzator, astfel incat sa se poata realiza <strong>maparea obiectual-relational</strong> prin intermediul <strong>JPA</strong>.</li>
 * </ul>
 */
@Entity
@Table(name = "log")
public class Log {
    /**
     * <ul>
     * 	<li>Identificatorul unic al unui eveniment</li>
     * 	<li> Va contine si adnotarea <strong>@Id</strong> pentru a sugera ca la nivel relational,
     * 	     acest camp trebuie sa fie <strong>PK</strong> pentru tabela.</li>
     * 	<li>Adnotarea <strong>@GeneratedValue</strong> va indica faptul ca acest id va fi auto-generat de serverul de BD</li>
     * </ul>
     */
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="registerId")
    int registerId;

    /**
     * Adresa IP a nodului in cadrul caruia s-a generat evenimentul.
     */
    @Column(name="node_address")
    String node_address;

    /**
     * Tipul evenimentului.
     */
    @Column(name="message_type")
    String message_type;

    /**
     * Descrierea evenimentului
     */
    @Column(name = "description")
    String description;

    /**
     * <ul>
     * 	<li>Momentul de timp al producerii evenimentului.</li>
     * 	<li> Prin adnotarea <strong>@CreationTimestamp</strong> se va genera in mod automat momentul de timp de tip <strong>Temporal.</li>
     * 	<li>TIMESTAMP</strong>.</li>
     * </ul>
     */
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "register_date")
    Date register_date;

    /**
     * Constructor vid
     */
    public Log(){}

    /**
     * Constructor cu parametri, care instantiaza membrii clasei
     */
    public Log(String node_address, String message_type, String description, Date register_date){
        this.node_address = node_address;
        this.message_type = message_type;
        this.description = description;
        this.register_date = register_date;
    }

    /**
     * Getter pentru id-ul inregistrarii evenimentului.
     */
    public int getRegisterId() {
        return registerId;
    }
    /**
     * Setter pentru id-ul inregistrarii evenimentului.
     */
    public void setRegisterId(int registerId) {
        this.registerId = registerId;
    }

    /**
     * Getter pentru adresa nodului la nivelul caruia s-a generat evenimentul.
     */
    public String getNode_address() {
        return node_address;
    }
    /**
     * Setter pentru adresa nodului la nivelul caruia s-a generat evenimentul.
     */
    public void setNode_address(String node_address) {
        this.node_address = node_address;
    }

    /**
     * Getter pentru tipul evenimentului.
     */
    public String getMessage_type() {
        return message_type;
    }
    /**
     * Setter pentru tipul evenimentului.
     */
    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    /**
     * Getter pentru descrierea evenimentului.
     */
    public String getDescription() {
        return description;
    }
    /**
     * Setter pentru descrierea evenimentului.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter pentru momentul de timp al producerii evenimentului.
     */
    public Date getRegister_date() {
        return register_date;
    }
    /**
     * Setter pentru momentul de timp al producerii evenimentului.
     */
    public void setRegister_date(Date register_date) {
        this.register_date = register_date;
    }
}
