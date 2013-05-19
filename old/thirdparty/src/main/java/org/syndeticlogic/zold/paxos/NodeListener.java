package org.syndeticlogic.zold.paxos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

class NodeListener extends Thread {
	/**
	 * 
	 */
	private Node node;
	private boolean isRunning;
	private ServerSocket serverSocket;

	public NodeListener(Node node) {
		this.node = node;
		isRunning = true;
		try {
			serverSocket = new ServerSocket(node.locationData.getPort());
		} catch (IOException e) {
			DebugHelper.writeDebug("IOException while trying to listen!", true, node.toString());
		}
	}

	public void run() {
		Socket socket = null;
		ObjectInputStream in;
		while (isRunning) {
			try {
				socket = serverSocket.accept();
				in = new ObjectInputStream(socket.getInputStream());
				node.deliver((Message) in.readObject());
			} catch (IOException e) {
				DebugHelper.writeDebug(
						"IOException while trying to accept connection!",
						true, node.toString());
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				DebugHelper.writeDebug(
						"ClassNotFoundException while trying to read Object!",
						true, node.toString());
			} finally {
				try {
					if (socket != null)
						socket.close();
				} catch (Exception e) {
				}
			}
		}
		try {
			if (serverSocket != null)
				serverSocket.close();
		} catch (Exception e) {
		}
	}

	public void kill() {
		isRunning = false;
	}
}