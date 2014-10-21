package test.hardware;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import Hardware.CPU;
import Hardware.MMU;
import Hardware.MainMemory;
import MainBoot.SysLogger;
import MemoryManagement.Clock;
import MemoryManagement.MemoryManager;
import MemoryManagement.ProcessManager;
import MemoryManagement.SwapFile;

public class CPUTest {

	@Before
	public void setUp() throws Exception {
		SysLogger.openLog();
	}

	@After
	public void tearDown() throws Exception {
		SysLogger.closeLog();
	}
	
	
	
	@Test
	public final void testIncVirtualAddress() {
		//set up
		MainMemory memory = new MainMemory(4*8);
		MemoryManager manager = new MemoryManager(memory, new SwapFile(500), new Clock() );
		ProcessManager processManager = new ProcessManager(manager, 4,32);
		MMU mmu = new MMU(memory, manager);
		CPU cpu = new CPU(mmu);
		cpu.setProcessManager(processManager);
		
		//exercise
		String virtualAddr = "90";								
		String result = cpu.incVirtualAddress(virtualAddr);
        SysLogger.writeLog(0, "->" + result);
        
		result = cpu.incVirtualAddress(result);
		SysLogger.writeLog(0, "->" + result);
		
		result = cpu.incVirtualAddress(result);
		SysLogger.writeLog(0, "->" + result);
		
		result = cpu.incVirtualAddress(result);
		SysLogger.writeLog(0, "->" + result);
		
		result = cpu.incVirtualAddress(result);
		SysLogger.writeLog(0, "->" + result);
		//verify outcome
		Assert.assertTrue("101".equals(result));
	}

}
