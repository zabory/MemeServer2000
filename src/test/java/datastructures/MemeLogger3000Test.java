package datastructures;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import datastructures.MemeLogger3000.level;

public class MemeLogger3000Test {
	
	static MemeLogger3000 logger;
	
	@Rule public TestName name = new TestName();
	
	@Before
	public void before() {
		logger = new MemeLogger3000(name.getMethodName());
		System.out.println("Output of test " + name.getMethodName() + "\n================================================================");
	}
	
	@After
	public void after() {
		System.out.println("================================================================");
		logger.close();
		logger.getOutputFile().delete();
	}

	@Test
	public void createLogFile() {
		assertTrue(logger.getOutputFile().exists());
	}
	
	@Test
	public void multiplePushesAtOnce() throws InterruptedException {
		
		logger.setConsoleOutput(false);
		
		//create threads to write to logger
		PushToLogger one = new PushToLogger(100);
		PushToLogger two = new PushToLogger(50);
		PushToLogger three = new PushToLogger(150);
		
		//start all the threads
		one.start();
		two.start();
		three.start();
		
		//bind and wait for the threads to finish
		one.join();
		two.join();
		three.join();
		
		assertEquals(logger.getMessageCount(), 300);
		
		try {
			Scanner input = new Scanner(logger.getOutputFile());
			
			input.nextLine();
			
			while(input.hasNextLine()) {
				String currentLine = input.nextLine();
				currentLine = currentLine.substring(24);
				currentLine = currentLine.replace("Heres a number: ", "");
				currentLine = currentLine.replace(", and heres another ", "");
				currentLine = currentLine.replace(", Im sorry, a third number? ", "");
				try{
					Long.parseLong(currentLine);
				}catch(NumberFormatException e) {
					assert(false);
				}
			}
			
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
	
	@Test
	public void sendMessagesToFile() {
		
		logger.println(level.INFO, "This is an info message");
		logger.println(level.FATAL, "This is an fatal message");
		logger.println(level.ERROR, "This is an error message");
		logger.println(level.WARNING, "This is an warning message");
		
		try {
			Scanner input = new Scanner(logger.getOutputFile());
			
			String fileInput = "";
			//load inputFile
			while(input.hasNextLine()) {
				fileInput += input.nextLine() + "\n";
			}
			
			input.close();
			
			assertTrue(fileInput.contains("This is an info message"));
			assertTrue(fileInput.contains("This is an fatal message"));
			assertTrue(fileInput.contains("This is an error message"));
			assertTrue(fileInput.contains("This is an warning message"));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	class PushToLogger extends Thread{
		
		Queue<String> msgs;
		
		public PushToLogger(int count) {
			msgs = new LinkedList<String>();
			populate(count);
		}
		
		public void run() {
			while(msgs.size() > 0) {
				logger.println(msgs.remove());
			}
		}
		
		private void populate(int count) {
			for(int i = 0 ; i < count; i++) {
				int rn1 = (int)(Math.random() * 10000);
				int rn2 = (int)(Math.random() * 10000);
				int rn3 = (int)(Math.random() * 10000);
				String msg = "Heres a number: " + rn1 + ", and heres another " + rn2 + ", Im sorry, a third number? " + rn3;
				msgs.add(msg);
			}
		}
	}

}
