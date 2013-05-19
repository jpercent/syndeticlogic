/*
 *    Authtor: James Percent (james@empty-set.net)
 *    Copyright 2010, 2011 James Percent
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.syndeticlogic.zold.array;

import static org.junit.Assert.*;

import org.junit.Test;

import org.syndeticlogic.utility.CompositeKey;
import org.syndeticlogic.utility.FixedLengthArrayGenerator;
import org.syndeticlogic.utility.Util;
import org.syndeticlogic.utility.VariableLengthArrayGenerator;
import org.syndeticlogic.zold.arrays.ArrayDescriptor;
import org.syndeticlogic.zold.arrays.ArrayScanner;
import org.syndeticlogic.zold.arrays.FixedLengthArray;
import org.syndeticlogic.zold.arrays.VariableLengthArray;

public class ArrayDescriptorTest {
	public class ArrayDesTest extends ArrayDescriptor {
		
		public ArrayDesTest(String prefix, String namespace, String setName,
				String arrayName, CompositeKey id, int elementSize, int length) {
			super(prefix, namespace, setName, arrayName, id, elementSize, length);
		}

		public ArrayDesTest(String prefix, byte[] rawMeta) throws Exception {
			super(prefix, rawMeta);
		}
		
		public int elementSize() {
			return elementSize;
		}
	}

    @Test
    public void testFixedLengthDescriptor() throws Exception {
        FixedLengthArrayGenerator g = new FixedLengthArrayGenerator("target"+System.getProperty("file.separator")+"123"+ArrayDescriptor.ARRAY_DATA_POSTFIX, 1337, 33, 847);
        g.generateFileArray();
        
        CompositeKey id = new CompositeKey();
        id.append(73);
        ArrayDesTest arrayDes = new ArrayDesTest("target", "1", "2", "3", id, 33, 847);
        assertEquals(id, arrayDes.getId());
        assertEquals(33, arrayDes.elementSize());
        assertEquals(847, arrayDes.getLength());
        assertEquals("1", arrayDes.getNamespace());
        assertEquals("2", arrayDes.getSetName());
        assertEquals("3", arrayDes.getArrayName());        
        //assertEquals(0x490000001fL, format.getLocator());
        ArrayScanner fa = arrayDes.createArray();
        assertTrue(fa instanceof FixedLengthArray);
        assertEquals(33*847, fa.size());
        
        byte[] meta = arrayDes.serialize();
        ArrayDescriptor arrayDes1 = new ArrayDescriptor("target", meta);
        
        assertEquals(id, arrayDes1.getId());
        //assertEquals(33, arrayDes1.getIndexSize());
        assertEquals(847, arrayDes1.getLength());
        assertEquals("1", arrayDes1.getNamespace());
        assertEquals("2", arrayDes1.getSetName());
        assertEquals("3", arrayDes1.getArrayName());
        //assertEquals(0x490000001fL, format1.getLocator());
        assertTrue(fa instanceof FixedLengthArray);
        assertEquals(33*847, fa.size());
    }

	
	
    @Test
    public void testVariableLengthArrayDescriptor() throws Exception {
        
    	String[] args = {"target"};
    	String path = Util.createPath(args) + "var-test01";
        VariableLengthArrayGenerator vcag = new VariableLengthArrayGenerator(path, 42, 168);
        vcag.generateFileArray();        
        
        //assertEquals(1024, ArrayDescriptor.getFormatSize());
        path = Util.createPath(args);
        CompositeKey id = new CompositeKey();
        id.append(2);
        ArrayDesTest arrayDes = new ArrayDesTest(path, "var-test", "0", "1", id,-1, 3);
        assertEquals(-1, arrayDes.elementSize());
        assertEquals(3, arrayDes.getLength());
        assertEquals(id, arrayDes.getId());
        assertEquals("1", arrayDes.getArrayName());        
        assertEquals("0", arrayDes.getSetName());
        assertEquals("var-test", arrayDes.getNamespace());
        ArrayScanner fa = arrayDes.createArray();
        assertTrue(fa instanceof VariableLengthArray);
        
        byte[] meta = arrayDes.serialize();
        ArrayDescriptor format1 = new ArrayDescriptor("target", meta);
        assertEquals(3, format1.getLength());
        assertEquals(id, format1.getId());
        assertEquals("1", format1.getArrayName());
        assertEquals("0", format1.getSetName());
        assertEquals("var-test", format1.getNamespace());
        fa = arrayDes.createArray();
        assertTrue(fa instanceof VariableLengthArray);
    }
}
