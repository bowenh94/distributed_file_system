package EZShare;

import EZShare.Connection.EZSocket;
import EZShare.Connection.SSLEZSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.cli.*;
import org.json.simple.*;
import EZShare.Functions.*;

/**
 * Created by hannnnn on 2017/3/24.
 */
public class Client {
	private static String channel = "";
	private static String resDescription = "";
	private static String exchangeServer = null;
	private static String host = "127.0.0.1";
	private static String resName = "";
	private static String resOwner = "";
	private static int port = 8899;
	private static int secureport = 3781;
	private static String secret = "5uv1ii7ec362me7hkch3s7l5c4";
	private static ArrayList<JSONObject> serverList = new ArrayList<>();
	private static String[] tags = null;
	private static String uri = "";
	private static String subID = "WDCSubID";
	private static Boolean secureFlag = false;
	public static boolean persistentConec = true;
	
//	private static String aaronHost = "http://sunrise.cis.unimelb.edu.au";
//	private static int aaronPort = 3781;
	private static boolean debugMode = false;
	
	
	
    public static void main(String [] args) throws IOException, ParseException, ParseException, org.apache.commons.cli.ParseException, org.json.simple.parser.ParseException{
    	
		Logger logger = Logger.getLogger(Client.class.getName());
		LogFormatter logformat = new LogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.FINEST);
		handler.setFormatter(logformat);
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		logger.addHandler(handler);
    	
    	// Command line options 
    	Options options = new Options();
    	options.addOption("help",false,"Show help message");

    	// Compulsory command line
    	options.addOption("secure", false, "security connection");
		options.addOption("channel", true, "channel");
		options.addOption("debug",false,"print debug information");
		options.addOption("description", true, "resource description");
		options.addOption("exchange",false,"exchange server list with server");
		options.addOption("fetch", false,"fetch resrouces from server");
		options.addOption("host",true,"server host, a domain name or IP address"); // we have drafted already
		options.addOption("name", true, "resrouce name");
		options.addOption("owner",true,"owner");
		options.addOption("port",true, "server port, an integer");
		options.addOption("publish",false,"publish resource on server");
		options.addOption("query",false,"query for resources from server");
		options.addOption("remove",false,"remove resource from server");
		options.addOption("secret", true, "secret");
		options.addOption("servers",true,"server list, host1:point1,host2:point2,...");
		options.addOption("share",false,"share resource on server");
		options.addOption("tags",true,"resource tags, tag1,tag2, tag3");
		options.addOption("uri",true,"resource URI");	
		
		/*
		 * !!!!!!!!!!!!!
		 */
		options.addOption("subscribe",false,"subscribe to a resource template with ID");

    	
    	CommandLineParser parser = new DefaultParser();
    	CommandLine commandLine = parser.parse(options, args);
    	// Get command line values
    	if(commandLine.hasOption("secure")){
    		secureFlag = true;
    		if(commandLine.hasOption("port")){
    			secureport = Integer.parseInt(commandLine.getOptionValue("port"));
    		}
    	}
    	if(commandLine.hasOption("channel")){
    		channel = commandLine.getOptionValue("channel");
    	}
    	if(commandLine.hasOption("description")){
    		resDescription = commandLine.getOptionValue("description");
    	}
    	if(commandLine.hasOption("host")){
    		host = commandLine.getOptionValue("host");
    	}
    	if(commandLine.hasOption("name")){
    		resName = commandLine.getOptionValue("name");
    	}
    	if(commandLine.hasOption("owner")){
    		resOwner = commandLine.getOptionValue("owner");
    	}
    	if(commandLine.hasOption("port")){
    		port = Integer.parseInt(commandLine.getOptionValue("port"));
    	}
    	if(commandLine.hasOption("secret")){
    		secret = commandLine.getOptionValue("secret");
    	}
    	if(commandLine.hasOption("servers")){
    		String servers = commandLine.getOptionValue("servers");
    		String[] s = servers.split(",");
    		for(String serverS:s){
//    			System.out.println(serverS);
    			String[] serverSplit = serverS.split(":");
    			if(serverSplit.length == 2){
    				JSONObject server = new JSONObject();
    				server.put("hostname", serverSplit[0]);
    				server.put("port", Integer.parseInt(serverSplit[1]));
    				serverList.add(server);
    			}
    			else{
    				JSONObject server = new JSONObject();
    				server.put("hostname", serverSplit[0]+":"+serverSplit[1]);
    				server.put("port", Integer.parseInt(serverSplit[2]));
    				serverList.add(server);
    			}
    		}

    	}
    	if(commandLine.hasOption("tags")){
    		String tagString = commandLine.getOptionValue("tags");
    		tags = tagString.split(",");
    	}
    	if(commandLine.hasOption("uri")){
    		uri = commandLine.getOptionValue("uri");
    	}
    	if(commandLine.hasOption("debug")){
    		debugMode = true;
    		logger.log(Level.INFO,"setting debug on");
    	}
    	if(commandLine.hasOption("subscribe")){
    		Random r=new java.util.Random();
    		subID = "WDCClientSub"+Integer.toString(Math.abs(r.nextInt(10000)));

		}
    	
