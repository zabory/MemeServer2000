package bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:MemeBot.properties")
@PropertySource("classpath:MemeBotTest.properties")
public class MemeBotConfigLoader3000 {

	@Value("${auth.token}")
	private String botToken;
	
	@Value("${channel}")
	private String approvalChannel;
	
	@Value("${helpChannel}")
	private String helpChannel;
	
	@Value("${approveEmoji}")
	private String approveEmoji;
	
	@Value("${denyEmoji}")
	private String denyEmoji;

	public String getBotToken() {
		return botToken;
	}

	public String getApprovalChannel() {
		return approvalChannel;
	}

	public String getHelpChannel() {
		return helpChannel;
	}

	public String getApproveEmoji() {
		return approveEmoji;
	}

	public String getDenyEmoji() {
		return denyEmoji;
	}
	
}
