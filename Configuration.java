import java.util.*;
import java.io.*;
import java.lang.Thread;
import javax.swing.JOptionPane;
import java.util.concurrent.TimeUnit;

public class Configuration implements Runnable {
  private Controller controller;

  public Configuration() {
    controller = Controller.getController();
  }

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
        try {
          if(tempFile.createNewFile() || !tempFile.createNewFile()) {
            reader = new BufferedReader(new FileReader(file));
            reader2 = new BufferedReader(new FileReader(tempFile));

            String line = reader.readLine();
            String line2 = reader2.readLine();

            while(line != null || line2 != null) {
              if(line != null && line2 != null &&
                !line.equals(line2) && !line.startsWith("#") && !line2.startsWith("#")) {
                String[] dataFile = line.split(";");
                String[] dataTemp = line2.split(";");
                if(dataFile[0].equals(dataTemp[0])) {
                  if(controller.isRunning()) controller.addToQueue(Replica.getReplica(dataFile[1]));
                }
                //replica removal
                else {
                  if(controller.isRunning()) {
                    ProcessBuilder processBuilder = new ProcessBuilder();
                    processBuilder.command("bash", "-c", controller.commands[1] + " " + Replica.getReplica(dataTemp[1]).getName());
                    Process process = processBuilder.start();
                    try {
                      process.waitFor();
                      String name = Replica.getReplica(dataTemp[1]).getName();
                      controller.getConfig().remove(Replica.getReplica(name));
                      Replica.getReplica(name).destroyReplica(name);
                      System.out.println("\n\n" + name + " was destroyed.");
                      System.out.print(">>>>");
                      break;
                    }catch(InterruptedException e) {}
                  }
                }
              }
              //replica addition
              else if(line != null && line2 == null && !line.startsWith("#")) {
                String[] dataFile = line.split(";");
                if(controller.isRunning()) controller.addToPool(new Replica(dataFile[1]));
              }
              //replica removal
              else if(line == null && line2 != null && !line2.startsWith("#")) {
                String[] dataTemp = line2.split(";");
                if(controller.isRunning()) {
                  ProcessBuilder processBuilder = new ProcessBuilder();
                  processBuilder.command("bash", "-c", controller.commands[1] + " " + Replica.getReplica(dataTemp[1]).getName());
                  Process process = processBuilder.start();
                  try {
                    process.waitFor();
                    String name = Replica.getReplica(dataTemp[1]).getName();
                    controller.getConfig().remove(Replica.getReplica(name));
                    Replica.getReplica(name).destroyReplica(name);
                    System.out.println("\n\n" + name + " was destroyed.");
                    System.out.print(">>>>");
                  }catch(InterruptedException e) {}
                }
              }
              line = reader.readLine();
              line2 = reader2.readLine();
            }

            reader.close();
            reader2.close();
          }
          controller.init();
        }catch(IOException e) {
          System.out.println(e);
        }
        fileCopy(file, tempFile);
      }
    };

    Timer timer = new Timer();
    //repeat the check every 2 seconds
    timer.schedule(task, new Date(), 2000);
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
}
