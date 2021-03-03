package model;

import java.util.List;

public class ClientCommunicationManager {
    /**
     * Enum care va cuprinde toate tipurile de interactiune cu clientul
     */
    public enum ClientRequest{
        NEW_FILE
    }

    /**
     * Functie care interpreteaza mesajul de la client si returneaza tipul de operatie solicitat
     * @param message Mesajul de la client
     * @return
     */
    public ClientRequest parseMessageFromClient(String message){
        if(message.contains("newfile")){
            return ClientRequest.NEW_FILE;
        }
        return null;
    }

    /**
     * Functie care va genera lantul de noduri la care se va stoca un fisier nou aparut in sistem
     * @param connectionTable Tabela conexiunilor (noduri disponibile)
     * @param message Mesajul clientului ce va cuprinde numele fisierului, dimensiunea fisierului si numarul
     *                de replici necesare
     * @return Lantul de noduri la care se va stoca fisierului
     */
    public String generateChain(ConnectionTable connectionTable, String message){
        String filesize = message.split("\\|")[0].split("\\s")[1];
        int replication_factor = Integer.parseInt(message.split("\\|")[1].split("\\s")[1]);
        System.out.println("User uploaded a new file with size : " + filesize + " and replication factor : " + replication_factor);
        List<String> connectionAddresses = connectionTable.getConnectionTable();
        if(connectionAddresses.size() <  replication_factor){
            System.out.println("Nu sunt suficiente noduri disponibile");
        }
        else{
            System.out.println("Generam token-ul..");
            String token = "";
            // criteriu de selectare a nodurilor
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

}
