import client_node.FileHeader;
import communication.Serializer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class InternalNodeCommunicationManager {

    public Socket GenerateNewFileCommunication(String destinationIP, int port) throws IOException {
        return new Socket(destinationIP, port);
    }

    public DataOutputStream GenerateNewFileDataStream(Socket socket, FileHeader header) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println("Next node in chain header : " + header);
        dataOutputStream.write(Serializer.Serialize(header));
        return dataOutputStream;
    }

    public void SendDataChunk(OutputStream dataOutputStream, byte[] buffer, int read) throws IOException {
        dataOutputStream.write(buffer, 0, read);
    }
}
