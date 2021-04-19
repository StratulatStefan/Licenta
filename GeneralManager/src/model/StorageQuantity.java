package model;

import data.Pair;

/**
 * Clasa care descrie cantitatea stocarii unei entitati;
 * Entitate : user/nod intern
 */
public class StorageQuantity{
    /** -------- Atribute -------- **/
    /**
     * Cantitatea totala de memorie disponibila pentru o entitate
     */
    private long totalStorage;
    /**
     * Cantitatea de memorie ramasa disponibila pentru o entitate
     */
    private long usedStorage = 0;

    /** -------- Gettere & Settere -------- **/
    /**
     * Getter pentru cantitatea ramasa de memorie
     */
    public long getUsedStorage() {
        return usedStorage;
    }

    /**
     * Setter pentru cantitatea ramasa de memorie
     * @param consumedStorage Cantitatea de memorie consumata.
     *
     */
    public void updateUsedStorage(long consumedStorage) {
        //if(this.availableStorage == -1)
        //    this.availableStorage = this.totalStorage;
        this.usedStorage = consumedStorage;
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