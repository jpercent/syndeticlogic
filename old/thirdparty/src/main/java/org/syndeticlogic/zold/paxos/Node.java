package org.syndeticlogic.zold.paxos;

import java.io.*;
import java.net.*;
import java.util.*;

public class Node {
	// timeout per connection
	public static final int socketTimeout = 1000;
	public static final int heartbeatTimeout = 5000;

	// if a proposer doesn't hear back from a majority of acceptors, try again
	public static final int proposeTimeout = 10000;

	// this is a range so that all heartbeats usually won't happen
	// simultaneously
	public static final int heartbeatDelayMin = 1000;
	public static final int heartbeatDelayMax = 2000;

	public static Integer nextPort = 37100;

	// Node Data
	private Set<NodeLocationData> nodes;
	NodeLocationData locationData;
	private NodeListener listener;
	private NodeHeartbeat heartbeat;
	private Map<Integer, NodeHeartbeatListener> heartbeatListeners;
	private boolean isRunning;

	// Proposer Variables
	private int currentCsn;
	private int psn;
	private Map<Integer, Integer> numAcceptRequests;
	private Map<Integer, Proposal> proposals;
	private Map<Integer, NodeReProposer> reProposers;

	// Acceptor Variables
	private Map<Integer, Integer> minPsns;
	private Map<Integer, Proposal> maxAcceptedProposals;

	// Learner Variables
	private Map<Integer, Integer> numAcceptNotifications;
	private Map<Integer, String> chosenValues;

	public Node(String host, int port, int psnSeed) {
		this.psn = psnSeed; // when used properly, this ensures unique PSNs.
		this.currentCsn = 0;
		this.locationData = new NodeLocationData(host, port, psnSeed);
		this.numAcceptRequests = new HashMap<Integer, Integer>();
		this.numAcceptNotifications = new HashMap<Integer, Integer>();
		this.proposals = new HashMap<Integer, Proposal>();
		this.reProposers = new HashMap<Integer, NodeReProposer>();
		this.minPsns = new HashMap<Integer, Integer>();
		this.maxAcceptedProposals = new HashMap<Integer, Proposal>();
		this.chosenValues = new HashMap<Integer, String>();
		this.nodes = new HashSet<NodeLocationData>();
		this.isRunning = false;
	}

	public Node(int psnSeed) {
		this("localhost", nextPort++, psnSeed);
	}

	public void setNodeList(Set<NodeLocationData> s) {
		this.nodes = s;
	}

	public void becomeLeader() {
		DebugHelper.writeDebug("I'm Leader", toString());
		locationData.becomeLeader();
		for (NodeLocationData node : nodes)
			node.becomeNonLeader();

		// fill skipped slots
		int n = 0;
		int m = 0;
		ArrayList<Integer> proposeBuffer = new ArrayList<Integer>();
		while (m < chosenValues.size()) {
			if (chosenValues.containsKey(n))
				m++;
			else
				proposeBuffer.add(n);
			n++;
		}
		for (int i = 0; i < proposeBuffer.size(); i++)
			propose("NOOP", proposeBuffer.get(i));
	}

	void electNewLeader() {
		if (!isRunning)
			return;
		int newNum = -1;

		// find old leader and calculate new leader num
		for (NodeLocationData node : nodes)
			if (node.isLeader()) {
				newNum = (node.getNum() + 1) % nodes.size();
				break;
			}

		NewLeaderNotificationMessage newLeaderNotification = new NewLeaderNotificationMessage(
				newNum);
		broadcast(newLeaderNotification);
		DebugHelper.writeDebug("Electing new leader: " + newNum, toString());
	}

	public synchronized Map<Integer, String> getValues() {
		return chosenValues;
	}

	public synchronized void start() {
		recoverStableStorage();

		listener = new NodeListener(this);
		listener.start();

		heartbeat = new NodeHeartbeat(this);
		heartbeat.start();

		heartbeatListeners = new HashMap<Integer, NodeHeartbeatListener>();
		for (NodeLocationData node : nodes) {
			if (node == locationData)
				continue;
			NodeHeartbeatListener x = new NodeHeartbeatListener(this, node);
			x.start();
			heartbeatListeners.put(node.getNum(), x);
		}

		isRunning = true;

		DebugHelper.writeDebug("Started", toString());
	}

	public synchronized void stop() {
		if (listener != null)
			listener.kill();
		listener = null;

		if (heartbeat != null)
			heartbeat.kill();
		heartbeat = null;

		if (heartbeatListeners != null) {
			for (NodeHeartbeatListener heartbeatListener : heartbeatListeners
					.values())
				heartbeatListener.kill();
			heartbeatListeners.clear();
		}
		heartbeatListeners = null;

		isRunning = false;

		DebugHelper.writeDebug("Stopped", toString());
	}

	public void propose(String value) {
		// testing purposes
		if (Main.slotSkippingFlag)
			currentCsn++;

		propose(value, currentCsn++);
	}

