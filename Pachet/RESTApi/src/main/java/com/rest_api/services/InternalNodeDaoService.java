package com.rest_api.services;

import com.rest_api.interfaces.InternalNodeDao;
import com.rest_api.model.InternalNode;
import com.rest_api.sql_handler.MySQLManager;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <ul>
 * 	<li>Serviciul care expune toate operatiile de tip CRUD specifice obiectului <strong>InternalNode</strong>.</li>
 * </ul>
 */
@Service
public class InternalNodeDaoService implements InternalNodeDao {
    /**
     * Obiectul de tip <strong>EntityManager</strong> care va asigura comunicarea cu serverul de baze de date.
     * Obiectul va expune toate metodele necesare persistarii, accesari si prelucrarii inregistrarilor din tabela <strong>InternalNode.</strong>
     */
    MySQLManager<InternalNode> mySQLManager = new MySQLManager<InternalNode>();

    /**
     * ============== CREATE ==============
     */
    /**
     * <ul>
     * 	<li> Crearea unui nou nod intern si persistarea acestuia in baza de date.</li>
     * </ul>
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
    /**
     * <ul>
     * 	<li>Extragerea tututor nodurilor interne, sub forma unei liste.</li>
     * </ul>
     */
    @Override
    public List<InternalNode> getAllInternalNodes() throws NullPointerException {
        List<InternalNode> userTypes = mySQLManager.findlAll(InternalNode.class);
        if(userTypes.size() == 0){
            throw new NullPointerException("No internal node found!");
        }
        return userTypes;
    }

    /**
     * <ul>
     * 	<li>Extragerea unui anumit nod intern pe baza adresei IP.</li>
     * </ul>
     */
    @Override
    public InternalNode getInternalNode(String ipAddress) throws NullPointerException {
        InternalNode internalNode = (InternalNode)mySQLManager.findByPrimaryKey(InternalNode.class, ipAddress);
        if(internalNode == null)
            throw new NullPointerException(String.format("Internal node with address %s not found!", ipAddress));
        return internalNode;
    }

    /**
     * <ul>
     * 	<li>Extragerea tuturor nodurilor interne localizate intr-o anumita <strong>tara</strong>.</li>
     * </ul>
     */
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
    /**
     * <ul>
     * 	<li>Modificarea statusului nodului intern, identificat pe baza adresei IP.</li>
     * </ul>
     */
    @Override
    public void updateInternalNodeStatus(String ipAddress, String newStatus) throws Exception {
        InternalNode internalNode = getInternalNode(ipAddress);
        internalNode.setStatus(newStatus);
        mySQLManager.update(internalNode);
    }

    /**
     * <ul>
     * 	<li>Modificarea locatiei nodului intern, identificat pe baza adresei IP.</li>
     * </ul>
     */
    @Override
    public void updateInternalNodeCountry(String ipAddress, String newCountry) throws Exception {
        InternalNode internalNode = getInternalNode(ipAddress);
        internalNode.setLocation_country(newCountry);
        mySQLManager.update(internalNode);
    }


    /**
     * ============== DELETE ==============
     */
    /**
     * <ul>
     * 	<li>Stergerea unui nod intern pe baza adresei IP</li>
     * </ul>
     */
    @Override
    public void deleteInternalNode(String ipAddress) throws Exception {
        int deleteStatus = mySQLManager.remove(InternalNode.class, ipAddress);
        if(deleteStatus == 1)
            throw new NullPointerException("Internal node not found!");
    }
}
