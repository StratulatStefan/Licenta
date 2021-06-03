package storage_quantity;
import data.Pair;
import model.StorageQuantity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Clasa care va contine detaliile referitoare la cantitatile de stocare disponibile;
 * 1. Pentru utilizatori; (Cantitatea de memorie disponibila, conform cu tipul de utilizator ales)
 * 2. Pentru nodurile interne.
 */
public abstract class StorageQuantityTable {
    /** -------- Tabele -------- **/
    /**
     * Tabela de status a memoriei;
     */
    protected final HashMap<String, StorageQuantity> storageStatus;


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
    protected long convertGBtoBytes(long gbytes){
        return gbytes * (1 << 30);
    }

    protected Pair<Double, String> convertToBestScale(double bytes, int scale){
        if(bytes / 1024. < 1){
            String[] units = new String[]{"", "K", "M", "G"};
            return new Pair<Double, String>((double) Math.round(bytes * 100) / 100, units[scale] + "B");
        }
        return convertToBestScale(bytes / 1024., scale + 1);
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
            memory.updateUsedStorage(consumedStorage);
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
            this.storageStatus.get(key).updateUsedStorage(consumedStorage);
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

    public List<HashMap<String, Object>> getStorageQuantityTable(){
        List<HashMap<String, Object>> storageTable = new ArrayList<>();
        for(String nodeAddress : new ArrayList<>(storageStatus.keySet())){
            HashMap<String, Object> node = new HashMap<>();
            node.put("ip_address", nodeAddress);
            node.put("total_storage", storageStatus.get(nodeAddress).getTotalStorage());
            node.put("used_storage", storageStatus.get(nodeAddress).getUsedStorage());
            storageTable.add(node);
        }
        return storageTable;
    }


    /** -------- Functii de baza, supraincarcate -------- **/
    @Override
    public String toString() {
        StorageQuantity storageRecord;
        Pair<Double, String> converted = null;
        synchronized (this.storageStatus) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String key : new ArrayList<>(this.storageStatus.keySet())) {
                storageRecord = this.storageStatus.get(key);
                converted = this.convertToBestScale(storageRecord.getUsedStorage(), 0);
                stringBuilder.append("\t").append(key).append(" --> ").append(converted.getFirst()).append(" ").append(converted.getSecond());
                converted = this.convertToBestScale(storageRecord.getTotalStorage(), 0);
                stringBuilder.append("/").append(converted.getFirst()).append(" ").append(converted.getSecond()).append("  \n");
            }
            stringBuilder.append("------------------------------------\n\n");
            return stringBuilder.toString();
        }

    }
}
