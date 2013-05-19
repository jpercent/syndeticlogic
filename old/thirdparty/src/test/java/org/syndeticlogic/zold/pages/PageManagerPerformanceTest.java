package org.syndeticlogic.zold.pages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class PageManagerPerformanceTest {
	private static final Log log = LogFactory.getLog(PageManagerPerformanceTest.class);
	//public static ApplicationContext context;
	
	@BeforeClass
	public static void loadApplicationContext() {
		//System.setProperty("log4j.properties", "log4j.properties");
		//System.setProperty("log4j.configuration", "log4j.properties");
		//System.out.println("LOg rj org.syndeticlogic.config == "+System.getProperty("log4j.configuration"));
		//context = new FileSystemXmlApplicationContext("src/test/resources/pageManagerPerformacneTest.xml");
	    // open/read the application context file
	  

		
		//context.
		System.out.println("LOg rj org.syndeticlogic.config == "+System.getProperty("log4j.configuration"));
	}
	
	@Test
	public void testPageManager() {
		//ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("pageManagerPerformanceTest.xml");
		//LinkedList fileEventDao = (LinkedList)ctx.getBean("linkedList");

		
		System.out.println("EHERE");
		log.debug("DEBUG logging");
		log.info("info ");
		
	}

}
