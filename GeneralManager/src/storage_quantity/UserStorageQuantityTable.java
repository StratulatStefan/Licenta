package storage_quantity;

import config.AppConfig;
import model.StorageStatusTable;

/**
 * Clasa care va contine detaliile referitoare la cantitatile de stocare disponibile pentru utilizatori;
 */
public class UserStorageQuantityTable extends StorageQuantityTable {
    /** -------- Attribute -------- **/
    /**
     * Cantitatea de memorie disponibila pentru un utilizator obisnuit
     */
    private long standardUserTotalStorageQuantity;
    /**
     * Cantitatea de memorie disponibila pentru un utilizator premium
     */
    private long premiumUserTotalStorageQuantity;


    /** -------- Constructor & Configurare -------- **/
    /**
     * Functie care citeste si initializeaza parametrii de configurare
     */
    public void readConfigParams(){
        standardUserTotalStorageQuantity = convertGBtoBytes(Integer.parseInt(AppConfig.getParam("basicUserStorageQuantity")));
        premiumUserTotalStorageQuantity = convertGBtoBytes(Integer.parseInt(AppConfig.getParam("premiumUserStorageQuantity")));
    }

    /**
     * Constructorul clasei;
     * Citeste si instantiaza parametrii de configurare
     */
    public UserStorageQuantityTable(){
        super();
        readConfigParams();
    }


    /** -------- Functii de prelucrare a tabelei -------- **/
    /**
     * Functie de adaugare a unei noi inregistrari in tabela de stocare a utilizatorilor.
     */
    public void addNewUserStorageRecord(String userId, String userType, long consumedMemory) throws Exception {
        long totalStorage = 0;
        switch (userType){
            case "PREMIUM": {
                totalStorage = premiumUserTotalStorageQuantity;
                break;
            }
            case "STANDARD" : {
                totalStorage = standardUserTotalStorageQuantity;
                break;
            }
        }
        this.addNewRegister(userId, totalStorage, consumedMemory);
    }

    /**
     * Functie de modificare a cantitatii de memorie disponibile pentru un anumit utilizator;
     * Se are in vedere consumul de memorie (adauarea unui fisier)
     * @throws Exception Exceptie generata daca inregistrarea cu cheia data nu exista.
     */
    public void registerMemoryConsumption(String userId, String userType, long consumedMemory) throws Exception{
        try {
            this.updateRegister(userId, consumedMemory);
        }
        catch (Exception exception){
            this.addNewUserStorageRecord(userId, userType, consumedMemory);
        }
    }

    /**
     * Functie de modificare a cantitatii de memorie disponibile pentru un anumit utilizator;
     * Se are in vedere eliberarea de memorie (stergerea unui fisier)
     * @throws Exception Exceptie generata daca inregistrarea cu cheia data nu exista.
     */
    public void registerMemoryRelease(String userId, long freeMemory) throws Exception{
        this.updateRegister(userId, -freeMemory);
    }


    /** -------- Functii de baza, supraincarcate -------- **/
    @Override
    public String toString() {
        return "------------------------------------\n" +
                "User Storage Quantity Status\n" +
                super.toString();
    }
}
