package org.syndeticlogic.apriori;
import static org.junit.Assert.*;

import java.util.BitSet;

import org.junit.Test;
import org.syndeticlogic.apriori.Transaction;

import java.util.Hashtable;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;

/**
 * This class is a basic test for the transaction utility class.
 * @author James Percent
 * 
 */
public class TransactionTest {

    /**
     * Test method for {@link Transaction#getBitSet()}.
     * 
     */
    @Test
    public void testGetBitSet() {
        // fail("Not yet implemented");
        BitSet bitSet = new BitSet(3);
        Hashtable<Integer, Integer> hash = new Hashtable<Integer, Integer>();
        hash.put(0, 0);
        hash.put(1, 1);
        hash.put(2, 2);
        
        bitSet.set(0, true);
        Transaction<Integer> t = new Transaction<Integer>(bitSet, hash, 0);
        BitSet subject = t.getBitSet();
        assertEquals(bitSet, subject);
        assertEquals(subject.get(0), true);
    }

    /**
     * Test method for {@link Transaction#getHash()}.
     */    
    @Test
    public void testHash() {
        Transaction<String> t = new Transaction<String>(0, 3);
        t.addItem(0, "zero");
        t.addItem(1, "one");
        t.addItem(2, "two");

        Hashtable<Integer, String> hash = t.getHash();
        Set<Map.Entry<Integer, String>> hashset = hash.entrySet();
        Iterator<Map.Entry<Integer, String>> i = hashset.iterator();
        while (i.hasNext()) {
            Map.Entry<Integer, String> e = i.next();
            if (e.getKey() == 0 && e.getValue() == "zero") {
                ;
            } else if (e.getKey() == 1 && e.getValue() == "one") {
                ;
            } else if (e.getKey() == 2 && e.getValue() == "two") {
                ;
            } else {
                assertEquals(e.getKey().toString(), e.getValue());
                fail("incorrect values");
            }
        }
    }
}
