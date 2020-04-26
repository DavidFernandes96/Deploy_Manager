public class Replica {
  private String name;
  private boolean status;
  private static int numberReplicas = 0;

  public Replica(String name) {
    this.name = name;
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

  public int getNumberReplicas() {
    return numberReplicas;
  }
}
