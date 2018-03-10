
package EZShare;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import EZShare.Functions.Exchange;
import EZShare.Functions.Publish;
import EZShare.Functions.Query;
import EZShare.Functions.Remove;
import EZShare.Functions.Share;
import EZShare.Functions.Subscribe;

public class ServerOperations {
	private String command;
	private JSONObject jObject;
	private Socket socket;
	private DataOutputStream dataOut = null;
	private ExecutorService subThreadPool;
//	private ExecutorService sslsubThreadPool;
//	private boolean secureFlag;
	private Logger logger = null;
	
	public ServerOperations(JSONObject jObject,Socket socket, Logger logger) {
		// TODO Auto-generated constructor stub
		this.jObject = jObject;
		this.socket = socket;
		this.logger = logger;
		subThreadPool = Executors.newFixedThreadPool(20);
		if(jObject.containsKey("command")){
			command = jObject.get("command").toString();
		}else {
			command = null;
			try{
				dataOut = new DataOutputStream(socket.getOutputStream());
				JSONObject missingCommand = new JSONObject();
    			missingCommand.put("response", "error");
    			missingCommand.put("errorMessage", "missing or incorrect type for command");
    			dataOut.writeUTF(missingCommand.toJSONString());
    			dataOut.flush();
				
			}catch (Exception e) {
				// TODO: handle exception
				e.getStackTrace();
			}
		}
	}
	public String getCommand(){
		return command;
	}
	@SuppressWarnings("unchecked")
	public void functions() throws ParseException{
		if(command!=null){
	    	switch (command) {
	    	// Need a check method to reject the duplicate server 
	    	/*
	    	 * 
	    	 */
	    	case "PUBLISH":
	    		String resultPub = "success";
	    		if(jObject.containsKey("resource")){
	    			JSONObject test = (JSONObject)jObject.get("resource");
	    			test.put("ezserver", Server.hostName);
	    			Resource pubRes = Resource.formResourceFromJSON(test);
	    			if(pubRes.getURI()==""||!Resource.isAbsoluteURI(pubRes.getURI())||pubRes.getOwner().equals("*")){
	    				resultPub = "invalid resource";
	    			}else{
		    			for(ArrayList<String> key:Server.resMap.keySet()){
		        			if(key.get(Server.PK_URI).equals(pubRes.getURI())){
		        				if(key.get(Server.PK_Channel).equals(pubRes.getChannel())){
		        					if(key.get(Server.PK_Owner).equals(pubRes.getOwner())){
			        					ArrayList<String> pkey = pubRes.getKey();
			        					Server.resMap.replace(pkey, pubRes);
		        					}
		        					else{
		        						resultPub = "cannot publish resource";
		        					}
		        				}
		        			}
		        		}
	    			}
	    			if(resultPub == "success"){
		        		ArrayList<String> prkey = pubRes.getKey();
		        		Server.resMap.put(prkey, pubRes);
		        		searchSubTemp(pubRes);
	    			}
	    		}
	    		else{
	    			resultPub = "missing resource";
	    		}
		
	    		DataOutputStream dataOutPub= null;
	    		JSONObject resultPubJ = new JSONObject();
	    		if(resultPub.equals("success")){
	    			resultPubJ.put("response", resultPub);
				}else{
					resultPubJ.put("response", "error");
					resultPubJ.put("erroMessage", resultPub);
				}
	    		try {
					dataOutPub = new DataOutputStream(socket.getOutputStream());
					dataOutPub.writeUTF(resultPubJ.toJSONString());
					logger.log(Level.INFO, "SEND:" + resultPubJ.toJSONString());
					dataOutPub.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
	    		break;
	    		
	    		
	    	case "REMOVE":
	    		String resultRev = "success";
	    		if(jObject.containsKey("resource")){
	    			JSONObject resjObj = new JSONObject();
	    			resjObj = (JSONObject) jObject.get("resource");
		    		if(resjObj.containsKey("uri")){
		    			String uri = (String) resjObj.get("uri");
		    			if(uri.equals("")){
		    				resultRev = "invalid resource";
		    			}else{
		    				if(resjObj.containsKey("channel")){
			    				String channel = (String) resjObj.get("channel");
			    				if(resjObj.containsKey("owner")){
			    					String owner = (String) resjObj.get("owner");
			    					if(owner.equals("*")){
			    						resultRev = "invalid resource";
			    					}else{
			    						uri = Resource.verifyStr(uri);
			    		        		channel = Resource.verifyStr(channel);
			    		        		owner = Resource.verifyStr(owner);
			    		        		boolean resourceFound = false;
			    		        		for(ArrayList<String> key:Server.resMap.keySet()){
	//		    		        			System.out.println("Key is "+ key);
			    		        			if(key.get(Server.PK_Owner).equals(owner) && key.get(Server.PK_Channel).equals(channel) && key.get(Server.PK_URI).equals(uri)){
			    		        				Server.resMap.remove(key);
			    		        				resourceFound = true;
			    		        				break;
			    		        			}
			    		        		}
			    		        		if(!resourceFound)
			    		        			resultRev = "cannot remove resource";
			    					}
			    				}else{
				    				resultRev = "invalid resource";
			    				}
			    			}else{
			    				resultRev = "invalid resource";
			    			}
		    			}
		    			
		        		
		    		}else{
		    			resultRev = "invalid resource";
		    		}
	    		}
	    		else {
	    			resultRev = "missing resource";
				}
	    		
	    		JSONObject resultRemJ = new JSONObject();
	    		DataOutputStream dataOutRem= null;
	    		
	    		if(resultRev.equals("success")){
	    			resultRemJ.put("response", resultRev);
				}else{
					resultRemJ.put("response", "error");
					resultRemJ.put("erroMessage", resultRev);
				}
	    		
	    		try {
					dataOutRem = new DataOutputStream(socket.getOutputStream());
					dataOutRem.writeUTF(resultRemJ.toJSONString());
					logger.log(Level.INFO, "SEND:" + resultRemJ.toJSONString());
					dataOutRem.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}	
	    		break;
	    	
	    	case "FETCH":
	    	{
			
		    	
				logger.log(Level.INFO,"Receive: "+jObject.toString());
				
	    		boolean FetchResourceFound = false;
	    		boolean FileExit = false;
				String 	resultFetch = "success";
	    		
				JSONObject resultFetchJson = new JSONObject();
				JSONObject resultFetchJsonRes = new JSONObject();
	    		DataOutputStream dataOutFetch= null;
				
				if(jObject.containsKey("resourceTemplate")){
					JSONObject jObjectRes = (JSONObject)jObject.get("resourceTemplate");
					String uriFetch = (String) jObjectRes.get("uri");
		    		String channelFetch = (String) jObjectRes.get("channel");
		    		String ownerFetch = (String) jObjectRes.get("owner");
		    		Resource.verifyStr(uriFetch);
		    		Resource.verifyStr(channelFetch);
		    		Resource.verifyStr(ownerFetch);
		    		// uri cannot be empty
		    		if(uriFetch.equals(""))
		    			resultFetch = "missing resourceTemplate";
		    		
		    		Resource FetchResource = Resource.formResourceFromJSON(jObjectRes);
		    		for(ArrayList<String> fetchres:Server.resMap.keySet()){
		    			if(fetchres.get(Server.PK_Owner).equals(ownerFetch) && fetchres.get(Server.PK_Channel).equals(channelFetch) && fetchres.get(Server.PK_URI).equals(uriFetch)){
		    				String[] test = uriFetch.split(":");
		    				File file = new File(test[1]);
		    				if(file.exists()){
		    					FetchResourceFound = true;
		    					FileExit = true;
		    					resultFetch = "success";
		    					JSONParser parser = new JSONParser();
		    	        		FetchResource = Server.resMap.get(fetchres);
		    	        		resultFetchJsonRes = Resource.formResourceInJSON(FetchResource);
		    	        		resultFetchJsonRes.put("resourceSize", file.length());
		    					break;
		    				}else{
		    					resultFetch = "invalid resourceTemplate";
		    				}
		    			}
		    		}
		    		
		    		if(!FetchResourceFound){
		    			resultFetch = "missing resourceTemplate";
						try {
							dataOutFetch = new DataOutputStream(socket.getOutputStream());
							resultFetchJson.put("response", "error");
							resultFetchJson.put("erroMessage", resultFetch);
							dataOutFetch.writeUTF(resultFetchJson.toString());
							logger.log(Level.INFO, "SEND:"+resultFetchJson.toJSONString());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    		}else{
		    			if(resultFetch.equals("success")){
			    			resultFetchJson.put("response", resultFetch);
						}else{
							resultFetchJson.put("response", "error");
							resultFetchJson.put("erroMessage", resultFetch);
						}
		    			try {
							dataOutFetch = new DataOutputStream(socket.getOutputStream());
							dataOutFetch.writeUTF(resultFetchJson.toJSONString());
							logger.log(Level.INFO, resultFetchJson.toJSONString());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			    		try {
							dataOutFetch = new DataOutputStream(socket.getOutputStream());
							try {
								File file = new File(uriFetch.substring(7,uriFetch.length()));
								
								// Send resource_tem to client
								dataOutFetch.writeUTF(resultFetchJsonRes.toJSONString());
								
								// Start sending file
								RandomAccessFile byteFile = new RandomAccessFile(file,"r");
								byte[] sendingBuffer = new byte[1024*1024];
								int num;
								// While there are still bytes to send..
								while((num = byteFile.read(sendingBuffer)) > 0){
	//								System.out.println(num);
									dataOutFetch.write(Arrays.copyOf(sendingBuffer, num));
								}
								byteFile.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
							
							dataOutFetch.flush();
						} catch (Exception e) {
							e.printStackTrace();
						}	
						try {
							dataOutFetch = new DataOutputStream(socket.getOutputStream());
				    		JSONObject resultFetchJsonSize = new JSONObject();
				    		if(resultFetch == "success"){
				    			if(FileExit){
				    				resultFetchJsonSize.put("resultSize", 1);
					    			dataOutFetch.writeUTF(resultFetchJsonSize.toJSONString());
				    			}else{
				    				resultFetchJsonSize.put("resultSize", 0);
				    				dataOutFetch.writeUTF(resultFetchJsonSize.toJSONString());
				    			}
				    			
				    		}else{
				    		}
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    		}
	
				}else{
					resultFetch = "missing resourceTemplate";
	    			resultFetchJson.put("response", resultFetch);
					try {
						dataOutFetch = new DataOutputStream(socket.getOutputStream());
						dataOutFetch.writeUTF(resultFetchJson.toString());
						dataOutFetch = new DataOutputStream(socket.getOutputStream());
						JSONObject resultFetchJsonSize = new JSONObject();
						resultFetchJsonSize.put("resultSize", 0);
	    				dataOutFetch.writeUTF(resultFetchJsonSize.toJSONString());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
	    		
	    		
			}
	    		break;	
	    	
	
	    	case "SHARE":
	    	{
	    		JSONObject resultShareJSON = new JSONObject();
	    		DataOutputStream dataOutShare = null;
	    		
	    		String resultSha = "success";
	    		
	    		if(jObject.containsKey("secret")){
	    			String secret = jObject.get("secret").toString();
	    			if(secret.equals(Server.secret)){
	    				if(jObject.containsKey("resource")){
	    					 JSONObject shareRes = (JSONObject) jObject.get("resource");
	        				 if(shareRes.containsKey("uri")){
	        					 String uri = shareRes.get("uri").toString();
	        					 String[] test = uri.split(":");
	        					 String Windowspattern = "^[a-zA-Z]:(((\\\\(?! )[^/:*?<>\\\"\\\"|\\\\]+)+\\\\?)|(\\\\)?)\\s*$";
	        					 String Linuxpattern = "^\\/\"?[a-zA-Z0-9\\/].*\\.[a-zA-Z0-9].*\"?";
	        					 Pattern winpat = Pattern.compile(Windowspattern);
	        					 Pattern lixpat = Pattern.compile(Linuxpattern);
	        					 Matcher winmat = winpat.matcher(test[1]);
	        					 Matcher lixmat = lixpat.matcher(test[1]);
	        					 if(winmat.matches()||lixmat.matches()){
	        						 if(Resource.isFileScheme(uri)){
	        							 Resource shareres = Resource.formResourceFromJSON(shareRes);
	        							 if(!shareRes.containsKey("channel")||!shareRes.containsKey("owner")){
	        				    				resultSha = "invalid resource";
	        				    			}else{
	        					    			for(ArrayList<String> key:Server.resMap.keySet()){
	        					        			if(key.get(Server.PK_URI).equals(shareres.getURI())){
	        					        				if(key.get(Server.PK_Channel).equals(shareres.getChannel())){
	        					        					if(key.get(Server.PK_Owner).equals(shareres.getOwner())){
	        						        					ArrayList<String> pkey = shareres.getKey();
	        						        					Server.resMap.replace(pkey, shareres);
	        					        					}
	        					        					else{
	        					        						resultSha = "cannot share resource";
	        					        					}
	        					        				}
	        					        			}
	        					        		}
	        					    			if(resultSha == "success"){
	        					    				ArrayList<String> prkey = shareres.getKey();
	        						        		Server.resMap.put(prkey, shareres);
	        						        		
	        						        		searchSubTemp(shareres);
	        					    			}
	        				    			}
	        							 
	        							 
	        						 }else{
	        							 resultSha = "cannot share resource";
	        						 }
	        					 }else{
	        						 resultSha = "cannot share resource";
	        					 }
	        				 }else{
	        					 resultSha = "invalid resource";
	        				 }
	    				}else{
	    	    			resultSha = "missing resource and/or secret";	
	    				}
	    				
	    			}else{
	        			resultSha = "incorrect secret";
	    			}
	    		}else{
	    			resultSha = "missing resource and/or secret";
	    		}
	    		
	    		if(resultSha == "success"){
	    			
	    			resultShareJSON.put("response", resultSha);
	    		}else{
	    			resultShareJSON.put("response", "error");
	 	    	   	resultShareJSON.put("errorMessage", resultSha);
	    		}
	    		
	    		try {
					dataOutShare = new DataOutputStream(socket.getOutputStream());
					dataOutShare.writeUTF(resultShareJSON.toString());
					logger.log(Level.INFO, "SEND:"+resultShareJSON.toJSONString());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	
	    	}
	    	   break;
	    		
			case "EXCHANGE":{
				
					ArrayList<JSONObject> c = (ArrayList<JSONObject>) jObject.get("serverList");
					//    		System.out.println(c.toString());
					    		JSONObject resultExcJ = new JSONObject();
					    		Exchange exchange = new Exchange(c);
					    		
					    		if(c.size() == 0 || c == null){
					    			resultExcJ.put("response", "error");
					    			resultExcJ.put("errorMessage", "missing resourceTemplate");
					    		}
					    		if(exchange.verifyServerList()){
					        		for(JSONObject sJObject : c){
					//        			System.out.println(sJObject.toJSONString());
					//    				System.out.println("BOOLEAN: "+c.contains(sJObject));
					        			if(!Server.serverList.contains(sJObject)){
					        				Server.serverList.add(sJObject);
					        				
					        				if(!Server.subList.isEmpty()){
					        					Iterator<Entry<String, ArrayList<Object>>> iterator = Server.subList.entrySet().iterator();
					        					if(iterator.hasNext()){
					        						Entry<String, ArrayList<Object>> entry = iterator.next();
					        						ArrayList<Object> subArrayList = entry.getValue();
					        						JSONObject resTemp = Resource.formResourceInJSON((Resource)subArrayList.get(Server.SUB_TEMP));
					        						
					        						JSONObject excSubJson = new JSONObject();
							        				excSubJson.put("command", "SUBSCRIBE");
							        				excSubJson.put("relay", false);
							        				excSubJson.put("id", entry.getKey());
							        				excSubJson.put("resourceTemplate",resTemp);
							        				subThreadPool.execute(new subFromServer(excSubJson,sJObject,logger));
					        					}
						        				
					        				}
					        			}
					        		}
					    			resultExcJ.put("response", "success");
					    		}
					    		else{
					    			resultExcJ.put("response", "error");
					    			resultExcJ.put("errorMessage", "missing or invalid server list");
					    		}
					
								try {
									dataOut = new DataOutputStream(socket.getOutputStream());
									dataOut.writeUTF(resultExcJ.toJSONString());
									logger.log(Level.INFO, "SEND:"+resultExcJ.toJSONString());
									dataOut.flush();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
//								System.out.println(""+Server.serverList.size());
				
	    		
	
				break;
			}
			case "QUERY":{
					JSONObject resultQuery = new JSONObject();
					if(jObject.containsKey("resourceTemplate")){
						JSONObject s2sJson = new JSONObject();
						s2sJson = (JSONObject) jObject.get("resourceTemplate");
						
						if(s2sJson == null){
							try {
								dataOut = new DataOutputStream(socket.getOutputStream());
				    			resultQuery.put("response", "error");
				    			resultQuery.put("errorMessage", "missing resourceTemplate");
				    			dataOut.writeUTF(resultQuery.toJSONString());
				    			dataOut.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else if(!((String)s2sJson.get("uri")).equals("") && !Resource.isAbsoluteURI((String) s2sJson.get("uri"))){
			//				System.out.println(Resource.isAbsoluteURI((String) s2sJson.get("uri")));
							try {
								dataOut = new DataOutputStream(socket.getOutputStream());
				    			resultQuery.put("response", "error");
				    			resultQuery.put("errorMessage", "invalid resourceTemplate");
				    			dataOut.writeUTF(resultQuery.toJSONString());
				    			dataOut.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else{
							if(jObject.containsKey("relay")){
								if((Boolean)jObject.get("relay") == true){
									// For each host in server list, send a client query with relay is false
									int resSize = 0;
									resSize+=serverQuery(jObject, socket,logger);
									// Get query result from other servers
									JSONParser jParser = new JSONParser();
									s2sJson.replace("owner", "*");
									try {
										dataOut = new DataOutputStream(socket.getOutputStream());
										for(JSONObject server:Server.serverList){
		//									System.out.println("Query to other server!");
											boolean reachable = true;
											try {
												Socket querySocket = new Socket((String)server.get("hostname"), ((Long) server.get("port")).intValue());
											} catch (ConnectException e) {
												// TODO: handle exception
												reachable = false;
											}
		//									System.out.println(reachable);
											if(reachable == true){
												
												Query sQuery = new Query();
												
												ArrayList<String> reStrings = sQuery.clientQuery((String)server.get("hostname"), ((Long) server.get("port")).intValue(), false, s2sJson,false,((Long) server.get("port")).intValue(),false);
												for(String res:reStrings){
													JSONObject resJ = (JSONObject) jParser.parse(res);
													if(resJ.containsKey("response")){
													}
													else if (resJ.containsKey("resultSize")) {
					//									System.out.println(resJ.get("resultSize").getClass());
														resSize+=((Long) resJ.get("resultSize")).intValue();
													}
													else{
														dataOut.writeUTF(resJ.toJSONString());
														dataOut.flush();
													}
												}
											}
										}
										// Send the last json object contains result size 
										JSONObject lastJson = new JSONObject();
										lastJson.put("resultSize", resSize);
										dataOut.writeUTF(lastJson.toJSONString());
										dataOut.flush();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								else{
									int resultSize = serverQuery(jObject, socket,logger);
									JSONObject lastJson = new JSONObject();
									lastJson.put("resultSize", resultSize);
									try {
										dataOut = new DataOutputStream(socket.getOutputStream());
										dataOut.writeUTF(lastJson.toJSONString());
										dataOut.flush();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
								}
							}else{
								int resultSize = serverQuery(jObject, socket,logger);
								JSONObject lastJson = new JSONObject();
								lastJson.put("resultSize", resultSize);
								try {
									dataOut = new DataOutputStream(socket.getOutputStream());
									dataOut.writeUTF(lastJson.toJSONString());
									dataOut.flush();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}else{
						try {
							dataOut = new DataOutputStream(socket.getOutputStream());
			    			resultQuery.put("response", "error");
			    			resultQuery.put("errorMessage", "missing resourceTemplate");
			    			dataOut.writeUTF(resultQuery.toJSONString());
			    			dataOut.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				
				
			}
			break;
			case "SUBSCRIBE":

//					System.out.println("inside subscribe!");
					DataOutputStream dataOutSub = null;
					String resultSub = "success";
					//Check resource template
					if(!jObject.containsKey("resourceTemplate")){
						resultSub = "missing resourceTemplate";
					}
					else {
						if (!jObject.containsKey("id")) {
							resultSub = "missing id";		
						}
						else{
							try {
								String idSub = (String) jObject.get("id");
								dataOutSub = new DataOutputStream(socket.getOutputStream());
								JSONObject resultSubJson = new JSONObject();
								resultSubJson.put("response", resultSub);
								resultSubJson.put("id", idSub);
								dataOutSub.writeUTF(resultSubJson.toJSONString());
								logger.log(Level.INFO, "SEND:"+resultSubJson.toJSONString());
								dataOutSub.flush();
								
								
								addSubMap(jObject, socket);
								if(jObject.containsKey("relay") && (boolean)jObject.get("relay")){
									/*
									 * relay to other servers 
									 */
									if(!Server.serverList.isEmpty()){
										for(JSONObject server:Server.serverList){
			//								System.out.println("Query to other server!");
//											System.out.println(jObject.toJSONString());
//											System.out.println(server.toJSONString());
											subThreadPool.execute(new subFromServer(jObject,server,logger));
										}
									}
									unSubHandler(socket, idSub,logger);
								}
								else{
									unSubHandler(socket, idSub,logger);
								}
								
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}	
					}
					if(!resultSub.equals("success")){
						JSONObject resultSubJson = new JSONObject();
						resultSubJson.put("response", "error");
						resultSubJson.put("errorMessage", resultSub);
						try {
							dataOutSub = new DataOutputStream(socket.getOutputStream());
							dataOutSub.writeUTF(resultSubJson.toJSONString());
							dataOutSub.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
				}

				
				break;
			default:
				try {
					dataOut = new DataOutputStream(socket.getOutputStream());
					JSONObject wrongCommand = new JSONObject();
	    			wrongCommand.put("response", "error");
	    			wrongCommand.put("errorMessage", "invalid command");
	    			dataOut.writeUTF(wrongCommand.toJSONString());
	    			dataOut.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
	    	}
		}
	}
	private static void addSubMap(JSONObject subJson,Socket socket) {
			Subscribe sub = new Subscribe(subJson);
			String subID = sub.getId();
			Resource subRes = sub.getRes();
//			System.out.println("get id");
			/*
			 * Put sub information into two hashmap
			 */
			ArrayList<Object> subArrayList = new ArrayList<>();
			subArrayList.add(Server.SUB_TEMP, subRes);
			subArrayList.add(Server.SUB_SOCKET, socket);
			subArrayList.add(Server.SUB_COUNTER, 0);
			subArrayList.add(Server.SUB_RESULT, new ArrayList<Resource>());
			Server.subList.put(subID, subArrayList);
		
//		Server.subTempList.put(subID, subRes);
//		Server.subSocketList.put(subID, socket);
//		Server.subCounterList.put(subID, 0);
//		Server.subResultList.put(subID, new ArrayList<Resource>());
//		System.out.println("SubTemp length: "+Server.subTempList.toString());
//		System.out.println("SubSocket: "+Server.subSocketList.toString());
//		System.out.println("SubCounter: "+Server.subCounterList.toString());
	}
	private static void unSubHandler(Socket socket,String idSub,Logger logger) {
		
			boolean subEndFlag = true;
			while(subEndFlag){
				DataInputStream dataInSub = null;
				DataOutputStream dataOutSub = null;
				try {
					
					dataInSub = new DataInputStream(socket.getInputStream());

					int singleByte;
					while(dataInSub.available()>0){
//						returnMsgs.add(dataIn.readUTF());

						String unSubIn = dataInSub.readUTF();
//						System.out.println(unSubIn);
						JSONParser jParserUnSub = new JSONParser();
						JSONObject jObjectUnSub = (JSONObject) jParserUnSub.parse(unSubIn);
						logger.log(Level.INFO,"RECEIVED:"+jObjectUnSub.toJSONString());
						if(jObjectUnSub.containsKey("command")){
							if(jObjectUnSub.get("command").equals("UNSUBSCRIBE")){
								if(jObjectUnSub.containsKey("id")){
									if(jObjectUnSub.get("id").equals(idSub)){
										/*
										 * Retrieve the counter in sub counter map
										 */
//										System.out.println(jObjectUnSub.toJSONString());
										ArrayList<Object> unsubArrayList = Server.subList.get(idSub);
										int counterSub = (int) unsubArrayList.get(Server.SUB_COUNTER);
										dataOutSub = new DataOutputStream(socket.getOutputStream());
										JSONObject finJsonSub = new JSONObject();
										finJsonSub.put("resultSize", counterSub);
										dataOutSub.writeUTF(finJsonSub.toJSONString());
										logger.log(Level.INFO, "SEND:"+finJsonSub.toJSONString());
										dataOutSub.flush();
										
										subEndFlag = false;
										Server.subList.remove(idSub);
									}
								}
							}
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.getStackTrace();
				}
			}
		
	}

	private static void searchSubTemp(Resource resource) {
		/*
		 * Search for sub Template, 
		 */
			if(!Server.subList.isEmpty()){
				Iterator<Entry<String, ArrayList<Object>>> iterator = Server.subList.entrySet().iterator();
				while(iterator.hasNext()){
					Entry<String, ArrayList<Object>> entry = iterator.next();
					ArrayList<Object> subArrayList = entry.getValue();
					Resource resTempInList = (Resource) subArrayList.get(Server.SUB_TEMP);
					if(Resource.queryTemplateMathcer(resTempInList, resource)){
//						System.out.println("Found sub temp!!");
						String idMatch = entry.getKey();
						/*
						 * Get a matched id from sublist, next step is get the socket store in server and send this resource back to client with this id.
						 */
						resource.setEZServer(Server.hostName);
						@SuppressWarnings("unchecked")
						ArrayList<Resource> resultList = (ArrayList<Resource>) subArrayList.get(Server.SUB_RESULT);
						
						resultList.add(resource);
						subArrayList.set(Server.SUB_RESULT, resultList);
						Server.subList.replace(idMatch, subArrayList);
					}
					
				}
			}	
	}
	
	/*
	 * 
	 */
	private static int serverQuery(JSONObject jObject,Socket socket,Logger logger){
		JSONParser jParser = new JSONParser();
//		System.out.println("jObject is: "+jObject.get("resourceTemplate").getClass());
		JSONObject query = new JSONObject();
		query = (JSONObject) jObject.get("resourceTemplate");
//		System.out.println("Query resTemp"+query.toJSONString());
		DataOutputStream dataOut = null;
		int resSize = 0;
		try {
			dataOut = new DataOutputStream(socket.getOutputStream());
			Resource resTemp = Resource.formResourceFromJSON(query);
			JSONObject firJson = new JSONObject();
			firJson.put("response", "success");
			dataOut.writeUTF(firJson.toJSONString());
			logger.log(Level.INFO,"SEDN:"+firJson.toJSONString());
			dataOut.flush();

			if(Server.resMap!=null){
//				System.out.println("Res map not null");
				Iterator<Entry<ArrayList<String>, Resource>> iterator = Server.resMap.entrySet().iterator();
				while(iterator.hasNext()){
					Entry<ArrayList<String>, Resource> entry = iterator.next();
//					System.out.println("equal Lists:"+resTemp.getKey().toString()+" "+entry.getValue().getKey().toString()+"result" +equalLists(resTemp.getKey(), entry.getValue().getKey()));
					if((Resource.queryTemplateMathcer(resTemp, entry.getValue()))){
						
						Resource respRes = entry.getValue();
//						for(String s:respRes.getAlltags()){
//							System.out.println(s);
//						}
//						System.out.println(Server.hostName);
						respRes.setEZServer(Server.hostName);
						respRes.setOwner();
						JSONObject resJson = Resource.formResourceInJSON(respRes);
//						System.out.println(resJson.toJSONString());
						dataOut.writeUTF(resJson.toJSONString());
						logger.log(Level.INFO, "SEND:"+ resJson.toJSONString());
						dataOut.flush();
						resSize+=1;

					}
				}
			}
			else{
				resSize = 0;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return resSize;	
	}

}

class subFromServer implements Runnable{
	private JSONObject subCommand;
	private JSONObject server;
//	private boolean secureFlag;
	private Logger logger=null;
	public subFromServer(JSONObject subCommand,JSONObject server,Logger logger) {
		// TODO Auto-generated constructor stub
		this.subCommand = subCommand;
		this.server = server;
//		this.secureFlag = secureFlag;
		this.logger = logger;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
			Socket s2sSocket=null;
			boolean reachable = true;
			try {
				s2sSocket = new Socket((String)server.get("hostname"), ((Long) server.get("port")).intValue());
			} catch (ConnectException e) {
			// TODO: handle exception
				Server.serverList.remove(server);
				reachable = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			System.out.println(reachable);
			if(reachable){
				subCommand.replace("relay", false);
				try {
					DataOutputStream dataOuts2s = new DataOutputStream(s2sSocket.getOutputStream());
					DataInputStream dataIns2s = new DataInputStream(s2sSocket.getInputStream());
					dataOuts2s.writeUTF(subCommand.toJSONString());
					logger.log(Level.INFO,"SEND:"+subCommand.toJSONString());
					dataOuts2s.flush();
					
					while(Server.subList.containsKey(subCommand.get("id"))){
						String idSubs2s = (String) subCommand.get("id");
						dataIns2s = new DataInputStream(s2sSocket.getInputStream());

						int singleByte;
						while(dataIns2s.available()>0){
							JSONObject s2sJson = Handler.stringtoJSON(dataIns2s.readUTF());
							if(s2sJson!=null){
								if(s2sJson.containsKey("response")){
								}
								else if (s2sJson.containsKey("resultSize")) {
								}
								else {
									Resource ress2s = Resource.formResourceFromJSON(s2sJson);
									ArrayList<Object> subArrayList = Server.subList.get(idSubs2s);
									@SuppressWarnings("unchecked")
									ArrayList<Resource> resultList = (ArrayList<Resource>) subArrayList.get(Server.SUB_RESULT);
									resultList.add(ress2s);
									subArrayList.set(Server.SUB_RESULT, resultList);
									Server.subList.replace(idSubs2s, subArrayList);
								}
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		
		
	}
	
}
