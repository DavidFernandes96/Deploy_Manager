import java.util.*;
import java.io.*;
import java.lang.Thread;

public class Controller implements Runnable {
  //private static final int THRESHOLD = ???;
  //specify the minimum number of replicas that should be running no matter what
  private static final int MIN_REPLICAS_RUNNING = 1;

  private static Controller controller = null;
  private static String[] commands;
  private static boolean running;
  private static Set<Replica> config; //set with running replicas
  private static Set<Replica> pool; //set with the available replicas (not running)
  private static Set<Replica> quarantined;

  private Thread t1, t2;
  private boolean moreInfo = true;

  private Controller() {
    commands = new String[]{"vagrant up --no-provision", "vagrant destroy --force",
    "vagrant status", "vagrant halt ", "vagrant up ", "vagrant reload --provision "};
    config = new LinkedHashSet<>();
    pool = new LinkedHashSet<>();
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
		      Thread.sleep(3000); //small pause to keep message on console before menu() clears screen
	    }catch(Exception e) {
		      System.out.println(e);
      }
      return;
    }
    System.out.println("\n\nStarting....\n");
    ProcessBuilder processBuilder = new ProcessBuilder();
    processBuilder.command("bash", "-c", commands[0]);
    try {
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      if(moreInfo) {
        String line;
        while((line = reader.readLine()) != null) System.out.println(line);
      }
      for(Replica r : pool) {
        r.setStatus(true);
        config.add(r);
      }
      pool.clear();
      running = true;
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
    System.out.println("\n\nTurning off the system...");
    running = false;
    System.out.println("Waiting for threads to finish...stand by...");
    try {
      t1.join();
      t2.join();
    }catch(InterruptedException e) {
      e.printStackTrace();
    }
    ProcessBuilder processBuilder = new ProcessBuilder();
    processBuilder.command("bash", "-c", commands[1]);
    try {
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while((line = reader.readLine()) != null) System.out.println(line);
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
      while((line = reader.readLine()) != null) System.out.println(line);
      int exitCode = process.waitFor();
      System.out.println("\nExited with error code : " + exitCode);
    }catch(IOException e) {
      e.printStackTrace();
    }catch(InterruptedException e) {
      e.printStackTrace();
    }
    String input;
    do {
      System.out.print("Continue? (Y/y): ");
      input = stdin.next();
    }while(!input.equals("y") && !input.equals("Y"));
  }

  public void init() {
    try {
      FileReader fileRead = new FileReader("config.txt");
      BufferedReader br = new BufferedReader(fileRead);
      String line = "";
      try {
        FileWriter fileWrite = new FileWriter("Vagrantfile");
        BufferedWriter bw = new BufferedWriter(fileWrite);
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
            bw.write("vm" + contVM + ".vm.hostname = \"" + data[1] + "\"");
            bw.newLine();
            bw.write("vm" + contVM + ".vm.box = \"" + data[0] + "\"");
            bw.newLine();
            bw.write("vm" + contVM + ".vm.network \"private_network\", ip: \"" + data[2] + "\"");
            bw.newLine();
            bw.write("vm" + contVM++ + ".vm.provision \"shell\", inline: <<-SHELL");
            bw.newLine();
            for(int i=3; i<data.length; i++) {
              bw.write(data[i]);
              bw.newLine();
            }
            bw.write("SHELL");
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
      }
  }

  private boolean hasMinimum() {
    int cont = 0;
    for(Replica r : config) {
      if(r.getStatus()) cont++;
      if(cont > MIN_REPLICAS_RUNNING) return true;
    }
    return false;
  }

  //Monitor function -> still in progress...
  /*public void monitor() {
    //config -> set of replicas executing
    if(risk(config)) >= THRESHOLD {
      for(Replica r : poll) {

      }
    }
  }*/

  public void run() {
    System.out.println("\n\nSTART config size: " + config.size());
    System.out.println("START pool size: " + pool.size());
    System.out.print(">>>>");
    t1 = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          removeReplica();
        }catch(InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    t2 = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          addReplica();
        }catch(InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    t1.start();
    t2.start();
  }

  private void removeReplica() throws InterruptedException {
    while(isRunning()) {
      for(Replica r : config) {
        synchronized(this) {
          while(config.size() <= MIN_REPLICAS_RUNNING) wait();
        }
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", commands[3] + r.getName());
        try {
          Process process = processBuilder.start();
          process.waitFor();
          System.out.println("\n\nRemoved one replica (" + r.getName() + ")");
          r.setStatus(false);
          pool.add(r);
          synchronized(this) {
            notify();
          }
          config.remove(r);
          System.out.println("\nconfig size: " + config.size());
          System.out.println("pool size: " + pool.size());
          System.out.print(">>>>");
          break;
        }catch(IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void addReplica() throws InterruptedException {
    while(isRunning()) {
      synchronized(this) {
        while(pool.isEmpty()) wait();
      }
      for(Replica r : pool) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", commands[4] + r.getName());
        try {
          Process process = processBuilder.start();
          process.waitFor();
          System.out.println("\n\nAdded one replica (" + r.getName() + ")");
          r.setStatus(true);
          config.add(r);
          synchronized(this) {
            if(config.size() > MIN_REPLICAS_RUNNING) notify();
          }
          pool.remove(r);
          System.out.println("\nconfig size: " + config.size());
          System.out.println("pool size: " + pool.size());
          System.out.print(">>>>");
          break;
        }catch(IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
