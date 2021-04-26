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

public class HttpConnectionService {
    private final ObjectMapper jackson = new ObjectMapper();

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
