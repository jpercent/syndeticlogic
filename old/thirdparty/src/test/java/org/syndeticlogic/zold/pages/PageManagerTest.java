package org.syndeticlogic.zold.pages;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.syndeticlogic.zold.pages.PageDescriptor;
import org.syndeticlogic.zold.pages.PageSystemAssembler;
import org.syndeticlogic.utility.FixedLengthArrayGenerator;
import org.syndeticlogic.utility.Util;
import org.syndeticlogic.zold.pages.PageVector;
import org.syndeticlogic.zold.pages.PersistentPageManager;

public class PageManagerTest {
	private static final Log log = LogFactory.getLog(PageManagerTest.class);

	//@Test
	public void testEvicted() {
		// fail("Not yet implemented");
	}

	//@Test
	public void test1() throws Exception {
		int pageSize = 64;
		int pages = 2;
		int seed = 42;
		int elementSize = 8;
		int length = 9;
		int workingSetSize = pages / 2 * elementSize;
		testLoadWorkingSet(pageSize, pages, seed, elementSize, length,
				workingSetSize);
	}

	//@Test
	public void test2() throws Exception {
		int pageSize = 64;
		int pages = 10;
		int seed = 42;
		int elementSize = 8;
		int length = 80;
		int workingSetSize = pageSize * 3;
		testLoadWorkingSet(pageSize, pages, seed, elementSize, length,
				workingSetSize);
	}

	//@Test
	public void test3() throws Exception {
		int pageSize = 4096;
		int pages = 100;
		int seed = 42;
		int elementSize = 4096;
		int length = 74;
		int workingSetSize = pageSize * 3;
		testLoadWorkingSet(pageSize, pages, seed, elementSize, length,
				workingSetSize);
	}

	public void testLoadWorkingSet(int pageSize, int pages, int seed,
			int elementSize, int length, int workingSetSize) throws Exception {
		List<String> files = new LinkedList<String>();
		String fileName = Util.prefixToPath("target") + "pagecachetest";

		FixedLengthArrayGenerator gen = new FixedLengthArrayGenerator(fileName,
				seed, elementSize, length);
		gen.generateFileArray();

		files.add(fileName);
		PageSystemAssembler pageSystemAssembler = new PageSystemAssembler(
				PageSystemAssembler.BufferPoolMemoryType.Java,
				PageSystemAssembler.CachingPolicy.PinnableLru, PageSystemAssembler.PageDescriptorType.Synchronized);
		PersistentPageManager pc = pageSystemAssembler.createPersistantPageManager(files, pageSize,
				pages, workingSetSize);

		PageVector pi = pc.createPageVector(fileName);
		PageDescriptor pdes = null;
		byte[] actual = new byte[elementSize];
		byte[] disk = new byte[elementSize];
		int totalBytes = elementSize * length;
		PriorityQueue<PageDescriptor> pq = new PriorityQueue<PageDescriptor>();
		FileInputStream fi = new FileInputStream(new File(fileName));
		for (int i = 0; i < pages; i++) {
			pdes = pi.next();
			assertEquals(true, pdes.isAssigned());
			log.trace("page number" + i);

			for (int j = 0; j < pageSize; j += elementSize) {
				byte[] expected = gen.generateMemoryArray(1).get(0);
				System.out.println(j + ", " + elementSize);
				/* int read = */pdes.read(actual, 0, j, elementSize);

				if (log.isTraceEnabled()) {
					System.out.println("Expected --------------------------------------------------------------------------");
					for (int k = 0; k < elementSize; k++) {
						System.out.print(Integer
								.toHexString(expected[k] & 0xff) + ",");
					}
					System.out.println();
					System.out.println("\nActual--------------------------------------------------------------------------");
					for (int k = 0; k < elementSize; k++) {
						System.out.print(Integer.toHexString(actual[k] & 0xff)
								+ ",");
						// actual[k] = 0;
					}
					fi.read(disk);
					System.out.println("\nDisk--------------------------------------------------------------------------");
					for (int f = 0; f < elementSize; f++) {
						System.out.print(Integer.toHexString(disk[f] & 0xff)
								+ ",");
					}
					System.out.println("\n\n");
					System.out.println(pq);
				}
				assertArrayEquals(expected, actual);
				for (int k = 0; k < elementSize; k++) {
					actual[k] = 0;
				}
				if ((i * pageSize + j) + elementSize == totalBytes) {
					i = pages;
					break;
				}
			}
			pi.release(pdes);
		}
	}
}
