import java.util.Scanner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Thread;

public class Controller {
  private static Controller controller = null;
  private String[] commands;
  private boolean running;

  private Controller() {
    commands = new String[]{"vagrant up", "vagrant destroy --force", "vagrant status"};
  }

  public static Controller getController() {
    if(controller == null) return controller = new Controller();
    else return controller;
  }

  public boolean isRunning() {
    return running;
  }

  public void start() {
    if(isRunning()) {
      System.out.println("System already running.");
      try {
		      Thread.sleep(3000);
	    }catch(Exception e) {
		      System.out.println(e);
      }
      return;
    }
    running = true;
    System.out.println("Starting....");
    ProcessBuilder processBuilder = new ProcessBuilder();
    processBuilder.command("bash", "-c", commands[0]);
    try {
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while((line = reader.readLine()) != null) {
          System.out.println(line);
      }

      int exitCode = process.waitFor();
      System.out.println("\nExited with error code : " + exitCode);
    }catch(IOException e) {
      e.printStackTrace();
    }catch(InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void stop() {
    if(!isRunning()) {
      System.out.println("System is not running.");
      try {
		      Thread.sleep(3000);
	    }catch(Exception e) {
		      System.out.println(e);
      }
      return;
    }
    running = false;
    ProcessBuilder processBuilder = new ProcessBuilder();
    processBuilder.command("bash", "-c", commands[1]);
    try {
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while((line = reader.readLine()) != null) {
          System.out.println(line);
      }

      int exitCode = process.waitFor();
      System.out.println("\nExited with error code : " + exitCode);
    }catch(IOException e) {
      e.printStackTrace();
    }catch(InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void status(Scanner stdin) {
    ProcessBuilder processBuilder = new ProcessBuilder();
    processBuilder.command("bash", "-c", commands[2]);
    try {
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while((line = reader.readLine()) != null) {
          System.out.println(line);
      }

      int exitCode = process.waitFor();
      System.out.println("\nExited with error code : " + exitCode);
    }catch(IOException e) {
      e.printStackTrace();
    }catch(InterruptedException e) {
      e.printStackTrace();
    }
    String input;
    do {
      System.out.print("Continue? (y): ");
      input = stdin.next();
    }while(!input.equals("y"));
  }
}
