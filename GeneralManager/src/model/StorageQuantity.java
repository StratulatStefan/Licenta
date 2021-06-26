package model;

import data.Pair;
import sun.plugin2.message.Serializer;

import java.io.Serializable;

/**
 * <ul>
 * 	<li>Clasa care descrie cantitatea stocarii unei entitati.</li>
 * 	<li> Entitate : user/nod intern.</li>
 * </ul>
 */
public class StorageQuantity implements Serializable {
    /**
     * Cantitatea totala de memorie disponibila pentru o entitate
     */
    private long totalStorage;
    /**
     * Cantitatea de memorie ramasa disponibila pentru o entitate
     */
    private long usedStorage = 0;

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