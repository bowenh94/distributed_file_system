package EZShare;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ServerMain {
	private ServerSocket serverSocket;
	private SSLServerSocket sslserverSocket;
	private ExecutorService executorService;
//	private ExecutorService sslexecutorService;
	private Logger logger = null;
	private int intervalLimit;
	long start;
	long end;
	public ServerMain(ServerSocket serverSocket, ExecutorService executorService, int intervalLimit, Logger logger) {
		// TODO Auto-generated constructor stub
		this.executorService = executorService;
		this.serverSocket = serverSocket;
		this.logger = logger;
	}
	public void service() throws IOException{
		Socket socket = null;
		while (true){
			try {
				socket = serverSocket.accept();
				executorService.execute(new Handler(socket,logger));
//				logger.log(Level.FINE,"Received: "+ future.get().toJSONString());
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
    }

	
}




class Handler implements Runnable{
	private JSONObject jsonObject;
	private Socket socket;
	private Logger logger = null;

	public Handler(Socket socket,Logger logger) {
		// TODO Auto-generated constructor stub
		this.socket = socket;
		this.logger = logger;
	}
	
	public void run(){
        DataInputStream br = null;
        DataOutputStream out = null;

        try {
            out = new DataOutputStream(socket.getOutputStream());
            String msg = null;
            PushbackInputStream pbi = new PushbackInputStream(socket.getInputStream());
            br = new DataInputStream(pbi);
            int singlebyte; 
	        if ((singlebyte = pbi.read())!= -1) {

	        	pbi.unread(singlebyte);
	            msg = br.readUTF();
	            
	            logger.log(Level.INFO,"Received: "+ msg);
	            jsonObject = stringtoJSON(msg);
	    		
	            boolean secureFlag = false;
	            ServerOperations sOperations = new ServerOperations(jsonObject,socket,logger);
	            sOperations.functions();
	        }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } finally {

            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (out != null) {
                try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
	}
    public static JSONObject stringtoJSON(String msg){
    	JSONParser jsonParser = new JSONParser();
    	JSONObject jsonObject;
		try {
			jsonObject = (JSONObject) jsonParser.parse(msg);
			return jsonObject;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
}

class SSLHandler implements Runnable{
	private JSONObject jsonObject;
	private SSLSocket sslsocket;
	private Logger logger =null;

	public SSLHandler(SSLSocket sslsocket,Logger logger) {
		// TODO Auto-generated constructor stub
		this.sslsocket = sslsocket;
		this.logger = logger;
	}
	
	public void run(){
        DataInputStream br = null;
        DataOutputStream out = null;

        try {
            out = new DataOutputStream(sslsocket.getOutputStream());
            String msg = null;
            PushbackInputStream pbi = new PushbackInputStream(sslsocket.getInputStream());
            br = new DataInputStream(pbi);
            int singlebyte; 
	        if ((singlebyte = pbi.read())!= -1) {
	        	pbi.unread(singlebyte);
	            msg = br.readUTF();
	            jsonObject = stringtoJSON(msg);
	            logger.log(Level.INFO,"RECEIVED:"+ msg);
	            boolean secureFlag = true;
	            sslServerOperations sOperations = new sslServerOperations(jsonObject,sslsocket,logger);
	            sOperations.functions();
	        }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } finally {

            try {
                if (sslsocket != null)
                    sslsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (out != null) {
                try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
	}
    public static JSONObject stringtoJSON(String msg){
    	JSONParser jsonParser = new JSONParser();
    	JSONObject jsonObject;
		try {
			jsonObject = (JSONObject) jsonParser.parse(msg);
			return jsonObject;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
}





