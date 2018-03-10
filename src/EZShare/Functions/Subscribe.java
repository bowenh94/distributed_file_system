package EZShare.Functions;

import org.json.simple.JSONObject;

import EZShare.Resource;


public class Subscribe {
	JSONObject jObject;
	public Subscribe(JSONObject jObject) {
		// TODO Auto-generated constructor stub
		this.jObject = jObject;
	}
	public String getId(){
		String id = (String) jObject.get("id");
		return id;
	}
	public Resource getRes(){
		JSONObject subResJson = (JSONObject) jObject.get("resourceTemplate");
		Resource subRes = Resource.formResourceFromJSON(subResJson);
		return subRes;
	}

}
