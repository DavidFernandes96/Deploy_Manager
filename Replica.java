
/**
 * Deploy Manager
 * 
 * @author David Fernandes
 * 
 * Replica class
 */

import java.util.*;

public class Replica {
	private String name;
	private boolean status;
	private static Map<String, Replica> map = new HashMap<>();
	private static int numberReplicas = -1;
	private boolean newReplica;
	private boolean update;

	public Replica(String name) {
		this.name = name;
		map.put(name, this);
		newReplica = true;
		update = false;
		numberReplicas++;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getStatus() {
		return this.status;
	}
	
	public void setStatus(boolean status) {
		this.status = status;
	}

	public static int getNumberReplicas() {
		return numberReplicas;
	}

	public static Replica getReplica(String name) {
		return map.get(name);
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

	public boolean getUpdate() {
		return this.update;
	}

	public void setUpdate(boolean bValue) {
		this.update = bValue;
	}

}
