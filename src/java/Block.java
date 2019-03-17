/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author eason
 */
public class Block extends Object {

    private int index;
    private Timestamp timestamp;
    private String data;
    private String previousHash;
    private BigInteger nonce;
    private int difficulty;
    
    public Block(int index, Timestamp timestamp, String data, int difficulty) {
        
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.difficulty = difficulty;
        this.nonce = BigInteger.ZERO;
        this.previousHash = "";
        
    }
    
    public String calculateHash(){
        
        String s = index+timestamp.toString()+data+previousHash+nonce.toString()+difficulty;
        byte[] data = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            data = messageDigest.digest(s.getBytes());
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Block.class.getName()).log(Level.SEVERE, null, ex);
        }
        return DatatypeConverter.printHexBinary(data);
    }
    
    public String proofOfWork() {
        
        String proved = null;
        int count = 0;
        do {
            this.nonce = this.nonce.add(BigInteger.ONE);
            count = 0;
            proved = calculateHash();
            for (int i=0; i< difficulty;i++) {
                if (proved.charAt(i) == '0') {
                    count++;
                }
            }
        } while (difficulty != count);
        
        return proved;
    }
    
    public String getData() {
        return this.data;
    }
    
    public int getDifficulty() {
        return this.difficulty;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    public String getPreviousHash() {
        return this.previousHash;
    }
    
    public Timestamp getTimestamp() {
        return this.timestamp;
    }
    
    public void setData(String data) {
        this.data = data;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }
    
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public  String toString() {
        return "{\"index\" :"+ index + ",\"time stamp\" : \""+ timestamp.toString() + "\",\"Tx\": \"" + data + "\",\"PrevHash\" : \""+ previousHash + "\",\"nonce\":"+ nonce+",\"difficulty\":"+ difficulty +"}";
    }
}