package communication;
import java.io.*;

/**
 * Clasa folosita pentru transmiterea obiectelor prin canalul de comunicatie;
 * Are rolul de serializa si deserializa obiectul
 */
public class Serializer {
    private static int bufferSize = 4096;
    /** -------- Functiile de serializare si deserializare -------- **/
    /**
     * Functia serializeaza un obiect primit ca parametru. Se tine cont de faptul ca orice mesaj
     * trimis prin retea trebuie sa aiba bufferSize octeti; Asadar, acolo unde este cazul, se completeaza
     * cu zerouri.
     * Obiect -> Stream binar care poate fi transmis prin canalul de comunicatie
     * @param object Obiectul ce se doreste a fi serializat
     * @return Stream-ul binar.
     */
    public static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();

        byte[] content = byteArrayOutputStream.toByteArray();
        byte[] data = new byte[bufferSize - content.length];
        byte[] result = new byte[bufferSize];
        System.arraycopy(content, 0, result, 0,  content.length);
        System.arraycopy(data, 0 , result, content.length, bufferSize - content.length);

        objectOutputStream.close();
        byteArrayOutputStream.close();

        return result;
    }

    /**
     * Functie care deserializeaza (transforma) un stream binar de date, intr-un obiect.
     * @param bytestream Stream-ul binar de date
     * @return Obiectul construit pe baza stream-ului de date.
     */
    public static Object deserialize(byte[] bytestream) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytestream);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Object obj = objectInputStream.readObject();
        objectInputStream.close();
        byteArrayInputStream.close();
        return obj;
    }
}
