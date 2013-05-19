/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: PreparedTxn.java,v 1.6 2008/01/07 14:28:56 cwl Exp $
 */

package com.sleepycat.je.txn;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.TransactionConfig;
import com.sleepycat.je.dbi.EnvironmentImpl;
import com.sleepycat.je.log.ReplicationContext;

/**
 * A PreparedTxn is used at recovery for processing a TXN_PREPARE log entry. It
 * is provides essentially the same functionality as a TXN but lets the calling
 * code set the transaction id.
 */
public class PreparedTxn extends Txn {

    private PreparedTxn(EnvironmentImpl envImpl,
                       TransactionConfig config,
                       long mandatedId)
        throws DatabaseException {

        super(envImpl, config, true /* noAPIReadLock */, mandatedId);
        setRepContext(ReplicationContext.NO_REPLICATE);
    }

    public static PreparedTxn createPreparedTxn(EnvironmentImpl envImpl,
						TransactionConfig config,
						long mandatedId)
        throws DatabaseException {

	PreparedTxn ret = null;
	try {
	    ret = new PreparedTxn(envImpl, config, mandatedId);
	    ret.initApiReadLock();
	} catch (DatabaseException DE) {
	    ret.close(false);
	    throw DE;
	}
	return ret;
    }

    /**
     * PrepareTxns use the mandated id.
     */
    @Override
    protected long generateId(TxnManager txnManager,
                              long mandatedId) {
        return mandatedId;
    }
}
