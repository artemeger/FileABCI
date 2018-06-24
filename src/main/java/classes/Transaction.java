package classes;

import java.io.Serializable;

public class Transaction implements Serializable{

    private final String owner;
    private final IPFSFile ipfsFile;

    public Transaction(String owner, IPFSFile file){
        this.owner = owner;
        this.ipfsFile = file;
    }

    public String getOwner() {
        return owner;
    }

    public IPFSFile getIpfsFile() {
        return ipfsFile;
    }
}