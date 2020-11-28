package model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

class Source implements Serializable {
    private String ipAddress;
    private int port;

    public Source(String ipAddress, int port){
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public String getIpAddress() { return this.ipAddress;}
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {return this.port;}
    public void setPort(int port){
        this.port = port;
    }
}


public class HearthBeat implements Serializable{
    private int id;
    private Source sourceAddress;
    private Date timestamp;

    public HearthBeat(int id, String ipAddress, int port, Date timestamp){
        this.id = id;
        this.sourceAddress = new Source(ipAddress, port);
        this.timestamp = timestamp;
    }

    public int getId() { return this.id; }
    public void setId(int id) {
        this.id = id;
    }

    public Source getSourceAddress() { return this.sourceAddress; }
    public void setSourceAddress(Source sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public Date getTimestamp() { return this.timestamp; }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}