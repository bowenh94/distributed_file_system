package EZShare.Functions;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.json.simple.JSONObject;

import EZShare.Client;
import EZShare.LogFormatter;
import EZShare.Resource;
import EZShare.Server;
import EZShare.Connection.EZSocket;
import EZShare.Connection.SSLEZSocket;

public class Publish {
	private final static Logger logger = Logger.getLogger(Client.class.getName());
	
	public void publishClient(CommandLine commandLine, String server, int port, boolean debugMode, int secureport, boolean secureflag){
		
		if(debugMode && secureflag){
			logger.log(Level.FINE, "publishing to \""+ server + ":" + secureport + "\"");
		}else if (debugMode) {
			logger.log(Level.FINE, "publishing to \""+ server + ":" + port + "\"");
		}
		
		JSONObject jsonObject = new JSONObject();
		
		// command type
		jsonObject.put("command", "PUBLISH");
		
		// resource in JSON format
		JSONObject resource = Resource.formResourceInJSON(commandLine);
		
		// complete JSON command and ready to be sent out
		jsonObject.put("resource", resource);
		
		// Open a socket or sslsocket
		if (!secureflag) {
			EZSocket clientConn = new EZSocket(server, port, debugMode);
			clientConn.Send(jsonObject.toJSONString());
			clientConn.Receive();
		}else {
			SSLEZSocket clientConn = new SSLEZSocket(server, secureport, debugMode);
			clientConn.Send(jsonObject.toJSONString());
		}
		
	}
	
	
}