package storage_quantity;

import config.AppConfig;

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


    /** -------- Functii de baza, supraincarcate -------- **/
    @Override
    public String toString() {
        return "------------------------------------\n" +
                "Node Storage Quantity Status\n" +
                super.toString();
    }
}
