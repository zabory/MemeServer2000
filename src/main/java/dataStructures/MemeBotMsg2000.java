package dataStructures;

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

	/**
	 * Create a message from variables
	 * 
	 * @param user      Username for message
	 * @param command   Command for message
	 * @param body      Body of the message
	 * @param channelID ChannelID of the message
	 */
	public MemeBotMsg2000(String user, String command, String body, long channelID) {
		this.user = user;
		this.command = command;
		this.body = body.toLowerCase();
		this.channelID = channelID;
	}

	/**
	 * Default constructor for class, setting everything to the default value
	 */
	public MemeBotMsg2000() {
		this("", "", "", 0);
	}

	/**
	 * Create message from JSON object
	 * 
	 * @param jObject JSON object of the message
	 */
	public MemeBotMsg2000(JSONObject jObject) {
		this(jObject.getString("user"), jObject.getString("command"), jObject.getString("body").toLowerCase(),
				jObject.getLong("channelID"));
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
		j.put("channelID", channelID);
		return j;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getCommand() {
		return command;
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

}
