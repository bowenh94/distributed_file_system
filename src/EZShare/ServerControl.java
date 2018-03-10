package EZShare;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerControl {
	private static String secret;
	private String hostname;
	public ServerControl(String secret,String hostname) {
		this.secret = secret;
		this.hostname = hostname;

	}
	public void info(){
		Logger logger = Logger.getLogger(ServerControl.class.getName());
		LogFormatter logformat = new LogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.FINEST);
		handler.setFormatter(logformat);
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		logger.addHandler(handler);
    	
		logger.log(Level.INFO,"using secret: "+secret);
		logger.log(Level.INFO,"using advertised hostname: "+hostname);
		
	}
	
	public static boolean validateSecret(String cSecret) {
		return cSecret.equals(secret);
	}

}
