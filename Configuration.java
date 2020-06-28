
/**
 * Deploy Manager
 *
 * @author David Fernandes
 *
 * As soon as a change is detected on the config.txt file
 * it acts accordingly
 */

import java.util.*;
import java.io.*;
import java.lang.Thread;
import javax.swing.JOptionPane;
import java.util.concurrent.TimeUnit;

public class Configuration implements Runnable {
	private Controller controller;
	private static Timer timer;

	public Configuration() {
		controller = Controller.getController();
		timer = null;
	}

	public static void shutdown() {
	        timer.cancel();
	}

	public void run() {
		File file = new File("config.txt");
		File tempFile = new File(".configTemp");

		/*
		 * a first copy from config file is made into a temp. file
		 */
		fileCopy(file, tempFile);
		BufferedReader reader;
		PrintWriter writer;

		String line;
		TimerTask task = new FileWatcher(file) {
			protected void onChange(File file) {
				System.out.println("\n\nA change has been made on the configuration file");
				System.out.print(">>>>");
				try {
					if (tempFile.createNewFile() || !tempFile.createNewFile()) {
						reader = new BufferedReader(new FileReader(file));
						reader2 = new BufferedReader(new FileReader(tempFile));

						String line = reader.readLine();
						String line2 = reader2.readLine();
						int lineCount = 0;

						while (line != null || line2 != null) {
							if (line != null && line2 != null && !line.equals(line2) && !line.startsWith("#")
									&& !line2.startsWith("#")) {
								lineCount++;
								String[] dataFile = line.split(";");
								String[] dataTemp = line2.split(";");
								if (dataFile[0].equals(dataTemp[0])) {
									if (controller.isRunning()) {
										/*
										*	change on backupReplica
										*/
										if(Replica.getReplica(dataFile[1]).getName().equals(Update.getBackupReplica().getName())) {
											controller.init();
											ProcessBuilder processBuilder = new ProcessBuilder();
											processBuilder.command("bash", "-c",
													controller.commands[5] + Replica.getReplica(dataTemp[1]).getName());
											Process process = processBuilder.start();
											try {
												process.waitFor();
												System.out.println("\n\nupdate on backup replica");
												System.out.print(">>>>");
											} catch (InterruptedException e) {}
										}
										/*
										*	other replicas changes
										*/
										else {
											while(!controller.getConfig().contains(Replica.getReplica(dataFile[1]))) {}
											controller.addToQueue(Replica.getReplica(dataFile[1]));
										}
									}

								}
								/*
								 * replica removal
								 */
								else {
									if (controller.isRunning()) {
										String name = Replica.getReplica(dataTemp[1]).getName();
										while(!controller.getConfig().contains(Replica.getReplica(name))) {}
										ProcessBuilder processBuilder = new ProcessBuilder();
										processBuilder.command("bash", "-c", controller.commands[1] + " "
												+ Replica.getReplica(dataTemp[1]).getName());
										Process process = processBuilder.start();
										try {
											process.waitFor();
											controller.getConfig().remove(Replica.getReplica(name));
											Replica.getReplica(name).destroyReplica(name);
											System.out.println("\n\n" + name + " was destroyed.");
											System.out.print(">>>>");
											break;
										} catch (InterruptedException e) {}
									}
								}
							}
							/*
							 * replica addition
							 */
							else if (line != null && line2 == null && !line.startsWith("#")) {
								lineCount++;
								String[] dataFile = line.split(";");
								if (controller.isRunning())
									controller.addToPool(new Replica(dataFile[1]));
							}
							/*
							 * replica removal (last line)
							 */
							else if (line == null && line2 != null && !line2.startsWith("#")) {
								lineCount++;
								String[] dataTemp = line2.split(";");
								if (controller.isRunning()) {
									String name = Replica.getReplica(dataTemp[1]).getName();
									while(!controller.getConfig().contains(Replica.getReplica(name))) {}
									ProcessBuilder processBuilder = new ProcessBuilder();
									processBuilder.command("bash", "-c",
											controller.commands[1] + " " + Replica.getReplica(dataTemp[1]).getName());
									Process process = processBuilder.start();
									try {
										process.waitFor();
										controller.getConfig().remove(Replica.getReplica(name));
										Replica.getReplica(name).destroyReplica(name);
										System.out.println("\n\n" + name + " was destroyed.");
										System.out.print(">>>>");
									} catch (InterruptedException e) {}
								}
							}
							line = reader.readLine();
							line2 = reader2.readLine();
						}

						reader.close();
						reader2.close();
					}
					if(controller.init() == -1) {
						System.out.println();
						JOptionPane.showMessageDialog(null, "Not enough replicas...add more on the configuration file");
						controller.stop();
						System.exit(-1);
					}
				} catch (IOException e) {
					System.out.println(e);
				}
				fileCopy(file, tempFile);
			}
		};

		timer = new Timer();
		/*
		 * repeat the check every 2 seconds
		 */
		timer.schedule(task, new Date(), 2000);
	}

	private void fileCopy(File in, File out) {
		BufferedReader reader;
		PrintWriter writer;
		String line;
		try {
			if (out.createNewFile() || !out.createNewFile()) {
				reader = new BufferedReader(new FileReader(in));
				writer = new PrintWriter(new FileWriter(out));

				while ((line = reader.readLine()) != null) {
					writer.println(line);
				}

				reader.close();
				writer.close();
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
