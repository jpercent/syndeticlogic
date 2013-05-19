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
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import org.syndeticlogic.zold.pages.PageSystemAssembler;
import org.syndeticlogic.utility.CompositeKey;
import org.syndeticlogic.utility.FixedLengthArrayGenerator;
import org.syndeticlogic.utility.Util;
import org.syndeticlogic.zold.arrays.ArrayDescriptor;
import org.syndeticlogic.zold.arrays.FixedLengthArrayScanner;
import org.syndeticlogic.zold.pages.PageVector;
import org.syndeticlogic.zold.pages.PersistentPageManager;

/**
 * @author percent
 * 
 */
public class FixedLengthArrayScannerTest {
	public static final Log log = LogFactory
			.getLog(FixedLengthArrayScannerTest.class);
	public FixedLengthArrayGenerator[] c;
	public FixedLengthArrayScanner[] arrays;
	public FileInputStream[] fi;
	public ArrayDescriptor[] arrayDescriptors;
	public PageSystemAssembler pageSystemAssembler;
	public String path;
	int[] attributeSizes = { 63};//4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384 };
	int cardinality;

	@Test
	public void testContents() throws Exception {
		try {
			pageSystemAssembler = new PageSystemAssembler(
					PageSystemAssembler.BufferPoolMemoryType.Native,
					PageSystemAssembler.CachingPolicy.PinnableLru, null);
			path = Util.prefixToPath("target");
			arrays = new FixedLengthArrayScanner[attributeSizes.length];
			arrayDescriptors = new ArrayDescriptor[attributeSizes.length];
			cardinality = 337;
			c = new FixedLengthArrayGenerator[attributeSizes.length];
			fi = new FileInputStream[attributeSizes.length];

			//int pageSize = 4096;
			int bufferPoolPages = 12800; // 50 MB
			int workingSetSize = 1048576;

			LinkedList<String> filenames = new LinkedList<String>();
			CompositeKey id = new CompositeKey();
			id.append(0);
			int seed = 31337;
			for (int i = 0; i < attributeSizes.length; i++) {
				filenames.add(path + "ArrayTestFile" + i
						+ ArrayDescriptor.ARRAY_DATA_POSTFIX);
				c[i] = new FixedLengthArrayGenerator(path + "ArrayTestFile" + i
						+ ArrayDescriptor.ARRAY_DATA_POSTFIX, seed,
						attributeSizes[i], cardinality);
				c[i].generateFixedLengthArray();
				arrayDescriptors[i] = new ArrayDescriptor(path, "Array",
						"Test", "File" + i, id, attributeSizes[i], cardinality);
				seed++;
				fi[i] = new FileInputStream(path + "ArrayTestFile" + i
						+ ArrayDescriptor.ARRAY_DATA_POSTFIX);

			}

			PersistentPageManager persistentPageManager = pageSystemAssembler.createPersistantPageManager(filenames,
					4096, bufferPoolPages, workingSetSize);

			for (int i = 0; i < attributeSizes.length; i++) {
				PageVector pageVector = persistentPageManager
						.createPageVector(filenames.get(i));
				arrays[i] = new FixedLengthArrayScanner(arrayDescriptors[i],
						pageVector, attributeSizes[i], cardinality);
				// arrays[i] = new FixedLengthArray(new File(path
				// + "ArrayTestFile" + i),
				// ((FixedLengthArrayDescriptor) arrayDescriptors[i])
				// .getIndexSize());
				c[i].reset();
			}

			for (int i = 0; i < cardinality; i++) {
				log.debug("Comparing index = " + i);
				compare(i);
			}

		} catch (Exception e) {
			e.printStackTrace();

			throw e;
		}
	}

	public void dumpData(byte[] expected, byte[] actual, byte[] disk) {
		if (log.isTraceEnabled()) {
			log.debug("Expected --------------------------------------------------------------------------");
			for (int i = 0; i < expected.length; i++) {
				System.out.print(Integer.toHexString(expected[i] & 0xff) + ",");
			}
			System.out
					.println("\nActual--------------------------------------------------------------------------");
			for (int i = 0; i < expected.length; i++) {
				System.out.print(Integer.toHexString(actual[i] & 0xff) + ",");
			}

			System.out
					.println("\nDisk--------------------------------------------------------------------------");
			for (int i = 0; i < expected.length; i++) {
				System.out.print(Integer.toHexString(disk[i] & 0xff) + ",");
			}
			System.out.println("\n\n");
		}

	}

