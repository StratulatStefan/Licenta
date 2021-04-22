package com.dropbox.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="user_type")
public class UserType {
    @Id
    @Column(name="user_type")
    String user_type;

    @Column(name="replication_factor")
    int replication_factor;

    @Column(name="available_storage")
    long available_storage;

    public UserType(){}

    public UserType(String user_type, int replication_factor, long available_storage){
        this.user_type = user_type;
        this.replication_factor = replication_factor;
        this.available_storage = available_storage;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public int getReplication_factor() {
        return replication_factor;
    }

    public void setReplication_factor(int replication_factor) {
        this.replication_factor = replication_factor;
    }

    public long getAvailable_storage() {
        return available_storage;
    }

    public void setAvailable_storage(long available_storage) {
        this.available_storage = available_storage;
    }
}
