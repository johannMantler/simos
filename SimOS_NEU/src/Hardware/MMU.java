

package Hardware;

import java.util.ArrayList;

import MainBoot.SysLogger;
import MemoryManagement.MemoryManager;
import MemoryManagement.PageTableEntry;

/**
 * <b>Aufgabe:</b> Uebernimmt die Umwandlung der virtuellen Adresse in die physische.
 * <p>
 * Die MMU stellt der CPU Methoden zur Verfuegung um auf den Hauptspeicher zuzugreifen, das
 * Heiszt die MMU stellt eine Art Zwischenschicht zwischen CPU und Hauptspeicher dar.
 * Die Virtuellen Adressen die die MMU von der CPU bekommt um eine Speicherzelle zu adressieren, werden
 * <ol>
 * <li>in Seitennummer und Offset aufgeteilt</li>
 * <li>dann anhand der Seitentabelle und der Seitennummer oder einem Seitenfehler die Rahmennummer ermittelt</li>
 * <li>und anschlieszend aus Rahmennummer und Offset die physische Adresse berechnet</li>
 * <ol>
 * </p>
 * 
 * @author Johann Mantler, Hoang Anh Duong, Hicham Belmoquadem
 *
 */
public class MMU {
	
  private MainMemory memory;		//Hauptspeicher
  private RegisterSet regSet;		//Das Registerset, wird von der CPU gesetzt
  private int offsetLength;	        //Stellenanzahl der Seitengroesze zb Seitengroesze: 5, stellenAnz: 1
  private int pageSize;				//Groesze einer Seite
  private int addrSpace;			//Groesze des virtuellen Adressraumes in Anzahl der Seiten
  private MemoryManager manager;	//Hauptspeicher-Verwalter-Objekt

  
  
  
  /**
   * Eine eigene Exception - Klasse, die immer dann geworfen wird,
   * wenn eine falsche Speicheraddresse, die nicht existiert, angegeben wird.
   */
  @SuppressWarnings("serial")
  static public class AccessViolation extends Exception{};
  
  
  
  
  /**
   * Bei der Erzeugung der MMU wird der Hauptspeicher mit angegeben.
   * @param memory der Hauptspeicher.
   */
  public MMU( MainMemory memory, MemoryManager m ) {
    this.memory = memory;
    this.pageSize = m.getPageSize();
    this.offsetLength = new Integer(pageSize).toString().length();
    this.addrSpace = m.getAddressSpaceSize();
    this.manager = m;
  }
  
  
  
  /**
   * Setzt die Register. In den Registern sind immer die aktuellen
   * Informationen zu dem in der CPU laufendem Prozess.
   * @param regSet das RegisterSet
   */
  public void setRegisterSet( RegisterSet regSet ) {
    this.regSet = regSet;
  }

  
  
  
  /**
   * Holt den Befehl anhand der uebergebenen virtuelle Adresse.
   * Intern, wird die virtuelle Adresse in die tatsaechliche, physische
   * Adresse umgewandelt und dann mit Hilfe dieser, der Befehl aus dem Hauptspeicher
   * geladen. 
   * <br/>
   * Bei der Adressumwandlung kann es sein, das es zu einem Seitenfehler kommt.
   * @param virtualAddr Die virtuelle/logische Adresse fuer den Befehl
   * @return Gibt den angeforderten Befehl als String wieder.
   * @throws AccessViolation wird geworfen wenn Die Adresse ungueltig ist. 
   * zb. Es wird auserhalb des Adressraumes zugegriffen
   */
  public String getMemoryCell( String virtualAddr ) throws AccessViolation {
	    
	  return this.memory.getContent( this.resolveAddress(virtualAddr) );
  }
  

  
  
  /**
   * 
   * Setzt einen Wert in eine Speicherzelle des Hauptspeichers. Das kann ein Datum oder
   * auch ein neuer Befehl sein, hauptsache die Speicherzelle liegt innerhalb des
   * Adressraumes.
   * <br/>
   * Intern, wird die virtuelle Adresse in die tatsaechliche, physikalische
   * Adresse umgewandelt und dann mit Hilfe dieser, der Befehl aus dem Hauptspeicher
   * geladen. 
   * <br/>
   * Bei der Adressumwandlung kann es sein, das es zu einem Seitenfehler kommt.
   * @param virtualAddr Die virtuelle/logische Adresse fuer den Befehl
   * @param value Der Wert als String
   * @throws AccessViolation wird geworfen wenn Die Adresse ungueltig ist. 
   * zb. Es wird auserhalb des Adressraumes zugegriffen
   */
  public void setMemoryCell( String virtualAddr, String value ) throws AccessViolation {
	  
	  this.memory.setContent(this.resolveAddress(virtualAddr), value);
  }


  
  
  /**
   * Speichert den Wert direkt an die Adresse in den Hauptspeicher.
   * <p>
   * <b>
   * Achtung!
   * </b>
   * Die Methode erwartet eine reale, physikalische Adresse. Und es wird
   * ohne Zugriffsschutz auf den Hauptspeicher geschrieben!
   * </p>
   * 
   * @param address die physikalische Adresse.
   * @param value Der Wert als String der gesetzt werden soll
   */
  public void setAbsoluteAddress( int realAddr, String value ) {
    memory.setContent( realAddr, value );
  }
  
  
  
  
  
