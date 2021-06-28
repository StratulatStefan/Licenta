package http;

import com.fasterxml.jackson.databind.ObjectMapper;
import http.HTTPMethod;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * <ul>
 * 	<li> Clasa care inglobeaza toate functionalitatile necesare efectuarii de operatii caracteristice HTTP <strong>mai exact REST API</strong>
 * 	     cum ar fi trimiterea unei cereri de PUT/POST si citirea raspunsului cererii.</li>
 * 	<li> In cadrul aplicatiei, se foloseste in contextul logging-ului, care se va efectua pe un server separat, cu o baza de date proprie.</li>
 * 	<li> Prin intermediul acestei clase, se vor trimite mesajele ce dorim a fi stocate.</li>
 * </ul>
 */
public class HttpConnectionService {
    /**
     * <ul>
     * 	<li> ObjectMapper-ul are rolul de a face conversia json <-> hashmap.</li>
     * 	<li> Avem nevoie de aceasta conversie, avand in vedere <strong>conform REST API</strong> ca corpul cererilor HTTP este in format JSON.</li>
     * </ul>
     */
    private final ObjectMapper jackson = new ObjectMapper();

    /**
     * <ul>
     * 	<li> Functie care genereaza o conexiune HTTP cu serverul aflat la distanta.</li>
     * 	<li> In general, o astfel de conexiune este folosita in contextul unei singure sesiuni <strong>singura cerere</strong>.</li>
     * 	<li> Conexiunea pregateste cererea ce se va trimite, setand tipul cererii.</li>
     * </ul>
     * @param urlink URL-ul
     * @param method Metoda HTTP ce se va efectua (<strong>GET, PUT, POST, DELETE</strong>)
     * @return Conexiune de tip HTTP, in cadrul careia se va trimite cererea si se va astepta raspunsul.
     */
    private HttpURLConnection generateConnection(String urlink, HTTPMethod method) throws IOException {
        URL url = new URL(urlink);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod(method.toString());
        return connection;
    }

    /**
     * <ul>
     * 	<li> Functie prin care se citeste raspunsul unei cereri de tip HTTP.</li>
     * 	<li> Se va furniza obiectul de tip conexiune <strong>HttpURLConnection</strong>.</li>
     * </ul>
     * @param connection Conexiune de tip HTTP, in cadrul careia se va trimite cererea si se va astepta raspunsul.
     * @return Raspunsul cererii
     */
    private String readResponse(HttpURLConnection connection) throws IOException{
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }

    /**
     * <ul>
     * 	<li>Functie care efectueaza o cerere de tip PUT.</li>
     * </ul>
     * @param urlink URL-ul pe care este mapata cererea
     * @param data Corpul cererii.
     */
    public String putRequest(String urlink, Map<String, Object> data) throws IOException{
        return createOrUpdateRequest(urlink, data, HTTPMethod.HTTP_PUT);
    }

    /**
     * <ul>
     * 	<li>Functie care efectueaza o cerere de tip POST.</li>
     * </ul>
     * @param urlink URL-ul pe care este mapata cererea
     * @param data Corpul cererii.
     */
    public String postRequest(String urlink, Map<String, Object> data) throws IOException{
        return createOrUpdateRequest(urlink, data, HTTPMethod.HTTP_POST);
    }

    /**
     * <ul>
     * 	<li>Functie care creeaza o cerere de tip HTTP.</li>
     * 	<li> Se creeaza obiectul de conexiune <strong>HttpURLConnection</strong>.</li>
     * 	<li> Se serializeaza corpul cererii folosind modulul <strong>Jackson</strong>.</li>
     * 	<li> Se trimitere cererea prin scrierea corpului in obiectul de conexiune.</li>
     * </ul>
     * @param urlink URL-ul pe care este mapata cererea
     * @param data Corpul cererii
     * @param method Tipul cererii HTTP
     */
    private String createOrUpdateRequest(String urlink, Map<String, Object> data, HTTPMethod method) throws IOException{
        String jsonData = jackson.writeValueAsString(data);
        
        byte[] postData = jsonData.getBytes(StandardCharsets.UTF_8);
        HttpURLConnection connection = this.generateConnection(urlink, method);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", Integer.toString(jsonData.length()));
        try( DataOutputStream wr = new DataOutputStream( connection.getOutputStream())) {
            wr.write( postData );
        }
        return this.readResponse(connection);
    }

}
