import client_node.FileHeader;
import communication.Serializer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class InternalNodeCommunicationManager {

    public Socket generateNewFileCommunication(String destinationIP, int port) throws IOException {
        return new Socket(destinationIP, port);
    }

    public DataOutputStream generateNewFileDataStream(Socket socket, FileHeader header) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println("Next node in chain header : " + header.getToken());
        dataOutputStream.write(Serializer.serialize(header));
        return dataOutputStream;
    }

    public void sendDataChunk(OutputStream dataOutputStream, byte[] buffer, int read) throws IOException {
        dataOutputStream.write(buffer, 0, read);
    }
}
