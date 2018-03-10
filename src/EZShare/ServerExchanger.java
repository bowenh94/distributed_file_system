package EZShare;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.BreakIterator;
import java.util.concurrent.ThreadLocalRandom;

import org.json.simple.JSONObject;
import org.omg.CORBA.PRIVATE_MEMBER;

public class ServerExchanger implements Runnable{
	private Socket socket;
	private JSONObject jObject;
	private boolean shutdown;
	public void run(){
		shutdown = false;
			if(Server.serverList.size()!=0){
				int randomIndex = ThreadLocalRandom.current().nextInt(0, Server.serverList.size());
				String host = (String) Server.serverList.get(randomIndex).get("hostname");
				String portS = (String) Server.serverList.get(randomIndex).get("port");
				int port = Integer.parseInt(portS);
				try {
					socket = new Socket(host, port);
					jObject = new JSONObject();
					jObject.put("command", "EXCHANGE");
					jObject.put("serverList", Server.serverList);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ConnectException e) {
					// If the host in unreachable, delete it.
					Server.serverList.remove(randomIndex);
					shutdown = true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				if(shutdown == false){
//					System.out.println("Still running");
					DataOutputStream dataOut = null;
					try {
						dataOut = new DataOutputStream(socket.getOutputStream());
						dataOut.writeUTF(jObject.toJSONString());
						dataOut.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						try {
							socket.close();
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
