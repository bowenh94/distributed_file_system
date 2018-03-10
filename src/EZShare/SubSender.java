package EZShare;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

public class SubSender implements Runnable{

	public SubSender() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(!Server.subList.isEmpty()){
			Iterator<Entry<String, ArrayList<Object>>> iterator = Server.subList.entrySet().iterator();
			if(iterator.hasNext()){
				Entry<String, ArrayList<Object>> entry = iterator.next();
				String id = entry.getKey();
				ArrayList<Object> subArrayList = entry.getValue();
				@SuppressWarnings("unchecked")
				ArrayList<Resource> resList = (ArrayList<Resource>) subArrayList.get(Server.SUB_RESULT);
				int resSize = resList.size();
				subArrayList.set(Server.SUB_COUNTER, (int)subArrayList.get(Server.SUB_COUNTER)+resSize);
				Server.subList.replace(id, subArrayList);
				Socket socket = (Socket) subArrayList.get(Server.SUB_SOCKET);
				DataOutputStream dataOut = null;
				try {
					dataOut = new DataOutputStream(socket.getOutputStream());
					for(Resource r:resList){
						JSONObject resJson = Resource.formResourceInJSON(r);
						dataOut.writeUTF(resJson.toJSONString());
						dataOut.flush();
						resList.remove(r);
					}
					subArrayList.set(Server.SUB_RESULT, resList);
					Server.subList.replace(id, subArrayList);
				} catch (Exception e) {
					// TODO: handle exception
					e.getStackTrace();
				}
				
			}
		}
		
	}

}
