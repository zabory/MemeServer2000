package datastructures;

import org.json.JSONObject;

/**
 * 
 * @author Ben Shabowski
 * @version 2000
 * @since 2000
 */
public class MemeBotMsg2000 {

	private String user;
	private String command;
	private String body;
	private long channelID;
	private boolean admin;
	private String url;
	private JSONObject json;
	private boolean[] tagDeny;
	/**
	 * Create a message from variables
	 * 
	 * @param user      Username for message
	 * @param command   Command for message
	 * @param body      Body of the message
	 * @param channelID ChannelID of the message
	 * @param admin If the message comes from a user with admin
	 * @param url embedded URL of an image
	 */
	public MemeBotMsg2000(String user, String command, String body, long channelID, boolean admin, String url, boolean...tagValues) {
		this.user = user;
		this.command = command;
		this.body = body.toLowerCase();
		this.channelID = channelID;
		this.admin = admin;
		this.url = url;
		tagDeny = new boolean[10];
		for(int i = 0; i < tagValues.length; i++) {
			tagDeny[i] = tagValues[i];
		}
	}

	/**
	 * Default constructor for class, setting everything to the default value
	 */
	public MemeBotMsg2000() {
		this("", "", "", 0, false, "");
	}

	/**
	 * Create message from JSON object
	 * 
	 * @param jObject JSON object of the message
	 */
	public MemeBotMsg2000(JSONObject jObject) {
		if(jObject.has("user")) {
			user = jObject.getString("user");
		}
		
		if(jObject.has("command")) {
			command = jObject.getString("command");
		}
		
		if(jObject.has("body")) {
			body = jObject.getString("body");
		}
		
		if(jObject.has("url")) {
			url = jObject.getString("url");
		}
		
		if(jObject.has("channelID")) {
			channelID = jObject.getLong("channelID");
		}
		
		if(jObject.has("admin")) {
			admin = jObject.getBoolean("admin");
		}
		
		for(int i = 0; tagDeny != null && i < tagDeny.length; i++) {
			if(jObject.has("tag" + (i + 1))) {
				tagDeny[i] = jObject.getBoolean("tag" + (i + 1));
			}
		}
		
		json = jObject;
	}

	/**
	 * Converts the class and all its variables into a JSON object
	 * 
	 * @return JSON object
	 */
	public JSONObject toJSON() {
		JSONObject j = new JSONObject();
		j.put("user", user);
		j.put("command", command);
		j.put("body", body);
		j.put("channelID", channelID + "");
		j.put("admin", admin);
		j.put("url", url);
		for(int i = 0; i < tagDeny.length; i++) {
			j.put("tag" + (i + 1), tagDeny[i]);
		}
		return j;
	}
	
	public String toString() {
		return json.toString();
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getCommand() {
		if(command != null) {
			return command;
		}else {
			return "print";
		}
	}
	
	public boolean[] getTagDeny() {
		return tagDeny;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public long getChannelID() {
		return channelID;
	}

	public void setChannelID(long channelID) {
		this.channelID = channelID;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public MemeBotMsg2000 user(String user) {
		this.user = user;
		return this;
	}

	public MemeBotMsg2000 command(String command) {
		this.command = command;
		return this;
	}

	public MemeBotMsg2000 body(String body) {
		this.body = body;
		return this;
	}

	public MemeBotMsg2000 channelID(long channelID) {
		this.channelID = channelID;
		return this;
	}
	
	public MemeBotMsg2000 admin(boolean admin) {
		this.admin = admin;
		return this;
	}
	
	public MemeBotMsg2000 url(String url) {
		this.url = url;
		return this;
	}

}