	public void compare(int current) throws Exception {
		for (int k = 0; k < attributeSizes.length; k++) {
			
			byte[] disk = new byte[attributeSizes[k]];
			byte[] actual = new byte[attributeSizes[k]];
			boolean ret = arrays[k].scanNextIndex(actual, 0);
			if (current < cardinality)
				assertEquals(true, ret);
			else
				assertEquals(false, ret);

			ArrayList<byte[]> expectedContents = c[k].generateMemoryArray(1);
			assert expectedContents.size() == 1;
			Iterator<byte[]> l = expectedContents.iterator();
			byte[] expected = l.next();
			fi[k].read(disk);
			dumpData(expected, actual, disk);
			assertArrayEquals(expected, actual);
		}
	}

	// @Test
	public void testSpeed() throws Exception {
		try {

			pageSystemAssembler = new PageSystemAssembler(
					PageSystemAssembler.BufferPoolMemoryType.Native,
					PageSystemAssembler.CachingPolicy.PinnableLru, null);
			path = Util.prefixToPath("target");
			arrays = new FixedLengthArrayScanner[attributeSizes.length];
			arrayDescriptors = new ArrayDescriptor[attributeSizes.length];
			cardinality = 37;
			c = new FixedLengthArrayGenerator[attributeSizes.length];

			//int pageSize = 4096;
			int bufferPoolPages = 12800; // 50 MB
			int workingSetSize = 1048576;

			LinkedList<String> filenames = new LinkedList<String>();
			CompositeKey id = new CompositeKey();
			id.append(0);
			int seed = 31337;
			for (int i = 0; i < attributeSizes.length; i++) {
				filenames.add(path + "ArrayTestFile" + i
						+ ArrayDescriptor.ARRAY_DATA_POSTFIX);
				c[i] = new FixedLengthArrayGenerator(path + "ArrayTestFile" + i
						+ ArrayDescriptor.ARRAY_DATA_POSTFIX, seed,
						attributeSizes[i], cardinality);
				seed++;
				c[i].generateFileArray();
				arrayDescriptors[i] = new ArrayDescriptor(path, "Array",
						"Test", "File" + i, id, attributeSizes[i], cardinality);

			}

			PersistentPageManager persistentPageManager = pageSystemAssembler.createPersistantPageManager(filenames,
					4096, bufferPoolPages, workingSetSize);
			long total = 0;
			ArrayList<byte[]> actual = new ArrayList<byte[]>(
					attributeSizes.length);
			for (int i = 0; i < attributeSizes.length; i++) {
				PageVector pageVector = persistentPageManager
						.createPageVector(filenames.get(i));
				arrays[i] = new FixedLengthArrayScanner(arrayDescriptors[i],
						pageVector, attributeSizes[i], cardinality);
				// arrays[i] = new FixedLengthArray(new File(path
				// + "ArrayTestFile" + i),
				// ((FixedLengthArrayDescriptor) arrayDescriptors[i])
				// .getIndexSize());
				total += cardinality * attributeSizes[i];
				actual.add(new byte[attributeSizes[i]]);
			}

			/*
			 * cardinality = 8192; path = Util.prefixToPath("target"); c = new
			 * FixedLengthArrayGenerator[attributeSizes.length]; arrays = new
			 * FixedLengthArray[attributeSizes.length]; arrayDescriptors = new
			 * IArrayDescriptor[attributeSizes.length]; long total = 0;
			 * CompositeKey id = new CompositeKey(); id.append(0);
			 * ArrayList<byte[]> actual = new
			 * ArrayList<byte[]>(attributeSizes.length); for (int i = 0; i <
			 * attributeSizes.length; i++) { c[i] = new
			 * FixedLengthArrayGenerator(path+"ArrayTestFile" + i, 1337,
			 * attributeSizes[i],cardinality); c[i].generateFileArray();
			 * 
			 * arrayDescriptors[i] = new ArrayDescriptor("target", "Array",
			 * "Test", "File" + i, id, attributeSizes[i], cardinality);
			 * arrays[i] = new FixedLengthArray(new
			 * File(path+"ArrayTestFile"+i), attributeSizes[i]);
			 * 
			 * }
			 */
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
