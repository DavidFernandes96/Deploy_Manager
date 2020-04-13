import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Thread;

public class Auto {
    public static void main(String[] args) {
	ProcessBuilder processBuilder = new ProcessBuilder();
	String[] out = {"vagrant up", "vagrant destroy --force"};
	for(int i=0; i<2; i++) {
	    processBuilder.command("bash", "-c", out[i]);

	    try {

		Process process = processBuilder.start();

		BufferedReader reader =
		    new BufferedReader(new InputStreamReader(process.getInputStream()));

		String line;
		while ((line = reader.readLine()) != null) {
		    System.out.println(line);
		}

		int exitCode = process.waitFor();
		System.out.println("\nExited with error code : " + exitCode);

	    }catch (IOException e) {
		e.printStackTrace();
	    }catch (InterruptedException e) {
		e.printStackTrace();
	    }
	    try {
		Thread.sleep(20000);
	    }catch(Exception e) {
		System.out.println(e);
	    }
	}
    }
}
