package app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
//@PropertySource("classpath:MemeBot.properties")
@PropertySource("classpath:MemeBotTest.properties")
public class MemeConfigLoader3000 {

	@Value("${auth.token}")
	private String botToken;
	
	@Value("${channel}")
	private String approvalChannel;

	@Value("${database}")
	private String databaseLocation;

	@Value("${memeTableName}")
	private String memeTableName;

	@Value("${cacheTableName}")
	private String cacheTableName;

	@Value("${tagLkpTableName}")
	private String tagLkpTableName;

	@Value("${userLkpTableName}")
	private String userLkpTableName;

	@Value("${memeTableDef}")
	private String memeTableDef;

	@Value("${cacheTableDef}")
	private String cacheTableDef;

	@Value("${tagLkpTableDef}")
	private String tagLkpTableDef;

	@Value("${userLkpTableDef}")
	private String userLkpTableDef;

	@Value("${time}")
	private String time;

	public String getBotToken() {
		return botToken;
	}

	public String getApprovalChannel() {
		return approvalChannel;
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

	public String getUserLkpTableName() {
		return userLkpTableName;
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

	public String getUserLkpTableDef() {
		return userLkpTableDef;
	}

	public String getTime() {
		return time;
	}
}
