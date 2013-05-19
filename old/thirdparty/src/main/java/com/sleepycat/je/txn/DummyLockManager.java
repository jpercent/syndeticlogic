/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: DummyLockManager.java,v 1.11.2.1 2008/09/10 11:57:10 cwl Exp $
 */

package com.sleepycat.je.txn;

import java.util.Set;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DeadlockException;
import com.sleepycat.je.LockStats;
import com.sleepycat.je.dbi.DatabaseImpl;
import com.sleepycat.je.dbi.EnvironmentImpl;
import com.sleepycat.je.dbi.MemoryBudget;

/**
 * DummyLockManager performs no locking for DS mode.
 */
public class DummyLockManager extends LockManager {

    /*
     * Even though a user may specify isNoLocking for performance reasons, JE
     * will sometimes still use transactions internally (e.g. to create
     * internal db's).  So we can not completely eliminate the Lock Manager
     * Instead, when isNoLocking is specified, we keep a txnal Lock Manager
     * around for use by transactional Lockers.  Delegate to that as needed.
     * [#16453]
     */
    private LockManager superiorLockManager;

    public DummyLockManager(EnvironmentImpl envImpl,
                            LockManager superiorLockManager)
    	throws DatabaseException {

        super(envImpl);
        this.superiorLockManager = superiorLockManager;
    }

    /**
     * @see LockManager#lookupLock
     */
    protected Lock lookupLock(Long nodeId)
	throws DatabaseException {

        Lock ret = superiorLockManager.lookupLock(nodeId);
	return ret;
    }

    /**
     * @see LockManager#attemptLock
     */
    protected LockAttemptResult attemptLock(Long nodeId,
                                            Locker locker,
                                            LockType type,
                                            boolean nonBlockingRequest)
        throws DatabaseException {

        if (locker.isTransactional()) {
            return superiorLockManager.attemptLock
                (nodeId, locker, type, nonBlockingRequest);
        } else {
            return new LockAttemptResult(null, LockGrantType.NEW, true);
        }
    }

    /**
     * @see LockManager#makeTimeoutMsg
     */
    protected DeadlockException makeTimeoutMsg(String lockOrTxn,
					       Locker locker,
					       long nodeId,
					       LockType type,
					       LockGrantType grantType,
					       Lock useLock,
					       long timeout,
					       long start,
					       long now,
					       DatabaseImpl database)
        throws DatabaseException {

        if (locker.isTransactional()) {
            return superiorLockManager.makeTimeoutMsg
                (lockOrTxn, locker, nodeId, type, grantType, useLock,
                 timeout, start, now, database);
        } else {
            return null;
        }
    }

    /**
     * @see LockManager#releaseAndNotifyTargets
     */
    protected Set<Locker> releaseAndFindNotifyTargets(long nodeId,
                                                      Locker locker)
        throws DatabaseException {

        if (locker.isTransactional()) {
            return superiorLockManager.
                releaseAndFindNotifyTargets(nodeId, locker);
        } else {
            return null;
        }
    }

    /**
     * @see LockManager#transfer
     */
    void transfer(long nodeId,
                  Locker owningLocker,
                  Locker destLocker,
                  boolean demoteToRead)
        throws DatabaseException {

        if (owningLocker.isTransactional()) {
            superiorLockManager.transfer
                (nodeId, owningLocker, destLocker, demoteToRead);
        } else {
            return;
        }
    }

    /**
     * @see LockManager#transferMultiple
     */
    void transferMultiple(long nodeId,
                          Locker owningLocker,
                          Locker[] destLockers)
        throws DatabaseException {

        if (owningLocker.isTransactional()) {
            superiorLockManager.transferMultiple
                (nodeId, owningLocker, destLockers);
        } else {
            return;
        }
    }

    /**
     * @see LockManager#demote
     */
    void demote(long nodeId, Locker locker)
        throws DatabaseException {

        if (locker.isTransactional()) {
            superiorLockManager.demote(nodeId, locker);
        } else {
            return;
        }
    }

    /**
     * @see LockManager#isLocked
     */
    boolean isLocked(Long nodeId)
        throws DatabaseException {

	return superiorLockManager.isLocked(nodeId);
    }

    /**
     * @see LockManager#isOwner
     */
    boolean isOwner(Long nodeId, Locker locker, LockType type)
        throws DatabaseException {

	return superiorLockManager.isOwner(nodeId, locker, type);
    }

    /**
     * @see LockManager#isWaiter
     */
    boolean isWaiter(Long nodeId, Locker locker)
        throws DatabaseException {

	return superiorLockManager.isWaiter(nodeId, locker);
    }

    /**
     * @see LockManager#nWaiters
     */
    int nWaiters(Long nodeId)
        throws DatabaseException {

	return superiorLockManager.nWaiters(nodeId);
    }

    /**
     * @see LockManager#nOwners
     */
    int nOwners(Long nodeId)
        throws DatabaseException {

	return superiorLockManager.nOwners(nodeId);
    }

    /**
     * @see LockManager#getWriterOwnerLocker
     */
    Locker getWriteOwnerLocker(Long nodeId)
        throws DatabaseException {

	return superiorLockManager.getWriteOwnerLocker(nodeId);
    }

    /**
     * @see LockManager#validateOwnership
     */
    protected boolean validateOwnership(Long nodeId,
                                        Locker locker,
                                        LockType type,
                                        boolean flushFromWaiters,
					MemoryBudget mb)
        throws DatabaseException {

        if (locker.isTransactional()) {
            return superiorLockManager.validateOwnership
                (nodeId, locker, type, flushFromWaiters, mb);
        } else {
            return true;
        }
    }

    /**
     * @see LockManager#dumpLockTable
     */
    protected void dumpLockTable(LockStats stats)
        throws DatabaseException {

        superiorLockManager.dumpLockTable(stats);
    }
}
