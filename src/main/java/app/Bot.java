package app;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Bot {
	
	private BufferedReader input;
	private BufferedReader error;
	private BufferedWriter output;
	
	public Bot() throws IOException {
		
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("cmd.exe", "/c", "node MemeBot2000");
		
		Process botProcess = pb.start();
		
		input =  new BufferedReader(new InputStreamReader(botProcess.getInputStream()));
		error = new BufferedReader(new InputStreamReader(botProcess.getErrorStream()));
		output = new BufferedWriter(new OutputStreamWriter(botProcess.getOutputStream()));
	}

	public BufferedReader getInput() {
		return input;
	}

	public BufferedReader getError() {
		return error;
	}

	public BufferedWriter getOutput() {
		return output;
	}

}
