package org.syndeticlogic.apriori;

/**
 * Tests Apriori class
 */
import static org.junit.Assert.*;

import org.junit.Test;
import org.syndeticlogic.apriori.Apriori;
import org.syndeticlogic.apriori.Transaction;

import java.util.BitSet;
import java.util.ArrayList;
import java.util.Iterator;

public class AprioriTest {
    public ArrayList<BitSet> mBinaryTransactionDataset;
    /**
     * Setups up a test data set
     */
    public void setup0() {
        /**
         * Tid     A   B  C   D 
         *  0      1   1  1   0 
         *  1      1   1  0   1 
         *  2      1   0  1   1 
         *  3      0   1  1   1
         *  4      1   1  0   0 
         */
        ArrayList<Transaction<String>> transactionDataSet = new ArrayList<Transaction<String>>();
        
        Transaction<String> cursor = new Transaction<String>(0, 4);
        cursor.addItem(0, "A");
        cursor.addItem(1, "B");
        cursor.addItem(2, "C");
        transactionDataSet.add(cursor);

        cursor = new Transaction<String>(1, 4);
        cursor.addItem(0, "A");
        cursor.addItem(1, "B");
        cursor.addItem(3, "D");
        transactionDataSet.add(cursor);
        
        cursor = new Transaction<String>(2, 4);
        cursor.addItem(0, "A");
        cursor.addItem(2, "C");
        cursor.addItem(3, "D");
    
        transactionDataSet.add(cursor);
        
        cursor = new Transaction<String>(3, 4);
        cursor.addItem(1, "B");
        cursor.addItem(2, "C");
        cursor.addItem(3, "D");
        transactionDataSet.add(cursor);

        cursor = new Transaction<String>(3, 4);
        cursor.addItem(0, "A");
        cursor.addItem(1, "B");
        transactionDataSet.add(cursor);

        Iterator<Transaction<String>> iter = transactionDataSet.iterator();
        mBinaryTransactionDataset = new ArrayList<BitSet>();
        while(iter.hasNext()) {
            mBinaryTransactionDataset.add(iter.next().getBitSet());
        }
        
    }
    /**
     * Setups up a test data set
     */    
    public void setup1() {
        
        /**
         * Tid   Bread Milk Diapers Beer Eggs Cola
         *  0      1    1     0     0     0    0
         *  1      1    0     1     1     1    0
         *  2      0    1     1     1     0    1 
         *  3      1    1     1     1     0    0
         *  4      1    1     1     0     0    1 
         */
        ArrayList<Transaction<String>> transactionDataSet = new ArrayList<Transaction<String>>();
        
        Transaction<String> cursor = new Transaction<String>(0, 6);
        cursor.addItem(0, "bread");
        cursor.addItem(1, "milk");
        transactionDataSet.add(cursor);

        cursor = new Transaction<String>(1, 6);
        cursor.addItem(0, "bread");
        cursor.addItem(2, "diapers");
        cursor.addItem(3, "beer");
        cursor.addItem(4, "eggs");
        transactionDataSet.add(cursor);
        
        cursor = new Transaction<String>(2, 6);
        cursor.addItem(1, "milk");
        cursor.addItem(2, "diapers");
        cursor.addItem(3, "beer");
        cursor.addItem(5, "cola");
        transactionDataSet.add(cursor);
        
        cursor = new Transaction<String>(3, 6);
        cursor.addItem(0, "bread");
        cursor.addItem(1, "milk");
        cursor.addItem(2, "diapers");
        cursor.addItem(3, "beer");
        transactionDataSet.add(cursor);
        
        cursor = new Transaction<String>(4, 6);
        cursor.addItem(0, "bread");
        cursor.addItem(1, "milk");
        cursor.addItem(2, "diapers");
        cursor.addItem(5, "cola");
        transactionDataSet.add(cursor);
        
        Iterator<Transaction<String>> iter = transactionDataSet.iterator();
        mBinaryTransactionDataset = new ArrayList<BitSet>();
        while(iter.hasNext()) {
            mBinaryTransactionDataset.add(iter.next().getBitSet());
        }
        
    }

