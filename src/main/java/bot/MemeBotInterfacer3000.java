package bot;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import app.MemeConfigLoader3000;

import datastructures.MemeBotMsg3000;
import datastructures.MemeBotMsg3000.MemeBotType;
import datastructures.MemeLogger3000;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import static datastructures.MemeBotMsg3000.MemeBotType.*;

/**
 * An interfacer between the memeBot and the main thread. Also contains and
 * hosts the process for the bot.
 * 
 * @author Ben Shabowski
 * @version 3001
 * @since 2000
 */
public class MemeBotInterfacer3000 {

	// input from main thread
	private BlockingQueue<MemeBotMsg3000> input;
	// output to main thread
	private BlockingQueue<MemeBotMsg3000> output;

	//logger yaaaaaas
	private MemeLogger3000 logger;

	// actual bot
	private JDA bot;
	
	//some things we need a lot
	private TextChannel approvalChannel;
	private TextChannel helpChannel;
	private Guild guild;

	public MemeBotInterfacer3000(MemeConfigLoader3000 config, BlockingQueue<MemeBotMsg3000> botInputQ,
			BlockingQueue<MemeBotMsg3000> botOutputQ, MemeLogger3000 logger) {

		this.input = botInputQ;
		this.output = botOutputQ;
		this.logger = logger;

		// launch bot
		bot = new MemeBot3000(config, this).getBot();
		logger.println("Bot logged in");
		
		guild = bot.getGuilds().get(0);
		approvalChannel = guild.getTextChannelsByName("meme-approval", true).get(0);
		helpChannel = guild.getTextChannelsByName("meme-bot-help", true).get(0);
		
		new inputThread().start();
	}

	public void messageHandler(MessageReceivedEvent e) {
		User user = e.getAuthor();
		MessageChannel channel = e.getChannel();
		Message message = e.getMessage();

		boolean allowedChannel = channel.getName().contains("meme-approval");

		// see if its not meme approval
		if (!allowedChannel) {
			// if its the !meme command
			if (message.getContentDisplay().contains("!meme")) {
				output.add(new MemeBotMsg3000().user(user.getName()).channelID(channel.getIdLong()).type(Fetch_Meme)
						.body(message.getContentDisplay().replace("!meme", "").replace("!request", "")));
			} else if (channel.getType() == ChannelType.PRIVATE) {
				if (message.getAttachments().size() != 0) {
					String url = message.getAttachments().get(0).getUrl();
					String tags = message.getContentDisplay();
					if (!tags.equals("")) {
						boolean admin = false;

						// check if person is an admin
						for (Role r : guild.retrieveMemberById(user.getId()).complete().getRoles()) {
							if (r.getName().equals("Meme curator")) {
								admin = true;
								break;
							}
						}

						output.add(new MemeBotMsg3000().user(user.getName()).admin(admin).channelID(channel.getIdLong())
								.url(url).body(tags).type(Submit_Meme));
					} else {
						logger.println(user.getName() + " did not give any tags");
						channel.sendMessage("Give me tags! Give me taaaaags!").queue();
					}
				} else {
					logger.println(user.getName() + " did not give me a meme. How rude");
					channel.sendMessage("I dont see a meme here??").queue();
				}
			}
		} else {
			approve(true, message.getContentDisplay(), user);
		}

	}
	
	/**
	 * have a meme be approved
	 * @param addedTags true: add the tags to the list, false: dont add tags
	 * @param tags tags to be added
	 * @param user user who approved
	 */
	public void approve(boolean addedTags, String tags, User user) {
		String approvedTags = "";
		for(TextChannel channel: guild.getTextChannels()) {
			if(channel.getName().equals("meme-approval")) {
				
				for(MessageReaction reaction : channel.getHistoryFromBeginning(3).complete().getRetrievedHistory().get(0).getReactions()) {
					int number = 11;
					if(reaction.getCount() < 2) {
						switch(reaction.getReactionEmote().getName()) {
						case "one":
							number = 1;
							break;
						case "two":
							number = 2;
							break;
						case "three":
							number = 3;
							break;
						case "four":
							number = 4;
							break;
						case "5":
							number = 5;
							break;
						case "six":
							number = 6;
							break;
						case "seven":
							number = 7;
							break;
						case "eight":
							number = 8;
							break;
						case "nine":
							number = 9;
							break;
						case "ten":
							number = 10;
							break;
						}
					}
					
					if(number != 11) {
						String tagsMessage = channel.getHistoryFromBeginning(3).complete().getRetrievedHistory().get(1).getContentDisplay();
						approvedTags += "," + tagsMessage.split("\n")[number].replace(number + ": ", "");
					}
				}
			}
		}
		
		//add admin tags to list
		if(addedTags) {
			approvedTags = approvedTags + "," + tags;
		}
		
		if(!approvedTags.equals("") && approvedTags.charAt(0) == ',') {
			approvedTags = approvedTags.replaceFirst(",", "");
		}
		
		output.add(
				new MemeBotMsg3000().type(Approve).tags(approvedTags).user(user.getName())
				);
	}

