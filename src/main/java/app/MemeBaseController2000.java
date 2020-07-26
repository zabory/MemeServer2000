package app;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * Performs actions on the DB based off of instruction in the inputQ and puts the output into the outputQ
 */
public class MemeBaseController2000 {
    private MemeBase2000 db;
    private BlockingQueue outputQ;
    private BlockingQueue inputQ;

    MemeBaseController2000(String filePath){
        db = new MemeBase2000(filePath);
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
