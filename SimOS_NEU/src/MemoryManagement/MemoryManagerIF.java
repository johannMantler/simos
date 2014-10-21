
package MemoryManagement;

/**
 * Schnittstelle, die die Klasse MemoryManager implementiert, um
 * dem ProzessManager nicht alle seine Methoden zu offenbaren.
 * @author Johann Mantler
 *
 */
public interface MemoryManagerIF {

  /**
   * @see MemoryManager#loadProgram(String, PCB)
   */
  boolean loadProgram( String file, PCB pcb );
  
  
  void setPageSize(int pageSize);
  
  void setAddressSpaceSize(int addressSpaceSize);
  
}
