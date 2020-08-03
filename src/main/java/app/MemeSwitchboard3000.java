package app;

import bot.MemeBotInterfacer2000;
import datastructures.MemeLogger3000;
import datastructures.MemeBotMsg2000;
import datastructures.MemeDBMsg2000;
import database.MemeDBC2000;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import static datastructures.MemeDBMsg2000.MsgDBType.*;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 *
 * @author Ben Shabowski
 * @author Jacob Marszalek
 * @version 3000
 * @since 2000
 *
 */

@SpringBootApplication
public class MemeSwitchboard3000 {
	private static final Integer qCapacity = 100;

	public static void main(String[] args) throws IOException {
		// parent logger
		MemeLogger3000 logger = new MemeLogger3000();

		//create the Qs
		BlockingQueue<MemeBotMsg2000> botOutputQ = new LinkedBlockingQueue<MemeBotMsg2000>(qCapacity);
		BlockingQueue<MemeBotMsg2000> botInputQ = new LinkedBlockingQueue<MemeBotMsg2000>(qCapacity);
		BlockingQueue<MemeDBMsg2000> dbOutputQ = new LinkedBlockingQueue<MemeDBMsg2000>(qCapacity);
		BlockingQueue<MemeDBMsg2000> dbInputQ = new LinkedBlockingQueue<MemeDBMsg2000>(qCapacity);
		BlockingQueue<Integer> approveQ = new LinkedBlockingQueue<Integer>(qCapacity);

		logger.println("Loading the config...");
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("app");
		context.refresh();
		MemeConfigLoader3000 config = context.getBean(MemeConfigLoader3000.class);
		context.close();

		logger.println("Initializing the DB...");
		MemeDBC2000 dbController = new MemeDBC2000(config, logger, dbInputQ, dbOutputQ);
		MemeDBReader3000 dbReader = new MemeDBReader3000(logger, config, botInputQ, dbOutputQ, dbInputQ, approveQ);
		dbController.start();
		dbReader.start();
		dbInputQ.add(new MemeDBMsg2000().type(INITIALIZE));

		logger.println("Waiting for initialization ACK from DB...");
		BlockingQueue<MemeBotMsg2000> tempQ = new LinkedBlockingQueue<MemeBotMsg2000>(qCapacity);
		try {
			while(true){
				MemeBotMsg2000 msg = botInputQ.take();
				if(msg.getCommand().equals("INIT"))
					break;
				else
					tempQ.put(msg);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.println("The DB has been initialized");

		logger.println("Initializing the bot...");
		botInputQ.add(new MemeBotMsg2000().command("clearQueue"));
		MemeBotInterfacer2000 memeBotInterfacer = new MemeBotInterfacer2000(config, botInputQ, botOutputQ);
		MemeBotReader3000 botReader = new MemeBotReader3000(logger, botOutputQ, dbInputQ, approveQ);
		botReader.start();

		logger.println("We're ready to GO!");
	}
}
