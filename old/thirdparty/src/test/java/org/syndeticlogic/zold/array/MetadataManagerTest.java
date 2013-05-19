/**
 * 
 */
package org.syndeticlogic.zold.array;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.File;

import java.util.Collection;

import org.junit.Test;

import org.syndeticlogic.codec.BasicCoder;
import org.syndeticlogic.utility.CompositeKey;
import org.syndeticlogic.utility.FixedLengthArrayGenerator;
import org.syndeticlogic.utility.VariableLengthArrayGenerator;
import org.syndeticlogic.zold.arrays.ArrayDescriptor;
import org.syndeticlogic.zold.arrays.IArrayDescriptor;
import org.syndeticlogic.zold.arrays.MetadataManager;

public class MetadataManagerTest {

    @Test
    public void testMetaData() throws Exception, IOException {
        
        VariableLengthArrayGenerator vcag = new VariableLengthArrayGenerator("target"+System.getProperty("file.separator")+"000", 73, 31);
        vcag.generateFileArray();           
        vcag = new VariableLengthArrayGenerator("target"+System.getProperty("file.separator")+"010", 73, 31);
        vcag.generateFileArray();           
        vcag = new VariableLengthArrayGenerator("target"+System.getProperty("file.separator")+"020", 73, 31);
        vcag.generateFileArray();           
        vcag = new VariableLengthArrayGenerator("target"+System.getProperty("file.separator")+"030", 73, 31);
        vcag.generateFileArray();           
        vcag = new VariableLengthArrayGenerator("target"+System.getProperty("file.separator")+"040", 73, 31);
        vcag.generateFileArray();           
        
        ArrayList<IArrayDescriptor> arrays = new ArrayList<IArrayDescriptor>();
        
        FixedLengthArrayGenerator g = new FixedLengthArrayGenerator("target"+System.getProperty("file.separator")+"001", 1337, 33, 847);
        g.generateFileArray();
        g = new FixedLengthArrayGenerator("target"+System.getProperty("file.separator")+"001"+ArrayDescriptor.ARRAY_DATA_POSTFIX, 1337, 4, 80);
        g.generateFileArray();
        g = new FixedLengthArrayGenerator("target"+System.getProperty("file.separator")+"002"+ArrayDescriptor.ARRAY_DATA_POSTFIX, 1337, 8, 80);
        g.generateFileArray();
        g = new FixedLengthArrayGenerator("target"+System.getProperty("file.separator")+"011"+ArrayDescriptor.ARRAY_DATA_POSTFIX, 1337, 16, 81);
        g.generateFileArray();
        g = new FixedLengthArrayGenerator("target"+System.getProperty("file.separator")+"012"+ArrayDescriptor.ARRAY_DATA_POSTFIX, 1337, 32, 81);
        g.generateFileArray();
        g = new FixedLengthArrayGenerator("target"+System.getProperty("file.separator")+"013"+ArrayDescriptor.ARRAY_DATA_POSTFIX, 1337, 64, 81);
        g.generateFileArray();
        
		CompositeKey id = new CompositeKey();
		id.append(0);
        
        arrays.add(new ArrayDescriptor("target", "0","0","1", id, 4, 80));
        /*arrays.add(IArrayDescriptor.create("target", "0","0","2", id, 8, 80));
        
		id = new CompositeKey();
		id.append(1);
      
        arrays.add(IArrayDescriptor.create("target", "0","1","1", id, 16, 81));
        arrays.add(IArrayDescriptor.create("target", "0","1","2", id, 32, 81));
        arrays.add(IArrayDescriptor.create("target", "0","1","3", id, 64, 81));
  */
        BasicCoder coder = new BasicCoder(null);
        File metaFile = new File("target"+System.getProperty("file.separator")+"vmeta-test-file");
        try {
            MetadataManager vmeta = new MetadataManager(coder,arrays);
            vmeta.write(metaFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            assertTrue(false);
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }

        try {
            MetadataManager vmeta1 = new MetadataManager(coder, 
            		new File("target"+System.getProperty("file.separator")+"vmeta-test-file"));
            Collection<IArrayDescriptor> diskMeta = vmeta1.getArrayDescriptors();
            assertEquals(arrays.size(),diskMeta.size());
            for (IArrayDescriptor array: arrays) {
                int found = 0;
                
                for(IArrayDescriptor diskArray : diskMeta) {
                    if(array.getId().equals(diskArray.getId()) && array.getNamespace().equals(diskArray.getNamespace()) 
                        && array.getSetName().equals(diskArray.getSetName())
                    	&&  array.getArrayName().equals(diskArray.getArrayName())) {
                        found++;
                        assertEquals(array.getLength(), diskArray.getLength());
                        assertTrue(array.createArray() != null);
                    }
                }
                assertEquals(1, found);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            assertTrue(false);
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
