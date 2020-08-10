package datastructures;

/**
 * 
 * @author Ben Shabowski
 * @version 2000
 * @since 2000
 */
public class MemeBotMsg3000 {
	
	public enum MemeBotType{
		// To switchboard
		
		/**Deny a meme*/
		Deny,
		
		/**Approve a meme*/
		Approve,
		
		/**Fetch a meme from DB with or without tags*/
		Fetch_Meme,
		
		/**Submit a meme to DB*/
		Submit_Meme,	
		
		// To bot
		/**Clear the queue in the auth channel*/
		Clear_Queue,
		
		/**Send a message to the user*/
		Send_User,
		
		/**Send a message to the channel*/
		Send_Channel,
		
		/**Send a meme to be approved in the approval channel*/
		Send_Queue,
		
		/**Send the queue size to be updated in the approval channel*/
		Queue_Size,
		
		/**Send all the tags to be updated in the help channel*/
		Send_Tags,
		
		/**Send all the commands to be updated in the help channel*/
		Send_Commands,
		
		/**Initialize*/
		INIT,
		
		/**Clear the help channel*/
		Clear_Help
	}

	private MemeBotType type;
	private String user;
	private String body;
	private long channelID;
	private boolean admin;
	private String url;
	private String tags;
	private long userID;

	/**
	 * Default constructor for class, setting everything to the default value
	 */
	public MemeBotMsg3000() {
		
	}
	
	
	
	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public MemeBotType getType() {
		return type;
	}
	
	public void setType(MemeBotType type) {
		this.type = type;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getBody() {
		return body;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
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

	public MemeBotMsg3000 user(String user) {
		this.user = user;
		return this;
	}

	public MemeBotMsg3000 body(String body) {
		this.body = body;
		return this;
	}

	public MemeBotMsg3000 channelID(long channelID) {
		this.channelID = channelID;
		return this;
	}
	
	public MemeBotMsg3000 admin(boolean admin) {
		this.admin = admin;
		return this;
	}
	
	public MemeBotMsg3000 url(String url) {
		this.url = url;
		return this;
	}
	
	public MemeBotMsg3000 tags(String tags) {
		this.tags = tags;
		return this;
	}
	
	public MemeBotMsg3000 type(MemeBotType type) {
		this.type = type;
		return this;
	}
	
	public MemeBotMsg3000 userID(long userID) {
		this.userID = userID;
		return this;
	}

}
