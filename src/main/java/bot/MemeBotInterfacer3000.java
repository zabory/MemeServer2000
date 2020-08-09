package bot;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import app.MemeConfigLoader3000;

import datastructures.MemeBotMsg3000;
import datastructures.MemeLogger3000;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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

	private MemeLogger3000 logger;

	// actual bot
	private MemeBot3000 bot;

	public MemeBotInterfacer3000(MemeConfigLoader3000 config, BlockingQueue<MemeBotMsg3000> botInputQ,
			BlockingQueue<MemeBotMsg3000> botOutputQ, MemeLogger3000 logger) {

		this.input = botInputQ;
		this.output = botOutputQ;
		this.logger = logger;

		// launch bot
		bot = new MemeBot3000(config);
		logger.println("Bot logged in");
	}

	public void messageHandler(MessageReceivedEvent e) {

	}

	public void messageReactionHandler(MessageReactionAddEvent e) {

	}
	
	public Queue<MemeBotMsg3000> getInput() {
		return input;
	}

	public Queue<MemeBotMsg3000> getOutput() {
		return output;
	}
	
	public static class Listener extends ListenerAdapter {

		@Override
		public void onReady(ReadyEvent event) {
			
		}

		@Override
		public void onMessageReceived(MessageReceivedEvent event) {

			event.getJDA().getGuilds().get(0).retrieveMemberById(event.getMessage().getAuthor().getId()).complete().getRoles().forEach(e -> {
				System.out.println(e.getName());
			});

			System.out.println(event.getAuthor().getName() + ": " + event.getMessage().getContentDisplay());

		}

		@Override
		public void onMessageReactionAdd(MessageReactionAddEvent event) {

		}
	}

}
