package storage_quantity;

import config.AppConfig;
import data.Pair;
import model.StorageQuantity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clasa care va contine detaliile referitoare la cantitatile de stocare disponibile pentru nodurile interne;
 */
public class NodeStorageQuantityTable extends StorageQuantityTable {
    /** -------- Attribute -------- **/
    /**
     * Cantitatea de memorie disponibila pentru un nod intern
     */
    private long internalNodeTotalStorageQuantity;


    /** -------- Constructor & Configurare -------- **/
    /**
     * Functie care citeste si initializeaza parametrii de configurare
     */
    public void readConfigParams(){
        internalNodeTotalStorageQuantity = convertGBtoBytes(Integer.parseInt(AppConfig.getParam("internalNodeStorageQuantity")));
    }

    /**
     * Constructorul clasei;
     * Citeste si instantiaza parametrii de configurare
     */
    public NodeStorageQuantityTable(){
        super();
        readConfigParams();
    }


    /** -------- Functii de prelucrare a tabelei -------- **/
    /**
     * Functie de adaugare a unei noi inregistrari in tabela de stocare a nodurilor.
     */
    public void addNewNodeStorageRecord(String nodeAddress, long consumedMemory) throws Exception {
        this.addNewRegister(nodeAddress, internalNodeTotalStorageQuantity, consumedMemory);
    }

    /**
     * Functie de modificare a cantitatii de memorie disponibile pentru un anumit nod;
     * Se are in vedere consumul de memorie (adauarea unui fisier)
     * @throws Exception Exceptie generata daca inregistrarea cu cheia data nu exista.
     */
    public void updateRegister(String nodeAddress, long consumedMemory) throws Exception{
        try{
            super.updateRegister(nodeAddress, consumedMemory);
        }
        catch (Exception exception){
            addNewNodeStorageRecord(nodeAddress, consumedMemory);
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
    /** -------- Functii de baza, supraincarcate -------- **/
    @Override
    public String toString() {
        return "------------------------------------\n" +
                "Node Storage Quantity Status\n" +
                super.toString();
    }
}
