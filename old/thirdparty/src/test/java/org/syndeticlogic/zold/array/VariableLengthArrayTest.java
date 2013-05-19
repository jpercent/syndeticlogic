/*
 *    Author: James Percent (james@empty-set.net)
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

package org.syndeticlogic.zold.array;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Test;

import org.syndeticlogic.utility.Util;
import org.syndeticlogic.utility.VariableLengthArrayGenerator;
import org.syndeticlogic.zold.arrays.ArrayDescriptor;
import org.syndeticlogic.zold.arrays.VariableLengthArray;

public class VariableLengthArrayTest {
	public VariableLengthArrayGenerator[] generator;
	public VariableLengthArray[] arrays;
	public int numArrays;
	public int arrayLength;
	public String path;
	
	@Test
	public void test() throws Exception {
		try {
			path = Util.prefixToPath("target")+"VariableLengthArrayTestFile";
			numArrays = 6;
			arrayLength = 80;
			generator = new VariableLengthArrayGenerator[numArrays];
			arrays = new VariableLengthArray[numArrays];  
			for(int i = 0; i < numArrays; i++) {
				generator[i] = new VariableLengthArrayGenerator(path+i, 1337, arrayLength);
				generator[i].generateFileArray();
                arrays[i] = new VariableLengthArray(new File(path+i+ArrayDescriptor.ARRAY_META_POSTFIX), 
                        new File(path+i+ArrayDescriptor.ARRAY_DATA_POSTFIX));
			}						 
			
			for(int i = 0; i < arrayLength; i++) {
				compare(i);
			}
		} catch(Exception e) {
		    e.printStackTrace();
		    throw e;
		}
	}
	
	public void compare(int current) throws Exception {
        for(int k = 0; k < numArrays; k++) {
            int elementSize = arrays[k].nextIndexSize();
            byte[] actual = new byte[elementSize];
            boolean ret = arrays[k].scanNextIndex(actual, 0);
            
            if(current + 1 == arrayLength) 
            	assertEquals(false, ret);
            else
            	assertEquals(true, ret);
            
            ArrayList<byte[]> expectedContents = generator[k].generateMemoryArray(1);
            assert expectedContents.size() == 1;		                    
            Iterator<byte[]> l = expectedContents.iterator();
            byte[] expected = l.next();
            assertArrayEquals(expected, actual);
        }
	}
}
