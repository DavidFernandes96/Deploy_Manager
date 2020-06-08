import java.util.*;
import java.io.*;
import java.lang.Thread;
import javax.swing.JOptionPane;
import java.util.concurrent.TimeUnit;

public class Update implements Runnable {
  private static final long UPDATE_FREQUENCY = 4L;

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
        for(int i=0; i<Replica.getNumberReplicas(); i++) {
          for(Replica r : controller.getConfig()) {
            while(controller.getQueueSize() == 0 && controller.getConfig().size() <= controller.getMinReplicasRunning()) {}
            controller.addToConfig(backupReplica);
            r.setUpdate(true);
            controller.addToQueue(r);
            break;
          }
          while(controller.getConfig().contains(backupReplica)) {}
        }
      };

    };
    if(controller.isRunning()) timer.scheduleAtFixedRate(tt, 2000, TimeUnit.MINUTES.toMillis(UPDATE_FREQUENCY));
  }
}
