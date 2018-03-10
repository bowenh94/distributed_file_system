package EZShare.Connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import EZShare.LogFormatter;

public class EZSocket {
	private String host;
	private int port;
	private Socket socket;
	private boolean debugMode;
	
	private final static Logger logger = Logger.getLogger(EZSocket.class.getName());
	
	public EZSocket(String host,int port,boolean debugMode) {
		// TODO Auto-generated constructor stub
		this.host = host;
		this.port = port;
		this.debugMode = debugMode;
		Logger logger = Logger.getLogger(EZSocket.class.getName());
		LogFormatter logformat = new LogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.FINEST);
		handler.setFormatter(logformat);
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		logger.addHandler(handler);
		try {
			this.socket = new Socket(host,port);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	public void Send(String msg){
		DataOutputStream dataOut = null;
		try {
			dataOut = new DataOutputStream(socket.getOutputStream());
			dataOut.writeUTF(msg);
			if(debugMode)
				logger.log(Level.FINE,"SENT: "+ msg);
			dataOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public ArrayList<String> Receive(){
		DataInputStream dataIn = null;
		ArrayList<String> returnMsgs = new ArrayList<>();
		try {
			PushbackInputStream pbi = new PushbackInputStream(socket.getInputStream());
			dataIn = new DataInputStream(pbi);

			int singleByte;
			while((singleByte = pbi.read())!=-1){
				pbi.unread(singleByte);
				returnMsgs.add(dataIn.readUTF());

			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				if(socket != null)
					socket.close();
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
			logger.log(Level.FINE, "RECEIVED: "+returnMsgs.toString());
		}else{
			System.out.println(returnMsgs.toString());
		}

		return returnMsgs;

		
	}
	
//	{"owner":"*","name":"Aaron","channel":"","description":"No more DoS please :-)",
//		"ezserver":"sunrise.cis.unimelb.edu.au:3781","resourceSize":1743506,
//		"uri":"file:\/\/\/usr\/local\/share\/ezshare\/photo.jpg","tags":["photo","jpeg","jpg"]}


	public void Receive_Fetch() throws IOException, ParseException{
		JSONParser parserJ = new JSONParser();
		DataInputStream input = new DataInputStream(socket.getInputStream());
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
