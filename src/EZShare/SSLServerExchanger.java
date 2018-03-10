package EZShare;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadLocalRandom;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.json.simple.JSONObject;


public class SSLServerExchanger implements Runnable{
	private JSONObject jObject;
	private boolean shutdown;
	public void run(){
		shutdown = false;
		System.setProperty("javax.net.ssl.keyStore", "WDCkeystore/serverKS.jks");
		System.setProperty("javax.net.ssl.keyStorePassword","123123");
		
		System.setProperty("javax.net.ssl.trustStore","WDCkeystore/trust_ks");
		SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		SSLSocket sslsocket = null;
			if(Server.sslserverList.size()!=0){
				int randomIndex = ThreadLocalRandom.current().nextInt(0, Server.sslserverList.size());
				String host = (String) Server.sslserverList.get(randomIndex).get("hostname");
				String portS = (String) Server.sslserverList.get(randomIndex).get("port");
				int port = Integer.parseInt(portS);
				try {
					sslsocket =  (SSLSocket) sslsocketfactory.createSocket(host, port);
					jObject = new JSONObject();
					jObject.put("command", "EXCHANGE");
					jObject.put("serverList", Server.sslserverList);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ConnectException e) {
					// If the host in unreachable, delete it.
					Server.sslserverList.remove(randomIndex);
					shutdown = true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				if(shutdown == false){
//					System.out.println("Still running");
					DataOutputStream dataOut = null;
					try {
						dataOut = new DataOutputStream(sslsocket.getOutputStream());
						dataOut.writeUTF(jObject.toJSONString());
						dataOut.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						try {
							sslsocket.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							dataOut.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
	}
}
