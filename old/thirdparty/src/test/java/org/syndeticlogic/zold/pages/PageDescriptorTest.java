package org.syndeticlogic.zold.pages;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.PriorityQueue;

import org.junit.Test;
import org.syndeticlogic.zold.pages.PageDescriptor;
import org.syndeticlogic.zold.pages.PageSystemAssembler;
import org.syndeticlogic.utility.FixedLengthArrayGenerator;
import org.syndeticlogic.utility.Util;

public class PageDescriptorTest {
	
	//@Test
	public void testJavaEqualsCompareTo() throws Exception {
		String filename = Util.prefixToPath("target") + "pagedescriptortest";
		int pageSize = 4096;
		int seed = 42;
		int elementSize = 8;
		int length = 313;
		PageSystemAssembler pageSystemAssembler = new PageSystemAssembler(PageSystemAssembler.BufferPoolMemoryType.Java, PageSystemAssembler.CachingPolicy.PinnableLru, 
		                                                                  PageSystemAssembler.PageDescriptorType.Synchronized);
		FixedLengthArrayGenerator gen = new FixedLengthArrayGenerator(filename, seed, elementSize, length);
		gen.generateFileArray();
		
		ByteBuffer buf1 = ByteBuffer.allocate(1024);
		PageDescriptor pdes2 = pageSystemAssembler.createPageDescriptor(filename, 2*pageSize, length);
		PageDescriptor pdes1 = pageSystemAssembler.createPageDescriptor(filename, pageSize, length);
		PageDescriptor pdes = pageSystemAssembler.createPageDescriptor(filename, 0, length);
		
		pdes.attachBuffer(buf1);
		pdes1.attachBuffer(buf1);
		pdes2.attachBuffer(buf1);
		
		assertEquals(-1, pdes.compareTo(pdes1));
		assertEquals(-1, pdes.compareTo(pdes2));
		assertEquals(0, pdes.compareTo(pdes));
		assertEquals(1, pdes1.compareTo(pdes));
		assertEquals(1, pdes2.compareTo(pdes));
	
		PriorityQueue<PageDescriptor> p = new PriorityQueue<PageDescriptor>();
		p.add(pdes2);
		p.add(pdes1);
		p.add(pdes);

		assertEquals(pdes, p.poll());
		assertEquals(pdes1, p.poll());
		assertEquals(pdes2, p.poll());
		assertEquals(null, p.poll());
	}
		
