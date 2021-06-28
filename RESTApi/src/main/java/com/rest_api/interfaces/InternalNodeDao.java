package com.rest_api.interfaces;

import com.rest_api.model.InternalNode;

import java.util.List;

/**
 * <ul>
 * 	<li>Interfata care expune toate operatiile de tip CRUD specifice obiectului <strong>InternalNode</strong>.</li>
 * </ul>
 */
public interface InternalNodeDao {
    /**
     * ============== CREATE ==============
     */
    /**
     * <ul>
     * 	<li> Crearea unui nou nod intern si persistarea acestuia in baza de date.</li>
     * </ul>
     */
    void insertInternalNode(InternalNode internalNode) throws Exception;

    /**
     * ============== RETRIEVE ==============
     */
    /**
     * <ul>
     * 	<li>Extragerea tututor nodurilor interne, sub forma unei liste.</li>
     * </ul>
     */
    List<InternalNode> getAllInternalNodes() throws NullPointerException;
    /**
     * <ul>
     * 	<li>Extragerea unui anumit nod intern pe baza adresei IP.</li>
     * </ul>
     */
    InternalNode getInternalNode(String ipAddress) throws NullPointerException;
    /**
     * <ul>
     * 	<li>Extragerea tuturor nodurilor interne localizate intr-o anumita <strong>tara</strong>.</li>
     * </ul>
     */
    List<InternalNode> getInternalNodesByCountry(String country) throws NullPointerException;

    /**
     * ============== UPDATE ==============
     */
    /**
     * <ul>
     * 	<li>Modificarea statusului nodului intern, identificat pe baza adresei IP.</li>
     * </ul>
     */
    void updateInternalNodeStatus(String ipAddress, String newStatus) throws Exception;
    /**
     * <ul>
     * 	<li>Modificarea locatiei nodului intern, identificat pe baza adresei IP.</li>
     * </ul>
     */
    void updateInternalNodeCountry(String ipAddress, String newCountry) throws Exception;

    /**
     * ============== DELETE ==============
     */
    /**
     * <ul>
     * 	<li>Stergerea unui nod intern pe baza adresei IP</li>
     * </ul>
     */
    void deleteInternalNode(String ipAddress) throws Exception;

}
