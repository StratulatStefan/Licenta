import model.Address;

import java.io.IOException;
import java.net.*;
import java.util.*;

/* important topic : https://stackoverflow.com/questions/19392173/multicastsocket-constructors-and-binding-to-port-or-socketaddress */
public class HearthBeatSocket extends MulticastSocket {
    private int port;
    /**
     * Constructorul unui socket de Multicast
     * @param port portul pe care se va deschide Socket-ul
     * @throws IOException
     */
    public HearthBeatSocket(int port) throws IOException {
        //super(new InetSocketAddress("192.168.0.1", port));
        super(port);
        this.port = port;
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
     * Trimiterea unui mesaj prin multicast.
     * Trebuie sa se aiba in vedere ca procesul curent sa paraseasca grupul de multicast, pentru a nu primi
     * mesajul trimis de el. Dupa trimiterea mesajului, se alatura din nou grupului, pentru a primi in continuare mesaje.
     * Totodata, mesajul se transmite sub forma unui DatagramPacket, care va ingloba atat mesajul propriu-zis, in format binar,
     * cat si adresa catre care va trimite mesajul (adresa ip + port)
     * @param group Adresa canalului de comunicatie de tip multicast.
     * @param message Mesajul care va fi trimis prin multicast.
     * @throws IOException
     */
    public void sendMessage(InetAddress group, String message) throws IOException {
        this.leaveGroup(group);
        byte[] messageContent = message.getBytes();
        DatagramPacket packet = new DatagramPacket(messageContent, messageContent.length, group, port);
        this.send(packet);
        this.joinGroup(group);
    }

    /**
     * Primirea unui mesaj prin multicast de la un alt nod al grupului.
     * Trebuie sa se aiba in vedere ca rezultatul va fi stocat intr-un obiect de tip DatagramPacket,
     * iar mesajul propriu zis va fi in format binar.
     * Totodata, aceasta functie este blocanta si (daca este setat) va genera o exceptie de Timeout daca
     * se asteapta prea mult timp in bucla de receptie.
     * @param group Adresa canalului de comunicatie de tip multicast.
     * @return Mesajul primit sub forma de string
     * @throws IOException
     */
    public String receiveMessage(InetAddress group) throws IOException{
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        this.receive(packet);
        return new String(packet.getData(), packet.getOffset(), packet.getLength());
    }
}
