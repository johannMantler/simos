package Scheduler;
import java.util.LinkedList;
import MemoryManagement.PCB;
import MemoryManagement.ProcessManager;
import Hardware.CPU;
import MainBoot.SysLogger;

public class Scheduler implements SchedulerIF {
  
  private LinkedList<EventPCB> blocklist;
  private LinkedList<PCB> readylist;
  private PCB running;
  private PCB idleProcess;
  private CPU cpu;
  private ProcessManager processManager;

  
  /**
   * Erzeugt einen Scheduler.
   * Damit die CPU nie wirklich im Leerlauf ist, laeuft ggf. ein Idle-Pseudoprozess.
   * Dieser hat einen speziellen PCB mit der pid 0 und der Basisadresse -1, damit
   * die CPU ihn erkennen kann.
   * @param cpu der Scheduler muss eine CPU kennen
   * @param processManager Der ProcessManager, damit der Scheduler ggf. Prozesse zerstoeren kann.
   */
  public Scheduler( CPU cpu, ProcessManager processManager ) {
    this.cpu = cpu;
    this.processManager = processManager;
    blocklist = new LinkedList<EventPCB>();
    readylist = new LinkedList<PCB>();
    idleProcess = new PCB( 0, 0, "idle" );

    idleProcess.getRegisterSet().setProgramCounter( null );
    running = idleProcess;
    cpu.restoreRegisters( running.getRegisterSet() );
  }

  /**
   * @return Gibt die ProzessID des aktuell auf der CPU laufenden Prozesses
   * zurueck.
   */
  public int getRunningPid() {
    return running.getPid();
  }

  /**
   * Scheduld den aktuell laufenden Prozess.
   * Der naechste, in der Warteschlange rechenbereite Prozess
   * wird ausgewaehlt.
   */
  public void timesliceOver() {
	  
    if ( ! readylist.isEmpty() ) {    // Ein anderer Prozess ist rechenbereit
     
      SysLogger.writeLog( 0,  "Scheduler.timesliceOver: process " + running.getPid() + " is suspended" );
      
      if( running != idleProcess ) {  // Der Idle-Prozess wird nicht ge-scheduled
        readylist.addLast(running);
        cpu.saveRegisters( running.getRegisterSet() );
      }
      
      running = readylist.get(0); //Hole Prozess aus der Warteschlange
      readylist.remove(0);
      
      SysLogger.writeLog( 0, "Scheduler.timesliceOver: switching to process " + running.getPid() );
      
      cpu.restoreRegisters( running.getRegisterSet() );
      
    } else if( running.getPid() > 0 ) {
    	
      SysLogger.writeLog( 0,  "Scheduler.timesliceOver: no one else is waiting" );
    }
  }
  
  /**
   * Zerstoert den aktuell laufenden Prozess.
   */
  public void endProcess() {
	  
    int pid = running.getPid();
    SysLogger.writeLog( 0, "Scheduler.endProcess: process " + pid + " is ending" );
    // Die aktuellen Registerinhalte werden beim Zerstören noch benötigt.
    cpu.saveRegisters( running.getRegisterSet() );
    processManager.destroyProcess( pid );
    
    for( int i = 0; i < blocklist.size(); i++ ) {
    	
      EventPCB blocked = blocklist.get(i);
      Event event = blocked.getEvent();
      
      if( event.getType() == Event.wait && event.getID() == pid ) {
        readylist.add( blocked.getPCB() );
        blocklist.remove(i);
        SysLogger.writeLog( 0, "Scheduler.endProcess: process " + blocked.getPCB().getPid() + " is returning from sleeping");
      }
    }
    
    if( readylist.isEmpty() ) {
      // Es muss zum Idle-Prozess geschaltet werden
      SysLogger.writeLog( 0, "Scheduler.endProcess: switching to idle process" );
      running = idleProcess;
      cpu.restoreRegisters( running.getRegisterSet() );
    } else {
      running = readylist.get(0);
      readylist.remove(0);
      SysLogger.writeLog( 0, "Scheduler.endProcess: switching to process " + running.getPid() );
      cpu.restoreRegisters( running.getRegisterSet() );
    }
  }
  
  /**
   * Fuegt einen neuen rechenbereiten Prozess in die Warteschlange ein.
   * @param pcb der Prozess als Prozesskontrollblock(PCB)
   */
  public void addProcess( PCB pcb ) {
    pcb.setPriority(4); // Reine Willkür (vorerst)
    pcb.setState("ready");
    readylist.add(pcb);
    SysLogger.writeLog( 0,  "Scheduler.addProcess: new process " + pcb.getPid()
                         + " added, readylist length: " + readylist.size() + "\n" );
  }
  
  
  
  /**
   * Loest die Blockade eines Prozesses.
   * <b> Als Argument darf nur ein abgearbeitetes Event uebergeben werden!
   * </b>
   * Der Prozess wird aus dem Zustand blockiert in den Zustand ready
   * versetzt.
   * 
   */
  public void unblock(Event event) {

    SysLogger.writeLog( 0, "Scheduler.unblock: received event " + event.toString() );
    
    for( int i = 0; i < blocklist.size(); i++ ) {
      EventPCB blocked = blocklist.get(i);
      int type = blocked.getEvent().getType();
      int id = blocked.getEvent().getID();
      if( type == event.getType() && id == event.getID() ) {
        // Wecke den Prozess auf
        readylist.add( blocked.getPCB() );
        blocklist.remove(i);
        SysLogger.writeLog( 0, "Scheduler.unblock: process " + blocked.getPCB().getPid() + " is returning from sleeping");
      }
    }
  }
  
  
  /**
   * Blockiert den aktuell laufenden Prozess. Dieser Prozess
   * wird aus der CPU genommen, seine Register werden gespeichert und dann
   * in die Liste der blockierenden Prozesse eingefuegt.
   * Das Event, warum der Prozess blockiert wurde, muss mit angegeben werden.
   * Anschlieszend wird der neachste rechenbereite Prozess zur Ausfuehrung
   * ausgewaehlt. Falls kein anderer rechenbereiter Prozess existiert,
   * laeuft der idle-Prozess.
   * @param event Das Event, warum der Prozess blockiert wurde.
   */
  public void block( Event event ) {
    int pid = running.getPid();
    SysLogger.writeLog( 0, "Scheduler.block: process " + pid + " is waiting for event " + event.toString() );
    cpu.saveRegisters( running.getRegisterSet() );
    EventPCB blocked = new EventPCB( event, running );
    blocklist.add( blocked );
    
    if( readylist.isEmpty() ) {  // dann muss zum Idle-Prozess geschaltet werden
      
      SysLogger.writeLog( 0, "Scheduler.block: switching to idle process" );
      running = idleProcess;
      cpu.restoreRegisters( running.getRegisterSet() );
      
    } else { //es gibt rechenbereite Prozesse..
        running = readylist.get(0);
        readylist.remove(0);
        SysLogger.writeLog( 0, "Scheduler.block: switching to process " + running.getPid() );
        cpu.restoreRegisters( running.getRegisterSet() );
    }
  }
  
}
