package storage_quantity;
import config.AppConfig;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Clasa care va contine detaliile referitoare la cantitatile de stocare disponibile;
 * 1. Pentru utilizatori; (Cantitatea de memorie disponibila, conform cu tipul de utilizator ales)
 * 2. Pentru nodurile interne.
 */
public abstract class StorageQuantityTable {
    /** -------- Tabele -------- **/
    /**
     * Tabela de status a memoriei utilizatorilor;
     */
    private final HashMap<String, StorageQuantity> storageStatus;


    /** -------- Constructor & Configurare -------- **/
    /**
     * Functie care citeste si initializeaza parametrii de configurare
     */
    public abstract void readConfigParams();

    /**
     * Constructorul clasei;
     * Initializeaza tabela
     */
    public StorageQuantityTable(){
        this.storageStatus = new HashMap<>();
    }


    /** -------- Functii generale de prelucrare -------- **/
    /**
     * Functie de conversie din GigaBytes in KiloBytes
     */
    protected Long convertGBytesToKiloBytes(int gigabytes){
        return (long) (gigabytes * (1 << 20));
    }

    /**
     * Funtie de conversie din KiloBytes in MegaBytes
     */
    protected double convertKiloBytesToMBytes(long kilobytes){
        double mbytes = (double) (kilobytes / 1024.);
        return Math.round(mbytes * 100.0) / 100.0;
    }



    /** -------- Functii de prelucrare a tabelei -------- **/
    /**
     * Functie de adaugare a unei noi inregistrari in tabela de status;
     * Se introduce cheia de identificare a entitatii, memoria totala disponibila;
     * Totodata, se presupune ca o noua inregistrare se va introduce odata cu prima solicitare
     * de stocare a unui fisier, motiv pentru care furnizam si cantitatea de memorie consumata.
     * @throws Exception Exceptie generata daca inregistrarea cu cheia specificata exista deja.
     */
    protected void addNewRegister(String key, long totalStorage, long consumedStorage) throws Exception{
        synchronized (this.storageStatus){
            if(this.checkIfContainsKey(key)){
                throw new Exception("Inregistrarea pentru " + key + " exista deja!");
            }
            StorageQuantity memory = new StorageQuantity();
            memory.setTotalStorage(totalStorage);
            memory.updateAvailableStorage(consumedStorage);
            this.storageStatus.put(key, memory);
        }
    }

    /**
     * Functie de modificare a cantitatii de memorie disponibile pentru o anumita inregistrare
     * @throws Exception Exceptie generata daca inregistrarea cu cheia data nu exista.
     */
    protected void updateRegister(String key, long consumedStorage) throws Exception{
        synchronized (this.storageStatus){
            if(!checkIfContainsKey(key)){
                throw new Exception("Inregistrarea " + key + " nu a fost gasita.");
            }
            this.storageStatus.get(key).updateAvailableStorage(consumedStorage);
        }
    }


    /** -------- Functii de validare -------- **/
    /**
     * Functie care verifica daca tabela de stocare contine o anumita cheie de identificare a unei entitati.
     */
    protected boolean checkIfContainsKey(String key){
        synchronized (this.storageStatus){
            return this.storageStatus.containsKey(key);
        }

    }


    /** -------- Functii de baza, supraincarcate -------- **/
    @Override
    public String toString() {
        StorageQuantity storageRecord;
        synchronized (this.storageStatus) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String key : new ArrayList<>(this.storageStatus.keySet())) {
                storageRecord = this.storageStatus.get(key);
                stringBuilder.append("\t")
                        .append(key).append(" --> ")
                        .append(storageRecord.getAvailableStorage())
                        .append("/")
                        .append(storageRecord.getTotalStorage())
                        .append(" KB \n");
            }
            stringBuilder.append("------------------------------------\n\n");
            return stringBuilder.toString();
        }

    }
}
