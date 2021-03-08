import communication.Address;
import communication.FileHeader;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ClientCommunicationManager {
    /**
     * Adresa nodului curent
     */
    private Address nodeAddress;

    /**
     * Dimensiunea bufferului in care vor fi citite datele de la un nod adiacent
     */
    private final static int bufferSize = 1024;

    private final static String storagePath = "D:\\Facultate\\Licenta\\Storage\\";

    private InternalNodeCommunicationManager internalCommunicationManager;

    public ClientCommunicationManager(Address address, InternalNodeCommunicationManager internalNodeCommunicationManager){
        this.nodeAddress = address;
        this.internalCommunicationManager = internalNodeCommunicationManager;
    }

    /**
     * Functie care inglobeaza activitatea de comunicare cu clientul si cu celelalte noduri din sistem;
     * Comunicarea cu clientul are in vedere receptionarea de noi fisiere (cazul in care nodul intern este primul din lant)
     * Comunicarea cu celelalte noduri interne se face in scopul trimiterii/primirii de fisiere (cazul in care nodul intern
     * nu se afla primul in lant)
     * @throws IOException Exceptie generata la crearea ServerSocket-ului.
     */
    public void ClientCommunicationLoop() throws Exception {
        ServerSocket serverSocket = new ServerSocket();
        try{
            serverSocket.bind(new InetSocketAddress(nodeAddress.getIpAddress(), nodeAddress.getPort()));
            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println(String.format("Client connected : [%s : %d]\n", clientSocket.getLocalAddress(), clientSocket.getLocalPort()));
                new Thread(ClientCommunicationThread(serverSocket, clientSocket)).start();
            }
        }
        catch (Exception exception){
            serverSocket.close();
            System.out.println(exception.getMessage());
        }
    }

    /**
     * Functie care inglobeaza comunicarea de date cu un nod adicant, avandu-se in vedere primirea de date de la un
     * nod adiacent si, eventual, trimiterea informatiilor mai departe, in cazul in care nu este nod terminal.
     * @param serverSocket Socket-ul pe care este mapat nodul curent.
     * @param clientSocket Socket-ul nodului adiacent, de la care primeste date.
     * @return Runnable-ul necesar pornirii unui thread separat pentru aceasta comunicare.
     */
    private Runnable ClientCommunicationThread(ServerSocket serverSocket, Socket clientSocket){
        return new Runnable() {
            @Override
            public void run(){
                try {
                    InputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                    OutputStream dataOutputStream = null;
                    FileOutputStream fileOutputStream = null;
                    Socket nextElementSocket = null;
                    byte[] buffer = new byte[bufferSize];
                    int read;
                    boolean header_found = false;
                    while((read = dataInputStream.read(buffer, 0, bufferSize)) > 0){
                        if(!header_found) {
                            try {
                                FileHeader header = new FileHeader(new String(buffer, StandardCharsets.UTF_8));
                                String filepath = storagePath + serverSocket.getInetAddress().getHostAddress() + "/" + header.getUserId();
                                Files.createDirectories(Paths.get(filepath ));
                                filepath += "/" + header.getFilename();
                                fileOutputStream = new FileOutputStream(filepath);
                                System.out.println("My header : " + header);
                                String token = cleanChain(header.getToken());
                                if(token != null){
                                    header.setToken(token);
                                    String nextDestination = getDestinationIpAddress(token);
                                    nextElementSocket = new Socket(nextDestination, nodeAddress.getPort());
                                    nextElementSocket = internalCommunicationManager.GenerateNewFileCommunication(nextDestination, nodeAddress.getPort());
                                    dataOutputStream = internalCommunicationManager.GenerateNewFileDataStream(nextElementSocket, header);
                                }
                                else{
                                    System.out.println("End of chain");
                                }
                                header_found = true;
                                continue;
                            } catch (Exception exception) {
                                System.out.println("Exceptie : " + exception.getMessage());
                            }
                        }
                        fileOutputStream.write(buffer, 0, read);
                        if(nextElementSocket != null){
                            internalCommunicationManager.SendDataChunk(dataOutputStream, buffer, read);
                        }
                    }
                    System.out.println("File write done");
                    dataInputStream.close();
                    fileOutputStream.close();
                    if(nextElementSocket != null) {
                        nextElementSocket.close();
                        dataOutputStream.close();
                    }
                    clientSocket.close();
                }
                catch (Exception exception){
                    System.out.println(exception.getMessage());
                    System.out.println(String.format("Could not properly close connection with my friend : [%s : %d]",
                            clientSocket.getLocalAddress(),
                            clientSocket.getLocalPort()));
                }
            }
        };
    }

    private static String cleanChain(String token){
        if(token.contains("-")){
            int delimiter = token.indexOf("-");
            return token.substring(delimiter + 1);
        }
        else{
            return null;
        }
    }

    private static String getDestinationIpAddress(String token) throws Exception{
        if(validateToken(token))
            return token.replace(" ","").split("\\-")[0];
        return null;
    }

    private static boolean validateToken(String token) throws Exception{
        if(token.length() == 0)
            throw new Exception("Null token!");
        String[] tokenItems = token.split("\\-");
        for(String tokenItem : tokenItems){
            String[] values = tokenItem.split("\\.");
            if(values.length != 4)
                throw new Exception("Invalid token! The address is not a valid IP Address (invalid length!)");
            for(String value : values){
                try{
                    int parsedValue = Integer.parseInt(value);
                    if(parsedValue < 0 || parsedValue > 255)
                        throw new Exception("Invalid token! The address is not a valid IP Address (8 bit representation of values)");
                }
                catch (NumberFormatException exception) {
                    throw new Exception("Invalid token! The address is not a valid IP Address (it should only contain numbers!)");
                }
            }
        }
        return true;
    }
}
