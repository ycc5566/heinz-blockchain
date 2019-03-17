/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 *
 * @author eason
 */
@WebServlet(name = "BlockChainService", urlPatterns = {"/BlockChainService/*"})
public class BlockChainService extends HttpServlet {

    // This arraylist holds blocks
    private BlockChain bc;

    @Override
    public void init() throws ServletException {
        super.init(); //To change body of generated methods, choose Tools | Templates.
        // initial the blockchain and genesis block
        BlockChain bc = new BlockChain();
        this.bc = bc;
        Block genesisBlock = new Block(bc.getChainSize(), bc.getTime(), "Genesis", 2);
        bc.addBlock(genesisBlock);
    }

    // RSA decryption
    private String decryptRSA(String signature) {
        
        BigInteger e = new BigInteger("65537");
        BigInteger n = new BigInteger("2688520255179015026237478731436571621031218154515572968727588377065598663770912513333018006654248650656250913110874836607777966867106290192618336660849980956399732967369976281500270286450313199586861977623503348237855579434471251977653662553");
        // decrypt RSA using public key
        BigInteger c = new BigInteger(signature);
        String decryptedsignature = c.modPow(e, n).toString();
        return decryptedsignature;
    }
    
    private String hashMessage(String realTransaction) {
        
        String serverHash = null;
        try {
            // hash the real transaction with SHA-256
            byte[] forSign = null;
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            forSign = messageDigest.digest(realTransaction.getBytes());
            
            // add 0 in the index[o] of the byte array
            byte[] RSASignArray = new byte[forSign.length + 1];
            RSASignArray[0] = '0';
            System.arraycopy(forSign, 0, RSASignArray, 1, forSign.length);
            BigInteger sign = new BigInteger(RSASignArray);
            serverHash = sign.toString();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(BlockChainService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return serverHash;
    }
    
    // GET returns a value given a key
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("Console: doGET visited");

        String result = "";
        
        // The name is on the path /name so skip over the '/'
        String name = (request.getPathInfo()).substring(1);
        
        PrintWriter out = response.getWriter();
        // deal with view blockchain
        if(name.equals("")) {
            
            // do view blockchain
            result = bc.toString();
        } else {

            // do verify blockchain
            long verifyStartTime = System.currentTimeMillis();
            boolean isValid = bc.isChainValid();
            long verifyEndTime = System.currentTimeMillis();

            // Things went well so set the HTTP response code to 200 OK
            result = "Chain verification: " + isValid + "\n"
                    + "Total execution time required to verify the chain was " + (verifyEndTime - verifyStartTime) + " milliseconds";
        }
        
        response.setStatus(200);
        // return the value from a GET request
        out.println(result);
        // tell the client the type of the response
        response.setContentType("text/plain;charset=UTF-8");
        return;
    }
    
    // POST is used to create a new variable
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("Console: doPost visited");
        
        // To look at what the client accepts examine request.getHeader("Accept")
        // We are not using the accept header here.
        
        // Read what the client has placed in the POST data area
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String data = br.readLine();
        
        // deal with received json object
        JSONObject jSONObject = new JSONObject(data); // parse whole object
        String dataWithSignature = jSONObject.getString("Tx").trim(); // get sensor ID
        String difficulty = jSONObject.getString("difficulty").trim(); // get longitude
        // prepare for verify signature
        String[] dataAndHashArray = dataWithSignature.split("#");
        String realTransaction = dataAndHashArray[0];
        String verifyHash = dataAndHashArray[1];
        
        PrintWriter out = response.getWriter();
        // handle transaction verification
        if (hashMessage(realTransaction).equals(decryptRSA(verifyHash))) {
            System.out.println("Verified"); // for debugging
            Block newBlock = new Block(bc.getChainSize(), bc.getTime(), dataWithSignature, Integer.parseInt(difficulty));
            long addBlockStartTime = System.currentTimeMillis();
            bc.addBlock(newBlock);
            long addBlockEndTime = System.currentTimeMillis();
            response.setStatus(200);
            out.println("Total execution time to add this block was " + (addBlockEndTime - addBlockStartTime) + " milliseconds");
        } else {
            response.setStatus(401);
            System.out.println("Not verified"); // for debugging
            out.println("This signature is not valid. Please re-sign and submit.");
        }
        return;
    }  
}
