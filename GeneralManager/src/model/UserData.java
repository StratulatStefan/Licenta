package model;

/**
 * Datele de interes ale utilizatorului, in contextul managerului general.
 */
public class UserData{
    /** -------- Attribute -------- **/
    /**
     * Id-ul utilizatorului.
     */
    private String userId;
    /**
     * Tipul utilizatorului
     */
    private String userType;


    /** -------- Constructori -------- **/
    /**
     * Constructor;
     */
    public UserData(String userId, String userType){
        this.userId = userId;
        this.userType = userType;
    }


    /** -------- Gettere & Settere -------- **/
    /**
     * Getter pentru Id-ul utilizatorului.
     */
    public String getUserId() {
        return userId;
    }
    /**
     * Setter pentru Id-ul utilizatorului.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter pentru tipul utilizatorului.
     */
    public String getUserType() {
        return userType;
    }
    /**
     * Setter pentru tipul utilizatorului.
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }
}
