package dataStructures;

import org.json.JSONObject;

public class MemeBotMessage2000 {
	
	private String user;
	private String command;
	private String body;
	private long channelID;

	/**
	 * Create message from JSON object
	 * @param j JSON object of the message
	 */
	public MemeBotMessage2000(JSONObject j) {
		user = j.getString("user");
		command = j.getString("command");
		body = j.getString("body");
		channelID = j.getLong("channelID");
	}

	/**
	 * Create a message from variables
	 * @param user Username for message
	 * @param command Command for message
	 * @param body Body of the message
	 * @param channelID ChannelID of the message
	 */
	public MemeBotMessage2000(String user, String command, String body, long channelID) {
		this.user = user;
		this.command = command;
		this.body = body;
		this.channelID = channelID;
	}
	
	/**
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
	
}
