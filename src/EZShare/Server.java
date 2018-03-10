package EZShare;

import EZShare.Connection.*;
import EZShare.Functions.*;
import jdk.nashorn.internal.scripts.JO;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.apache.commons.cli.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.omg.CORBA.PRIVATE_MEMBER;

import com.sun.corba.se.spi.orb.Operation;
import com.sun.glass.ui.TouchInputSupport;
import com.sun.media.jfxmedia.control.VideoDataBuffer;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.xml.internal.ws.transport.http.server.ServerAdapterList;

import java.util.concurrent.*;

/**
 * Created by hannnnn on 2017/3/24.
 */
public class Server {
	// Position of primary key
	public static final int PK_Owner = 0;
	public static final int PK_Channel = 1;
	public static final int PK_URI = 2;
	
	public static final int SUB_TEMP = 0;
	public static final int SUB_SOCKET = 1;
	public static final int SUB_COUNTER = 2;
	public static final int SUB_RESULT = 3;
	
	private static int port = 8899;
	private static int secureport = 3781;
    static String secret = "5uv1ii7ec362me7hkch3s7l5c4";
    static String hostName = "WDCServer";
    private static int exchangeInterval = 600;
    private static int intervalLimit = 1000;
    
    static ConcurrentHashMap<ArrayList<String>, Resource> resMap = new ConcurrentHashMap<>();
    static ArrayList<JSONObject> serverList = new ArrayList<>();
    static ArrayList<JSONObject> sslserverList = new ArrayList<>();
    
    static ConcurrentHashMap<String, ArrayList<Object>> subList = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, ArrayList<Object>> sslsubList = new ConcurrentHashMap<>();


    private static ExecutorService executorService;
    private static ExecutorService sslexecutorService;
    private final static int POOL_SIZE = 20;
    
    public static void main(String [] args) throws IOException, ParseException, org.apache.commons.cli.ParseException {
    	
		Logger logger = Logger.getLogger(Server.class.getName());
		LogFormatter logformat = new LogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.FINEST);
		handler.setFormatter(logformat);
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		logger.addHandler(handler);
    	
		logger.log(Level.INFO,"Starting the EZShare Server");
    	
    	// Command line options 
    	Options options = new Options();
    	options.addOption("receive",false,"Server start listening");
    	options.addOption("help",false,"Show help message");
    	options.addOption("advertisedhostname",true, "advertised hostname");
    	options.addOption("connectionintervallimit",true, "connection interval limit in seconds");
    	options.addOption("exchangeinterval",true,"exchange interval in seconds");
    	options.addOption("port",true,"server port, an integer");
    	options.addOption("sport",true,"secure server port, an integer,default is 3781");
    	options.addOption("secret",true,"secret");
    	options.addOption("debug",false,"debug");
    	
    	// Create command line parser
    	CommandLineParser parser = new DefaultParser();
    	CommandLine commandLine = parser.parse(options, args);
    	
    	// Get command line arguements
    	if(commandLine.hasOption("advertisedhostname")){
        	hostName = commandLine.getOptionValue("advertisedhostname");
    	}
    	if(commandLine.hasOption("intervallimit")){
    		intervalLimit = Integer.parseInt(commandLine.getOptionValue("connectionintervallimit"));
    	}
    	if(commandLine.hasOption("exchangeinterval")){
    		exchangeInterval = Integer.parseInt(commandLine.getOptionValue("exchangeinterval"));
    	}
    	if(commandLine.hasOption("port")){
    		port = Integer.parseInt(commandLine.getOptionValue("port"));
    	}
    	if(commandLine.hasOption("sport")){
    		secureport = Integer.parseInt(commandLine.getOptionValue("sport"));
    	}
    	if(commandLine.hasOption("secret")){
    		secret = commandLine.getOptionValue("secret");
    	}
    	// help command
    	if(commandLine.hasOption("help")){
    		HelpFormatter helpFormatter = new HelpFormatter();
    		helpFormatter.printHelp("Server", options);
    	}
    	