    /**
     * Setups up a test data set
     */
    public void setup2() {
        
        /**
         * Tid   Bread Milk Diapers Beer Eggs Cola
         *  0      1    1     0     0     0    0
         *  1      1    0     1     1     1    0
         *  2      0    1     1     1     0    1 
         *  3      1    1     1     1     0    0
         *  4      1    1     1     0     0    1 
         */
        ArrayList<Transaction<String>> transactionDataSet = new ArrayList<Transaction<String>>();
        
        Transaction<String> cursor = new Transaction<String>(0, 6);
        cursor.addItem(0, "bread");
        cursor.addItem(1, "milk");
        transactionDataSet.add(cursor);

        cursor = new Transaction<String>(1, 6);
        cursor.addItem(0, "bread");
        cursor.addItem(2, "diapers");
        cursor.addItem(3, "beer");
        cursor.addItem(4, "eggs");
        transactionDataSet.add(cursor);
        
        cursor = new Transaction<String>(2, 6);
        cursor.addItem(1, "milk");
        cursor.addItem(2, "diapers");
        cursor.addItem(3, "beer");
        cursor.addItem(5, "cola");
        transactionDataSet.add(cursor);
        
        cursor = new Transaction<String>(3, 6);
        cursor.addItem(0, "bread");
        cursor.addItem(1, "milk");
        cursor.addItem(2, "diapers");
        cursor.addItem(3, "beer");
        transactionDataSet.add(cursor);
        
        cursor = new Transaction<String>(4, 6);
        cursor.addItem(0, "bread");
        cursor.addItem(1, "milk");
        cursor.addItem(2, "diapers");
        cursor.addItem(5, "cola");
        transactionDataSet.add(cursor);
        
        cursor = new Transaction<String>(5, 6);
        cursor.addItem(0, "bread");
        cursor.addItem(1, "milk");
        cursor.addItem(2, "diapers");
        transactionDataSet.add(cursor);

        Iterator<Transaction<String>> iter = transactionDataSet.iterator();
        mBinaryTransactionDataset = new ArrayList<BitSet>();
        while(iter.hasNext()) {
            mBinaryTransactionDataset.add(iter.next().getBitSet());
        }
        
    }

    /**
     * Test method for {@link Apriori#computeFrequentSets()}.
     */
    @Test
    public void testApriori() {

        setup0();
        Apriori apriori = new Apriori(mBinaryTransactionDataset, 0.25, 5);
        ArrayList<ArrayList<BitSet>> frequent = apriori.computeFrequentSets();
        assertEquals(2, frequent.size());
        System.out.println("----- for the transaction data set: ");
        System.out.println(mBinaryTransactionDataset);
        System.out.println("the frequent item sets with support = "+.25+"  are: ");
        printBitSetSet(frequent);
        
        setup1();
        apriori = new Apriori(mBinaryTransactionDataset, 2.0/5.0, 6);
        frequent = apriori.computeFrequentSets();
        assertEquals(3, frequent.size());
        System.out.println("for the transaction data set: ");
        System.out.println(mBinaryTransactionDataset);
        System.out.println("the frequent item sets with support = "+2.0/5.0+"  are: ");
        printBitSetSet(frequent);
                 
        setup2();
        apriori = new Apriori(mBinaryTransactionDataset, 3.0/6.0, 6);
        frequent = apriori.computeFrequentSets(); 
        assertEquals(3, frequent.size());
        System.out.println("\nfor the transaction data set: ");
        System.out.println(mBinaryTransactionDataset);
        System.out.println("\nthe frequent item sets with support = "+3.0/6.0+" are: ");
        printBitSetSet(frequent);
    }        

    private void printBitSetSet(ArrayList<ArrayList<BitSet>> bset) {
        Iterator<ArrayList<BitSet>> bi = bset.iterator();
        while (bi.hasNext()) {
            printBitSet(bi.next());
        }
    }
    /**
     * A debugging utility method to print an Array of BitSets.
     * 
     * @param bset
     */
    private void printBitSet(ArrayList<BitSet> bset) {
        Iterator<BitSet> bi = bset.iterator();
        while (bi.hasNext()) {
            printBitSet(bi.next());
        }
    }

    /**
     * A debugging utility method that prints the elements of a BitSet.
     * 
     * @param bset
     */
    private void printBitSet(BitSet bset) {
        System.out.println("bset = " + bset.toString());
    }
}