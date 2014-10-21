
package MemoryManagement;
import Hardware.RegisterSet;

/**
 * Repraesentiert einen Prozesskontrollblock als Klasse.
 * 
 */
public class PCB {
  
  private int pid;			//Eigenschaften, die jeder Prozess hat
  private int priority;
  private String state;
  private RegisterSet reg;

  /**
   * Erzeugt einen neuen Prozesskontrollblock.
   * @param pid Die ID-Nummer des Prozesses
   * @param priority Die Prioritaet des Prozesses fuers Scheduling
   * @param state Status als Hilfsinformation fuer den Scheduler
   */
  public PCB( int pid, int priority, String state ) {
    this.priority = priority;
    this.state = state;
    this.pid = pid;
    reg = new RegisterSet();
  }

  public int getPriority(){
    return this.priority;
  }
  public void setPriority(int priority){
    this.priority = priority;
  }

  public int getPid(){
    return this.pid;
  }
  public void setPid(int pid){
    this.pid=pid;
  }

  public String getState(){
    return this.state;
  }
  public void setState(String state){
    this.state = state;
  }

  public RegisterSet getRegisterSet(){
    return reg;
  }
 
  public String toString(){
   return "[pid " + pid + " priority " + priority + " pageTableAddress " + reg.getPageTableAddr() 
   			+ " allocateTableAddress " + reg.getAllocateTableAddr() + "]";
	  
  }

}