	public void propose(String value, int csn) {
		if (!isRunning)
			return;
		if (reProposers.containsKey(csn))
			reProposers.remove(csn).kill();
		numAcceptRequests.put(csn, 0);
		Proposal proposal = new Proposal(csn, psn, value);
		reProposers.put(csn, new NodeReProposer(this, proposal));
		proposals.put(csn, proposal);
		broadcast(new PrepareRequestMessage(csn, psn));
		psn += nodes.size();
	}

	void broadcast(Message m) {
		if (!isRunning)
			return;

		m.setSender(locationData);
		for (NodeLocationData node : nodes) {
			// immediately deliver to self
			if (this.locationData == node)
				deliver(m);

			// send message
			else
				unicast(node, m);
		}
	}

	private void unicast(NodeLocationData node, Message m) {
		if (!isRunning)
			return;

		Socket socket = null;
		ObjectOutputStream out = null;
		m.setReciever(node);

		try {
			socket = new Socket(node.getHost(), node.getPort());
			socket.setSoTimeout(socketTimeout);
			out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(m);
			out.flush();
		} catch (ConnectException e) {
			DebugHelper.writeDebug("Detected crash from " + node.getNum() + " (refused)",
					true, toString());

			// if was leader, elect a new one and try THIS retransmission again,
			// else, do nothing
			if (node.isLeader() && !(m instanceof NewLeaderNotificationMessage))
				electNewLeader();
		} catch (SocketTimeoutException e) {
			DebugHelper.writeDebug("Detected crash from " + node.getNum() + " (timeout)",
					true, toString());

			// if was leader, elect a new one and try THIS retransmission again,
			// else, do nothing
			if (node.isLeader() && !(m instanceof NewLeaderNotificationMessage))
				electNewLeader();
		} catch (IOException e) {
			e.printStackTrace();
			DebugHelper.writeDebug("IOException while trying to send message!", true, toString());
		} finally {
			try {
				if (out != null)
					out.close();
				if (socket != null)
					socket.close();
			} catch (IOException e) {
			}
		}
	}

