package EZShare.Functions;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Iterator;


import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import EZShare.LogFormatter;
import EZShare.Resource;
import EZShare.Connection.EZSocket;
import EZShare.Connection.SSLEZSocket;


public class Query {
	
	public ArrayList<String> clientQuery(String hostname, int port, Boolean relay, JSONObject resTemp, boolean debugMode, int secureport, boolean secureflag) {
		
		Logger logger = Logger.getLogger(Query.class.getName());
		LogFormatter logformat = new LogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.FINEST);
		handler.setFormatter(logformat);
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		logger.addHandler(handler);
		
		
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("command", "QUERY");
		jsonObject.put("relay", relay);
		jsonObject.put("resourceTemplate", resTemp);

		
		// Send the JSON
		ArrayList<String> returnMsg = null;
		
		if (!secureflag) {
			EZSocket ezSocket = new EZSocket(hostname, port, debugMode);
			ezSocket.Send(jsonObject.toJSONString());
			returnMsg = ezSocket.Receive();
		}else{
			SSLEZSocket sslezSocket = new SSLEZSocket(hostname, secureport, debugMode);
			returnMsg = sslezSocket.Send(jsonObject.toJSONString());
			 
		}
		
		return returnMsg;
	
	}
}