    	// Server start 
    	ServerControl sControl = new ServerControl(secret, hostName);
    	sControl.info();
    	
    	
    	// Bond server socket to 
    	ServerIO sIo = new ServerIO(port,secureport);
    	ServerSocket serverSocket = sIo.getSocket();
//    	SSLServerSocket sslserverSocket = (SSLServerSocket) sIo.getSSLSocket();
    	
    	// Create Schedule executor pool for exchange
    	ServerExchange();

    	// Create thread pool
    	executorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOL_SIZE);
// Create ssl thread pool
		sslexecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().
				availableProcessors()*POOL_SIZE);

		
		new serverthread(serverSocket,executorService,intervalLimit,logger).start();
		new sslserverthread(secureport,sslexecutorService, intervalLimit,logger).start();
			
//		System.out.println("sub!!!");
		ScheduledExecutorService scheduledSub = Executors.newScheduledThreadPool(2);
		scheduledSub.scheduleWithFixedDelay(new SubSender(), 0, 3, TimeUnit.SECONDS);
		scheduledSub.scheduleWithFixedDelay(new SSLSubSender(), 0, 3, TimeUnit.SECONDS);
		
    }
    private static void ServerExchange(){
    	// Create Schedule executor pool for exchange
    	ScheduledExecutorService schedule = Executors.newScheduledThreadPool(2);
    	schedule.scheduleWithFixedDelay(new ServerExchanger(), 0, exchangeInterval, TimeUnit.SECONDS);
    	schedule.scheduleWithFixedDelay(new SSLServerExchanger(), 0, exchangeInterval, TimeUnit.SECONDS);
    	
    	
		Logger logger = Logger.getLogger(ServerExchanger.class.getName());
		LogFormatter logformat = new LogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.FINEST);
		handler.setFormatter(logformat);
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		logger.addHandler(handler);
    	
		logger.log(Level.INFO,"ServerExchangerStarted");
		
		Logger ssllogger = Logger.getLogger(SSLServerExchanger.class.getName());
		LogFormatter ssllogformat = new LogFormatter();
		ConsoleHandler sslhandler = new ConsoleHandler();
		sslhandler.setLevel(Level.FINEST);
		sslhandler.setFormatter(ssllogformat);
		ssllogger.setUseParentHandlers(false);
		ssllogger.setLevel(Level.ALL);
		ssllogger.addHandler(sslhandler);
    	
		ssllogger.log(Level.INFO,"SSLServerExchangerStarted");
    }
    
}


class serverthread extends Thread{
	private ServerSocket serverSocket;
	private ExecutorService executorService;
	private int intervalLimit;
	private Logger logger = null;
	public serverthread(ServerSocket serverSocket, ExecutorService executorService, int intervalLimit, Logger logger){
		this.serverSocket = serverSocket;
		this.executorService = executorService;
		this.intervalLimit = intervalLimit;
		this.logger = logger;
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
//		System.out.println("publish 41");
		ServerMain serverMain = new ServerMain(serverSocket, executorService, intervalLimit,logger);
		try {
			serverMain.service();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

class sslserverthread extends Thread{
	private int secureport;
	private ExecutorService sslexecutorService;
	private int intervalLimit;
	private Logger logger =null;

	public sslserverthread(int secureport, ExecutorService executorService, int intervalLimit,Logger logger){
		this.sslexecutorService = executorService;
		this.secureport = secureport;
		this.intervalLimit = intervalLimit;
		this.logger = logger;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.setProperty("javax.net.ssl.keyStore","WDCkeystore/serverKS.jks");
		System.setProperty("javax.net.ssl.keyStorePassword","123123");
		System.setProperty("javax.net.ssl.trustStore","WDCkeystore/trust_ks");
		System.setProperty("javax.net.debug","handshake");
		SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory
				.getDefault();
		SSLServerSocket sslserversocket;
		try {
			sslserversocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(secureport);
			
			sslserversocket.setNeedClientAuth(true);
			SSLSocket sslsocket = null;
			
			while (true){
				try {
					sslsocket = (SSLSocket) sslserversocket.accept();
					
					SSLHandler sHandler = new SSLHandler(sslsocket,logger);
					sslexecutorService.execute(sHandler);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}
	
}

