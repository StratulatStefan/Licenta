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
 * Clasa care inglobeaza toate functionalitatile necesare efectuarii de operatii caracteristice HTTP (mai exact REST API)
 * cum ar fi trimiterea unei cereri de PUT/POST si citirea raspunsului cererii;
 * In cadrul aplicatiei, se foloseste in contextul logging-ului, care se va efectua pe un server separat,
 * cu o baza de date proprie; Prin intermediul acestei clase, se vor trimite mesajele ce dorim a fi stocate.
 */
public class HttpConnectionService {
    /**
     * ObjectMapper-ul are rolul de a face conversia json <-> hashmap;
     * Avem nevoie de aceasta conversie, avand in vedere (conform REST API) ca corpul cererilor HTTP
     * este in format JSON
     */
    private final ObjectMapper jackson = new ObjectMapper();

    /**
     * Functie care genereaza o conexiune HTTP cu serverul aflat la distanta;
     * In general, o astfel de conexiune este folosita in contextul unei singure sesiuni (singura cerere).
     * Conexiunea pregateste cererea ce se va trimite, setand tipul cererii
     * @param urlink URL-ul
     * @param method
     * @return
     * @throws IOException
     */
    private HttpURLConnection generateConnection(String urlink, HTTPMethod method) throws IOException {
        URL url = new URL(urlink);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod(method.toString());
        return connection;
    }

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


    public String putRequest(String urlink, Map<String, Object> data) throws IOException{
        return createOrUpdateRequest(urlink, data, HTTPMethod.HTTP_PUT);
    }

    public String postRequest(String urlink, Map<String, Object> data) throws IOException{
        return createOrUpdateRequest(urlink, data, HTTPMethod.HTTP_POST);
    }

    private String createOrUpdateRequest(String urlink, Map<String, Object> data, HTTPMethod method) throws IOException{
        String jsonData = jackson.writeValueAsString(data);
        
        byte[] postData = jsonData.getBytes(StandardCharsets.UTF_8);
        HttpURLConnection connection = this.generateConnection(urlink, method);
        connection.setDoOutput(true);
        connection.setRequestProperty( "Content-Type", "application/json");
        connection.setRequestProperty( "Content-Length", Integer.toString(jsonData.length()));
        try( DataOutputStream wr = new DataOutputStream( connection.getOutputStream())) {
            wr.write( postData );
        }
        return this.readResponse(connection);
    }

}
