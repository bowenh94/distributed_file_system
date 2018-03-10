package EZShare.Connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import EZShare.LogFormatter;

public class SSLEZSocket {
	private String host;
	private int secureport;
	private SSLSocket sslsocket;
	private boolean debugMode;
	
	private final static Logger logger = Logger.getLogger(SSLEZSocket.class.getName());
	
	public SSLEZSocket(String host,int secureport,boolean debugMode) {
		// TODO Auto-generated constructor stub
		this.host = host;
		this.secureport = secureport;
		this.debugMode = debugMode;
		Logger logger = Logger.getLogger(SSLEZSocket.class.getName());
		LogFormatter logformat = new LogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.FINEST);
		handler.setFormatter(logformat);
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		logger.addHandler(handler);
		try {
			System.setProperty("javax.net.ssl.keyStore", "WDCkeystore/clientKS.jks");
			System.setProperty("javax.net.ssl.keyStorePassword","456456");
			
			System.setProperty("javax.net.ssl.trustStore","WDCkeystore/trust_ks");
// 			System.setProperty("javax.net.debug","ssl,handshake");

			SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			this.sslsocket = (SSLSocket) sslsocketfactory.createSocket(host, secureport);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	public ArrayList<String> Send(String msg){
		DataOutputStream dataOut = null;
		DataInputStream dataIn = null;
		ArrayList<String> returnMsgs = new ArrayList<>();
		try {
			dataOut = new DataOutputStream(sslsocket.getOutputStream());
			dataOut.writeUTF(msg);
			if(debugMode)
				logger.log(Level.INFO,"SENT: "+ msg);
			dataOut.flush();
			
			PushbackInputStream pbi = new PushbackInputStream(sslsocket.getInputStream());
			dataIn = new DataInputStream(pbi);

			int singleByte;
			while((singleByte = pbi.read())!=-1){
				pbi.unread(singleByte);
//				System.out.println("read return msg"+dataIn.readUTF());
				returnMsgs.add(dataIn.readUTF());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(sslsocket != null)
					sslsocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(dataIn!=null)
					dataIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(debugMode){
			logger.log(Level.INFO, "RECEIVED: "+returnMsgs.toString());
		}else{
			System.out.println(returnMsgs.toString());
		}
		return returnMsgs;
	}

	

	public void Receive_Fetch() throws IOException, ParseException{
		JSONParser parserJ = new JSONParser();
		DataInputStream input = new DataInputStream(sslsocket.getInputStream());
		String serverResponse1 = input.readUTF();
		JSONObject objectIn1 = (JSONObject)parserJ.parse(serverResponse1);
    	if(objectIn1.get("response").equals("error")){
    		if(debugMode){
    			logger.log(Level.INFO, "RECEIVED: "+objectIn1.toString());
    		}else {
    	    	System.out.println(objectIn1.toString());
    		}
    	}else{
    		if(debugMode){
    			logger.log(Level.FINE, "RECEIVED: "+objectIn1.toString());
    		}else {
    	    	System.out.println(objectIn1.toString());
    		}
    		String serverResponse2 = input.readUTF();
        	JSONObject objectIn2 = (JSONObject)parserJ.parse(serverResponse2);
        	if(objectIn2.containsKey("resourceSize")){
        		if(debugMode){
        			logger.log(Level.FINE, "RECEIVED: "+objectIn2.toString());
        		}else {
        	    	System.out.println(objectIn1.toString());
        		}
        		int len = new Long((long) objectIn2.get("resourceSize")).intValue();
        		String[] get_name = objectIn2.get("uri").toString().split("/");
        	    RandomAccessFile downloadingFile = new RandomAccessFile(get_name[get_name.length-1], "rw");
        	    int fileSizeRemaining = Integer.valueOf(objectIn2.get("resourceSize").toString());
        	    int chunkSize = setChunkSize(len);
        		// Represents the receiving buffer
        		byte[] receiveBuffer = new byte[chunkSize];
        		
        		// Variable used to read if there are remaining size left to read.
        		int num;
        		
        		while((num=input.read(receiveBuffer))>0){
        			// Write the received bytes into the RandomAccessFile
        			downloadingFile.write(Arrays.copyOf(receiveBuffer, num));
        			
        			// Reduce the file size left to read..
        			fileSizeRemaining-=num;
        			
        			// Set the chunkSize again
        			chunkSize = setChunkSize(fileSizeRemaining);
        			receiveBuffer = new byte[chunkSize];
        			
        			// If you're done then break
        			if(fileSizeRemaining==0){
        				break;
        			}
        		}
        		String serverResponse3 = input.readUTF();
        		JSONObject objectIn4 = (JSONObject)parserJ.parse(serverResponse3);
            	if(debugMode){
        			logger.log(Level.FINE, "RECEIVED: "+objectIn4.toString());
        		}else{
        			System.out.println(objectIn4.toString());
        		}
            	downloadingFile.close();
        	}else{
            	if(debugMode){
        			logger.log(Level.FINE, "RECEIVED: "+objectIn2.toString());
        		}else{
        			System.out.println(objectIn2.toString());
        		}
        	}
        	
        	input.close();
    	}
    	
    	
		
	}
	
	public static int setChunkSize(long fileSizeRemaining){
		// Determine the chunkSize
		int chunkSize=1024*1024;
		
		// If the file size remaining is less than the chunk size
		// then set the chunk size to be equal to the file size.
		if(fileSizeRemaining<chunkSize){
			chunkSize=(int) fileSizeRemaining;
		}
		
		return chunkSize;
	}
	
	
	


}
