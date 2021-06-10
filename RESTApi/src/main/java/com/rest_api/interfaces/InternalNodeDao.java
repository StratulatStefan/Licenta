package com.rest_api.interfaces;

import com.rest_api.model.InternalNode;

import java.util.List;

public interface InternalNodeDao {
    /**
     * ============== CREATE ==============
     */
    void insertInternalNode(InternalNode internalNode) throws Exception;

    /**
     * ============== RETRIEVE ==============
     */
    List<InternalNode> getAllInternalNodes() throws NullPointerException;
    InternalNode getInternalNode(String ipAddress) throws NullPointerException;
    List<InternalNode> getInternalNodesByCountry(String country) throws NullPointerException;


    /**
     * ============== UPDATE ==============
     */
    void updateInternalNodeStatus(String ipAddress, String newStatus) throws Exception;
    void updateInternalNodeCountry(String ipAddress, String newCountry) throws Exception;

    /**
     * ============== DELETE ==============
     */
    void deleteInternalNode(String ipAddress) throws Exception;

}
