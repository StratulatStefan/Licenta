package tables;

import config.AppConfig;
import data.Pair;
import model.StorageQuantity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Clasa care va contine detaliile referitoare la cantitatile de stocare disponibile pentru nodurile interne;
 */
public class NodeStorageQuantityTable{
    /** -------- Attribute -------- **/
    /**
     * Cantitatea de memorie disponibila pentru un nod intern
     */
    private long internalNodeTotalStorageQuantity = Integer.parseInt(AppConfig.getParam("internalNodeStorageQuantity"));// * (1 << 30);
    /**
     * Tabela de status a memoriei;
     */
    public final HashMap<String, StorageQuantity> storageStatus;


    /** -------- Constructor & Configurare -------- **/
    /**
     * Constructorul clasei;
     * Citeste si instantiaza parametrii de configurare
     */
    public NodeStorageQuantityTable(){
        this.storageStatus = new HashMap<>();
    }


    /** -------- Functii de prelucrare a tabelei -------- **/
    /**
     * Functie de adaugare a unei noi inregistrari in tabela de stocare a nodurilor.
     * Functie de adaugare a unei noi inregistrari in tabela de status;
     * Se introduce cheia de identificare a entitatii, memoria totala disponibila;
     * Totodata, se presupune ca o noua inregistrare se va introduce odata cu prima solicitare
     * de stocare a unui fisier, motiv pentru care furnizam si cantitatea de memorie consumata.
     * @throws Exception Exceptie generata daca inregistrarea cu cheia specificata exista deja.
     */
    public void addNewNodeStorageRecord(String nodeAddress, long consumedMemory) throws Exception {
        synchronized (this.storageStatus){
            if(this.checkIfContainsKey(nodeAddress)){
                throw new Exception("Inregistrarea pentru " + nodeAddress + " exista deja!");
            }
            StorageQuantity memory = new StorageQuantity();
            memory.setTotalStorage(internalNodeTotalStorageQuantity * (1 << 30));
            memory.updateUsedStorage(consumedMemory);
            this.storageStatus.put(nodeAddress, memory);
        }
    }

    /**
     * Functie de modificare a cantitatii de memorie disponibile pentru un anumit nod;
     * Se are in vedere consumul de memorie (adauarea unui fisier)
     * @throws Exception Exceptie generata daca inregistrarea cu cheia data nu exista.
     */
    public void updateRegister(String nodeAddress, long consumedMemory) throws Exception{
        synchronized (this.storageStatus){
            if(!checkIfContainsKey(nodeAddress)){
                addNewNodeStorageRecord(nodeAddress, consumedMemory);
            }
            this.storageStatus.get(nodeAddress).updateUsedStorage(consumedMemory);
        }
    }

    public List<String> getMostSuitableNodes(long filesize){
        synchronized (this.storageStatus) {
            List<Pair<String, Long>> availableQuantities = new ArrayList<Pair<String, Long>>();
            for (String node : new ArrayList<>(this.storageStatus.keySet())) {
                StorageQuantity quantity = this.storageStatus.get(node);
                availableQuantities.add(new Pair<String, Long>(node, quantity.getTotalStorage() - quantity.getUsedStorage() - filesize));
            }
            availableQuantities.sort(Comparator.comparing(Pair::getSecond));
            Collections.reverse(availableQuantities);
            return availableQuantities.stream().filter(node -> node.getSecond() > 0).map(Pair::getFirst).collect(Collectors.toList());
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

    private Pair<Double, String> convertToBestScale(double bytes, int scale){
        if(bytes / 1024. < 1){
            String[] units = new String[]{"", "K", "M", "G"};
            return new Pair<Double, String>((double) Math.round(bytes * 100) / 100, units[scale] + "B");
        }
        return convertToBestScale(bytes / 1024., scale + 1);
    }

    /** -------- Functii de baza, supraincarcate -------- **/
    @Override
    public String toString() {
        StorageQuantity storageRecord;
        Pair<Double, String> converted = null;
        StringBuilder stringBuilder = new StringBuilder();
        synchronized (this.storageStatus) {
            for (String key : new ArrayList<>(this.storageStatus.keySet())) {
                storageRecord = this.storageStatus.get(key);
                converted = this.convertToBestScale(storageRecord.getUsedStorage(), 0);
                stringBuilder.append("\t").append(key).append(" --> ").append(converted.getFirst()).append(" ").append(converted.getSecond());
                converted = this.convertToBestScale(storageRecord.getTotalStorage(), 0);
                stringBuilder.append("/").append(converted.getFirst()).append(" ").append(converted.getSecond()).append("  \n");
            }
            stringBuilder.append("------------------------------------\n\n");
        }
        return "------------------------------------\n" +
                "Node Storage Quantity Status\n" + stringBuilder.toString();
    }
}
