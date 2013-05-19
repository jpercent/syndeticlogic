package org.syndeticlogic.apriori;
/**
 * An implementation of the Apriori Algorithm.  Given a support threshold and
 * a transaction data set, the algorithm computes the frequent item sets.
 *  @author James Percent
 */

import java.util.BitSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.Math;

public class Apriori {
    /**
     * Constructor. This is only constructor for the class. It takes a
     * transaction data set, a support threshold, and a count of the distinct
     * elements in transaction data set.
     * 
     * @param tset
     *            The transaction dataset.
     * @param mu
     *            The support threshold.
     * @param items
     *            The number of unique items in the transaction data set.
     */
    public Apriori(ArrayList<BitSet> tset, double mu, int items) {
        assert mu >= 0.0 && mu <= 1.0;

        mTSet = tset;
        mMu = mu;
        mItems = items;

        mFrequentSets = null;
        mCurrentKF = null;
        mPreviousKF = null;
        mCandidates = null;

        double transactions = (double) tset.size();
        mSCount = (int) Math.ceil((double) transactions * mMu);

        // System.out.println("support count = " +mSCount);
        // System.out.println("mu = " + transactions * mMu + ", " + mMu);
    }

    /**
     * Encapsulates the process of generating the frequent subsets of a
     * transaction data set based on the support threshold. The return value is
     * the set of frequent item sets that "hold" this level of support.
     * 
     * @return The set of frequent item sets.
     */
    public ArrayList<ArrayList<BitSet>> computeFrequentSets() {

        initialize();
        while (mCurrentKF.size() > 0) {
            mK++;
            generate();
            support();
            assert mCandidates == null;
        }
        return mFrequentSets;
    }

    /**
     * Initializes and generates the 1-frequent item set. This method is
     * private.
     */
    private void initialize() {
        mK = 1;
        int[] freq = new int[mItems];
        for (int i = 0; i < mItems; i++) {
            freq[i] = 0;
        }

        mCurrentKF = new ArrayList<BitSet>();
        Iterator<BitSet> iter = mTSet.iterator();
        while (iter.hasNext()) {
            BitSet bset = iter.next();
            for (int i = bset.nextSetBit(0); i >= 0; i = bset.nextSetBit(i + 1)) {
                freq[i]++;
                if (freq[i] == mSCount) {
                    BitSet new_set = new BitSet(mItems);
                    new_set.set(i);
                    mCurrentKF.add(new_set);
                }
            }
        }
        // System.out.println("transaction data set: ");
        // printBitSet(mTSet);

        // System.out.println("the 1-frquent sets are");
        // printBitSet(mCurrentKF);

        mFrequentSets = new ArrayList<ArrayList<BitSet>>();
        mFrequentSets.add(mCurrentKF);
    }

    /**
     * Checks that candidate sets meet the support requirement; if so, then they
     * are added to the frequent item set for the given level.
     * 
     * In mathematical terms, given the set of candidate sets C = { ci | ci is
     * set of candidates and |ci| = k + 1}, the transaction data set T = { ti |
     * ti is a transaction}, the item set I, the support threshold, and the
     * supportCount = |I| * supportThreshold. For each ci belongs to C, if ci is
     * a subset of at least supportCount ti's, then ci is a frequent item set for
     * level k+1. Latex is my friend!
     * 
     * This method is private. It is only called by computeFrequentSets.
     */
    private void support() {

        /**
         * mPreviousKF is null for the 2-frequent. 2-frequent level is a special
         * case.
         */
        if (mPreviousKF == null) {
            mPreviousKF = mCurrentKF;
            mCurrentKF = mCandidates;
            mFrequentSets.add(mCurrentKF);
            // System.out.println("the 2-frequent bitsets are: ");
            // printBitSet(mCurrentKF);
            return;
        } else {
            mPreviousKF = mCurrentKF;
            mCurrentKF = new ArrayList<BitSet>();
        }

        Iterator<BitSet> citer = mCandidates.iterator();
        while (citer.hasNext()) {
            BitSet c = citer.next();
            Iterator<BitSet> titer = mTSet.iterator();
            int scount = 0;
            while (titer.hasNext()) {
                BitSet t = titer.next();
                boolean matched = true;
                for (int i = c.nextSetBit(0); i >= 0; i = c.nextSetBit(i + 1)) {
                    if (!t.get(i)) {
                        matched = false;
                        break;
                    }
                }

                if (matched) {
                    scount++;
                    if (scount == mSCount) {
                        mCurrentKF.add(c);
                        break;
                    }
                }
            }

        }
        if (mCurrentKF.size() > 0) {
            mFrequentSets.add(mCurrentKF);
        }
        // System.out.println("the " + (mK) + "-frequent bitsets are: ");
        // printBitSet(mCurrentKF);
        mCandidates = null;
    }

