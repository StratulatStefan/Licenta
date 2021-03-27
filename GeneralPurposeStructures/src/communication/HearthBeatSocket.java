package communication;
import java.io.IOException;
import java.net.*;

/* important topic : https://stackoverflow.com/questions/19392173/multicastsocket-constructors-and-binding-to-port-or-socketaddress */
/* better understanding of multicastSocket concept : https://tldp.org/HOWTO/Multicast-HOWTO-2.html */

/**
 * Clasa incapsuleaza toate atributele corespunzatore unui Socket de multicast;
 * Pe langa construirea efectiva a canalului de comunicatie, avem la dispozitie si functii de trimitere/receptionare de continut.
 */
public class HearthBeatSocket extends MulticastSocket {
    /** -------- Extra-descrieri -------- **/
    /**
     * Enum care incapsuleaza principalele interfete de retea folosite
     */
    public enum NetworkInterfacesTypes {
        LOCALHOST, /** Pe acelasi dispozitiv **/
        LOCAL_PUBLIC_NETWORK /** Dispozitive diferite din cadrul aceleiasi retele locale **/
    }


    /** -------- Atribute -------- **/
    /**
     * Portul de multicast
     */
    private int multicastPort;
    /**
     * Adresa socket-ului de multicast.
     */
    private Address address;



    /** -------- Constructori & Configurare -------- **/
    /**
     * Constructorul unui socket de Multicast
     * @param address Adresa la care va face bind socket-ul procesului curent
     * @param multicastPort portul la care se face bind pentru ascultarea de mesaje din reteaua de multicast
     * @throws IOException Exceptie generata la crearea socket-ului si a adresei acestuia
     */
    public HearthBeatSocket(Address address, int multicastPort) throws IOException {
        super(new InetSocketAddress(address.getIpAddress(), multicastPort));
        this.multicastPort = multicastPort;
        this.address = address;
    }

    /**
     * Setarea timeout-ului pentru functia de receive.
     * @param timeOut numarul de milisecunde la care se genereaza exceptia de Timeout
     * @throws SocketException Exceptie generata daca programul este blocat  prea mult (timeOut ms) in bucla de receptie
     */
    public void setTimeOut(int timeOut) throws SocketException {
        this.setSoTimeout(timeOut);
    }

    /**
     * Functie pentru starea tipului interfetei retelei.
     * @param type Tipul interfetei de retea
     */
    public void setNetworkInterface(HearthBeatSocket.NetworkInterfacesTypes type){
        String networkInterfaceName = "";
        String osName = System.getProperty("os.name").toLowerCase();
        switch (type){
            case LOCALHOST: networkInterfaceName = "lo";
            case LOCAL_PUBLIC_NETWORK:
                if(osName.contains("windows")){
                    networkInterfaceName = "wlan2";
                }
                else if(osName.contains("linux") || osName.contains("raspian")){
                    networkInterfaceName = "eth2";
                }
            default: networkInterfaceName = "lo";
        }
        try {
            this.setNetworkInterface(NetworkInterface.getByName(networkInterfaceName));
        }
        catch (Exception exception){
            System.out.println("Exception occured at setting the network interface type");
        }
    }



    /** -------- Trimitere & Receptionare mesaje -------- **/

    /**
     * Trimiterea unui mesaj prin multicast.
     * Trebuie sa se aiba in vedere ca procesul curent sa paraseasca grupul de multicast, pentru a nu primi
     * mesajul trimis de el. Dupa trimiterea mesajului, se alatura din nou grupului, pentru a primi in continuare mesaje.
     * Totodata, mesajul se transmite sub forma unui DatagramPacket, care va ingloba atat mesajul propriu-zis, in format binar,
     * cat si adresa catre care va trimite mesajul (adresa ip + port)
     * @param group Adresa canalului de comunicatie de tip multicast.
     * @param message Mesajul care va fi trimis prin multicast.
     */
    public void sendBinaryMessage(InetAddress group, byte[] message) throws IOException {
        this.leaveGroup(group);
        DatagramPacket packet = new DatagramPacket(message, message.length, group, this.multicastPort);
        this.send(packet);
        this.joinGroup(group);
    }

    /**
     * Functie care foloseste functia de trimitere a mesajului binar. Insa, aceastsa functie primeste mesajul
     * ca si string si face conversia la binar, inainte de trimitere.
     * @param group Adresa canalului de comunicatie de tip multicast.
     * @param message Mesajul care va fi trimis prin multicast.
     */
    public void sendMessage(InetAddress group, String message) throws IOException {
        byte[] messageContent = message.getBytes();
        this.sendBinaryMessage(group, messageContent);
    }

    /**
     * Primirea unui mesaj prin multicast de la un alt nod al grupului.
     * Trebuie sa se aiba in vedere ca rezultatul va fi stocat intr-un obiect de tip DatagramPacket,
     * iar mesajul propriu zis va fi in format binar.
     * Totodata, aceasta functie este blocanta si (daca este setat) va genera o exceptie de Timeout daca
     * se asteapta prea mult timp in bucla de receptie.
     * @return Mesajul primit sub forma de string
     */
    public Object receiveMessage() throws IOException, ClassNotFoundException {
        byte[] buffer = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        this.receive(packet);
        return Serializer.deserialize(buffer);
    }
}
