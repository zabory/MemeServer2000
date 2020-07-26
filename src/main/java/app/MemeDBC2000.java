package app;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * Performs actions on the DB based off of instruction in the inputQ and puts the output into the outputQ
 */
public class MemeDBC2000 {
    private MemeDB2000 db;
    private BlockingQueue outputQ;
    private BlockingQueue inputQ;

    MemeDBC2000(String filePath){
        db = new MemeDB2000(filePath);
        outputQ = new LinkedBlockingQueue();
        inputQ = new LinkedBlockingQueue();
    }

    public BlockingQueue getInputQ() {
        return inputQ;
    }

    public BlockingQueue getOutputQ() {
        return outputQ;
    }

    public void spin(){
        if(!inputQ.isEmpty()){

        }
    }

}
