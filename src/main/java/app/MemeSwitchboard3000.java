package app;

import bot.MemeBotInterfacer2000;
import datastructures.MemeLogger3000;
import datastructures.MemeBotMsg2000;
import datastructures.MemeDBMsg2000;
import db.MemeDBC2000;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import static datastructures.MemeDBMsg2000.MsgDBType.*;

/**
 * 
 * @author Ben Shabowski
 * @author Jacob Marszalek
 * @version 3000
 * @since 2000
 * 
 */
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

		// construct the bot controller and output reader
		MemeBotInterfacer2000 memeBotInterfacer = new MemeBotInterfacer2000(botInputQ, botOutputQ);
		MemeBotReader3000 botReader = new MemeBotReader3000(logger, botOutputQ, dbInputQ);

		// construct to DB controller and output reader
		MemeDBC2000 dbController = new MemeDBC2000("C:\\MemeDBFolder2000\\", dbInputQ, dbOutputQ);
		MemeDBReader3000 dbReader = new MemeDBReader3000(logger, botInputQ, dbOutputQ, dbInputQ);

		// spin up the threads running the controllers and readers
		dbController.start();
		dbReader.start();
		botReader.start();

        // initialize the DB and bot
		botInputQ.add(new MemeBotMsg2000().command("clearQueue"));
		dbInputQ.add(new MemeDBMsg2000().type(INITIALIZE));

		logger.println("Controllers started, meme queue cleared. We're ready to GO!");
	}
}
