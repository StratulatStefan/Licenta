package model;

import java.io.Serializable;

public class VersionData implements Serializable {
    private String timestamp;
    private String versionName;
    private long hash;
    private String description;

    public VersionData(String timestamp, String versionName, long hash, String description){
        this.timestamp = timestamp;
        this.versionName = versionName;
        this.hash = hash;
        this.description = description;
    }

    public VersionData(){}

    public VersionData(String metadataFileRegister){
        String[] fields = metadataFileRegister.split(" ");
        this.timestamp = fields[0];
        this.versionName = fields[1];
        this.hash = Long.parseLong(fields[2]);
        StringBuilder stringBuilder = new StringBuilder();
        for(String field : fields) {
            stringBuilder.append(field).append(" ");
        }
        this.description = stringBuilder.toString().replace("{", "").replace("}","");
    }

    public long getHash() {
        return hash;
    }

    public void setHash(long hash) {
        this.hash = hash;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getVersionNumber(){
        return Integer.parseInt(versionName.substring(1));
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setVersionNumber(int versionNumber){
        this.versionName = "v" + versionNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return timestamp + " " + versionName + " " + hash + " {" + description + "}";
    }
}