  /**
   * Wandelt die uebergebene virtuelle Adresse um in die physikalsche Adresse.
   * <p>
   * <ol>
   * <li>Die virtuelle Adresse wird aufgespalten in Seitennummer und Offset</li>
   * <li>Mit Hilfe der SeitentabellenAdresse im RegisterSet wird die Seitentabelle geholt</li>
   * <li>
   * 	Die Rahmennummer wird
   * 	<ul>
   * 		<li>entweder mithilfe der Seitentabelle</li>
   * 		<li>oder durch einen Seitenfehler ermittelt</li>
   * 	</ul>
   * </li>
   * <li>Rahmennummer und Offset ergeben dann die pysikalische Adresse</li>
   * </ol>
   * </p>
   * @param virtualAddr Die virtuelle Adresse, die in die physikalische umgewandelt werden soll
   * @return die physikalische Adresse
   * @throws AccessViolation wird geworfen wenn Die Adresse ungueltig ist. 
   * zb. Es wird auserhalb des Adressraumes zugegriffen
   */
  public int resolveAddress( String virtualAddr ) throws AccessViolation {
	  
	  int[] array = this.trimVirtualAddr(virtualAddr);
	  
	  int pageNr = array[0];
	  int offset = array[1];
	 
	  int pageTableAddr = this.regSet.getPageTableAddr();	//Im RegisterSet steht die Adresse der Seitentabelle
	  ArrayList<PageTableEntry> table =						//Hole die Seitentabelle des laufenden Prozesses 
		  this.manager.getPageTable(pageTableAddr);
	  
	  PageTableEntry entry = table.get(pageNr);
	  table.get(pageNr).setReferenced(true);				//markiere die Seite als referenziert, Wichtig fuer Seitenersetzung!
	  int frameNr = entry.getFrameNr();			            //Hole Rahmennummer aus der Seitentabelle
	  if(-1 == frameNr) {									//->pageFault, falls RahmenNr nicht in Seitentabelle
		  													//dann liefert pageFault die RahmenNr
		  
		  SysLogger.writeLog( 0, "MMU.resolveAddress: Pagefault by virtual Address: " + virtualAddr);
		  frameNr = this.manager.pageFault(pageNr, entry, this.regSet.getAllocateTableAddr(), pageTableAddr);
		  
	  }
	  
	  
	  table.get(pageNr).setReferenced(true);				//markiere die Seite als referenziert, Wichtig fuer Seitenersetzung!
	  
	  SysLogger.writeLog( 0, "MMU.resolveAddress: RahmenNr: " + frameNr + " Offset: " + offset + "\n");
	  
	  int realAddr = frameNr * this.pageSize + offset;
	  
	  return realAddr;
  }
  
  


  


  /**
   * Teilt die String-Repraesentation der virtuellen Addresse auf in
   * Integer Seitennummer und Integer Offset.
   * <br/>
   * Dabei wird auch geprueft, ob die virtuelle Addresse gueltig ist.
   * 
   * <p>
   * <b>Achtung!</b> In der virtuellen Adresse muessen alle Stellen vorhanden sein oder
   * mit 0 aufgefuellt werden, damit dieser Algorithmus korrekt terminiert.
   * Wenn z.B. auf die 1. Seite in einem Adressraum der Groesze von 100 Seiten zugegriffen
   * werden soll, so muss als Seitennummer in der virtuellen Adresse eine 001 angegeben werden.
   * Das gleiche gilt auch fuer das Offset.
   * </p>
   * @param virtualAddr Die virtuelle Adresse als String
   * @return Ein int[], wobei Index 0 auf die Seitennummer und Index 1 auf das Offset zeigt.
   * @throws AccessViolation Falls die virtuelle Adresse ungueltig ist.
   */
  private int[] trimVirtualAddr(String virtualAddr) throws AccessViolation {
	  
      int vAddrLen = virtualAddr.length();
	  
	  String offsetStr = virtualAddr.substring(vAddrLen - this.offsetLength); //offsetLength entspricht der maximalen Laenge des Offsets
	  int offset = new Integer(offsetStr);
	  
	  
	  int pageNr;
	  
	  if (vAddrLen - this.offsetLength == 0) {
		  pageNr = 0;
	  } else {
		  pageNr = new Integer(virtualAddr.substring(0, vAddrLen - this.offsetLength));  
	  }
	  
	  if(offset >= this.pageSize || pageNr >= this.addrSpace) {
		  SysLogger.writeLog( 0, "MMU: AccessViolation! virtual address-> "+ virtualAddr + " does not exist.");
		  throw new AccessViolation();
	  }
	  
	  return new int[]{pageNr, offset};
  }
  
  
  /**
   * Gibt den Inhalt der Speicherzellen in dem Hauptspeicher bis zur
   * Zelle limit auf die Logdatei aus.
   * @param limit Die "Grenzspeicherzelle" bis zu welcher der Inhalt ausgegeben
   * werden soll
   */
  public void dumpMemory( int limit ) {
    SysLogger.writeLog( 1, "MMU.dumpMemory" );
    for( int i = 0; i < limit; i++ ) {
      SysLogger.writeLog( 1, i + ": " + memory.getContent(i) );
    }
  }

}
