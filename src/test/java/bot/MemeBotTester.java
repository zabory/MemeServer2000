package bot;

import static org.junit.Assert.*;

import org.junit.Test;

public class MemeBotTester {

	@Test
	public void test() {
		MemeBot3000 bot = new MemeBot3000("NzM4ODUxMzM2NTY0NzY4ODY4.XyR67Q.pz8-QF-yOdV8LF95qIFNF5E9cQc");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