    	// Help command
    	if(commandLine.hasOption("help")){
    		HelpFormatter helpFormatter = new HelpFormatter();
    		helpFormatter.printHelp("Client", options);
    	}

    	/*
    	 * 
    	 */

    	// Command line options 
		if(commandLine.hasOption("publish")){
			Publish publish = new Publish();
			publish.publishClient(commandLine, host, port, debugMode, secureport, secureFlag);
			
		}else if(commandLine.hasOption("remove")){
			Remove remove = new Remove();
			remove.removeClient(commandLine, host, port, debugMode, secureport, secureFlag);
			
		}else if(commandLine.hasOption("share")){
			Share share = new Share();
			JSONObject shareRes = Resource.formResourceInJSON(commandLine);
			share.clientShare(shareRes, secret, host, port, debugMode, secureport, secureFlag);
			
		}else if(commandLine.hasOption("query")){
			Query query = new Query();
			
			JSONObject resource = Resource.formResourceInJSON(commandLine);
			
			query.clientQuery(host, port, true, resource,debugMode, secureport, secureFlag);
			
		}else if(commandLine.hasOption("fetch")){
			Fetch fetch = new Fetch();
			fetch.fetchClient(commandLine, host, port, debugMode, secureport, secureFlag);
			
		}else if(commandLine.hasOption("exchange")){
			if(secureFlag){
				SSLEZSocket sslezSocket = new SSLEZSocket(host, secureport, debugMode);
				JSONObject exchange = new JSONObject();
				exchange.put("command", "EXCHANGE");
				exchange.put("serverList", serverList);
				sslezSocket.Send(exchange.toJSONString());
//				sslezSocket.Receive();
			}
			else{
				EZSocket ezSocket = new EZSocket(host,port,debugMode);
				JSONObject exchange = new JSONObject();
				exchange.put("command", "EXCHANGE");
				exchange.put("serverList", serverList);
				ezSocket.Send(exchange.toJSONString());
				ezSocket.Receive();
			}
			
		}else if (commandLine.hasOption("subscribe")) {
			if(secureFlag){

				
				//Generate the resource template from command line
				JSONObject subRes = Resource.formResourceInJSON(commandLine);
				//Start connction and set a stop flag
				System.setProperty("javax.net.ssl.keyStore", "WDCkeystore/clientKS.jks");
				System.setProperty("javax.net.ssl.keyStorePassword","456456");
				
				System.setProperty("javax.net.ssl.trustStore","WDCkeystore/trust_ks");
				SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				
				SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(host, secureport);
				
				
				JSONObject subCommand = new JSONObject();
				subCommand.put("command", "SUBSCRIBE");
				subCommand.put("id", subID);
				subCommand.put("relay",true);
				subCommand.put("resourceTemplate", subRes);
				
				DataOutputStream out = null;
				DataInputStream in = null;
				try {
					out = new DataOutputStream(sslsocket.getOutputStream());
					out.writeUTF(subCommand.toJSONString());
					if(debugMode)
						logger.log(Level.FINE,"SENT: "+ subCommand.toJSONString());
					out.flush();
					
					EnterListener eListener = new EnterListener();
					ReadRecourse readRecourse = new ReadRecourse(sslsocket,logger,debugMode);
					
					eListener.start();
					readRecourse.start();
					
					while(persistentConec){
						Thread.sleep(5);
						
					}
//					while(persistentConec){
//						PushbackInputStream pbi = new PushbackInputStream(sslsocket.getInputStream());
//						in = new DataInputStream(pbi);
//						System.out.println("Stuck here 247");
//						System.out.println("Stuck here 249");
//						while(persistentConec){
//							in.readUnsignedShort();
//							System.out.println("Stuck here 253");
//						}
//						
//
//					}
				} catch (Exception e) {
					// TODO: handle exception
					e.getStackTrace();
				}
				
				JSONObject unSubCommand = new JSONObject();
				unSubCommand.put("command", "UNSUBSCRIBE");
				unSubCommand.put("id", subID);
				try {
					out.writeUTF(unSubCommand.toJSONString());
					out.flush();
					if(debugMode){
						logger.log(Level.FINE, "SEND:" + unSubCommand.toJSONString());
					}else{					
						System.out.println(unSubCommand.toJSONString());
					}
					PushbackInputStream pbi = new PushbackInputStream(sslsocket.getInputStream());
					in = new DataInputStream(pbi);

//					int singleByte;
//					while((singleByte = pbi.read())!=-1){
//						pbi.unread(singleByte);
						String resultSize = in.readUTF();
						System.out.println(resultSize);
//					}
				} catch (Exception e) {
					// TODO: handle exception
					e.getStackTrace();
				} finally {
					try {
						if(out!=null)
							out.close();
					} catch (Exception e2) {
						// TODO: handle exception
						e2.getStackTrace();
					}
					try {
						if(sslsocket != null)
							sslsocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						if(in!=null)
							in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}	
			}
			else {
				//Generate the resource template from command line
				JSONObject subRes = Resource.formResourceInJSON(commandLine);
				//Start connction and set a stop flag
				Socket socket = new Socket(host, port);
				boolean persistentConec = true;
				
				JSONObject subCommand = new JSONObject();
				subCommand.put("command", "SUBSCRIBE");
				subCommand.put("id", subID);
				subCommand.put("relay",true);
				subCommand.put("resourceTemplate", subRes);
				
				DataOutputStream out = null;
				DataInputStream in = null;
				try {
					out = new DataOutputStream(socket.getOutputStream());
					out.writeUTF(subCommand.toJSONString());
					if(debugMode)
						logger.log(Level.FINE,"SENT: "+ subCommand.toJSONString());
					out.flush();
					
					EnterListener eListener = new EnterListener();
					eListener.start();
					in = new DataInputStream(socket.getInputStream());
					while(persistentConec){
						
						in = new DataInputStream(socket.getInputStream());

						while(in.available()>0){
//							pbi.unread(singleByte);
							String subRet = in.readUTF();
							logger.log(Level.FINE, "RECEIVED:"+subRet);
//							System.out.println(subRet);
						}
//						System.out.println("client 338 "+eListener.returnStatus());
						if(eListener.returnStatus()){
							persistentConec = false;
						}

					}
				} catch (Exception e) {
					// TODO: handle exception
					e.getStackTrace();
				}
//				System.out.println("no unsub command!");
				
				JSONObject unSubCommand = new JSONObject();
				unSubCommand.put("command", "UNSUBSCRIBE");
				unSubCommand.put("id", subID);
				try {
					out.writeUTF(unSubCommand.toJSONString());
					out.flush();
					if(debugMode){
						logger.log(Level.FINE, "SEND:" + unSubCommand.toJSONString());
					}else{					
						System.out.println(unSubCommand.toJSONString());
					}
					PushbackInputStream pbi = new PushbackInputStream(socket.getInputStream());
					in = new DataInputStream(pbi);

					int singleByte;
					while((singleByte = pbi.read())!=-1){
						pbi.unread(singleByte);
						String resultSize = in.readUTF();
						logger.log(Level.FINE, "RECEIVED:"+resultSize);
//						System.out.println(resultSize);
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.getStackTrace();
				} finally {
					try {
						if(out!=null)
							out.close();
					} catch (Exception e2) {
						// TODO: handle exception
						e2.getStackTrace();
					}
					try {
						if(socket != null)
							socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						if(in!=null)
							in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}	
			}
			
		}
    }
}
class EnterListener extends Thread{
	private boolean enterFlag = false;
	private Scanner s = new Scanner(System.in);
	public EnterListener() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public void run(){
		String string =s.nextLine();
		if(string.equals("")){
			enterFlag = true;
			Client.persistentConec = false;
			
		}
	}
	public boolean returnStatus(){
		return enterFlag;
	}
}

class ReadRecourse extends Thread{
	private SSLSocket sslSocket;
	private Logger logger = null;
	private boolean debugMode = false;
	public ReadRecourse(SSLSocket sslSocket, Logger logger,boolean debugMode){
		this.sslSocket = sslSocket;
		this.logger = logger;
		this.debugMode = debugMode;
	}
	
	public void run(){
		PushbackInputStream pbi;
		try {
//			pbi = new PushbackInputStream();
			DataInputStream in = new DataInputStream(sslSocket.getInputStream());
			
			while(Client.persistentConec){
				String inmsg = in.readUTF();
				if(debugMode){
					logger.log(Level.FINE,"RECEIVED: "+ inmsg);

				}else{
					System.out.println(inmsg);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}