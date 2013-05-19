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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Test;

import org.syndeticlogic.utility.CompositeKey;
import org.syndeticlogic.utility.FixedLengthArrayGenerator;
import org.syndeticlogic.utility.Util;
import org.syndeticlogic.zold.arrays.ArrayDescriptor;
import org.syndeticlogic.zold.arrays.FixedLengthArray;
import org.syndeticlogic.zold.arrays.IArrayDescriptor;

/**
 * @author percent
 * 
 */
public class FixedLengthArrayTest {
	public FixedLengthArrayGenerator[] c;
	public FixedLengthArray[] arrays;
	public IArrayDescriptor[] arrayDescriptors;
	public String path;
	int[] attributeSizes = { 4, 64, 128, 8192, 16384};
	int cardinality;

	@Test
	public void testContents() throws Exception {
		try {
			path = Util.prefixToPath("target");
			arrays = new FixedLengthArray[attributeSizes.length];
			arrayDescriptors = new IArrayDescriptor[attributeSizes.length];
			cardinality = 37;
			c = new FixedLengthArrayGenerator[attributeSizes.length];
			
			CompositeKey id = new CompositeKey();
			id.append(0);
			int seed = 31337;
			for (int i = 0; i < attributeSizes.length; i++) {
				c[i] = new FixedLengthArrayGenerator(
						path + "ArrayTestFile" + i+ArrayDescriptor.ARRAY_DATA_POSTFIX, seed, attributeSizes[i],
						cardinality);
				seed++;
				arrays[i] = c[i].generateFixedLengthArray();

				arrayDescriptors[i] = new ArrayDescriptor(path, "Array",
						"Test", "File" + i, id, attributeSizes[i], cardinality);
				//arrays[i] = new FixedLengthArray(new File(path
                //      + "ArrayTestFile" + i),
                //      ((FixedLengthArrayDescriptor) arrayDescriptors[i])
                //              .getIndexSize());

			}

			for (int i = 0; i < cardinality; i++) {
				compare(i);
			}

		} catch (Exception e) {
			e.printStackTrace();
			
			throw e;
		}
	}

	public void compare(int current) throws IOException {
		for (int k = 0; k < attributeSizes.length; k++) {

			byte[] actual = new byte[attributeSizes[k]];
			boolean ret = arrays[k].scanNextIndex(actual, 0);
			if (current+1 < cardinality)
				assertEquals(true, ret);
			else
				assertEquals(false, ret);

			ArrayList<byte[]> expectedContents = c[k].generateMemoryArray(1);
			assert expectedContents.size() == 1;

			Iterator<byte[]> l = expectedContents.iterator();
			byte[] expected = l.next();
			assertArrayEquals(expected, actual);
		}
	}

	//@Test
	public void testSpeed() throws Exception {
		try {
			cardinality = 8192;
			path = Util.prefixToPath("target");
			c = new FixedLengthArrayGenerator[attributeSizes.length];
			arrays = new FixedLengthArray[attributeSizes.length];
			arrayDescriptors = new IArrayDescriptor[attributeSizes.length];
			long total = 0;
			CompositeKey id = new CompositeKey();
			id.append(0);
			ArrayList<byte[]> actual = new ArrayList<byte[]>(attributeSizes.length);
			for (int i = 0; i < attributeSizes.length; i++) {
				c[i] = new FixedLengthArrayGenerator(path+"ArrayTestFile" + i, 1337, attributeSizes[i],cardinality);
				c[i].generateFileArray();
				
				arrayDescriptors[i] = new ArrayDescriptor("target", "Array", "Test", "File" + i, id, attributeSizes[i], cardinality);
				arrays[i] = new FixedLengthArray(new File(path+"ArrayTestFile"+i), attributeSizes[i]);
				total += cardinality * attributeSizes[i];
				actual.add(new byte[attributeSizes[i]]);
			}

			long begin = System.nanoTime();
			for (int i = 0; i < cardinality; i++) {
				for (int j = 0; j < attributeSizes.length; j++) {
					assert arrays[j].nextIndexSize() == attributeSizes[j];
					arrays[j].scanNextIndex(actual.get(j), 0);
				
				}
			}
			long totalNano = System.nanoTime() - begin;
			long totalSecs = totalNano / 1000000000;
			float MB = ((float) total) / ((float) 1048576);
			float MBPerSec = MB / (float) totalSecs;
			System.out.println("Data in MB = " + MB);
			System.out.println("Response time in seconds = " + totalSecs);
			System.out.println("Throughput in MB/s = " + MBPerSec);

		} catch (Exception e) {
			throw e;
		}
	}
}
