package test.MemoryManagement;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import MainBoot.SysLogger;
import MemoryManagement.SwapFile;

/**
 * 
 * @author Johann Mantler, Hoang Anh Duong
 *
 */
public class SwapFileTest {

	private int swapFileSize = 500;
	
	@Before
	public void setUp() throws Exception {
		SysLogger.openLog();
	}

	@After
	public void tearDown() throws Exception {
		SysLogger.closeLog();
	}
	
	
	@Test
	public final void testSwapFile() {
		//set up
		SwapFile swap;
		
		//exercise
		swap = new SwapFile(this.swapFileSize);
		
		//verify outcome
		Assert.assertNotNull(swap);
	}

	@Test
	public final void testSetGetPage() {
		//set up
		SwapFile swap = new SwapFile(this.swapFileSize);
		
		String[] page1 = new String[]{"10","12","13","14"};
		String[] page2 = new String[]{"20","22","23","24"};
		String[] page3 = new String[]{"30","32","33","34"};
		String[] page4 = new String[]{"40","42","43","44"};
		
		//exercise 
		swap.setPage(page1);
		swap.setPage(page2);
		
		//verify outcome
		Assert.assertTrue(swap.getPage(0)[1].equals("12"));
		Assert.assertTrue(swap.getPage(1)[1].equals("22"));
		
		//exercise 

		swap.setPage(page3);
		swap.setPage(page4);
		swap.setPage(page4);
		
		//verify outcome
		Assert.assertTrue(swap.getPage(0)[0].equals("30"));
		Assert.assertTrue(swap.getPage(1)[0].equals("40"));
		Assert.assertTrue(swap.getPage(2)[3].equals("44"));
		
		
		
	}

}
