package org.syndeticlogic.zold.paxos;

import java.io.PrintStream;

public class DebugHelper {
	
	public static void writeDebug(String s, String locationData) {
		writeDebug(s, false, locationData);
	}

	public synchronized static void writeDebug(String s, boolean isError, String locationData) {
		if (!Main.isDebugging)
			return;

		PrintStream out = isError ? System.err : System.out;
		out.print(locationData);
		out.print(": ");
		out.println(s);
	}
}
