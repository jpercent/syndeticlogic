/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: SecondaryKeyCreator.java,v 1.13 2008/01/07 14:28:46 cwl Exp $
 */

package com.sleepycat.je;

/**
 * The interface implemented for extracting single-valued secondary keys from
 * primary records.
 *
 * <p>The key creator object is specified by calling {@link
 * SecondaryConfig#setKeyCreator SecondaryConfig.setKeyCreator}. The secondary
 * database configuration is specified when calling {@link
 * Environment#openSecondaryDatabase Environment.openSecondaryDatabase}.</p>
 *
 * <p>For example:</p>
 *
 * <pre>
 *     class MyKeyCreator implements SecondaryKeyCreator {
 *         public boolean createSecondaryKey(SecondaryDatabase secondary,
 *                                             DatabaseEntry key,
 *                                             DatabaseEntry data,
 *                                             DatabaseEntry result)
 *             throws DatabaseException {
 *             //
 *             // DO HERE: Extract the secondary key from the primary key and
 *             // data, and set the secondary key into the result parameter.
 *             //
 *             return true;
 *         }
 *     }
 *     ...
 *     SecondaryConfig secConfig = new SecondaryConfig();
 *     secConfig.setKeyCreator(new MyKeyCreator());
 *     // Now pass secConfig to Environment.openSecondaryDatabase
 * </pre>
 *
 * <p>Use this interface when zero or one secondary key is present in a single
 * primary record, in other words, for many-to-one and one-to-one
 * relationships. When more than one secondary key may be present (for
 * many-to-many and one-to-many relationships), use the {@link
 * SecondaryMultiKeyCreator} interface instead.  The table below summarizes how
 * to create all four variations of relationships.</p>
 * <div>
 * <table border="yes">
 *     <tr><th>Relationship</th>
 *         <th>Interface</th>
 *         <th>Duplicates</th>
 *         <th>Example</th>
 *     </tr>
 *     <tr><td>One-to-one</td>
 *         <td>{@link SecondaryKeyCreator}</td>
 *         <td>No</td>
 *         <td>A person record with a unique social security number key.</td>
 *     </tr>
 *     <tr><td>Many-to-one</td>
 *         <td>{@link SecondaryKeyCreator}</td>
 *         <td>Yes</td>
 *         <td>A person record with a non-unique employer key.</td>
 *     </tr>
 *     <tr><td>One-to-many</td>
 *         <td>{@link SecondaryMultiKeyCreator}</td>
 *         <td>No</td>
 *         <td>A person record with multiple unique email address keys.</td>
 *     </tr>
 *     <tr><td>Many-to-many</td>
 *         <td>{@link SecondaryMultiKeyCreator}</td>
 *         <td>Yes</td>
 *         <td>A person record with multiple non-unique organization keys.</td>
 *     </tr>
 * </table>
 *
 * </div>
 *
 * <p>To configure a database for duplicates. pass true to {@link
 * DatabaseConfig#setSortedDuplicates}.</p>
 */
public interface SecondaryKeyCreator {

    /**
     * Creates a secondary key entry, given a primary key and data entry.
     *
     * <p>A secondary key may be derived from the primary key, primary data, or
     * a combination of the primary key and data.  For secondary keys that are
     * optional, the key creator method may return false and the key/data pair
     * will not be indexed.  To ensure the integrity of a secondary database
     * the key creator method must always return the same result for a given
     * set of input parameters.</p>
     *
     * @param secondary the database to which the secondary key will be
     * added. This parameter is passed for informational purposes but is not
     * commonly used.
     *
     * @param key the primary key entry.  This parameter must not be modified
     * by this method.
     *
     * @param data the primary data entry.  This parameter must not be modified
     * by this method.
     *
     * @param result the secondary key created by this method.
     *
     * @return true if a key was created, or false to indicate that the key is
     * not present.
     *
     * @throws DatabaseException if an error occurs attempting to create the
     * secondary key.
     */
    public boolean createSecondaryKey(SecondaryDatabase secondary,
				      DatabaseEntry key,
				      DatabaseEntry data,
				      DatabaseEntry result)
	throws DatabaseException;
}
