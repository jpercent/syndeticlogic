package org.syndeticlogic.apriori;

/**
 * This is a utility class to help arrange the data.  It is completely decoupled from the Apiori class, and can be ignored.
 */

import java.util.Hashtable;
import java.util.BitSet;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;

public class Transaction<ItemType> {

    public Transaction(int tid, int itemSetSize) {
        assert itemSetSize > 0;
        mTid = tid;
        mHash = new Hashtable<Integer, ItemType>();
        mBits = new BitSet(itemSetSize);
        mBits.clear();
    }
    
    public Transaction(BitSet bits, Hashtable<Integer, ItemType> items, int tid) {
        mTid = tid;
        mHash = new Hashtable<Integer, ItemType>();
        mBits = bits;
        for (int i = 0; i < bits.size(); i++) {
            if (bits.get(i)) {
               mHash.put(i, items.get(i));
            }
        }
    }
    
    public int getTid() {
        return mTid;
    }
    
    public BitSet getBitSet() {
        return mBits;
    }

    public void addItem(int key, ItemType value) {
        mHash.put(key, value);    
        mBits.set(key);
    }
    
    public void printBits() {
        System.out.println("tid = "+mTid+" and bits ="+mBits.toString());
    }
    
    public Hashtable<Integer, ItemType> getHash() {
        return mHash;
    }

    public void printTransaction() {
        Set<Map.Entry<Integer, ItemType>> hash = mHash.entrySet();
        Iterator<Map.Entry<Integer, ItemType>> i = hash.iterator();
        while (i.hasNext()) {
            Map.Entry<Integer, ItemType> e = i.next();
            System.out.print(e.getKey() + " = " + e.getValue());
            if (i.hasNext()) {
                System.out.print(", ");
            }
        }
        System.out
                .println("\n---------------------------------------------------------------------------------");
    }
    
    private Hashtable<Integer, ItemType> mHash;    
    private BitSet mBits;
    private int mTid;
}