	@Test
	public void testJavaReadWrite() throws Exception {
		
		ByteBuffer buf1 = ByteBuffer.allocate(8192);
		System.out.println("buf capacity"+buf1.capacity());
		System.out.println("buf "+buf1.limit());
		System.out.println("buf "+buf1.position());
		System.out.println("buf "+buf1.remaining());
		//System.out.println("buf "+buf.);
		
		String filename = Util.prefixToPath("target") + "pagedescriptortest";
		int pageSize = 4096;
		int seed = 42;
		int elementSize = 8;
		int length = 313;
		FixedLengthArrayGenerator gen = new FixedLengthArrayGenerator(filename, seed, elementSize, length);
		gen.generateFileArray();
		
		PageSystemAssembler pageSystemAssembler = new PageSystemAssembler(PageSystemAssembler.BufferPoolMemoryType.Java, 
		        PageSystemAssembler.CachingPolicy.PinnableLru, PageSystemAssembler.PageDescriptorType.Synchronized);
		
		PageDescriptor pdes = pageSystemAssembler.createPageDescriptor(filename, 0, pageSize);
		ByteBuffer buf = ByteBuffer.allocate(pageSize);
		pdes.attachBuffer(buf);
	
		assertEquals(pageSize,buf.capacity());
		assertEquals(0, buf.position());
			
		byte[] bytebag = new byte[8];
		int pageOffset = 0;
		List<byte[]> bytes = gen.generateMemoryArray(length);
		
		for(int i=0; i < length; i++, pageOffset+=8) {
			pdes.read(bytebag, 0, pageOffset, elementSize);
			assertArrayEquals(bytebag, bytes.get(i));
			if(i == seed) {
				bytebag[0] = 0xd;
				bytebag[1] = 0xe;
				bytebag[2] = 0xa;
				bytebag[3] = 0xd;
				bytebag[4] = 0xb;
				bytebag[5] = 0xe;
				bytebag[6] = 0xe;				
				bytebag[7] = 0xf;
				pdes.write(bytebag, 0, pageOffset, elementSize);
			}
			for(int j = 0; j < elementSize; j++) 
				bytebag[j] = 0;
		}
		
		pdes.detachBuffer();
		PageDescriptor pdes1 = pageSystemAssembler.createPageDescriptor(filename, 0, pageSize);
		buf = ByteBuffer.allocate(pageSize);
		pdes1.attachBuffer(buf);
		assertEquals(pageSize,buf.capacity());
		assertEquals(0, buf.position());
		
		pageOffset = 0;
		gen.reset();
		bytes = gen.generateMemoryArray(length);
		
		for(int i=0; i < length; i++, pageOffset+=8) {
			pdes1.read(bytebag, 0, pageOffset, elementSize);
			if(i == seed) {
				byte[] writeTest = {0xd,0xe,0xa,0xd,0xb,0xe,0xe,0xf};
				assertArrayEquals(bytebag, writeTest);
			} else {
				assertArrayEquals(bytebag, bytes.get(i));
			}
			
			if(i == seed+1) {
				bytebag[0] = 0xd;
				bytebag[1] = 0xe;
				bytebag[2] = 0xa;
				bytebag[3] = 0xd;
				bytebag[4] = 0xb;
				bytebag[5] = 0xe;
				bytebag[6] = 0xe;				
				bytebag[7] = 0xf;
				pdes1.write(bytebag, 0, pageOffset, elementSize);
			}				
			for(int j = 0; j < elementSize; j++) 
				bytebag[j] = 0;
		}
		pdes1.flush();
		assertEquals(false, pdes1.isDirty());
		pdes = null;
		
		PageDescriptor pdes2 = pageSystemAssembler.createPageDescriptor(filename, 0, pageSize);
		buf = ByteBuffer.allocate(pageSize);
		pdes2.attachBuffer(buf);
		
		assertEquals(pageSize,buf.capacity());
		assertEquals(0, buf.position());
		
		pageOffset = 0;
		gen.reset();
		bytes = gen.generateMemoryArray(length);
		
		for(int i=0; i < length; i++, pageOffset+=8) {
			pdes2.read(bytebag, 0, pageOffset, elementSize);
			
			if(i == seed || i == seed+1) {
				byte[] writeTest = {0xd,0xe,0xa,0xd,0xb,0xe,0xe,0xf};
				assertArrayEquals(bytebag, writeTest);
			} else {
				assertArrayEquals(bytebag, bytes.get(i));
			}
			
			for(int j = 0; j < elementSize; j++) 
				bytebag[j] = 0;
		}
	}

	//@Test
	public void testNativeEqualsCompareTo() throws Exception {
		String filename = Util.prefixToPath("target") + "pagedescriptortest";
		int pageSize = 4096;
		int seed = 42;
		int elementSize = 8;
		int length = 313;
		PageSystemAssembler pageSystemAssembler = new PageSystemAssembler(PageSystemAssembler.BufferPoolMemoryType.Native, PageSystemAssembler.CachingPolicy.PinnableLru, PageSystemAssembler.PageDescriptorType.Synchronized);
		FixedLengthArrayGenerator gen = new FixedLengthArrayGenerator(filename, seed, elementSize, length);
		gen.generateFileArray();
		
		ByteBuffer buf1 = ByteBuffer.allocateDirect(1024);
		PageDescriptor pdes2 = pageSystemAssembler.createPageDescriptor(filename, 2*pageSize, length);
		PageDescriptor pdes1 = pageSystemAssembler.createPageDescriptor(filename, pageSize, length);
		PageDescriptor pdes = pageSystemAssembler.createPageDescriptor(filename, 0, length);
		
		pdes.attachBuffer(buf1);
		pdes1.attachBuffer(buf1);
		pdes2.attachBuffer(buf1);
		
		assertEquals(-1, pdes.compareTo(pdes1));
		assertEquals(-1, pdes.compareTo(pdes2));
		assertEquals(0, pdes.compareTo(pdes));
		assertEquals(1, pdes1.compareTo(pdes));
		assertEquals(1, pdes2.compareTo(pdes));
	
		PriorityQueue<PageDescriptor> p = new PriorityQueue<PageDescriptor>();
		p.add(pdes2);
		p.add(pdes1);
		p.add(pdes);

		assertEquals(pdes, p.poll());
		assertEquals(pdes1, p.poll());
		assertEquals(pdes2, p.poll());
		assertEquals(null, p.poll());
	}
	
