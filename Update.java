import java.util.*;
import java.io.*;
import java.lang.Thread;
import javax.swing.JOptionPane;
import java.util.concurrent.TimeUnit;

public class Update implements Runnable {
  private static final long UPDATE_FREQUENCY = 3L;

  private Controller controller;
  private static Replica backupReplica;

  public Update() {
    controller = Controller.getController();
  }

  public static void setBackupReplica(Replica replica) {
    backupReplica = replica;
  }

  public static Replica getBackupReplica() {
    return backupReplica;
  }

  public void run() {
    Timer timer = new Timer();
    TimerTask tt = new TimerTask() {

      @Override
      public void run() {
        for(Replica r : controller.config) {
          while(controller.queue.size() == 0 && controller.config.size() <= controller.minReplicasRunning) {}
          controller.config.add(backupReplica);
          System.out.println("\n\n added backup replicaaaaaa");
          r.setToUpdate(true);
          controller.queue.add(r);
          //synchronized(backupReplica) {
            while(controller.config.contains(backupReplica)) {
              /*try {
                wait();
              }catch(InterruptedException e) {}*/
            }
          //}
          System.out.println("\n\n reached here!");
          //controller.config.remove(backupReplica);
        }
      };

    };
    if(controller.isRunning()) timer.scheduleAtFixedRate(tt, 2000, TimeUnit.MINUTES.toMillis(UPDATE_FREQUENCY));
  }
}
