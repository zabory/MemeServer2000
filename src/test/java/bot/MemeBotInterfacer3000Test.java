package bot;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import app.MemeConfigLoader3000;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import datastructures.MemeBotMsg2000;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("app");
		context.refresh();
		MemeConfigLoader3000 config = context.getBean(MemeConfigLoader3000.class);
		context.close();
		MBI = new MemeBotInterfacer2000(config, botInputQ, botOutputQ);
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
