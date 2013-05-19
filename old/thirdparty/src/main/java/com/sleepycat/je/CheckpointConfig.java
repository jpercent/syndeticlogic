/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: CheckpointConfig.java,v 1.20 2008/06/10 00:21:30 cwl Exp $
 */

package com.sleepycat.je;

/**
 * Specifies the attributes of a checkpoint operation invoked from {@link
 * com.sleepycat.je.Environment#checkpoint Environment.checkpoint}.
 */
public class CheckpointConfig  {

    /**
     * Default configuration used if null is passed to {@link
     * com.sleepycat.je.Environment#checkpoint Environment.checkpoint}.
     */
    public static final CheckpointConfig DEFAULT = new CheckpointConfig();

    private boolean force = false;
    private int kbytes = 0;
    private int minutes = 0;
    private boolean minimizeRecoveryTime = false;

    /**
     * An instance created using the default constructor is initialized with
     * the system's default settings.
     */
    public CheckpointConfig() {
    }

    /**
     * Configures the checkpoint log data threshold, in kilobytes.
     *
     * <p>The default is 0 for this class and the database environment.</p>
     *
     * @param kbytes If the kbytes parameter is non-zero, a checkpoint will
     * be performed if more than kbytes of log data have been written since
     * the last checkpoint.
     */
    public void setKBytes(int kbytes) {
        this.kbytes = kbytes;
    }

    /**
     * Returns the checkpoint log data threshold, in kilobytes.
     *
     * <p>This method may be called at any time during the life of the
     * application.</p>
     *
     * @return The checkpoint log data threshold, in kilobytes.
     */
    public int getKBytes() {
        return kbytes;
    }

    /**
     * Configures the checkpoint time threshold, in minutes.
     *
     * <p>The default is 0 for this class and the database environment.</p>
     *
     * @param minutes If the minutes parameter is non-zero, a checkpoint is
     * performed if more than min minutes have passed since the last
     * checkpoint.
     */
    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    /**
     * Returns the checkpoint time threshold, in minutes.
     *
     * @return The checkpoint time threshold, in minutes.
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * Configures the checkpoint force option.
     *
     * <p>The default is false for this class and the BDB JE environment.</p>
     *
     * @param force If set to true, force a checkpoint, even if there has
     * been no activity since the last checkpoint.
     */
    public void setForce(boolean force) {
        this.force = force;
    }

    /**
     * Returns the configuration of the checkpoint force option.
     *
     * @return The configuration of the checkpoint force option.
     */
    public boolean getForce() {
        return force;
    }

    /**
     * Configures the minimize recovery time option.
     *
     * <p>The default is false for this class and the BDB JE environment.</p>
     *
     * @param minimizeRecoveryTime If set to true, the checkpoint will itself
     * take longer but will cause a subsequent recovery (Environment.open) to
     * finish more quickly.
     */
    public void setMinimizeRecoveryTime(boolean minimizeRecoveryTime) {
        this.minimizeRecoveryTime = minimizeRecoveryTime;
    }

    /**
     * Returns the configuration of the minimize recovery time option.
     *
     * @return The configuration of the minimize recovery time option.
     */
    public boolean getMinimizeRecoveryTime() {
        return minimizeRecoveryTime;
    }

    /**
     * Returns the values for each configuration attribute.
     *
     * @return the values for each configuration attribute.
     */
    @Override
    public String toString() {
        return "minutes=" + minutes +
            "\nkBytes=" + kbytes +
            "\nforce=" + force +
            "\nminimizeRecoveryTime=" + minimizeRecoveryTime +
            "\n";
    }
}
