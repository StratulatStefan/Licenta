package storage_quantity;

/**
 * Clasa care descrie cantitatea stocarii unei entitati;
 * Entitate : user/nod intern
 */
class StorageQuantity{
    /** -------- Atribute -------- **/
    /**
     * Cantitatea totala de memorie disponibila pentru o entitate
     */
    private long totalStorage;
    /**
     * Cantitatea de memorie ramasa disponibila pentru o entitate
     */
    private long availableStorage = -1;


    /** -------- Gettere & Settere -------- **/
    /**
     * Getter pentru cantitatea ramasa de memorie
     */
    public long getAvailableStorage() {
        return availableStorage;
    }
    /**
     * Setter pentru cantitatea ramasa de memorie
     * @param consumedStorage Cantitatea de memorie consumata.
     *
     */
    public void updateAvailableStorage(long consumedStorage) {
        if(this.availableStorage == -1)
            this.availableStorage = this.totalStorage;
        this.availableStorage -= consumedStorage;
    }

    /**
     * Getter pentru cantitatea totala de memorie
     */
    public long getTotalStorage() {
        return totalStorage;
    }
    /**
     * Setter pentru cantitatea totala de memorie
     */
    public void setTotalStorage(long totalStorage) {
        this.totalStorage = totalStorage;
    }
}