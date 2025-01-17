
/**
 * Deploy Manager
 *
 * @author David Fernandes
 *
 * Autonomous system which removes and adds virtual machines
 * given a configuration file.
 */

import java.util.Scanner;
import java.io.*;
import javax.swing.JOptionPane;

public class Main {

	private static void menu() {
		clearScreen();
		System.out.println("***Deploy Manager***\n");
		System.out.println("1) Start");
		System.out.println("2) System status");
		System.out.println("3) Stop");
		System.out.println("4) Exit");
		System.out.print(">>>>");
	}

	private static void clearScreen() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}

	public static void main(String[] args) {
		Scanner stdin = new Scanner(System.in);
		int opt;

		File f = new File("config.txt");
		if(!f.exists()) {
			System.out.println();
			JOptionPane.showMessageDialog(null, "Configuration file does not exist. Create one. If you need help checkout the README file.");
			System.exit(-1);
		}

		Controller controller;
		menu();
		controller = Controller.getController();
		if(controller.init() == -1) {
			System.out.println();
			JOptionPane.showMessageDialog(null, "Not enough replicas...add more on the configuration file");
			System.exit(-1);
		}
		Thread t1 = null, t2 = null, t3 = null;
		while (true) {
			try {
				opt = stdin.nextInt();
				switch (opt) {
				case 1: {
					controller = Controller.getController();
					if(controller.start() != -1) {
						t1 = new Thread(Controller.getController());
						t1.start();
					 	t2 = new Thread(new Update());
						t2.start();
						t3 = new Thread(new Configuration());
						t3.start();
					}
					menu();
					break;
				}
				case 2:
					controller = Controller.getController();
					controller.statusV2(stdin);
					menu();
					break;
				case 3:
					controller = Controller.getController();
					controller.stop();
					t1.interrupt(); t2.interrupt(); t3.interrupt();
					try {
						t1.join(5000);
						t2.join(5000);
						t3.join(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Update.shutdown();
					Configuration.shutdown();
					controller.init();
					menu();
					break;
				case 4:
					File file = new File(".configTemp");
					file.delete();
					System.exit(0);
				default:
					menu();
				}
			} catch (Exception e) {
				String bad_input = stdin.next();
				menu();
				continue;
			}
		}
	}
}
