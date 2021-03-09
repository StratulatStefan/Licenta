import model.ConnectionTable;

import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ClientCommunicationManager {
    /**
     * Enum care va cuprinde toate tipurile de interactiune cu clientul
     */
    public enum ClientRequest{
        NEW_FILE
    }

    public enum ClientRequestStatus{
        OK,
        FILE_ALREADY_EXISTS
    }

    /**
     * Colectie care va contine
     */
    public static final ConcurrentHashMap<String, HashMap<String, String[]>> contentTable = new ConcurrentHashMap();

    /**
     * Functie care identifica tipul operatiei solicitate de utilizator
     * @param operation String-ul ce identifica operatia
     * @return Tipul operatiei sau null daca nu s-a identificat nicio operatie valida
     */
    public ClientRequest getOperationType(String operation){
        if(operation.equals("newfile")){
            return ClientRequest.NEW_FILE;
        }
        return null;
    }

    /**
     * Functie care va genera lantul de noduri la care se va stoca un fisier nou aparut in sistem
     * @param connectionTable Tabela conexiunilor (noduri disponibile)
     * @param filesize Dimensiunea fisierului ce va fi stocat
     * @param replication_factor Factorul de replicare al fisierului
     * @return Lantul de noduri la care se va stoca fisierului
     */
    public String generateChain(ConnectionTable connectionTable, int filesize, int replication_factor){
        System.out.println("User uploaded a new file with size : " + filesize + " and replication factor : " + replication_factor);
        List<String> connectionAddresses = connectionTable.getConnectionTable();
        if(connectionAddresses.size() <  replication_factor){
            System.out.println("Nu sunt suficiente noduri disponibile");
        }
        else{
            System.out.println("Generam token-ul..");
            String token = "";
            /* !!!!!! criteriu de selectare a nodurilor !!!!!! */
            for(String address : connectionAddresses){
                if(replication_factor == 0){
                    break;
                }
                token = token + address + "-";
                replication_factor -= 1;
            }
            token = token.substring(0, token.length() - 1);
            return token;
        }
        return null;
    }

    /**
     * Functie apealata daca s-a solicitat adaugarea unui nou fisier, astfel incat nodul general
     * sa cunoasca fisierul si nodurile la care este stocat
     * @param chain (nodurile catre care se va trimite fisierul
     * @param user Id-ul utilizatorului ce face solicitarea
     * @param filename Numele fisierului nou
     */
    public void registerUserNewFileRequest(String chain, String user, String filename){
        synchronized (contentTable){
            if(contentTable.containsKey(user)){
                HashMap<String, String[]> existent = contentTable.get(user);
                existent.put(filename, chain.split("-"));
                contentTable.put(user, existent);
            }
            else {
                HashMap<String, String[]> fileMapping = new HashMap<>();
                fileMapping.put(filename, chain.split("-"));
                contentTable.put(user, fileMapping);
            }
        }
    }

    public ClientRequestStatus checkFileStatus(String user, String filename){
        synchronized (contentTable){
            if(contentTable.containsKey(user)){
                HashMap<String, String[]> existent = contentTable.get(user);
                for(String fname : existent.keySet()){
                    if(fname.equals(filename)){
                        System.out.println("File already exists!");
                        return ClientRequestStatus.FILE_ALREADY_EXISTS;
                    }
                }
            }
        }
        return ClientRequestStatus.OK;
    }
}
