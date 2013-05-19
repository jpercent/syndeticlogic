/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: StatsConfig.java,v 1.19 2008/06/10 00:21:30 cwl Exp $
 */

package com.sleepycat.je;

import java.io.PrintStream;

/**
 * Specifies the attributes of a statistics retrieval operation.
 */
public class StatsConfig {

    /**
     * A convenience instance embodying the default configuration.
     */
    public static final StatsConfig DEFAULT = new StatsConfig();

    private boolean fast = false;
    private boolean clear = false;
    private PrintStream showProgressStream = null;
    private int showProgressInterval = 0;

    /**
     * An instance created using the default constructor is initialized with
     * the system's default settings.
     */
    public StatsConfig() {
    }

    /**
     * Configures the statistics operation to return only the values which do
     * not incur some performance penalty.
     *
     * <p>The default value is false.</p>
     *
     * <p>For example, skip stats that require a traversal of the database or
     * in-memory tree, or which lock down the lock table for a period of
     * time.</p>
     *
     * @param fast If set to true, configure the statistics operation to return
     * only the values which do not incur some performance penalty.
     */
    public void setFast(boolean fast) {
        this.fast = fast;
    }

    /**
     * Returns true if the statistics operation is configured to return only
     * the values which do not require expensive actions.
     *
     * @return true if the statistics operation is configured to return only
     * the values which do not require expensive actions.
     */
    public boolean getFast() {
        return fast;
    }

    /**
     * Configures the statistics operation to reset statistics after they are
     * returned. The default value is false.
     *
     * @param clear If set to true, configure the statistics operation to
     * reset statistics after they are returned.
     */
    public void setClear(boolean clear) {
        this.clear = clear;
    }

    /**
     * Returns true if the statistics operation is configured to reset
     * statistics after they are returned.
     *
     * @return true if the statistics operation is configured to reset
     * statistics after they are returned.
     */
    public boolean getClear() {
        return clear;
    }

    /**
     * Configures the statistics operation to display progress to the
     * PrintStream argument.  The accumulated statistics will be displayed
     * every N records, where N is the value of showProgressInterval.
     */
    public void setShowProgressStream(PrintStream showProgressStream) {
        this.showProgressStream = showProgressStream;
    }

    /**
     * Returns the PrintStream on which the progress messages will be displayed
     * during long running statistics gathering operations.
     */
    public PrintStream getShowProgressStream() {
        return showProgressStream;
    }

    /**
     * When the statistics operation is configured to display progress the
     * showProgressInterval is the number of LNs between each progress report.
     */
    public void setShowProgressInterval(int showProgressInterval) {
        this.showProgressInterval = showProgressInterval;
    }

    /**
     * Returns the showProgressInterval value, if set.
     */
    public int getShowProgressInterval() {
        return showProgressInterval;
    }

    /**
     * Returns the values for each configuration attribute.
     *
     * @return the values for each configuration attribute.
     */
    @Override
    public String toString() {
        return "fast=" + fast +
            "\nclear=" + clear +
            "\nshowProgressStream=" + showProgressStream +
            "\nshowProgressInterval=" + showProgressInterval +
            "\n";
    }
}
