package com.dropbox.model;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "log")
public class Log {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="registerId")
    int registerId;

    @Column(name="node_address")
    String node_address;

    @Column(name="message_type")
    String message_type;

    @Column(name = "description")
    String description;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "register_date")
    Date register_date;

    public Log(){}

    public Log(String node_address, String message_type, String description, Date register_date){
        this.node_address = node_address;
        this.message_type = message_type;
        this.description = description;
        this.register_date = register_date;
    }

    public int getRegisterId() {
        return registerId;
    }
    public void setRegisterId(int registerId) {
        this.registerId = registerId;
    }

    public String getNode_address() {
        return node_address;
    }
    public void setNode_address(String node_address) {
        this.node_address = node_address;
    }

    public String getMessage_type() {
        return message_type;
    }
    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Date getRegister_date() {
        return register_date;
    }
    public void setRegister_date(Date register_date) {
        this.register_date = register_date;
    }
}
