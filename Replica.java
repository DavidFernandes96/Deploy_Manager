import java.util.*;

public class Replica {
  private String name;
  private boolean status;
  private static Map<String, Replica> map = new HashMap<>();
  private static int numberReplicas = -1;
  private boolean newReplica;
  private boolean destroy;
  private boolean update;

  public Replica(String name) {
    this.name = name;
    map.put(name, this);
    newReplica = true;
    destroy = false;
    update = false;
    numberReplicas++;
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

  public static Replica getReplica(String name) {
    return map.get(name);
  }

  public static void changeName(String oldName, String newName) {
    Replica replica = map.remove(oldName);
    map.put(newName, replica);
  }

  public void destroyReplica(String name) {
    Replica replica = map.remove(name);
    numberReplicas--;
  }

  public boolean isNew() {
    return this.newReplica;
  }

  public void setNew(boolean bValue) {
    this.newReplica = bValue;
  }

  public boolean toDestroy() {
    return destroy;
  }

  public void setToDestroy() {
    this.destroy = true;
  }

  public boolean toUpdate() {
    return this.update;
  }

  public void setToUpdate(boolean bValue) {
    this.update = bValue;
  }

}
