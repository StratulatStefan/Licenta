package com.rest_api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * <ul>
 * 	<li>Clasa care va contine toate atributele corespunzatoare reprezentarii unui nod intern.</li>
 * 	<li> Clasa va fi adnotata corespunzator, astfel incat sa se poata realiza <strong>maparea obiectual-relational</strong> prin intermediul <strong>JPA</strong>.</li>
 * </ul>
 */
@Entity
@Table(name="internal_node")
public class InternalNode {
    /**
     * <ul>
     * 	<li>Adresa IP a nodului.</li>
     * 	<li> Va contine si adnotarea <strong>@Id</strong> pentru a sugera ca la nivel relational, acest camp trebuie sa fie <strong>PK</strong> pentru tabela.</li>
     * </ul>
     */
    @Id
    @Column(name = "ip_address")
    String ip_address;

    /**
     * Locatia nodului (o tara).
     */
    @Column(name = "location_country")
    String location_country;

    /**
     * Statusul nodului (ON, OFF)
     */
    @Column(name = "status")
    String status;

    /**
     * Constructor vid
     */
    public InternalNode(){}

    /**
     * Constructor cu parametri, care instantiaza membrii clasei
     */
    public InternalNode(String ip_address, String location_country, String status){
        this.ip_address = ip_address;
        this.location_country = location_country;
        this.status = status;
    }

    /**
     * Getter pentru adresa IP.
     */
    public String getIp_address() {
        return ip_address;
    }
    /**
     * Setter pentru adresa IP.
     */
    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }
    /**
     * Getter pentru locatie
     */
    public String getLocation_country() {
        return location_country;
    }
    /**
     * Setter pentru locatie
     */
    public void setLocation_country(String country) {
        this.location_country = country;
    }

    /**
     * Getter pentru status
     */
    public String getStatus() {
        return status;
    }
    /**
     * Setter pentru status
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
