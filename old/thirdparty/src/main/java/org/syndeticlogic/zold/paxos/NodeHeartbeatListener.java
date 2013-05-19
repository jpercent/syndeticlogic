package org.syndeticlogic.zold.paxos;

class NodeHeartbeatListener extends Thread {
	/**
	 * 
	 */
	private Node listener;
	private boolean isRunning;
	private long lastHeartbeat;
	private NodeLocationData locationData;

	public NodeHeartbeatListener(Node node2, NodeLocationData node) {
		listener = node2;
		this.isRunning = true;
		this.lastHeartbeat = System.currentTimeMillis();
		this.locationData = node;
	}

	public void resetTimeout() {
		lastHeartbeat = System.currentTimeMillis();
	}

	public void run() {
		while (isRunning) {
			if (Node.heartbeatTimeout < System.currentTimeMillis()
					- lastHeartbeat) {
				DebugHelper.writeDebug("Detected crash from " + locationData.getNum()
						+ " (heartbeat)", true, locationData.toString());

				// if was leader, elect a new one
				if (locationData.isLeader())
					listener.electNewLeader();

				lastHeartbeat = System.currentTimeMillis();
			}
			yield(); // so the while loop doesn't spin too much
		}
	}

	public void kill() {
		isRunning = false;
	}
}