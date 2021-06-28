package tables;

import communication.Serializer;
import config.AppConfig;
import data.Pair;
import model.StorageQuantity;
import os.FileSystem;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Clasa care va contine detaliile referitoare la cantitatile de stocare disponibile pentru nodurile interne;
 */
public class NodeStorageQuantityTable{
    /**
     * Cantitatea de memorie disponibila pentru un nod intern
     */
    private long internalNodeTotalStorageQuantity = Integer.parseInt(AppConfig.getParam("internalNodeStorageQuantity"));// * (1 << 30);
    /**
     * Tabela de status a memoriei;
     */
    public final HashMap<String, StorageQuantity> storageStatus;

    /**
     * <ul>
     * 	<li>Constructorul clasei.</li>
     * 	<li>Citeste si instantiaza parametrii de configurare.</li>
     * </ul>
     */
    public NodeStorageQuantityTable(){
        this.storageStatus = new HashMap<>();
    }

    /**
     * <ul>
     * 	<li>Functie de adaugare a unei noi inregistrari in tabela de stocare a nodurilor.</li>
     * 	<li>Functie de adaugare a unei noi inregistrari in tabela de status.</li>
     * 	<li>Se introduce cheia de identificare a entitatii, memoria totala disponibila.</li>
     * 	<li>Totodata, se presupune ca o noua inregistrare se va introduce odata cu prima solicitarede stocare a unui fisier,
     *      motiv pentru care furnizam si cantitatea de memorie consumata.</li>
     * </ul>
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
     * <ul>
     * 	<li>Functie de modificare a cantitatii de memorie disponibile pentru un anumit nod.</li>
     * 	<li>Se are in vedere consumul de memorie <strong>adaugarea unui fisier</strong>.</li>
     * </ul>
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

    /**
     * <ul>
     * 	<li>Tabela cantitatilor de inregistrare va contine toate nodurile conectate la nodul general,
     *      impreuna cu date despre cantitatile de stocare ocupate.</li>
     * 	<li>Aceasta functie va identifica nodurile care au disponibila o anumita cantitate de memorie si
     *      le va returna in orice descrescatoare a cantitatii de memorie disponibila.</li>
     * </ul>
     * @param filesize Dimensiunea ce se doreste a fi ocupata.
     * @return Lista de noduri care au disponibila cantitatea cautata de memorie
     */
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

    /**
     * Functie care verifica daca tabela de stocare contine o anumita cheie de identificare a unei entitati.
     */
    protected boolean checkIfContainsKey(String key){
        synchronized (this.storageStatus){
            return this.storageStatus.containsKey(key);
        }
    }

    /**
     * <ul>
     * 	<li>Functie care creeaza o reprezentare a tebelei de stocare, astfel incat sa poata fitrimisa catre client.</li>
     * </ul>
     * @return Lista cu inregistrarile din tabela, organizate sub forma de <strong>HashMap</strong>
     */
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

    public String getSize(){
        try {
            long size = Serializer.getObjectSize(storageStatus);
            Pair<Double, String> sz = FileSystem.convertToBestScale(size, 0);
            return sz.getFirst() + " " + sz.getSecond();
        }
        catch (Exception exception){
            System.out.println("Nu pot calcula dimensiunea!");
            return "";
        }
    }

    @Override
    public String toString() {
        StorageQuantity storageRecord;
        Pair<Double, String> converted = null;
        StringBuilder stringBuilder = new StringBuilder();
        synchronized (this.storageStatus) {
            for (String key : new ArrayList<>(this.storageStatus.keySet())) {
                storageRecord = this.storageStatus.get(key);
                converted = FileSystem.convertToBestScale(storageRecord.getUsedStorage(), 0);
                stringBuilder.append("\t").append(key).append(" --> ").append(converted.getFirst()).append(" ").append(converted.getSecond());
                converted = FileSystem.convertToBestScale(storageRecord.getTotalStorage(), 0);
                stringBuilder.append("/").append(converted.getFirst()).append(" ").append(converted.getSecond()).append("  \n");
            }
            stringBuilder.append("------------------------------------\n\n");
        }
        return "------------------------------------\n" +
                "Node Storage Quantity Status\n" + stringBuilder.toString();
    }
}