	//@Test
	public void testNativeReadWrite() throws Exception {
		
		ByteBuffer buf1 = ByteBuffer.allocateDirect(8192);
		System.out.println("buf capacity"+buf1.capacity());
		System.out.println("buf "+buf1.limit());
		System.out.println("buf "+buf1.position());
		System.out.println("buf "+buf1.remaining());
		//System.out.println("buf "+buf.);
		
		String filename = Util.prefixToPath("target") + "pagedescriptortest";
		int pageSize = 4096;
		int seed = 42;
		int elementSize = 8;
		int length = 313;
		FixedLengthArrayGenerator gen = new FixedLengthArrayGenerator(filename, seed, elementSize, length);
		gen.generateFileArray();
		
		PageSystemAssembler pageSystemAssembler = new PageSystemAssembler(PageSystemAssembler.BufferPoolMemoryType.Native, PageSystemAssembler.CachingPolicy.PinnableLru, PageSystemAssembler.PageDescriptorType.Synchronized);
		
		PageDescriptor pdes = pageSystemAssembler.createPageDescriptor(filename, 0, pageSize);
		ByteBuffer buf = ByteBuffer.allocateDirect(pageSize);
		pdes.attachBuffer(buf);
	
		assertEquals(pageSize,buf.capacity());
		assertEquals(0, buf.position());
			
		byte[] bytebag = new byte[8];
		int pageOffset = 0;
		List<byte[]> bytes = gen.generateMemoryArray(length);
		
		for(int i=0; i < length; i++, pageOffset+=8) {
			pdes.read(bytebag, 0, pageOffset, elementSize);
			assertArrayEquals(bytebag, bytes.get(i));
			if(i == seed) {
				bytebag[0] = 0xd;
				bytebag[1] = 0xe;
				bytebag[2] = 0xa;
				bytebag[3] = 0xd;
				bytebag[4] = 0xb;
				bytebag[5] = 0xe;
				bytebag[6] = 0xe;				
				bytebag[7] = 0xf;
				pdes.write(bytebag, 0, pageOffset, elementSize);
			}
			for(int j = 0; j < elementSize; j++) 
				bytebag[j] = 0;
		}
		
		pdes.detachBuffer();
		PageDescriptor pdes1 = pageSystemAssembler.createPageDescriptor(filename, 0, pageSize);
		buf = ByteBuffer.allocateDirect(pageSize);
		pdes1.attachBuffer(buf);
		assertEquals(pageSize,buf.capacity());
		assertEquals(0, buf.position());
		
		pageOffset = 0;
		gen.reset();
		bytes = gen.generateMemoryArray(length);
		
		for(int i=0; i < length; i++, pageOffset+=8) {
			pdes1.read(bytebag, 0, pageOffset, elementSize);
			if(i == seed) {
				byte[] writeTest = {0xd,0xe,0xa,0xd,0xb,0xe,0xe,0xf};
				assertArrayEquals(bytebag, writeTest);
			} else {
				assertArrayEquals(bytebag, bytes.get(i));
			}
			
			if(i == seed+1) {
				bytebag[0] = 0xd;
				bytebag[1] = 0xe;
				bytebag[2] = 0xa;
				bytebag[3] = 0xd;
				bytebag[4] = 0xb;
				bytebag[5] = 0xe;
				bytebag[6] = 0xe;				
				bytebag[7] = 0xf;
				pdes1.write(bytebag, 0, pageOffset, elementSize);
			}				
			for(int j = 0; j < elementSize; j++) 
				bytebag[j] = 0;
		}
		pdes1.flush();
		assertEquals(false, pdes1.isDirty());
		pdes = null;
		
		PageDescriptor pdes2 = pageSystemAssembler.createPageDescriptor(filename, 0, pageSize);
		buf = ByteBuffer.allocateDirect(pageSize);
		pdes2.attachBuffer(buf);
		
		assertEquals(pageSize,buf.capacity());
		assertEquals(0, buf.position());
		
		pageOffset = 0;
		gen.reset();
		bytes = gen.generateMemoryArray(length);
		
		for(int i=0; i < length; i++, pageOffset+=8) {
			pdes2.read(bytebag, 0, pageOffset, elementSize);
			
			if(i == seed || i == seed+1) {
				byte[] writeTest = {0xd,0xe,0xa,0xd,0xb,0xe,0xe,0xf};
				assertArrayEquals(bytebag, writeTest);
			} else {
				assertArrayEquals(bytebag, bytes.get(i));
			}
			
			for(int j = 0; j < elementSize; j++) 
				bytebag[j] = 0;
		}
	}
	
