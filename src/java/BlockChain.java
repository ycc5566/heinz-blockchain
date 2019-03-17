/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eason
 */
public class BlockChain extends Object {

    private ArrayList<Block> blocks;
    private String latestChainHash;

    // blockchain constructor
    public BlockChain() {

        this.blocks = new ArrayList<>();
        this.latestChainHash = "";
    }

    public Timestamp getTime() {
        return new Timestamp(System.currentTimeMillis());
    }

    public Block getLatestBlock() {
        return blocks.get(blocks.size() - 1);
    }

    public int getChainSize() {
        return blocks.size();
    }

    public int hashesPerSecond() {

        int count = 0;
        String s = "00000000";
        MessageDigest messageDigest;
        byte[] data = s.getBytes();

        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            long start = System.currentTimeMillis();
            long end = start + 1000;
            
            // start hash
            while (System.currentTimeMillis() < end) {
                data = messageDigest.digest(data);
                count++;
            }
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(BlockChain.class.getName()).log(Level.SEVERE, null, ex);
        }

        return count;
    }

    public void addBlock(Block newBlock) {
        
        // set new block's previous hash
        newBlock.setPreviousHash(latestChainHash);
        // set latest chain hash in blockchain class
        latestChainHash = newBlock.proofOfWork();
        blocks.add(newBlock);
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();
        for (Block b : blocks) {
            sb.append(b.toString() + ",");
        }
        String result = "{\"ds_chain\" : [" + sb.toString().substring(0, sb.toString().length() - 1) + "], \"chainHash\":\"" + latestChainHash + "\"}";
        return result;
    }

    public boolean isChainValid() {

        if (getChainSize() == 0) {
            return false;
        }
        // verify genesis block
        if (getChainSize() == 1) {
            if (!blocks.get(0).calculateHash().equals(latestChainHash)) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < blocks.get(0).getDifficulty(); i++) {
                    sb.append("0");
                }
                System.out.println("..Improper hash on node 1 Does not begin with " + sb.toString());
                return false;
            }
        // verify the other blocks
        } else {
            // verify from node 1 to latest block
            for (int i = 1; i < getChainSize(); i++) {
                if (!blocks.get(i - 1).calculateHash().equals(blocks.get(i).getPreviousHash())) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < blocks.get(i - 1).getDifficulty(); j++) {
                        sb.append("0");
                    }
                    System.out.println("..Improper hash on node " + (i - 1) + " Does not begin with " + sb.toString());
                    return false;
                }
            }
            // verify the latestChainHash in blockchain
            if (!blocks.get(getChainSize() - 1).calculateHash().equals(latestChainHash)) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < blocks.get(getChainSize() - 1).getDifficulty(); i++) {
                    sb.append("0");
                }
                System.out.println("..Improper hash on node " + (getChainSize()) + " Does not begin with " + sb.toString());
                return false;
            }
        }
        return true;
    }

    public void repairChain() {

        // repair from node 1 to latest block
        for (int i = 1; i < getChainSize(); i++) {
            if (!blocks.get(i - 1).calculateHash().equals(blocks.get(i).getPreviousHash())) {
                blocks.get(i).setPreviousHash(blocks.get(i - 1).proofOfWork());
            }
        }
        // repair the latestChainHash in blockchain
        if (!blocks.get(getChainSize() - 1).calculateHash().equals(latestChainHash)) {
            latestChainHash = blocks.get(getChainSize() - 1).proofOfWork();
        }
    }
}


