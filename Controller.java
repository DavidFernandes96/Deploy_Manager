import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Thread;
import java.io.*;
import java.util.concurrent.ThreadLocalRandom;

public class Controller implements Runnable {
  //private static final int THRESHOLD = ???;

  private static Controller controller = null;
  private String[] commands;
  private boolean running;
  private Set<Replica> config;
  private Set<Replica> pool; //set with the available replicas (not running)
  private Set<Replica> quarantined;

  private Controller() {
    commands = new String[]{"vagrant up", "vagrant destroy --force", "vagrant status", "vagrant suspend ", "vagrant resume "};
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
    config = new HashSet<>();
    for(Replica r : pool) {
      r.setStatus(true);
      config.add(r);
    }
    pool.clear();
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

  public void init() {
    pool = new HashSet<Replica>();
    try {
      FileReader fileRead = new FileReader("config.txt");
      BufferedReader br = new BufferedReader(fileRead);
      String line = "";
      try {
        FileWriter fileWrite = new FileWriter("Vagrantfile");
        BufferedWriter bw = new BufferedWriter(fileWrite);
        bw.write("# -*- mode: ruby -*-");
        bw.newLine();
        bw.write("# vi: set ft=ruby :");
        bw.newLine();
        bw.write("Vagrant.configure(\"2\") do |config|");
        bw.newLine();
        bw.close();
      }catch(IOException e) {
        System.out.println("A write error has occurred");
      }
      int contVM = 1;
      while((line = br.readLine()) != null) {
        if(!line.startsWith("#")) {
          try {
            String[] data = line.split(";");
            FileWriter fileWrite = new FileWriter("Vagrantfile", true);
            BufferedWriter bw = new BufferedWriter(fileWrite);
            bw.write("config.vm.define \"" + data[1] + "\" do |vm" + contVM + "|");
            bw.newLine();
            pool.add(new Replica(data[1]));
            if(data[0].equals("Vagrant-Alpine")) bw.write("vm" + contVM + ".ssh.shell = \"ash\""); //need to install bash on alpine...this part shouldn't be here
            else bw.write("vm" + contVM + ".vm.hostname = \"" + data[1] + "\"");
            bw.newLine();
            bw.write("vm" + contVM++ + ".vm.box = \"" + data[0] + "\"");
            bw.newLine();
            bw.write("end");
            bw.newLine();
            bw.close();
          }catch(IOException e) {
            System.out.println("A write error has occurred");
          }
        }
      }
      try {
        FileWriter fileWrite = new FileWriter("Vagrantfile", true);
        BufferedWriter bw = new BufferedWriter(fileWrite);
        bw.write("end");
        bw.close();
      }catch(IOException e) {
        System.out.println("A write error has occurred");
      }
      br.close();
      }catch(IOException e) {
      System.out.println("A read error has occurred");
      System.out.println("INIT config size: " + config.size());
      System.out.println("INIT pool size: " + pool.size());
    }
  }

  //Monitor function -> still in progress...
  /*public void monitor() {
    //config -> set of replicas executing
    if(risk(config)) >= THRESHOLD {
      for(Replica r : poll) {

      }
    }
  }*/

  @Override
  public void run() {
    while(isRunning()) {
      try {
        Thread.sleep(5000);
      }catch(Exception e) {
         System.out.println(e);
      }
      int enter = ThreadLocalRandom.current().nextInt(1, 2 + 1);
      String nome = "";
      switch(enter) {
        case 1: if(config.size() > 1) {
          int repToRem = ThreadLocalRandom.current().nextInt(1, Replica.getNumberReplicas() + 1);
          for(Replica r : config) {
            if(r.getId() == repToRem) {
              r.setStatus(false);
              nome = r.getName();
              pool.add(r);
              config.remove(r);
              removeReplica(r.getName());
              System.out.println("\n\nRemoved one replica (" + nome + ")");
              System.out.println("config size: " + config.size());
              System.out.println("pool size: " + pool.size());
              System.out.print(">>>>");
              break;
            }
          }
        }break;
        case 2: if(pool.size() > 0) {
          int repToRem = ThreadLocalRandom.current().nextInt(1, Replica.getNumberReplicas() + 1);
          for(Replica r : pool) {
            if(r.getId() == repToRem) {
              r.setStatus(false);
              nome = r.getName();
              config.add(r);
              pool.remove(r);
              addReplica(r.getName());
              System.out.println("\n\nAdded one replica (" + nome + ")");
              System.out.println("config size: " + config.size());
              System.out.println("pool size: " + pool.size());
              System.out.print(">>>>");
              break;
            }
          }
        }break;
      }
    }
  }

  public void removeReplica(String rep) {
    ProcessBuilder processBuilder = new ProcessBuilder();
    processBuilder.command("bash", "-c", commands[3] + rep);
    try {
      Process process = processBuilder.start();
    }catch(IOException e) {
      e.printStackTrace();
    }
  }

  public void addReplica(String rep) {
    ProcessBuilder processBuilder = new ProcessBuilder();
    processBuilder.command("bash", "-c", commands[4] + rep);
    try {
      Process process = processBuilder.start();
    }catch(IOException e) {
      e.printStackTrace();
    }
  }
}
