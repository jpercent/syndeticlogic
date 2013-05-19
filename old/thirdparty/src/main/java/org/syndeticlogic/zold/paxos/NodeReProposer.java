package org.syndeticlogic.zold.paxos;

class NodeReProposer extends Thread {
	/**
	 * 
	 */
	private Node reproposer;
	private boolean isRunning;
	private long expireTime;
	private Proposal proposal;

	public NodeReProposer(Node node, Proposal proposal) {
		reproposer = node;
		this.isRunning = true;
		this.proposal = proposal;
	}

	public void run() {
		expireTime = System.currentTimeMillis() + Node.proposeTimeout;
		while (isRunning) {
			if (expireTime < System.currentTimeMillis()) {
				reproposer.propose(proposal.getValue(), proposal.getCsn());
				kill();
			}
			yield(); // so the while loop doesn't spin too much
		}
	}

	public void kill() {
		isRunning = false;
	}

}