package test.hardware;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import Hardware.MMU;
import Hardware.MMU.AccessViolation;
import Hardware.MainMemory;
import MainBoot.BootLoader;
import MainBoot.SysLogger;
import MemoryManagement.Clock;
import MemoryManagement.MemoryManager;
import MemoryManagement.PCB;
import MemoryManagement.SwapFile;

/**
 * 
 * @author Johann Mantler, Hoang Anh Duong
 *
 */
public class MMUTest {

	private int frames = 2;
	private int addrSpace = 32;
	private int pageSize = BootLoader.pageSize;
	private int swapFileSize = 500;
	
	private MainMemory memory = new MainMemory(frames * pageSize);
	private MemoryManager manager = new MemoryManager(memory, new SwapFile(swapFileSize), pageSize, addrSpace, new Clock());
	

	
	@Before
	public void setUp() throws Exception {
		SysLogger.openLog();
	}

	@After
	public void tearDown() throws Exception {
		SysLogger.closeLog();
	}
	
	
	
	@Test
	public final void testMMU() {
		//set up
		MMU mmu;
		
		//exercise
		mmu = new MMU(memory, manager);
		
		//verify outcome
		Assert.assertNotNull(mmu);
	}

	@Test
	public final void testResolveAddress() throws AccessViolation {
		
		this.frames = 2;
		
		
		//set up
		MMU mmu = new MMU(memory, manager);
		PCB pcb = new PCB(1, 0, "fresh");
		manager.loadProgram("init", pcb);
		mmu.setRegisterSet(pcb.getRegisterSet());
		
		Assert.assertTrue("write_val >".equals(mmu.getMemoryCell("03")));
		//exercise
	   int realAddr1 = mmu.resolveAddress("00"); //frameNr = 0 Offset 0
	   int realAddr2 = mmu.resolveAddress("10"); //frameNr = 1 Offset 1
	   int realAddr3 = mmu.resolveAddress("21");//frameNr = 2 Offset 1
	  // SysLogger.writeLog( 0, "dsada"+realAddr2);
	   
		//verify outcome
		Assert.assertTrue(realAddr1 == 0);
		Assert.assertTrue(realAddr2 == 4);
		Assert.assertTrue(realAddr3 == 1);
		
		String instruction = mmu.getMemoryCell("00"); //der Befehl ist schon im RAM->darf kein PageFault geben!
		SysLogger.writeLog( 0, instruction + "");
		//Assert.assertNull(instruction);
		Assert.assertTrue(instruction.equals("create_console"));
		
		
	
	}
	
	
	
	@Test
	public final void testGetMemoryCell() throws AccessViolation {
		//set up
		MMU mmu = new MMU(memory, manager);
		PCB pcb = new PCB(1, 0, "fresh");
		manager.loadProgram("init", pcb);
		
		mmu.setRegisterSet(pcb.getRegisterSet());
		
		//exercise
		String instruction = mmu.getMemoryCell("000");
		
		//verify outcome
		Assert.assertTrue(instruction.equals("create_console"));
	}

	
	
	@Test
	public final void setMemoryCell() throws AccessViolation {
		
		//set up
		MMU mmu = new MMU(memory, manager);
		PCB pcb = new PCB(1, 0, "fresh");
		manager.loadProgram("init", pcb);
		
		mmu.setRegisterSet(pcb.getRegisterSet());
		
		//exercise
		String instruction1 = mmu.getMemoryCell("000");
		String instruction2 = mmu.getMemoryCell("010");
		String instruction3 = mmu.getMemoryCell("020");
		
		mmu.setMemoryCell("000", "1. Befehl");
		String instruction4 = mmu.getMemoryCell("000");
		
		//verify outcome
		Assert.assertTrue(instruction1.equals("create_console"));
		Assert.assertTrue(instruction4.equals("1. Befehl"));
	}
	
}