	//@Test
	public void testNativeReadParameters() throws Exception {


		int pageSize = 64;
		int seed = 42;
		int elementSize = 63;
		int length = 2;
		
		String filename = Util.prefixToPath("target") + "pagedescriptortest";
		
		FixedLengthArrayGenerator gen = new FixedLengthArrayGenerator(filename, seed, elementSize, length);
		gen.generateFileArray();	
		
		PageSystemAssembler pageSystemAssembler = new PageSystemAssembler(PageSystemAssembler.BufferPoolMemoryType.Native, PageSystemAssembler.CachingPolicy.PinnableLru, PageSystemAssembler.PageDescriptorType.Synchronized);
		PageDescriptor pdes = pageSystemAssembler.createPageDescriptor(filename, 0, pageSize);
		ByteBuffer buf = ByteBuffer.allocateDirect(pageSize);		
		pdes.attachBuffer(buf);
		
		byte[] bytebag = new byte[elementSize];
		int pageOffset = 0;
		List<byte[]> bytes = gen.generateMemoryArray(length);
		
		int read = pdes.read(bytebag, 0, pageOffset, elementSize);
		assertEquals(63, read);
		assertArrayEquals(bytes.get(0), bytebag); 
		assertTrue(bytes.get(1)[0] != bytebag[0]);
		
		read = pdes.read(bytebag, 0,  63, 1);
		assertEquals(1, read);
		
		read = pdes.read(bytebag, 0,  64, 1);
		assertEquals(0, read);
		
		assertEquals(bytes.get(1)[0], bytebag[0]);
		pdes.detachBuffer();
		pdes = pageSystemAssembler.createPageDescriptor(filename, pageSize, pageSize);
		pdes.attachBuffer(buf);
		read = pdes.read(bytebag, 0,  0, 62);
		assertEquals(62, read);
		
		for(int i = 1, j = 0; j < 62; i++, j++) {
			assertEquals(bytes.get(1)[i], bytebag[j]);
		}
	}	
	
	
	//@Test
	public void testJavaReadParameters() throws Exception {


		int pageSize = 64;
		int seed = 42;
		int elementSize = 63;
		int length = 2;
		
		String filename = Util.prefixToPath("target") + "pagedescriptortest";
		
		FixedLengthArrayGenerator gen = new FixedLengthArrayGenerator(filename, seed, elementSize, length);
		gen.generateFileArray();	
		
		PageSystemAssembler pageSystemAssembler = new PageSystemAssembler(PageSystemAssembler.BufferPoolMemoryType.Java, PageSystemAssembler.CachingPolicy.PinnableLru, PageSystemAssembler.PageDescriptorType.Synchronized);
		PageDescriptor pdes = pageSystemAssembler.createPageDescriptor(filename, 0, pageSize);
		ByteBuffer buf = ByteBuffer.allocate(pageSize);		
		pdes.attachBuffer(buf);
		
		byte[] bytebag = new byte[elementSize];
		int pageOffset = 0;
		List<byte[]> bytes = gen.generateMemoryArray(length);
		
		int read = pdes.read(bytebag, 0, pageOffset, elementSize);
		assertEquals(63, read);
		assertArrayEquals(bytes.get(0), bytebag); 
		assertTrue(bytes.get(1)[0] != bytebag[0]);
		
		read = pdes.read(bytebag, 0,  63, 1);
		assertEquals(1, read);
		
		read = pdes.read(bytebag, 0,  64, 1);
		assertEquals(0, read);
		
		assertEquals(bytes.get(1)[0], bytebag[0]);
		pdes.detachBuffer();
		pdes = pageSystemAssembler.createPageDescriptor(filename, pageSize, pageSize);
		pdes.attachBuffer(buf);
		read = pdes.read(bytebag, 0,  0, 62);
		assertEquals(62, read);
		
		for(int i = 1, j = 0; j < 62; i++, j++) {
			assertEquals(bytes.get(1)[i], bytebag[j]);
		}
	}	

	
	
	@Test
	public void testJavaWriteParameters() throws Exception {
	}	

}