    /**
     * Given a set of k-frequent item sets Fk = { fk | fk is frequent and |f| =
     * k}, we generate the candidate frequent item sets C = { l union m | l, m
     * belong to F and l != m and l intersection m belongs Fk-1 and there does
     * not exist a subset of l union m, of size k, that belongs to F}.
     * 
     * Note that 1-frequent item sets are generated in the initialize method,
     * and 2-frequent item sets are a special case that does not make use of the
     * pruning step. This method is private and it is only called by
     * computeFrequentSets.
     */
    protected void generate() {
        int k = mK;
        assert k > 0;

        mCandidates = new ArrayList<BitSet>();
        Iterator<BitSet> frequentK = mCurrentKF.iterator();

        while (frequentK.hasNext()) {
            BitSet cursorFk = frequentK.next();
            Iterator<BitSet> remainingFk = mCurrentKF.iterator();

            while (remainingFk.hasNext()) {
                BitSet current = remainingFk.next();
                if (current == cursorFk) {
                    break;
                }
            }

            while (remainingFk.hasNext()) {
                BitSet nextFk = remainingFk.next();

                assert nextFk != cursorFk;
                assert !nextFk.equals(cursorFk);
                assert cursorFk.length() > 0 && nextFk.length() > 0;

                BitSet c = (BitSet) cursorFk.clone();
                BitSet c1 = (BitSet) cursorFk.clone();
                c.and(nextFk);
                c1.or(nextFk);

                if (mPreviousKF != null && !marked(c1)) {
                    Iterator<BitSet> previousKFIter = mPreviousKF.iterator();

                    assert previousKFIter != null;

                    assert c.size() <= (mK - 1);
                    boolean found = false;
                    while (previousKFIter.hasNext()) {
                        if (c.equals(previousKFIter.next())) {
                            found = true;
                            break;
                        }
                    }
                    if (found && !prune(c1)) {
                        mCandidates.add(c1);
                    }
                } else if (mPreviousKF == null) {
                    mCandidates.add(c1);
                }
            }

        }
    }

    /**
     * Given a candidate set C, if the candidate has already been found, return
     * true. Otherwise return false indicating this is a unique candidate.
     * 
     * @param c
     *            a candidate frequent set.
     * @return returns a boolean indicating whether the candidate is already
     *         being tracked in the set of candidate sets for this level.
     */
    protected boolean marked(BitSet c) {
        Iterator<BitSet> markedIter = mCandidates.iterator();
        while (markedIter.hasNext()) {
            if (c.equals(markedIter.next())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given a candidate set C, where |C| = k + 1, and a set of frequent sets F
     * = { fk | fk is frequent and |fk| = k}. This method determines if there
     * exists a set S` = { i | i subset of C and |i| = k and i is not a subset
     * of fk for all fks in F}. If a set S` is found then the method returns
     * true indicating that the candidate should be pruned. Conversely, if S`
     * does not exist then the candidate set could be frequent and false is
     * returned to indicating that the candidate should not be pruned.
     * 
     * @param candidate
     *            set C.
     * @return returns <code> true </code> if the candidate set should be
     *         pruned.
     */
    protected boolean prune(BitSet candidate) {

        ArrayList<BitSet> subsets = computeSubsets(candidate);
        Iterator<BitSet> iter = subsets.iterator();

        while (iter.hasNext()) {
            BitSet csubset = iter.next();
            boolean found = false;
            Iterator<BitSet> kfIter = mCurrentKF.iterator();
            while (kfIter.hasNext()) {
                BitSet aprioriFrequent = kfIter.next();
                if (aprioriFrequent.equals(csubset)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given a lexicographically ordered candidate set S, this method computes
     * S` = { i | i is subset of S and |i| = |S| - 1}.
     * 
     * @param candidate
     *            set
     * @return the set S`
     */
    protected ArrayList<BitSet> computeSubsets(BitSet candidate) {
        ArrayList<BitSet> subsets = new ArrayList<BitSet>();

        for (int i = candidate.nextSetBit(0); i >= 0; i = candidate
                .nextSetBit(i + 1)) {
            BitSet clone = (BitSet) candidate.clone();
            clone.clear(i);
            subsets.add(clone);
        }
        // System.out.println("Candidate is:");
        // printBitSet(candidate);
        // System.out.println("subsets are k-1");
        // printBitSet(subsets);
        return subsets;
    }

    /*
     * A debugging utility method to print an Array of BitSets. private void
     * printBitSet(ArrayList<BitSet> bset) { Iterator<BitSet> bi =
     * bset.iterator(); while (bi.hasNext()) { printBitSet(bi.next()); } }
     * private void printBitSet(BitSet bset) { System.out.println("bset = " +
     * bset.toString()); }
     */
    /**
     * An ordered list from 1 to K of the frequent item sets for the transaction
     * data set.
     */
    private ArrayList<ArrayList<BitSet>> mFrequentSets;
    /**
     * The current or Kth set of frequent item sets. This set is used by the
     * generate method to generate the k+1 frequent item sets, if they exist in
     * the transaction data set. exits.
     */
    private ArrayList<BitSet> mCurrentKF;
    /**
     * The previous set of frequent item sets. This set is used by the generate
     * method to generate the k+1 frequent item sets, if they exist in the
     * transaction data set. exits.
     */
    private ArrayList<BitSet> mPreviousKF;
    /**
     * The current k+1 candidate item sets.
     */
    private ArrayList<BitSet> mCandidates;
    /**
     * The transaction data-set.
     */
    private ArrayList<BitSet> mTSet;
    /**
     * The support threshold.
     */
    private double mMu;
    /**
     * The support count.
     */
    private int mSCount;
    /**
     * The number of distinct items in the transaction data set.
     */
    private int mItems;
    private int mK;
}
