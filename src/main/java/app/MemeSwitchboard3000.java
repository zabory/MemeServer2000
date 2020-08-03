package app;

import bot.MemeBotInterfacer3000;
import datastructures.MemeLogger3000;
import datastructures.MemeBotMsg3000;
import datastructures.MemeDBMsg3000;
import database.MemeDBC3000;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import static datastructures.MemeDBMsg3000.MsgDBType.*;

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
	private static final Integer qCapacity = 1000;

	public static void main(String[] args) throws IOException {
		// parent logger
		MemeLogger3000 logger = new MemeLogger3000();

		//create the Qs
		BlockingQueue<MemeBotMsg3000> botOutputQ = new LinkedBlockingQueue<MemeBotMsg3000>(qCapacity);
		BlockingQueue<MemeBotMsg3000> botInputQ = new LinkedBlockingQueue<MemeBotMsg3000>(qCapacity);
		botInputQ.add(new MemeBotMsg3000().command("clearQueue"));
		BlockingQueue<MemeDBMsg3000> dbOutputQ = new LinkedBlockingQueue<MemeDBMsg3000>(qCapacity);
		BlockingQueue<MemeDBMsg3000> dbInputQ = new LinkedBlockingQueue<MemeDBMsg3000>(qCapacity);
		BlockingQueue<Integer> approveQ = new LinkedBlockingQueue<Integer>(qCapacity);

		logger.println("Loading the config...");
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("app");
		context.refresh();
		MemeConfigLoader3000 config = context.getBean(MemeConfigLoader3000.class);
		context.close();

		logger.println("Initializing the DB...");
		MemeDBC3000 dbController = new MemeDBC3000(config, logger, dbInputQ, dbOutputQ);
		MemeDBReader3000 dbReader = new MemeDBReader3000(logger, config, botInputQ, dbOutputQ, dbInputQ, approveQ);
		dbController.start();
		dbReader.start();
		dbInputQ.add(new MemeDBMsg3000().type(INITIALIZE));

		logger.println("Waiting for initialization ACK from DB...");
		BlockingQueue<MemeBotMsg3000> tempQ = new LinkedBlockingQueue<MemeBotMsg3000>(qCapacity);
		try {
			while(true){
				MemeBotMsg3000 msg = botInputQ.take();
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
		MemeBotInterfacer3000 memeBotInterfacer = new MemeBotInterfacer3000(config, botInputQ, botOutputQ);
		MemeBotReader3000 botReader = new MemeBotReader3000(logger, botOutputQ, dbInputQ, approveQ);
		botReader.start();
		logger.println("The bot has been initialized");

		logger.println("We're ready to GO!");
	}
}