	public void messageReactionHandler(MessageReactionAddEvent e) {
		
		if(e.getChannel().getName().equals("meme-approval")) {
			if(e.getReactionEmote().getName().equals("x_")) {
				output.add(new MemeBotMsg3000().type(Deny).user(e.getUser().getName()));
			}else if(e.getReactionEmote().getName().equals("check")) {
				approve(false, "", e.getUser());
			}
		}
	}
	
	public void input(MemeBotMsg3000 message) {
		//logger.println(message.getType() + "");
		MemeBotType command = message.getType();
		
		String body;
		switch(command){
		case Send_User:
			try {
				body = message.getBody();
				bot.openPrivateChannelById(message.getUserID()).complete().sendMessage(body).complete();
				logger.println("Sending message to user " + message.getUser());
			}catch (NullPointerException e) {
				logger.println("Unable to find user " + message.getUser() + " to send a message to");
			}
			break;
		case Send_Channel:
			body = message.getBody();
			TextChannel channel = guild.getTextChannelById(message.getChannelID());
			channel.sendMessage(body).queue();
			logger.println("Send message to channel " + message.getChannelID());
			break;
		case Send_Queue:
			body = message.getBody();
			TextChannel channelId = approvalChannel;
			channelId.sendMessage(body).complete();
			
			Message m = channelId.getHistory().retrievePast(3).complete().get(0);
			
			m.addReaction(guild.getEmotesByName("check", true).get(0)).queue();
			m.addReaction(guild.getEmotesByName("x_", true).get(0)).queue();
			String tags = channelId.getHistory().retrievePast(3).complete().get(1).getContentDisplay();
			
			for(int i = 0; i < tags.split("\n").length - 1; i++) {
				String emojiName = "";
				switch (i) {
				case 0:
					emojiName = "one";
					break;
				case 1:
					emojiName = "two";
					break;
				case 2:
					emojiName = "three";
					break;
				case 3:
					emojiName = "four";
					break;
				case 4:
					emojiName = "five";
					break;
				case 5:
					emojiName = "six";
					break;
				case 6:
					emojiName = "seven";
					break;
				case 7:
					emojiName = "eight";
					break;
				case 8:
					emojiName = "nine";
					break;
				case 9:
					emojiName = "ten";
					break;
				}
				
				m.addReaction(guild.getEmotesByName(emojiName, true).get(0)).complete();
			}
			
			logger.println("Added meme and reactions to meme-approval channel");
			break;
		case Clear_Queue:
			try {
				List<Message> history = approvalChannel.getHistory().retrievePast(50).complete();
				approvalChannel.deleteMessages(history).complete();
				logger.println("We cleared the queue");
			}catch (IllegalArgumentException e) {
				logger.println("We tried to clear an empty queue");
			}
			break;
		case Clear_Help:
			try {
				List<Message> history = helpChannel.getHistory().retrievePast(50).complete();
				helpChannel.deleteMessages(history).complete();
				logger.println("We cleared the help");
			}catch (IllegalArgumentException e) {
				logger.println("We tried to clear an empty help");
			}
			break;
		case Send_Tags:
			
			//each message that needs to be sent out
			Queue<String> tagMessages = new LinkedList<String>();
			
			//each tag
			List<String> indiTags = new LinkedList<String>(Arrays.asList(message.getBody().split(",")));
			
			int currentCharCount = 0;
			String currentMessage = "";
			while(!indiTags.isEmpty()) {
				//get the top tag
				String nextTag = indiTags.remove(0);
				
				//test if the tag will put us over the limit
				if(currentCharCount + nextTag.length() + 1 > 1500) {
					tagMessages.add(currentMessage);
					currentCharCount = nextTag.length();
					currentMessage = nextTag + "\n";
				}else {
					currentMessage += nextTag + "\n";
				}
				
			}
			//add the last message to tagMessages
			tagMessages.add(currentMessage);
			
			//TODO edit existing tag messages or create new ones
			
			
			
			
			break;
		case Send_Commands:
			//TODO get this done boi
			break;
		case Queue_Size:
			if(approvalChannel.getHistoryFromBeginning(3).complete().getRetrievedHistory().size() > 0)
				approvalChannel.getHistoryFromBeginning(3).complete().getRetrievedHistory().get(2).editMessage("**Queue count**:" + message.getBody());
			logger.println("We probably didnt increase the size there");
			break;
		default:
			logger.println(MemeLogger3000.level.ERROR, "Unknown type of " + command);
			break;
		}
		
		
	}


	private class inputThread extends Thread{
		
		public void run() {
			while(true) {
			try {
				input(input.take());
			} catch (InterruptedException e) {
				logger.println(MemeLogger3000.level.ERROR, e.toString());
				e.printStackTrace();
			}
			}
			
		}
	}

}
