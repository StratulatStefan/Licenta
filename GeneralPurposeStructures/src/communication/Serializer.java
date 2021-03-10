package communication;

import java.io.*;

/**
 * Clasa folosita pentru transmiterea obiectelor prin canalul de comunicatie;
 * Are rolul de serializa si deserializa obiectul
 */
public class Serializer {
    /**
     * Functia serializeaza un obiect primit ca parametru.
     * Obiect -> Stream binar care poate fi transmis prin canalul de comunicatie
     * @param object Obiectul ce se doreste a fi serializat
     * @return Stream-ul binar.
     */
    public static byte[] Serialize(Object object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
        byte[] content = byteArrayOutputStream.toByteArray();
        byte[] data = new byte[1024 - content.length];
        byte[] result = new byte[1024];
        System.arraycopy(content, 0, result, 0,  content.length);
        System.arraycopy(data, 0 , result, content.length, 1024 - content.length);
        objectOutputStream.close();
        byteArrayOutputStream.close();

        return result;
    }

    /**
     * Functie care deserializeaza (transforma) un stream binar de date, intr-un obiect.
     * @param bytestream Stream-ul binar de date
     * @return Obiectul construit pe baza stream-ului de date.
     */
    public static Object Deserialize(byte[] bytestream) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytestream);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Object obj = objectInputStream.readObject();
        objectInputStream.close();
        byteArrayInputStream.close();
        return obj;
    }
}
