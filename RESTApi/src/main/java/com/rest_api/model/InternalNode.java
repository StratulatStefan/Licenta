package com.rest_api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="internal_node")
public class InternalNode {
    @Id
    @Column(name = "ip_address")
    String ip_address;

    @Column(name = "location_country")
    String location_country;

    @Column(name = "status")
    String status;

    public InternalNode(){}

    public InternalNode(String ip_address, int port, String location_country, String status){
        this.ip_address = ip_address;
        this.location_country = location_country;
        this.status = status;
    }


    public String getIp_address() {
        return ip_address;
    }
    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public String getLocation_country() {
        return location_country;
    }
    public void setLocation_country(String country) {
        this.location_country = country;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
