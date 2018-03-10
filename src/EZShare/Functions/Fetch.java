package EZShare.Functions;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import EZShare.Client;
import EZShare.LogFormatter;
import EZShare.Resource;
import EZShare.Connection.EZSocket;
import EZShare.Connection.SSLEZSocket;

public class Fetch {
	
	@SuppressWarnings("unchecked")
	public void fetchClient(CommandLine commandLine, String server, int port, boolean debugMode, int secureport, boolean secureflag) throws IOException, ParseException {
		// TODO Auto-generated method stub
		// set up logger

		Logger logger = Logger.getLogger(Fetch.class.getName());
		LogFormatter logformat = new LogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.FINEST);
		handler.setFormatter(logformat);
		logger.setLevel(Level.ALL);
		logger.addHandler(handler);
				
		if(debugMode){
			logger.log(Level.FINE, "fetching to "+ server + " : " + port);
		}
				
		JSONObject jsonObject = new JSONObject();
				
		// command type
		jsonObject.put("command", "FETCH");
		
		// resource in JSON format
		JSONObject resource = Resource.formResourceInJSON(commandLine);
		
		// complete JSON command and ready to be sent out
		jsonObject.put("resourceTemplate", resource);
		
		// Open a socket 
		if (!secureflag) {
			EZSocket ezsocket = new EZSocket(server, port, debugMode);
			ezsocket.Send(jsonObject.toString());
			ezsocket.Receive_Fetch();
		}else{
			SSLEZSocket sslezSocket = new SSLEZSocket(server, secureport, debugMode);
			sslezSocket.Send(jsonObject.toJSONString());
			sslezSocket.Receive_Fetch();
		}
		
		
	}
}