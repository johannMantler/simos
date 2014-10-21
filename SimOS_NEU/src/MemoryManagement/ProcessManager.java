

package MemoryManagement;
import java.util.Hashtable;
import Scheduler.SchedulerIF;
import Hardware.SysConsole;
import MainBoot.SysLogger;

/**
 * 
 * @author Johann
 *
 */
public class ProcessManager {
  
  private int pageSize;
  private int addressSpaceSize;
  private SchedulerIF scheduler;
  private MemoryManagerIF memoryManager;
  
  private Hashtable<Integer, PCB> PCBTable = new Hashtable<Integer, PCB>();
  private int pidCounter = 0;
  
  static final int initPid = 1;
  
  
  
  /**
   * Der ProzessManager muss den MemoryManager kennen. 
   * @param memoryManager der MemoryManager
   */
  public ProcessManager( MemoryManagerIF memoryManager , int pageSize, int addressSpaceSize) {
    this.memoryManager = memoryManager;
    this.addressSpaceSize = addressSpaceSize;
    this.pageSize = pageSize;
    
    this.memoryManager.setAddressSpaceSize(addressSpaceSize);
    this.memoryManager.setPageSize(pageSize);
  }
  
  


/**
   * Setzt das Scheduler-Objekt des ProcessManagers.
   * @param scheduler Der Scheduler
   */
  public void setScheduler( SchedulerIF scheduler ) {
    this.scheduler = scheduler;
  }
  
  
  /**
   * Erzeugt einen Prozess, der aus einer Datei gelesen wird.
   * <ol>
   * <li>Dabei werden alle seine Seiten aus dem Programmcode der Datei erzeugt,
   * und erstmal in die Auslagerungsdatei geschrieben. Erst wenn die erste Seite
   * gebraucht wird, wird sie durch einen Seitenfehler in den Hauptspeicher geladen
   * ->DemandPaging
   * </li>
   * <li>Der Prozess erhaelt einen neuen Eintrag in der Prozesstabelle
   * und erbt die Konsole seines Vaters.
   * </li>
   * <li>Zum Schluss wird der neue Prozess dem Scheduler bekannt gemacht. 
   * </li>
   * </ol>
   * @param file Der Programmcode des Prozesses wird aus der Datei gelesen
   * @return Gibt die Prozess-ID des erzeugten Prozesses zurueck
   * @see MemoryManager#loadProgram(String, PCB)
   */
  public int createProcess( String file ) {
    
	SysLogger.writeLog( 0, "ProcessManagment.createProcess: creating new Process..");  
	  
	pidCounter++; // PID 0 ist reserviert fuer den Idle-Prozess
    int priority = 0;
    String state = "fresh";
    
    PCB pcb = new PCB( pidCounter, priority, state );
    memoryManager.loadProgram( file, pcb );
    
    if( pidCounter != initPid ) { // ist es nicht der erste Prozess? dann..
    							  // ..neuer Prozess erbt die Konsole seines Erzeugers
      int parentPid = scheduler.getRunningPid();
      PCB parent = PCBTable.get( parentPid );
      SysConsole parentConsole = parent.getRegisterSet().getConsole();
      pcb.getRegisterSet().setConsole( parentConsole );
      parentConsole.incRefCount();
    }
    
    
    PCBTable.put( pidCounter, pcb ); //Eintrag in der Prozesstabelle machen
    
    SysLogger.writeLog( 0, "ProcessManagment.createProcess: " + pcb.toString() + "\n");
    
    scheduler.addProcess( pcb );     // Beim Scheduler anmelden
    return pidCounter;
  }
  
  
  /**
   * Zerstoert den Prozess mit der pid.
   * @param pid Die Prozess-ID, des Prozesses der zerstoert werden soll.
   */
  public void destroyProcess( int pid ) {
	  
    PCB pcb = PCBTable.get( pid );
    SysLogger.writeLog( 0, "ProcessManagment.destroyProcess: " + pcb.toString() );
    
    // Falls der Prozess eine Konsole hatte, wird diese nun zerstoert
    SysConsole console = pcb.getRegisterSet().getConsole();
   
    if( console != null ) {
      console.decRefcount();
      if( console.zeroRefCount() ) {
        SysLogger.writeLog( 0, "ProcessManagment.destroyProcess: destroying console " + console.getId() );
	    console.dispose();
      }
    }
    
    //TODO Scheduler ??
    PCBTable.remove(pid);
  }

  
  public final int getPageSize() {
		return pageSize;
	}


	  public final int getAddressSpaceSize() {
		return addressSpaceSize;
	}

  
}
