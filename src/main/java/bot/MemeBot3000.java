package bot;


import javax.security.auth.login.LoginException;

import app.MemeConfigLoader3000;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;


/**
 * Class to create and handle the meme bot
 * @author Ben Shabowski
 * @Version 3001
 * @since 2000
 */
public class MemeBot3000 {
	
	JDA bot;
	
	public MemeBot3000(MemeConfigLoader3000 botConfig) {
		
		this(botConfig.getBotToken());
		
	}
	
	public MemeBot3000(String token) {
		JDABuilder bot = JDABuilder.createDefault(token).setMemberCachePolicy(MemberCachePolicy.ALL).enableIntents(GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL);
		
		bot.addEventListeners(new MemeBotInterfacer3000.Listener());
		bot.setActivity(Activity.playing("Someone get this man a meme"));
		
		try {
			this.bot = bot.build().awaitReady();
		} catch (LoginException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public JDA getBot() {
		return bot;
	}

}
