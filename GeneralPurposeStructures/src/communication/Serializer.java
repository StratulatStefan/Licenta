package communication;
import java.io.*;

/**
 * <ul>
 * 	<li> Clasa folosita pentru transmiterea obiectelor prin canalul de comunicatie.</li>
 * 	<li> Are rolul de serializa si deserializa obiectul.</li>
 * </ul>
 */
public class Serializer {
    /**
     * Dimensiunea unui pachet de date.
     */
    private final static int bufferSize = 16184;

    /**
     * <ul>
     * 	<li> Functia serializeaza un obiect primit ca parametru.</li>
     * 	<li> Se tine cont de faptul ca orice mesaj trimis prin retea trebuie sa aiba bufferSize octeti.</li>
     * 	<li> Asadar, acolo unde este cazul, se completeaza cu zerouri.</li>
     * 	<li> Obiect -> Stream binar care poate fi transmis prin canalul de comunicatie.</li>
     * </ul>
     * @param object Obiectul ce se doreste a fi serializat
     * @return Stream-ul binar.
     */
    public static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();

        byte[] content = byteArrayOutputStream.toByteArray();
        System.out.println("----------------------> " + content.length + " <----------------------");
        byte[] data = new byte[bufferSize - content.length];
        byte[] result = new byte[bufferSize];
        System.arraycopy(content, 0, result, 0,  content.length);
        System.arraycopy(data, 0 , result, content.length, bufferSize - content.length);

        objectOutputStream.close();
        byteArrayOutputStream.close();

        return result;
    }

    /**
     * <ul>
     * 	<li> Functie care deserializeaza <strong>transforma</strong> un stream binar de date, intr-un obiect.</li>
     * 	<li>  Conversia obiectului la tipul dorit se face la folosire.</li>
     * </ul>
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

    public static long getObjectSize(Object object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();

        byte[] content = byteArrayOutputStream.toByteArray();
        return content.length;
    }
}
