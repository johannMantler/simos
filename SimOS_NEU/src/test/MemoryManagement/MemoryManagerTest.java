package test.MemoryManagement;


import java.io.IOException;
import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import Hardware.MainMemory;
import MainBoot.SysLogger;
import MemoryManagement.Clock;
import MemoryManagement.MemoryManager;
import MemoryManagement.PCB;
import MemoryManagement.PageTableEntry;
import MemoryManagement.SwapFile;

/**
 * 
 * @author Johann Mantler, Hoang Anh Duong
 *
 */
public class MemoryManagerTest {

	private int frames = 8;				//Testdaten
	private int addrSpace = 32;
	private int pageSize = 4;
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
	public final void testMemoryManager() {
		//set up
		MemoryManager m;
	
		//exercise
		m = new MemoryManager(new MainMemory(4*8), new SwapFile(500), 4, 32, new Clock());
		
		//verify outcome
		Assert.assertNotNull(m);
	} 

	@Test
	public final void testLoadProgram() throws IOException {
		//set up
		SwapFile swapFile = new SwapFile(this.swapFileSize);
		MemoryManager m = new MemoryManager(new MainMemory(4*8), swapFile, 4, 32, new Clock());
	    int pid = 1;
		
	    PCB pcb = new PCB( 1, 0, "fresh" );
		//exercise
		m.loadProgram("init", pcb);
		
		int allocateTableAddr = pcb.getRegisterSet().getAllocateTableAddr();
		ArrayList<Integer> allocateTable = m.getAllocateTable(allocateTableAddr);
		
		//verify outcome
		Assert.assertTrue( m.getPageTable(pid).size() == 32 ); //Teste ob die Seitentabelle auch 32 Seiten grosz ist
	
		String[] page1 = swapFile.getPage(allocateTable.get(0));	//Teste Seite 1
		
		
		Assert.assertTrue(page1[0].equals("create_console")); //Teste, ob dieser Befehl auch im SwapFile liegt
		
		//Assert.assertTrue(page1[1].equals("write_val Moin\0020Moin"));
		Assert.assertTrue(page1[2].equals("write_nl"));
		Assert.assertTrue(page1[3].equals("write_val >"));
		
		
		String[] page2 = swapFile.getPage(allocateTable.get(1));
	
		Assert.assertTrue(page2[0].equals("read 20"));
		Assert.assertTrue(page2[1].equals("load 1 20"));
		Assert.assertTrue(page2[2].equals("load 2 #exit"));
		Assert.assertTrue(page2[3].equals("jeq 11"));
		
	
		String[] page3 = swapFile.getPage(allocateTable.get(2));
		
		Assert.assertTrue(page3[0].equals("create_process 20"));
		Assert.assertTrue(page3[1].equals("wait 1"));
		Assert.assertTrue(page3[2].equals("jmp 3"));
		Assert.assertTrue(page3[3].equals("quit"));
		
		String[] page5 = swapFile.getPage(allocateTable.get(4));
		
		Assert.assertTrue(page5[0].equals(""));
		Assert.assertTrue(page5[1].equals(""));
		Assert.assertTrue(page5[2].equals(""));
		Assert.assertTrue(page5[3].equals(""));
		
		String[] page32 = swapFile.getPage(allocateTable.get(31));
		
		Assert.assertTrue(page32[0].equals(""));
		Assert.assertTrue(page32[1].equals(""));
		Assert.assertTrue(page32[2].equals(""));
		Assert.assertTrue(page32[3].equals(""));
	
	
	}
	
	
	@Test
	public final void testPageFault() {
		//set up
		
		int frameAnzahl = 2;
		MainMemory memory = new MainMemory(4*frameAnzahl);
		MemoryManager m = new MemoryManager(memory, new SwapFile(500), 4, 32, new Clock());
		
		PCB pcb = new PCB( 1, 0, "fresh" );
		m.loadProgram("init", pcb);
		
		int pageTableAddr = pcb.getRegisterSet().getPageTableAddr();
		int allocateTableAddr = pcb.getRegisterSet().getAllocateTableAddr();
		
		
		//exercise and verify outcome
		int pageNr = 0; 					//Die Seite, die den pageFault ausloest
		PageTableEntry entry = m.getPageTable(pageTableAddr).get(pageNr);
	
		m.pageFault(pageNr, entry, allocateTableAddr, pageTableAddr);
		
		Assert.assertTrue(memory.getContent(0).equals("create_console"));
		//Assert.assertTrue(memory.getContent(1).equals("write_val Moin\0020Moin"));
		Assert.assertTrue(memory.getContent(2).equals("write_nl"));
		Assert.assertTrue(memory.getContent(3).equals("write_val >"));
		Assert.assertTrue(m.getPageTable(pageTableAddr).get(pageNr).getFrameNr() == 0);
		Assert.assertTrue(m.getAllocateTable(allocateTableAddr).get(pageNr) == -1);
		
		
		
		pageNr = 1; 					//Die Seite, die den pageFault ausloest
		entry = m.getPageTable(pageTableAddr).get(pageNr);
	
		m.pageFault(pageNr, entry, allocateTableAddr, pageTableAddr);
		
		Assert.assertTrue(memory.getContent(4).equals("read 20"));
		Assert.assertTrue(memory.getContent(5).equals("load 1 20"));
		Assert.assertTrue(memory.getContent(6).equals("load 2 #exit"));
		Assert.assertTrue(memory.getContent(7).equals("jeq 11"));
		Assert.assertTrue(m.getPageTable(pageTableAddr).get(pageNr).getFrameNr() == 1);
		Assert.assertTrue(m.getAllocateTable(allocateTableAddr).get(pageNr) == -1);
	
		
		
		
		
		
		pageNr = 2; 					//Die Seite, die den pageFault ausloest
		entry = m.getPageTable(pageTableAddr).get(pageNr);
	
		m.pageFault(pageNr, entry, allocateTableAddr, pageTableAddr);
		
		Assert.assertTrue(memory.getContent(0).equals("create_process 20"));
		Assert.assertTrue(memory.getContent(1).equals("wait 1"));
		Assert.assertTrue(memory.getContent(2).equals("jmp 3"));
		Assert.assertTrue(memory.getContent(3).equals("quit"));
		Assert.assertTrue(m.getPageTable(pageTableAddr).get(pageNr).getFrameNr() == 0);
		Assert.assertTrue(m.getAllocateTable(allocateTableAddr).get(pageNr) == -1);

	}

}
