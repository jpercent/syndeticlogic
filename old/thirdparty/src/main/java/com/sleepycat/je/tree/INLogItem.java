/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: INLogItem.java,v 1.1 2008/04/18 22:57:37 mark Exp $
 */

package com.sleepycat.je.tree;

import com.sleepycat.je.log.LogItem;

/**
 * Extends LogItem to add fields used by IN.beforeLog and afterLog methods.
 */
public class INLogItem extends LogItem {
    
    /**
     * Parent IN of IN to be logged, or null for the root IN.  Used to count
     * utilization for provisional logging.
     *
     * Set by caller.
     */
    public IN parent = null;

    /**
     * Index of parent slot for IN to be logged.  Used to identify and update
     * the slot.
     *
     * Set by caller.
     */
    public int parentIndex = -1;

    /* Fields used internally by beforeLog and afterLog methods. */
    boolean isDelta = false;
}
