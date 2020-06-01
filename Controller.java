import java.util.*;
import java.io.*;
import java.lang.Thread;
import javax.swing.JOptionPane;

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
  private static Queue<String> queue;

  private Thread t1, t2, t3;
  private boolean moreInfo = true;
  private boolean firstInit = true;

  private Controller() {
    commands = new String[]{"vagrant up --no-provision", "vagrant destroy --force",
    "vagrant status", "vagrant halt ", "vagrant up ", "vagrant reload --provision "};
    config = new LinkedHashSet<>();
    pool = new LinkedHashSet<>();
    queue = new LinkedList<>();
  }

  public static Controller getController() {
    if(controller == null) return controller = new Controller();
    else return controller;
  }

  public boolean isRunning() {
    return running;
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
      File file = new File(".configTemp");
      file.delete();
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
    t3 = new Thread(new Runnable() {
      @Override
      public void run() {
        File file = new File("config.txt");
        File tempFile = new File(".configTemp");

        //a first copy from config file is made into a temp. file
        fileCopy(file, tempFile);
        BufferedReader reader;
        PrintWriter writer;

        String line;
        TimerTask task = new FileWatcher(file) {
          protected void onChange(File file) {
            System.out.println("\n\nA change has been made on the configuration file");
            System.out.print(">>>>");
            init();
            try {
              if(tempFile.createNewFile() || !tempFile.createNewFile()) {
                reader = new BufferedReader(new FileReader(file));
                reader2 = new BufferedReader(new FileReader(tempFile));

                String line = reader.readLine();
                String line2 = reader2.readLine();
                int lineNum = 1;

                while(line != null && line2 != null) {
                  if(!line.equals(line2) && lineNum != 1 && line != null && line2 != null) {
                    String[] data = line2.split(";");
                    if(isRunning()) queue.add(data[1]);
                  }
                  else if(!line.equals(line2) && lineNum != 1 && line != null && line2 == null) {
                    String[] data = line2.split(";");
                    if(isRunning()) pool.add(new Replica(data[1]));
                  }
                  line = reader.readLine();
                  line2 = reader2.readLine();
                  lineNum++;
                }

                reader.close();
                reader2.close();
              }
            }catch(IOException e) {
              System.out.println(e);
            }
            fileCopy(file, tempFile);
          }
        };

        Timer timer = new Timer();
        //repeat the check every second
        timer.schedule(task, new Date(), 1000);

      }
    });
    t1.start();
    t2.start();
    t3.start();
  }

  private void fileCopy(File in, File out) {
    BufferedReader reader;
    PrintWriter writer;
    String line;
    try {
      if(out.createNewFile() || !out.createNewFile()) {
        reader = new BufferedReader(new FileReader(in));
        writer = new PrintWriter(new FileWriter(out));

        while((line = reader.readLine()) != null) {
          writer.println(line);
        }

        reader.close();
        writer.close();
      }
    }catch(IOException e) {
      System.out.println(e);
    }
  }

  private void removeReplica() throws InterruptedException {
    while(!Thread.currentThread().isInterrupted()) {
      synchronized(this) {
        while(config.size() <= MIN_REPLICAS_RUNNING) wait();
      }
      if(queue.size() > 0) {
        String name = queue.remove();
        for(Replica r : config) {
          if(r.getName().equals(name)) {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", commands[3] + name);
            try {
              Process process = processBuilder.start();
              process.waitFor();
              System.out.println("\n\nRemoved one replica (" + name + ")");
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
    }
  }

  private void addReplica() throws InterruptedException {
    while(!Thread.currentThread().isInterrupted()) {
      synchronized(this) {
        while(pool.isEmpty()) wait();
      }
      for(Replica r : pool) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", commands[4] + r.getName());
        try {
          Process process = processBuilder.start();
          process.waitFor();
          System.out.println("\n\n" + r.getName() + " updated!");
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
