package org.syndeticlogic.zold.paxos;

import java.util.Random;

class NodeHeartbeat extends Thread {
	/**
	 * 
	 */
	private Node heartbeat;
	private boolean isRunning;
	private long lastHeartbeat;
	private Random rand;

	public NodeHeartbeat(Node node) {
		heartbeat = node;
		isRunning = true;
		lastHeartbeat = System.currentTimeMillis();
		rand = new Random();
	}

	public void run() {
		int heartbeatDelay = rand.nextInt(Node.heartbeatDelayMax
				- Node.heartbeatDelayMin)
				+ Node.heartbeatDelayMin;
		while (isRunning) {
			if (heartbeatDelay < System.currentTimeMillis() - lastHeartbeat) {
				heartbeat.broadcast(new HeartbeatMessage());
				lastHeartbeat = System.currentTimeMillis();
				heartbeatDelay = rand.nextInt(Node.heartbeatDelayMax
						- Node.heartbeatDelayMin)
						+ Node.heartbeatDelayMin;
			}
			yield(); // so the while loop doesn't spin too much
		}
	}

	public void kill() {
		isRunning = false;
	}
}