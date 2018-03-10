package EZShare.Functions;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import EZShare.LogFormatter;
import EZShare.Resource;
import EZShare.ServerControl;
import EZShare.Connection.EZSocket;
import EZShare.Connection.SSLEZSocket;

public class Share {

	private final static Logger logger = Logger.getLogger(Share.class.getName());

	public void clientShare(JSONObject res, String secret, String hostname, int port, boolean debugMode, int secureport, boolean secureflag) {
		// set up logger and formatter
		LogFormatter logformat = new LogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.FINEST);
		handler.setFormatter(logformat);
		logger.setLevel(Level.ALL);
		logger.addHandler(handler);

		// setup debug mode
		if (debugMode) {
			logger.log(Level.FINE, "sharing to " + hostname + ": " + port);
		}

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("command", "SHARE");
		jsonObject.put("secret", secret);
		jsonObject.put("resource", res);
		
		// Send the JSON
		if(!secureflag){
			EZSocket ezSocket = new EZSocket(hostname, port, debugMode);
			ezSocket.Send(jsonObject.toJSONString());
			ezSocket.Receive();
		}else {
			SSLEZSocket sslezSocket = new SSLEZSocket(hostname, secureport, debugMode);
			sslezSocket.Send(jsonObject.toJSONString());
		}
		
	}

	

}
