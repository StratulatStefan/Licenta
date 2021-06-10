package com.rest_api.model;

import javax.persistence.*;

@Entity
@Table(name="user")
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    int id;

    @Column(name="name")
    String name;

    @Column(name="email")
    String email;

    @Column(name="password")
    String password;

    @Column(name="country")
    String country;

    @Column(name="type")
    String type;

    @Column(name="storage_quantity")
    long storage_quantity;

    @Column(name="number_of_file")
    int number_of_file;

    public User(){}

    public User(String name, String email, String password, String county){
        this.name = name;
        this.email = email;
        this.password = password;
        this.country = county;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }

    public long getStorage_quantity() {
        return storage_quantity;
    }
    public void setStorage_quantity(long storage_quantity) {
        this.storage_quantity = storage_quantity;
    }

    public String getType() {
        return type;
    }
    public void setType(String usertype) {
        this.type = usertype;
    }

    public static User usercopy(User user){
        User ucopy = new User();
        ucopy.setName(user.getName());
        ucopy.setEmail(user.getEmail());
        ucopy.setType(user.getType());
        ucopy.setCountry(user.getCountry());
        ucopy.setStorage_quantity(user.getStorage_quantity());
        ucopy.setNumber_of_file(user.getNumber_of_file());
        return ucopy;
    }

    public int getNumber_of_file() {
        return number_of_file;
    }
    public void setNumber_of_file(int number_of_files) {
        this.number_of_file = number_of_files;
    }
}