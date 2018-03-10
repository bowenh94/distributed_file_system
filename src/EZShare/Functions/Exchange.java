package EZShare.Functions;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

import EZShare.Resource;
import EZShare.Server;

/*
 * When a server start, send a Exchange to core server in this system, which is Arron's server. 
 * Then the core server add our server into the Server List, and randomly send it's server list 
 * to us, well, hopefully.
 * 
 */
public class Exchange {
	private ArrayList<JSONObject> serverList = new ArrayList<>();
	
	private static final String DOMAIN_NAME_PATTERN = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$";
	private static final String IP_ADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
	public Exchange(ArrayList<JSONObject> serverList) {
		// TODO Auto-generated constructor stub
		this.serverList = serverList;
	}
	public boolean verifyServerList(){

		boolean result = true;
		for(JSONObject server: serverList){
			Pattern patternDN = Pattern.compile(DOMAIN_NAME_PATTERN);
			Pattern patternIP = Pattern.compile(IP_ADDRESS_PATTERN);
			String hoString = (String) server.get("hostname");
			int port = ((Long) server.get("port")).intValue();
			hoString = Resource.verifyStr(hoString);
			
			Matcher matcherDN = patternDN.matcher(hoString);
			Matcher mathcerIP = patternIP.matcher(hoString);
			result = result&verifyPort(port)&(matcherDN.matches()|mathcerIP.matches());
			
		}
		return result;
	}
	private boolean verifyPort(int port){
		if(port >65535 || port < 0){
			return false;
		}
		else 
			return true;
	}

}


