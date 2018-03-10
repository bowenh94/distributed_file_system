package EZShare;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.istack.internal.localization.NullLocalizable;

import org.apache.commons.validator.routines.UrlValidator;


public class Resource {
	// local variables
	private String name = "";
	private String description ="";
	private String[] tags;
	private String uri ="";
	private String channel ="";
	private String owner ="";
	private String ezServer = null;
	private static final int tagSize = 10;
	private int tagNum = 0;
	
	// constructors
	public Resource(String owner, String channel, String uri){
		this.owner = owner;
		this.channel = channel;
		this.uri = uri;
		tags = new String[tagSize];
	}
	
	public Resource(String uri){
		this.uri = uri;
	}
	
	public Resource(String name, String[] tags, String description, String uri, String channel, String owner){
		this.name = name;
		this.tags = tags;
		this.description = description;
		this.uri = uri;
		this.channel = channel;
		this.owner = owner;
	}
	
	public Resource(String name, String[] tags, String description, String uri, String channel, String owner, String ezServer){
		this.name = name;
		this.tags = tags;
		this.description = description;
		this.uri = uri;
		this.channel = channel;
		this.owner = owner;
		this.ezServer=ezServer;
	}
	
	//accessors
	public String getName(){return this.name;}
	
	public String getDescription(){return this.description;}
	
	public String[] getAlltags(){
		return this.tags;
	}
	
	public String getURI(){return this.uri;}
	
	public String getChannel(){return this.channel;}
	
	public String getOwner(){return this.owner;}
	
	public String getEZServer(){return this.ezServer;}
	
	public ArrayList<String> getKey(){
		ArrayList<String> pk = new ArrayList<String>();
		pk.add(this.owner);
		pk.add(this.channel);
		pk.add(this.uri);
		return pk;
	}
	
	// mutators
	public void setEZServer(String newValue){
		this.ezServer = newValue;}
	public void setOwner(){
		this.owner = "*";
	}
	
	// form a Resource from a command line. Strings are not checked.
	@SuppressWarnings("unchecked")
	public static JSONObject formResourceInJSON(CommandLine commandLine){
		
		String value = null;
		
		JSONObject resource = new JSONObject();
		
		// name
		value = commandLine.getOptionValue("name");
		if(value == null){
			value ="";
			resource.put("name",value);
		}else{
			resource.put("name",value);
		}
		
		
		// tags, an array
		value = commandLine.getOptionValue("tags");
		JSONArray list = new JSONArray();
		if(value == null){
			list.add("");
			resource.put("tags", null);
		}else{
			List<String> tags = Arrays.asList(value.split(","));
			for(int i = 0; i < tags.size(); i++){
				list.add(tags.get(i));
			}
			resource.put("tags",list);
		}
		
		
		
		// description
		value = commandLine.getOptionValue("description");
		if(value == null){
			value ="";
			resource.put("description",value);
		}else{
			resource.put("description",value);
		}
		
		
		// uri
		value = commandLine.getOptionValue("uri");
		if(value == null){
			value ="";
			resource.put("uri",(value));
		}else{
			resource.put("uri",(value));
		}
		
		
		// channel
		value = commandLine.getOptionValue("channel");
		if(value == null){
			value ="";			
			resource.put("channel",value);
		}else{
			resource.put("channel",value);
		} 
		
		
		// owner
		value = commandLine.getOptionValue("owner");
		if(value == null){
			value ="";
			resource.put("owner",value);
		}else{
			resource.put("owner",value);
		}
		
		
		//ezserver
		value = commandLine.getOptionValue("ezserver");
		if(value == null){
			value = null;
			resource.put("ezserver", null);
		}else {
			resource.put("ezserver", value);
		}
		
		return resource;
	}
	
	// Transfer Resource object into JSONObject, assuming all string in Resource do not break any generic String Rules.
	public static JSONObject formResourceInJSON(Resource orgResource){
		
		String value = null;
		
		JSONObject resource = new JSONObject();
		
		// name
		value = orgResource.getName();
		if(value == null){
			value ="";
			resource.put("name",value);
		}else{
			resource.put("name",value);
		}
		
		
		// tags, an array
		String[] vnima = orgResource.getAlltags();
		if(vnima == null){
			resource.put("tags", null);
		}else{
			resource.put("tags",vnima);
		}
		
		
		
		// description
		value = orgResource.getDescription();
		if(value == null){
			value ="";
			resource.put("description",value);
		}else{
			resource.put("description",value);
		}
		
		
		// uri
		value = orgResource.getURI();
		if(value == null){
			value ="";
			resource.put("uri",(value));
		}else{
			resource.put("uri",(value));
		}
		
		
		// channel
		value = orgResource.getChannel();
		if(value == null){
			value ="";			
			resource.put("channel",value);
		}else{
			resource.put("channel",value);
		} 
		
		
		// owner
		value = orgResource.getOwner();
		if(value == null){
			value ="";
			resource.put("owner",value);
		}else{
			resource.put("owner",value);
		}
		
		
		//ezserver
		value = orgResource.getEZServer();
		if(value == null){
			value = null;
			resource.put("ezserver", null);
		}else {
			resource.put("ezserver", value);
		}
		
		return resource;
	}
	
