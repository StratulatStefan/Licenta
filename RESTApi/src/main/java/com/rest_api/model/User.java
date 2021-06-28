package com.rest_api.model;

import javax.persistence.*;

/**
 * <ul>
 * 	<li>Clasa care va contine toate atributele corespunzatoare reprezentarii unui utilizator.</li>
 * 	<li> Clasa va fi adnotata corespunzator, astfel incat sa se poata realiza <strong>maparea obiectual-relational</strong> prin intermediul <strong>JPA</strong>.</li>
 * </ul>
 */
@Entity
@Table(name="user")
public class User {
    /**
     * <ul>
     * 	<li>Identificatorul unic al unui utilizator.</li>
     * 	<li> Va contine si adnotarea <strong>@Id</strong> pentru a sugera ca la nivel relational,
     * 	     acest camp trebuie sa fie <strong>PK</strong> pentru tabela.</li>
     * 	<li>Adnotarea <strong>@GeneratedValue</strong> va indica faptul ca acest id va fi auto-generat de serverul de BD</li>
     * </ul>
     */
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    int id;

    /**
     * Numele utilizatorului.
     */
    @Column(name="name")
    String name;

    /**
     * Email-ul utilizatorului.
     */
    @Column(name="email")
    String email;

    /**
     * Parola utilizatorului.
     */
    @Column(name="password")
    String password;

    /**
     * Locatia utilizatorului.
     */
    @Column(name="country")
    String country;

    /**
     * Tipul utilizatorului.
     */
    @Column(name="type")
    String type;

    /**
     * Cantitatea de memorie disponibila pentru utilizator.
     */
    @Column(name="storage_quantity")
    long storage_quantity;

    /**
     * Numarul de fisiere ale utilizatorului.
     */
    @Column(name="number_of_file")
    int number_of_file;

    /**
     * Constructor vid
     */
    public User(){}

    /**
     * Constructor cu parametri, care instantiaza membrii clasei
     */
    public User(String name, String email, String password, String county){
        this.name = name;
        this.email = email;
        this.password = password;
        this.country = county;
    }

    /**
     * Functie care realizeaza o (copie profunda) a obiectului
     * @param user Utilizatorul pe baza caruia se va realiza copia.
     * @return Copia utilizatorului.
     */
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

    /**
     * Getter pentru identificatorul unic al utilizatorului..
     */
    public int getId() {
        return id;
    }
    /**
     * Setter pentru identificatorul unic al utilizatorului.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Getter pentru numele utilizatorului.
     */
    public String getName() {
        return name;
    }
    /**
     * Setter pentru numele utilizatorului.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter pentru emailul utilizatorului.
     */
    public String getEmail() {
        return email;
    }
    /**
     * Setter pentru emailul utilizatorului.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Getter pentru parola utilizatorului.
     */
    public String getPassword() {
        return password;
    }
    /**
     * Setter pentru parola utilizatorului.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter pentru locatia utilizatorului.
     */
    public String getCountry() {
        return country;
    }
    /**
     * Setter pentru locatia utilizatorului.
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Getter pentru cantitatea de memorie disponibila pentru utilizator
     */
    public long getStorage_quantity() {
        return storage_quantity;
    }
    /**
     * Setter pentru cantitatea de memorie disponibila pentru utilizator
     */
    public void setStorage_quantity(long storage_quantity) {
        this.storage_quantity = storage_quantity;
    }

    /**
     * Getter pentru tipul utilizatorului.
     */
    public String getType() {
        return type;
    }
    /**
     * Setter pentru tipul utilizatorului.
     */
    public void setType(String usertype) {
        this.type = usertype;
    }

    /**
     * Getter pentru numarul de fisiere ale utilizatorului.
     */
    public int getNumber_of_file() {
        return number_of_file;
    }
    /**
     * Setter pentru numarul de fisiere ale utilizatorului.
     */
    public void setNumber_of_file(int number_of_files) {
        this.number_of_file = number_of_files;
    }
}