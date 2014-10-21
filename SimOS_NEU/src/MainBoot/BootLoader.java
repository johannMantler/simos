package MainBoot;
import MemoryManagement.*;
import Scheduler.*;
import Hardware.*;
import java.io.*;

/**
 * Initialisiert und startet das Betriebssystem.
 *
 */
public class BootLoader {
	
  public static int pageSize = 4;
  public static int frameAnz = 8;
  public static int addressSpace = 32;
  public static int memSize = pageSize * frameAnz;
  public static int swapFileSize = 320; //Platz fuer 10 Prozesse
  
  
  
  
  
  @SuppressWarnings("serial")
  static public class ShutdownException extends Exception{};
  
  /**
   * Die Instanzen fuer die verschiedenen Programmteile werden hier erzeugt
   * und durchgereicht.
   * @param args not used
   * @throws IOException
   */
  public static void main(String [] args) throws IOException {
    
	SysLogger.openLog();
    
	MainMemory memory = new MainMemory( memSize );
    
    MemoryManager memoryManager = new MemoryManager( 
    		memory,
    		new SwapFile(BootLoader.swapFileSize),
    		BootLoader.pageSize,
    		BootLoader.addressSpace,
    		new Clock() );
    
    MMU mmu = new MMU( memory, memoryManager ); // Nur die MMU hat Zugriff auf den Hauptspeicher
    CPU cpu = new CPU( mmu );    //CPU greift ueber die MMU auf den Hauptspeicher zu
    
    ProcessManager processManager = new ProcessManager( memoryManager, BootLoader.pageSize, BootLoader.addressSpace );
    
    
    SchedulerIF scheduler = new Scheduler( cpu, processManager );
    processManager.setScheduler( scheduler );
    
    cpu.setProcessManager( processManager );
    cpu.setScheduler( scheduler );
    
    int pid = processManager.createProcess("init");
    SysLogger.writeLog( 0, "BootLoader: initial process created, pid: " + pid );

    SysLogger.writeLog( 0, "BootLoader: starting the cpu" );
    try {
      cpu.operate();
    } catch( ShutdownException x ) {
      SysLogger.writeLog( 0, "BootLoader: shutting down" );
      processManager.destroyProcess(pid);
      SysLogger.closeLog();
    }
  }
}
