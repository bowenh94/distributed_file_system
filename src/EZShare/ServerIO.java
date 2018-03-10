package EZShare;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class ServerIO {
	private int port;
	private int secureport;
	public ServerIO(int port, int secureport) throws IOException {
		this.port = port;
		this.secureport = secureport;
//		System.out.println("ServerIO");
		Logger logger = Logger.getLogger(ServerIO.class.getName());
		LogFormatter logformat = new LogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.FINEST);
		handler.setFormatter(logformat);
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		logger.addHandler(handler);
		
		logger.log(Level.INFO,"bond to port " + port);
		logger.log(Level.INFO,"bond to secureport " + secureport);
		
	}
	public ServerSocket getSocket() throws IOException{
		
		ServerSocket serverSocket = new ServerSocket(this.port);
		return serverSocket;
	}
	

	
	

}