	public static Resource formResourceFromJSON(JSONObject json){
		String name = "";
		if(json.get("name")!=null){
			name = (String)json.get("name");
		}
		
//		System.out.println(json.get("tags").getClass());
		String[] tags = null;
		if(json.get("tags")!=null){
			JSONArray tagsJArray = (JSONArray) json.get("tags");

				tags = new String[tagsJArray.size()];
				for(int i = 0;i<tagsJArray.size();i++){
					tags[i] = (String) tagsJArray.get(i);
				}
			for(int i = 0; i<tags.length;i++){
				tags[i] = Resource.verifyStr(tags[i]);
			}
		}
		
		String description = "";
		if(json.get("description")!= null){
			description = (String) json.get("description");
		}
		
		String uri = "";
		if(json.get("uri")!= null){
			uri = (String) json.get("uri");
		}
		
		String channel = "";
		if(json.get("channel")!=null){
			channel = (String) json.get("channel");
		}
		
		String owner = "";
		if(json.get("owner")!=null){
			owner = (String) json.get("owner");
		}
		
		String ezserver = null;
//		System.out.println("EzServer is "+json.get("ezserver"));
		if(json.get("ezserver")!=null){
			ezserver = (String) json.get("ezserver");
		}
		
		
		name = Resource.verifyStr(name);

		description = Resource.verifyStr(description);
		uri = Resource.verifyStr(uri);
		channel = Resource.verifyStr(channel);
		owner = Resource.verifyStr(owner);
		ezserver = Resource.verifyStr(ezserver);
//		System.out.println(ezserver);

		Resource resource = new Resource(name, tags, description, uri, channel, owner, ezserver);

		return resource;
	}
	
	
	public static Resource formResourceFromJSONOnURI(JSONObject json){
		String uri = (String) json.get("uri");
		String channel = (String) json.get("channel");
		String owner = (String) json.get("owner");
		
		Resource.verifyStr(uri);
		Resource.verifyStr(channel);
		Resource.verifyStr(owner);
		
		Resource resource = new Resource(uri, channel, owner);
		return resource;
	}
	
	 // validate an uri
	 public static boolean isAbsoluteURI(String str){
		 UrlValidator urlVal = new UrlValidator();
		 return urlVal.isValid(str);
	 }
	
	// verify a string
	public static String verifyStr(String str){
		if(str!=null){
			if(!str.equals("")){
				if(str.contains("\\0"))
					str = str.replaceAll("(\\\\0)","");
				return str.trim();
			}
			return str;
		}
		else
			return null;
	}
	
	// validate an uri
	 public static boolean isFileScheme(String path){
		 if(path.contains(":")){
			 String[] filepath = path.split(":");
			  File file = new File(filepath[1]);
			  return file.isFile();
		 }else
			 return false;
	 
	  
	 }
	 
	 public static boolean isFileAbsoluteScheme(String path){
		 if(path.contains(":")){
			 String[] filepath = path.split(":");
			 if(filepath[0].equals("file"))
				 return true;
			 else
				 return false;
		 }else
			 return false;
	 }
	 
	 //Method to verify if a resource template is suitable for query and subscribe command
	 public static boolean queryTemplateMathcer(Resource temp, Resource target) {
		 if(temp.getChannel().equals(target.getChannel())){
			 if(temp.getOwner().equals("") || temp.getOwner().equals(target.getOwner())){
				 if(equalLists(temp.getAlltags(), target.getAlltags())){
					 if(temp.getURI().equals("") || temp.getURI().equals(target.getURI())){
						 if(target.getName().contains(temp.getName()) || target.getDescription().contains(temp.getDescription()) || (temp.getName().equals("") && temp.getDescription().equals(""))){
							 return true;
							 
						 }
						 else{
							 System.out.println("1");
							 return false;
						 }
							 
					 }
					 else{
						 System.out.println("2");
						 return false;
					 }
				 }
				 else{
					 System.out.println("3");
					 return false;
				 }
			 }
			 else{
				 System.out.println("4");
				 return false;
			 }
		 }
		 else{
			 System.out.println("5");
			 return false;
		 }
		
	}
		private static boolean equalLists(String[] alltags, String[] alltags2) {
			// TODO Auto-generated method stub
			if(alltags==null&&alltags2==null){
				return true;
			}
			else if (alltags==null) {
				return true;
			}
			else if (alltags2==null) {
				return false;
			}
			else {
				boolean result = true;
				ArrayList<String> aList = new ArrayList<>();
				for(String s:alltags2){
					aList.add(s);
				}
				for(String s1:alltags){
					result = result&aList.contains(s1);
				}
				return result;
			}

		}
	
//	public JSONObject resource2JSONObject(Resource resource){
//		JSONObject obj = new JSONObject();
//		return obj;
//	}
}
