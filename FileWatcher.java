
/**
 * Deploy Manager
 * 
 * @author David Fernandes
 *
 * Watches for changes on the config.txt file
 */

import java.util.*;
import java.io.*;

public abstract class FileWatcher extends TimerTask {
	private long timeStamp;
	private File file;

	BufferedReader reader, reader2;
	PrintWriter writer;

	String line, line2;

	public FileWatcher(File file) {
		this.file = file;
		this.timeStamp = file.lastModified();
	}

	public final void run() {
		long timeStamp = file.lastModified();

		if (this.timeStamp != timeStamp) {
			this.timeStamp = timeStamp;
			onChange(file);
		}
	}

	protected abstract void onChange(File file);
}
