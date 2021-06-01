package model;

import data.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Clasa care inglobeaza atributele fiecarui fisier
 */
public class FileAttributesForStorage implements Serializable {
    /** -------- Atribute -------- **/
    private String userId;
    /**
     * Numele fisierului
     */
    private String filename;
    /**
     * Lista nodurilor pe care se afla fisierului
     */
    private List<Pair<String, FileVersionData>> nodes;


    /** -------- Constructor -------- **/
    public FileAttributesForStorage(){
        this.nodes = new ArrayList<Pair<String, FileVersionData>>();
    }


    /** -------- Gettere & Settere -------- **/
    /**
     * Getter pentru numele fisierului
     */
    public String getFilename() {
        return filename;
    }
    /**
     * Setter pentru numele fisierului
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Getter pentru CRC
     */
    public long getCrc(String nodeAddress) throws NullPointerException{
        for(Pair<String, FileVersionData> node : nodes){
            if(node.getFirst().equals(nodeAddress)){
                return node.getSecond().getCrc();
            }
        }
        throw new NullPointerException("getCRC : Node not found!");
    }
    /**
     * Setter pentru CRC
     */
    public void setCrc(String nodeAddress, long crc) throws NullPointerException{
        for(Pair<String, FileVersionData> node : nodes){
            if(node.getFirst().equals(nodeAddress)){
                node.getSecond().setCrc(crc);
                return;
            }
        }
        throw new NullPointerException("setCRC : Node not found!");
    }


    /**
     * Getter pentru Numarul versiunii
     */
    public String getVersionNo(String nodeAddress) throws NullPointerException{
        for(Pair<String, FileVersionData> node : nodes){
            if(node.getFirst().equals(nodeAddress)){
                return node.getSecond().getVersionNo();
            }
        }
        throw new NullPointerException("getVersionNo : Node not found!");
    }

    /**
     * Setter pentru numarul versiunii
     */
    public void setVersionNo(String nodeAddress, String versionNo) throws NullPointerException {
        for(Pair<String, FileVersionData> node : nodes){
            if(node.getFirst().equals(nodeAddress)){
                node.getSecond().setVersionNo(versionNo);
                return;
            }
        }
        throw new NullPointerException("setVersionNo : Node not found!");
    }

    /**
     * Getter pentru lista nodurilor
     */
    public List<Pair<String, FileVersionData>> getNodes() {
        return nodes;
    }
    /**
     * Getter pentru adresele nodurilor
     */
    public List<String> getNodesAddresses(){
        List<String> addresses = new ArrayList<>();
        for(Pair<String, FileVersionData> node : this.nodes){
            addresses.add(node.getFirst());
        }
        return addresses;
    }
    /**
     * Getter pentru CRC pentru fisierul de la fiecare nod
     */
    public List<Long> getNodesCRCs(){
        List<Long> crcs = new ArrayList<>();
        for(Pair<String, FileVersionData> node : this.nodes){
            crcs.add(node.getSecond().getCrc());
        }
        return crcs;
    }

    /**
     * Getter pentru CRC pentru fisierul de la fiecare nod
     */
    public List<String> getNodesVersions(){
        List<String> versions = new ArrayList<>();
        for(Pair<String, FileVersionData> node : this.nodes){
            versions.add(node.getSecond().getVersionNo());
        }
        return versions;
    }
    /**
     * Getter pentru dimensiunea listei de noduri
     */
    public int getNodeListSize(){
        return this.nodes.size();
    }
    /**
     * Setter pentru lista nodurilor.
     */
    public void setNodes(List<Pair<String, FileVersionData>> nodes) {
        this.nodes = nodes;
    }


    /** -------- Functii de prelucrare -------- **/
    /**
     * Functie de adaugare a unui nou nod in lista
     * @param nodeAddress Adresa nodului
     */
    public void addNode(String nodeAddress, long crc, String versionNo) throws Exception{
        if(!this.containsAddress(nodeAddress))
            this.nodes.add(new Pair<String, FileVersionData>(nodeAddress, new FileVersionData(crc, versionNo, "")));
        else
            throw new Exception("Node already exists!");
    }

    /**
     * Functie de eliminare a unui nou nod in lista
     * @param nodeAddress Adresa nodului
     */
    public void removeNode(String nodeAddress) throws NullPointerException{
        if(this.containsAddress(nodeAddress)) {
            this.nodes.removeIf(node -> node.getFirst().equals(nodeAddress));
        }
        else
            throw new NullPointerException("Node not found!");
    }


    /** -------- Functii de validare -------- **/
    /**
     * Functie care verifica daca un anumit nod se afla in lista
     * @param nodeAddress Adresa nodului
     */
    public boolean containsAddress(String nodeAddress){
        for(Pair<String, FileVersionData> node : this.nodes){
            if(node.getFirst().equals(nodeAddress)){
                return true;
            }
        }
        return false;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** -------- Functii de baza, supraincarcate -------- **/
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\tFilename : ").append(filename).append("\n");
        for (Pair<String, FileVersionData> node : this.nodes) {
            stringBuilder.append("\t\t")
                    .append(node.getFirst())
                    .append("  [CRC : ")
                    .append(Long.toHexString(node.getSecond().getCrc()))
                    .append(" | VersionNo : ")
                    .append(node.getSecond().getVersionNo())
                    .append("]\n");
        }
        return stringBuilder.toString();
    }
}
