import java.util.Scanner;

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

    Controller controller;
    menu();
    while(true) {
      try {
        opt = stdin.nextInt();
        switch(opt) {
          case 1: controller = Controller.getController(); controller.start(); menu(); break;
          case 2: controller = Controller.getController(); controller.status(stdin); menu(); break;
          case 3: controller = Controller.getController(); controller.stop(); menu(); break;
          case 4: return;
          default: menu();
        }
      }catch(Exception e) {
        String bad_input = stdin.next();
        menu();
        continue;
      }
    }
  }
}