package bot;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import datastructures.MemeBotMsg2000;

public class MemeBotInterfacer3000Test {
	
	static MemeBotInterfacer2000 MBI;
	static BlockingQueue<MemeBotMsg2000> botOutputQ;
	static BlockingQueue<MemeBotMsg2000> botInputQ;
	
	@Rule public TestName name = new TestName();
	
	@Before
	public void before() {
		System.out.println(name.getMethodName() + " test output\n=========================================================================");
		botOutputQ = new LinkedBlockingQueue<MemeBotMsg2000>(100);
		botInputQ = new LinkedBlockingQueue<MemeBotMsg2000>(100);
		MBI = new MemeBotInterfacer2000(botInputQ, botOutputQ);
	}
	
	@After
	public void after() {
		System.out.println("=========================================================================");
	}

	@Test
	public void test() {
		System.out.println("This is a test to make JJ happy");
		assert(true);
	}
}
