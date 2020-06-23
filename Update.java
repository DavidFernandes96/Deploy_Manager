
/**
 * Deploy Manager
 *
 * @author David Fernandes
 *
 * Updates all replicas running. While one replica
 * is updating it replaces it with a backup one.
 */

import java.util.*;
import java.io.*;
import java.lang.Thread;
import java.util.concurrent.TimeUnit;

public class Update implements Runnable {
	private static final long UPDATE_FREQUENCY = 4L;

	private static volatile boolean done;
	private Controller controller;
	private static Replica backupReplica;

	public Update() {
		controller = Controller.getController();
		done = false;
	}

	public static void shutdown() {
    done = true;
  }

	public static Replica getBackupReplica() {
		return backupReplica;
	}

	public static void setBackupReplica(Replica replica) {
		backupReplica = replica;
	}

	private Timer timer = new Timer();

	public void run() {
		TimerTask tt = new TimerTask() {

			@Override
			public void run() {
				if(!controller.isRunning()) {
					System.out.println("\n\nentrou aqui");
					quit();
					Thread.currentThread().interrupt();
				}
				try {
					System.out.println("\n\nUpdate about to begin...");
					System.out.print(">>>>");
					Object[] arrayList = controller.getConfig().toArray();
					Replica[] arrayReplica = new Replica[arrayList.length];
					for (int i = 0; i<arrayList.length; i++) arrayReplica[i] = (Replica)arrayList[i];
					for (int i = 0; i < arrayReplica.length; i++) {
						if(controller.getConfig().contains(arrayReplica[i])) {
							controller.addToConfig(backupReplica);
							Replica replica = arrayReplica[i];
							replica.setUpdate(true);
							controller.addToQueue(replica);
							while (controller.getConfig().contains(backupReplica)) {}
						}
					}
					System.out.println("\n\nEnd of Update.");
				}catch(Exception e) {
					System.out.println(e);
				}
			};

		};

		timer.scheduleAtFixedRate(tt, 2000, TimeUnit.MINUTES.toMillis(UPDATE_FREQUENCY));

	}

	private void quit(){
		timer.cancel();
	}
}
