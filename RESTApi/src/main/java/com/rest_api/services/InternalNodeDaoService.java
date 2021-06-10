package com.rest_api.services;

import com.rest_api.interfaces.InternalNodeDao;
import com.rest_api.model.InternalNode;
import com.rest_api.sql_handler.MySQLManager;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InternalNodeDaoService implements InternalNodeDao {
    MySQLManager<InternalNode> mySQLManager = new MySQLManager<InternalNode>();

    /**
     * ============== CREATE ==============
     */
    @Override
    public void insertInternalNode(InternalNode internalNode) throws Exception {
        if(internalNode.getStatus() == null){
            internalNode.setStatus("OFF");
        }
        mySQLManager.insert(internalNode);
    }


    /**
     * ============== RETRIEVE ==============
     */
    @Override
    public List<InternalNode> getAllInternalNodes() throws NullPointerException {
        List<InternalNode> userTypes = mySQLManager.findlAll(InternalNode.class);
        if(userTypes.size() == 0){
            throw new NullPointerException("No internal node found!");
        }
        return userTypes;
    }

    @Override
    public InternalNode getInternalNode(String ipAddress) throws NullPointerException {
        InternalNode internalNode = (InternalNode)mySQLManager.findByPrimaryKey(InternalNode.class, ipAddress);
        if(internalNode == null)
            throw new NullPointerException(String.format("Internal node with address %s not found!", ipAddress));
        return internalNode;
    }

    @Override
    public List<InternalNode> getInternalNodesByCountry(String country) throws NullPointerException {
        Map<String, Object> criteria = new HashMap<String, Object>(){{
            put("location_country", country);
        }};
        List<InternalNode> internalNodes = mySQLManager.findByCriteria(InternalNode.class, criteria)
                .stream()
                .map(node -> (InternalNode)node)
                .collect(Collectors.toList());
        if(internalNodes.size() == 0)
            throw new NullPointerException("No internal node found from " + country);
        return internalNodes;
    }


    /**
     * ============== UPDATE ==============
     */
    @Override
    public void updateInternalNodeStatus(String ipAddress, String newStatus) throws Exception {
        InternalNode internalNode = getInternalNode(ipAddress);
        internalNode.setStatus(newStatus);
        mySQLManager.update(internalNode);
    }

    @Override
    public void updateInternalNodeCountry(String ipAddress, String newCountry) throws Exception {
        InternalNode internalNode = getInternalNode(ipAddress);
        internalNode.setLocation_country(newCountry);
        mySQLManager.update(internalNode);
    }


    /**
     * ============== DELETE ==============
     */
    @Override
    public void deleteInternalNode(String ipAddress) throws Exception {
        int deleteStatus = mySQLManager.remove(InternalNode.class, ipAddress);
        if(deleteStatus == 1)
            throw new NullPointerException("Internal node not found!");
    }
}