	synchronized void deliver(Message m) {
		if (!isRunning)
			return;

		if (m instanceof HeartbeatMessage) {
			if (m.getSender() == this.locationData)
				return;

			// too much spam
			// writeDebug("Got Heartbeat from " + m.getSender());

			heartbeatListeners.get(m.getSender().getNum()).resetTimeout();
		} else if (m instanceof PrepareRequestMessage) // Acceptor
		{
			PrepareRequestMessage prepareRequest = (PrepareRequestMessage) m;
			int csn = prepareRequest.getCsn();
			int psn = prepareRequest.getPsn();

			if (currentCsn <= csn)
				currentCsn = csn + 1;

			DebugHelper.writeDebug("Got Prepare Request from " + prepareRequest.getSender()
					+ ": (" + csn + ", " + psn + ")", toString());

			// new minPsn
			if (!minPsns.containsKey(csn) || minPsns.get(csn) < psn)
				minPsns.put(csn, psn);

			// respond
			PrepareResponseMessage prepareResponse = new PrepareResponseMessage(
					csn, minPsns.get(csn), maxAcceptedProposals.get(csn));
			prepareResponse.setSender(locationData);
			unicast(prepareRequest.getSender(), prepareResponse);

			updateStableStorage();
		} else if (m instanceof PrepareResponseMessage) // Proposer
		{
			PrepareResponseMessage prepareResponse = (PrepareResponseMessage) m;
			Proposal acceptedProposal = prepareResponse.getProposal();
			int csn = prepareResponse.getCsn();
			int minPsn = prepareResponse.getMinPsn();
			Proposal proposal = proposals.get(csn);

			if (currentCsn <= csn)
				currentCsn = csn + 1;

			DebugHelper.writeDebug("Got Prepare Response from "
					+ prepareResponse.getSender()
					+ ": "
					+ csn
					+ ", "
					+ minPsn
					+ ", "
					+ (acceptedProposal == null ? "None" : acceptedProposal
							.toString()), toString());

			if (!numAcceptRequests.containsKey(csn)) // ignore if already heard
														// from a majority
				return;

			// if acceptors already accepted something higher, use it instead
			if (acceptedProposal != null
					&& acceptedProposal.getPsn() > proposal.getPsn())
				proposal = acceptedProposal;

			// if acceptors already promised something higher, use higher psn
			if (minPsn > proposal.getPsn()) {
				while (psn < prepareResponse.getMinPsn())
					psn += nodes.size();
				propose(proposal.getValue(), proposal.getCsn());
				return;
			}

			int n = numAcceptRequests.get(csn);
			n++;
			if (n > (nodes.size() / 2)) // has heard from majority?
			{
				numAcceptRequests.remove(csn);
				if (reProposers.containsKey(csn))
					reProposers.remove(csn).kill();
				AcceptRequestMessage acceptRequest = new AcceptRequestMessage(
						proposal);
				broadcast(acceptRequest);
			} else
				numAcceptRequests.put(csn, n);
		} else if (m instanceof AcceptRequestMessage) // Acceptor
		{
			AcceptRequestMessage acceptRequest = (AcceptRequestMessage) m;
			Proposal requestedProposal = acceptRequest.getProposal();
			int csn = requestedProposal.getCsn();
			int psn = requestedProposal.getPsn();

			if (currentCsn <= csn)
				currentCsn = csn + 1;

			DebugHelper.writeDebug("Got Accept Request from " + acceptRequest.getSender()
					+ ": " + requestedProposal.toString(), toString());

			if (psn < minPsns.get(csn))
				return; // ignore

			// "accept" the proposal
			if (psn > minPsns.get(csn))
				minPsns.put(csn, psn);
			maxAcceptedProposals.put(csn, requestedProposal);
			DebugHelper.writeDebug("Accepted: " + requestedProposal.toString(), toString());

			// Notify Learners
			AcceptNotificationMessage acceptNotification = new AcceptNotificationMessage(
					requestedProposal);
			broadcast(acceptNotification);

			updateStableStorage();
		} else if (m instanceof AcceptNotificationMessage) // Learner
		{
			AcceptNotificationMessage acceptNotification = (AcceptNotificationMessage) m;
			Proposal acceptedProposal = acceptNotification.getProposal();
			int csn = acceptedProposal.getCsn();

			if (currentCsn <= csn)
				currentCsn = csn + 1;

			DebugHelper.writeDebug("Got Accept Notification from "
					+ acceptNotification.getSender()
					+ ": "
					+ (acceptedProposal == null ? "None" : acceptedProposal
							.toString()), null);

			// ignore if already learned
			if (chosenValues.get(csn) != null)
				return;

			if (numAcceptNotifications.get(csn) == null)
				numAcceptNotifications.put(csn, 0);

			int n = numAcceptNotifications.get(csn);
			n++;
			if (n > (nodes.size() / 2)) // has heard from majority?
			{
				numAcceptNotifications.remove(csn);
				chosenValues.put(csn, acceptedProposal.getValue());
				DebugHelper.writeDebug("Learned: " + acceptedProposal.getCsn() + ", "
						+ acceptedProposal.getValue(), toString());

				updateStableStorage();
			} else
				numAcceptNotifications.put(csn, n);
		} else if (m instanceof NewLeaderNotificationMessage) // Leader Election
		{
			NewLeaderNotificationMessage newLeaderNotification = (NewLeaderNotificationMessage) m;
			int newNum = newLeaderNotification.getNum();

			DebugHelper.writeDebug("Got New Leader Notification from "
					+ newLeaderNotification.getSender() + ": " + newNum, toString());

			// am i new leader?
			if (locationData.getNum() == newNum)
				becomeLeader();

			// find new leader, make others non-leaders
			for (NodeLocationData node : nodes)
				if (node.getNum() == newNum)
					node.becomeLeader();
				else
					node.becomeNonLeader();
		} else
			DebugHelper.writeDebug("Unknown Message recieved", true, toString());
	}

	public NodeLocationData getLocationData() {
		return locationData;
	}

	public boolean isLeader() {
		return locationData.isLeader();
	}

	

	private synchronized void recoverStableStorage() {

		ObjectInputStream in = null;
		try {
			File f = new File("stableStorage/" + toString() + ".bak");
			if (!f.exists()) {
				DebugHelper.writeDebug("No stable storage found", toString());
				return;
			}
			in = new ObjectInputStream(new FileInputStream(f));
			NodeStableStorage stableStorage = (NodeStableStorage) in
					.readObject();
			minPsns = stableStorage.minPsns;
			maxAcceptedProposals = stableStorage.maxAcceptedProposals;
		} catch (IOException e) {
			DebugHelper.writeDebug("Problem reading from stable storage!", true, toString());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			DebugHelper.writeDebug(
					"ClassNotFoundException while reading from stable storage!",
					true, toString());
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
			}
		}
	}

	private synchronized void updateStableStorage() {
		NodeStableStorage stableStorage = new NodeStableStorage();
		stableStorage.minPsns = minPsns;
		stableStorage.maxAcceptedProposals = maxAcceptedProposals;

		ObjectOutputStream out = null;
		try {
			File dir = new File("stableStorage");
			if (!dir.exists())
				dir.mkdir();

			out = new ObjectOutputStream(new FileOutputStream("stableStorage/"
					+ toString() + ".bak"));
			out.writeObject(stableStorage);
			out.flush();
		} catch (IOException e) {
			DebugHelper.writeDebug("Problem writing to stable storage!", true, toString());
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
			}
		}
	}

	public synchronized void clearStableStorage() {
		File f = new File("stableStorage/" + toString() + ".bak");
		if (f.exists())
			f.delete();
	}
	

	public String toString() {
		return locationData.toString();
	}

}
