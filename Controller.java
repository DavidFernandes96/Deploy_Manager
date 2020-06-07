import java.util.*;
import java.io.*;
import java.lang.Thread;
import javax.swing.JOptionPane;

public class Controller implements Runnable {

  //specify the minimum number of replicas that should be running no matter what
  private static int minReplicasRunning;
  private static Controller controller = null;
  private static String[] commands;
  private static boolean running;
  private static Set<Replica> config; //set with running replicas
  private static Set<Replica> pool; //set with the available replicas (not running)
  private static Queue<Replica> queue;

  private Thread t1, t2;
  private boolean moreInfo = true;
  private boolean firstInit = true;

  private Controller() {
    commands = new String[]{"vagrant up --no-provision", "vagrant destroy --force",
    "vagrant status", "vagrant halt ", "vagrant up ", "vagrant reload --provision "};
    config = new LinkedHashSet<>();
    pool = new LinkedHashSet<>();
    queue = new LinkedList<>();
  }

  public static int getMinReplicasRunning() {
    return minReplicasRunning;
  }

  public static Controller getController() {
    if(controller == null) return controller = new Controller();
    else return controller;
  }

  public boolean isRunning() {
    return running;
  }

  public static Set<Replica> getConfig() {
    return config;
  }

  public static void addToConfig(Replica replica) {
    config.add(replica);
  }

  public static void addToPool(Replica replica) {
    pool.add(replica);
  }

  public static void addToQueue(Replica replica) {
    queue.add(replica);
  }

  public static int getQueueSize() {
    return queue.size();
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
            if(firstInit) pool.add(new Replica(data[1]));
            bw.write("vm" + contVM + ".vm.hostname = \"" + data[1] + "\"");
            bw.newLine();
            bw.write("vm" + contVM + ".vm.box = \"" + data[0] + "\"");
            bw.newLine();
            bw.write("vm" + contVM + ".vm.network \"private_network\", ip: \"" + data[2] + "\"");
            bw.newLine();
            if(data.length > 3) {
              bw.write("vm" + contVM + ".vm.provision \"shell\", inline: <<-SHELL");
              bw.newLine();
              for(int i=3; i<data.length; i++) {
                bw.write(data[i]);
                bw.newLine();
              }
              bw.write("SHELL");
              bw.newLine();
            }
            bw.write("end");
            bw.newLine();
            bw.close();
            contVM++;
          }catch(IOException e) {
            System.out.println("A write error has occurred");
          }
        }
      }
      try {
        //grab the backup replica
        if(firstInit) {
          for(Replica r : pool) {
            pool.remove(r);
            Update.setBackupReplica(r);
            break;
          }
        }
        minReplicasRunning = (Replica.getNumberReplicas() - ((Replica.getNumberReplicas() - 1) / 3)) != Replica.getNumberReplicas() ?
          (Replica.getNumberReplicas() - ((Replica.getNumberReplicas() - 1) / 3)) : Replica.getNumberReplicas() - 1;
        FileWriter fileWrite = new FileWriter("Vagrantfile", true);
        BufferedWriter bw = new BufferedWriter(fileWrite);
        bw.write("end");
        bw.close();
        firstInit = false;
      }catch(IOException e) {
        System.out.println("A write error has occurred");
      }
      br.close();
    }catch(IOException e) {
      System.out.println("A read error has occurred");
    }
  }

  public void start() {
    if(isRunning()) {
      Thread t = new Thread(() -> {
        JOptionPane.showMessageDialog(null, "System already running.");
      });
      t.start();
      return;
    }
    System.out.println("\n\nStarting....\n");
    ProcessBuilder processBuilder = new ProcessBuilder();
    processBuilder.command("bash", "-c", commands[4]);
    try {
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      if(moreInfo) {
        String line;
        while((line = reader.readLine()) != null) System.out.println(line);
      }
      for(Replica r : pool) {
        r.setStatus(true);
        r.setNew(false);
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
      Thread t = new Thread(() -> {
        JOptionPane.showMessageDialog(null, "System is not running.");
      });
      t.start();
      return;
    }
    System.out.println("\n\nTurning off the system...");
    running = false;
    System.out.println("\n\nWaiting for threads to finish...stand by...\n\n");
    t1.interrupt(); t2.interrupt();
    try {
      t1.join(5000);
      t2.join(5000);
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
      File file = new File(".configTemp"); file.delete();
      firstInit = true;
      pool.clear(); config.clear(); queue.clear();
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
    System.out.println("\nPress enter to continue...");
    try {
      System.in.read();
    }catch(Exception e) {}
  }

  public void statusV2(Scanner stdin) {
    System.out.println("\n\nReplicas running: ");
    for(Replica r : config) System.out.println("* " + r.getName());
    System.out.println("\n\nReplicas on the pool: ");
    for(Replica r : pool) System.out.println("* " + r.getName());
    System.out.println("\nPress enter to continue...");
    try {
      System.in.read();
    }catch(Exception e) {}
  }


  private boolean hasMinimum() {
    int cont = 0;
    for(Replica r : config) {
      if(r.getStatus()) cont++;
      if(cont > minReplicasRunning) return true;
    }
    return false;
  }

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
          System.out.println("1st Thread interrupted...");
        }
      }
    });
    t2 = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          addReplica();
        }catch(InterruptedException e) {
          System.out.println("2nd Thread interrupted...");
        }
      }
    });
    t1.start();
    t2.start();
  }

  private void removeReplica() throws InterruptedException {
    while(!Thread.currentThread().isInterrupted()) {
      if(queue.size() > 0 && config.size() > minReplicasRunning) {
        Replica replica = queue.remove();
        for(Replica r : config) {
          if(r == replica) {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if(r.toDestroy()) {
              processBuilder.command("bash", "-c", commands[1] + " " + r.getName());
            }
            else processBuilder.command("bash", "-c", commands[3] + r.getName());
            try {
              Process process = processBuilder.start();
              process.waitFor();
              if(r.toDestroy()) {
                System.out.println("\n\n" + r.getName() + " was destroyed.");
                r.destroyReplica(r.getName());
              }
              else {
                System.out.println("\n\nRemoved one replica (" + r.getName() + ")");
                r.setStatus(false);
                pool.add(r);
                config.remove(r);
              }
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
  }

  private void addReplica() throws InterruptedException {
    while(!Thread.currentThread().isInterrupted()) {
      if(!pool.isEmpty()) {
        for(Replica r : pool) {
          ProcessBuilder processBuilder = new ProcessBuilder();
          processBuilder.command("bash", "-c", commands[4] + r.getName());
          try {
            Process process = processBuilder.start();
            process.waitFor();
            if(r.toUpdate()) {
              config.remove(Update.getBackupReplica());
              r.setToUpdate(false);
            }
            if(r.isNew()) {
              System.out.println("\n\n" + r.getName() + " was added to the configuration!");
              r.setNew(false);
            }
            else System.out.println("\n\n" + r.getName() + " updated!");
            r.setStatus(true);
            config.add(r);
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
}
