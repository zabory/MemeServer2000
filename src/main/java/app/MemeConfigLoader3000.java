package app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:MemeBot.properties")
@PropertySource("classpath:MemeBotTest.properties")
public class MemeConfigLoader3000 {

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

	@Value("${database}")
	private String databaseLocation;

	@Value("${memeTableName}")
	private String memeTableName;

	@Value("${cacheTableName}")
	private String cacheTableName;

	@Value("${tagLkpTableName}")
	private String tagLkpTableName;

	@Value("${memeTableDef}")
	private String memeTableDef;

	@Value("${cacheTableDef}")
	private String cacheTableDef;

	@Value("${tagLkpTableDef}")
	private String tagLkpTableDef;

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

	public String getDatabaseLocation() {
		return databaseLocation;
	}

	public String getMemeTableName() {
		return memeTableName;
	}

	public String getCacheTableName() {
		return cacheTableName;
	}

	public String getTagLkpTableName() {
		return tagLkpTableName;
	}

	public String getMemeTableDef() {
		return memeTableDef;
	}

	public String getCacheTableDef() {
		return cacheTableDef;
	}

	public String getTagLkpTableDef() {
		return tagLkpTableDef;
	}
}
