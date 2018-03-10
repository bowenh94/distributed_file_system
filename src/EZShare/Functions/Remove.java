package EZShare.Functions;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.json.simple.JSONObject;

import EZShare.LogFormatter;
import EZShare.Resource;
import EZShare.Server;
import EZShare.Connection.EZSocket;
import EZShare.Connection.SSLEZSocket;

public class Remove {
	private final static Logger logger = Logger.getLogger(Remove.class.getName());
	
	public void removeClient(CommandLine commandLine, String server, int port, boolean debugMode, int secureport, boolean secureflag) {
		// set up logger
		LogFormatter logformat = new LogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.FINE);
		handler.setFormatter(logformat);
		logger.setLevel(Level.ALL);
		logger.addHandler(handler);
		
		if(debugMode){
			logger.log(Level.FINE, "publishing to \""+ server + ":" + port + "\"");
		}
		
		JSONObject jsonObject = new JSONObject();

		// command type
		jsonObject.put("command", "REMOVE");
		
		String owner = "";
		String channel = "";
		String uri = "";
		//  owner 
		if(commandLine.hasOption("owner")){
			owner = commandLine.getOptionValue("owner");
		}
		
		
		// channel
		if(commandLine.hasOption("channel")){
			channel = commandLine.getOptionValue("channel");
			System.out.println(channel);
		}
		
		
		// URI
		if(commandLine.hasOption("uri"))
			uri = commandLine.getOptionValue("uri");
			
				
		
		// create a Resource object which only contain the primary keys
		Resource resource = new Resource(owner, channel, uri);
		
		// transform Resource object into JSON format
		JSONObject jsonResource = Resource.formResourceInJSON(resource);
		
		jsonObject.put("resource", jsonResource);
		
		
		// Open a socket 
		if (!secureflag) {
			EZSocket ezSocket = new EZSocket(server, port, debugMode);
			ezSocket.Send(jsonObject.toJSONString());
			ezSocket.Receive();
		}else {
			SSLEZSocket sslezSocket = new SSLEZSocket(server, secureport, debugMode);
			sslezSocket.Send(jsonObject.toJSONString());
//			sslezSocket.Receive();
			
		}
		
	}
	
	public String removeServer(ConcurrentHashMap<ArrayList<String>, Resource> resMap, JSONObject jObject){
		String result = "success";
		
		String uri = (String) jObject.get("uri");
		String channel = (String) jObject.get("channel");
		String owner = (String) jObject.get("owner");
		
		Resource.verifyStr(uri);
		Resource.verifyStr(channel);
		Resource.verifyStr(owner);
		
		// Owner cannot be "*"
		if(owner.equals("*"))
			result = "invalid resource";
		
		// uri cannot be empty
		if(uri.equals(""))
			result = "missing resource";
		
		Resource rResource = new Resource("");
		boolean resourceFound = false;
		for(ArrayList<String> key:resMap.keySet()){
			if(key.get(Server.PK_Owner).equals(owner) && key.get(Server.PK_Channel).equals(channel) && key.get(Server.PK_URI).equals(uri)){
				resMap.remove(key);
				resourceFound = true;
				break;
			}
		}
		
		if(!resourceFound)
			result = "cannot remove resource";
		
		return result;
	}
}