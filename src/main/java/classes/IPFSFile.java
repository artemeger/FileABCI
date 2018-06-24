package classes;

import java.io.Serializable;

public class IPFSFile implements Serializable{

    private final String name;
    private final String sender;
    private final String reciever;
    private final String type;
    private final String ipfshash;
    private final byte [] secretKey;
    private final long time;

    public IPFSFile(String name, String sender, String reciever, String type, String ipfshash, byte [] secretKey){
        this.name = name;
        this.sender = sender;
        this.reciever = reciever;
        this.type = type;
        this.ipfshash = ipfshash;
        this.secretKey = secretKey;
        this.time = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public String getSender() {
        return sender;
    }

    public String getReciever() {
        return reciever;
    }

    public String getType() {
        return type;
    }

    public String getIpfshash() {
        return ipfshash;
    }

    public byte[] getSecretKey() {
        return secretKey;
    }

}