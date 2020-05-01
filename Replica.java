import java.util.*;

public class Replica {
  private String name;
  private boolean status;
  private int id;
  private static int numberReplicas = 0;

  public Replica(String name) {
    this.name = name;
    numberReplicas++;
    this.id = numberReplicas;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

  public boolean getStatus() {
    return this.status;
  }

  public static int getNumberReplicas() {
    return numberReplicas;
  }

  public int getId() {
    return this.id;
  }
}
