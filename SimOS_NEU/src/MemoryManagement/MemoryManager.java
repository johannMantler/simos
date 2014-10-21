
package MemoryManagement;

import Hardware.MainMemory;
import MainBoot.SysLogger;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * <b>Aufgabe:</b> Der MemoryManager verwaltet die Seiten- und Plattenzuordnungstabellen und
 * kuemmert sich um die Abarbeitung eines Seitenfehlers.
 * @author Johann Mantler, Hoang Anh Duong
 *
 */
public class MemoryManager implements MemoryManagerIF {
  
  private MainMemory memory;
  private int pageSize;
  private int addressSpaceSize;
  private SwapFile swapFile;
  private PageReplacementIF pageReplacer;
  
  /**
   * Datenstruktur fuer alle Seitentabellen der Prozesse.
   * Jeder Prozess speichert in seinem RegisterSet die Adresse seiner PageTable
   * Diese Adresse ist der Key in der HashMap.
   */
  private HashMap<Integer, ArrayList<PageTableEntry>> pageTables;
  
  /**
   * Plattenzuordnungstabelle. Hier werden virtuelle Seiten auf
   * Festplattenadressen abgebildet.
   * Jeder Prozess speichert in seinem RegisterSet die Adresse seiner allocateTable
   * Diese Adresse ist der Key in der HashMap.
   */
  private HashMap<Integer, ArrayList<Integer>> allocateTables;
  
  
  
  public MemoryManager( MainMemory memory, SwapFile swapFile, int pageSize, int addressSpaceSize, PageReplacementIF pr) {
      this.memory = memory;
      this.pageSize = pageSize;
      this.swapFile = swapFile;
      this.addressSpaceSize = addressSpaceSize;
      this.pageReplacer = pr;
      
      this.allocateTables = new HashMap<Integer, ArrayList<Integer>>();
      this.pageTables = new HashMap<Integer, ArrayList<PageTableEntry>>();
      
  }

  
  /**
   * Hier muss pageSize und AddressSize geänder werden TODO
   * @param memory
   * @param swapFile
   * @param pr
   */
  public MemoryManager( MainMemory memory, SwapFile swapFile, PageReplacementIF pr) {
      this.memory = memory;
      this.swapFile = swapFile;
      this.pageReplacer = pr;
      
      this.allocateTables = new HashMap<Integer, ArrayList<Integer>>();
      this.pageTables = new HashMap<Integer, ArrayList<PageTableEntry>>();
      
  }
  
  
  /**
   * Liefert die Seitentabelle aus der Liste aller Seitentabelln
   * mit Hilfe der uebergebenen Adresse.
   * @param addr Die Adresse, mit der man die Seitentabelle finden kann.
   * @return Die Seitentabelle
   */
  public final ArrayList<PageTableEntry> getPageTable(int addr) {
	  return this.pageTables.get(addr);
  }
  
  /**
   * Liefert die Plattenzuordnungstabelle aus der Liste aller Plattenzuordnungstabelln
   * mit Hilfe der uebergebenen Adresse.
   * @param addr Die Adresse, mit der man die Plattenzuordnungstabelle finden kann.
   * @return Die Plattenzuordnungstabelle
   */
  public final ArrayList<Integer> getAllocateTable(int addr) {
	  return allocateTables.get(addr);
  }
  
  /**
   * Liefert die Seitengroesze.
   * @return Die Seitengroesze
   */
  public final int getPageSize() {
		return pageSize;
  }

  /**
   * Liefert die Groesze des virtuellen Adressraums gemessen an der Anzahl der Seiten.
   * @return Die Adressraumgroesze
   */
  public final int getAddressSpaceSize() {
		return addressSpaceSize;
  }
  
  
  
  public final void setPageSize(int pageSize) {
		this.pageSize = pageSize;
  }

  public final void setAddressSpaceSize(int addressSpaceSize) {
		this.addressSpaceSize = addressSpaceSize;
  }

  
  
  /**
   * Laedt ein Programm aus einer Datei in das Betriebssystem.
   * Dabei werden:<br/>
   * <ul>
   * <li>Seitentabelle von der Groesze des erlaubten virtuellen Adressraumes erzeugt</li>
   * <li>Alle Seiten des Prozesses ausgelagert. ->Demand-Paging</li>
   * <li>Plattenzuordnungstabelle erzeugt und die Festplattenadressen der einzelnen Seiten reingeschrieben</li>
   * <li>Adressen der Seiten- und Plattenzuordnungstabelle im RegisterSet des PCB's gesetzt</li>
   * <li>Befehlszaehler auf den ersten Befehl des Programmes gesetzt</li>
   * </ul>
   * @param file Die Datei aus der Zeilenweise der Programmcode gelesen wird.
   * @param pcb Der Prozesskontrollblock, fuer den der Programmcode gelesen wird.
   * @return Liefert true zurueck bei Erfolg, sonst false bei Fehlschlag.
   * Fehlschlag: Wenn die Datei nicht gefunden/geoeffnet/gelesen werden konnte oder
   * das Programm zu grosz ist.
   * 
   * @see MemoryManager#checkProgramSize(String)
   * @see MemoryManager#createPages(ArrayList, ArrayList, BufferedReader)
   * 
   */
  @Override
  public boolean loadProgram( String file, PCB pcb ) {
	  try {
		  BufferedReader input = new BufferedReader( new FileReader(file) );
		  String line = input.readLine();
		  int size = this.checkProgramSize(line); //In der ersten Zeile steht die Programgroesze
		  
		  if(-1 == size) {
			  return false;
		  }
		  
    	  SysLogger.writeLog( 0, "MemoryManager.loadProgram:" + file + " with size " + size +" is going to be read");
        
        
    	  ArrayList<PageTableEntry> pageTable = new ArrayList<PageTableEntry>();//Seitentabelle erzeugen
    	  ArrayList<Integer> allocateTable = new ArrayList<Integer>();			//Plattenzuordnungstabelle erzeugen
    	  
    	  this.createPages(pageTable, allocateTable, input);    //Text lesen und alle Seiten erzeugen
    	    	  
    	  int addr = pcb.getPid();
    	  
    	  this.pageTables.put(addr, pageTable);		//Seitentabelle zu den anderen hinzufuegen
    	  pcb.getRegisterSet().setPageTableAddr(addr);
    	  
    	  
    	  this.allocateTables.put(addr, allocateTable); //Plattenzuordnungstabelle zu den anderen hinzufuegen
    	  pcb.getRegisterSet().setAllocateTableAddr(addr);


    	  pcb.getRegisterSet().setProgramCounter( this.setVirtualAddrToZero() );   //virtuelle Adresse: SeitenNr 0 und Offset 0


    	  input.close();
      
    } catch( IOException e ) {
      System.err.println(e.toString());
      return false;
    }
    
    return true;
  }
  
  

/**
   * Erzeugt eine virtuelle Adresse als String, die auf den allerersten Befehl verweist.
   * Die Adresse passt sich dynamisch an der Seitengroesze und dem Adressraum eines
   * jeden Prozesses an.
   * @return Die virtuelle Adresse
   */
  private String setVirtualAddrToZero() {
	  
	  int offsetLen = new Integer(this.pageSize).toString().length();
	  int pageNrLen = new Integer(this.addressSpaceSize).toString().length();
	  
	  
	  String virtualAddr = "";
	  for(int i = 0; i < offsetLen + pageNrLen; i++) {
		  virtualAddr = virtualAddr + "0";
	  }
	  
	  return virtualAddr;
  }
  
  
  /**
   * <p><i>Hilfsmethode fuer loadProgram().</i></p>
   * Liest und laedt ein Programm von dem uebergebenen Eingabestrom.<br/>
   * Dabei werden die gelesenen Daten in "Haepchen" von jeweils 4 Zeilen (Seiten)
   * aufgeteilt.<br/>
   * Wenn fuer die letzte eingelesene Seite <4 Zeilen bereitstehen, wird diese
   * mit null-Werte aufgefuellt (interne Fragmentierung).<br/>
   * Sobald eine Seite fertig gestellt wurde, wird der Seiteninhalt auf die Auslagerungsdatei
   * geschrieben und die Festplattenadresse der Seite in der Plattenzuordnungstabelle vermerkt.
   * Auch wird ein Seitentabelleneintrag erzeugt und der Seitentabelle hinzugefuegt.
   * <p>
   * Auch wenn der Programmtext nicht <code>addressSpaceSize</code> , d.H. der groesze des
   * moeglichen virtuellen Adressraumes belegt, so werden dennoch alle Seiten erzeugt und mit
   * einem leeren String "" gefuellt. <br/>
   * Dadurch kann der Prozess spaeter noch bis zu <code>addressSpaceSize</code> anwachsen.
   * </p>
   * @param pageTable Die Seitentabelle des Prozesses von dem das Programm eingelesen wird.
   * @param allocateTable Die Plattenzuordnungstabelle des Prozesses von dem das Programm eingelesen wird.
   * @param input Der Eingabestromg von dem das Programm gelesen wird.
   * @throws IOException Wird geworfen, wenn etwas mit dem Eingabestrom nicht stimmt.
   */
  private void createPages(ArrayList<PageTableEntry> pageTable, ArrayList<Integer> allocateTable, BufferedReader input) throws IOException {
	  
	  String line;
	  
	  while ( (line = input.readLine() ) != null) {			//das 1. Datum in der Seite darf nich null sein
      	
      	String[] content = new String[this.pageSize];		//Jede Seite ist ein String[]
      	content[0] = line;
 	        
      	for(int i = 1; i < this.pageSize; i++) {		    //restliche Daten in die Seite fuellen
      													    //Jede Zeile ist eine groeszeneinheit
      		content[i] = input.readLine();
      	    												//Wenn keine Daten mehr kommen wird die Seite mit null-Werten gefuellt.
      	}													//ggf->interne Fragmentierung
      	
      	int swapAddr = this.swapFile.setPage(content);      //Seite erstmal auslagern ->Demand Paging
      	allocateTable.add(swapAddr);                 	    //und die Adresse in der Plattenzuordnungstabelle speichern
      	
      	pageTable.add(new PageTableEntry());			    //jede Seite hat ihren Seitentabelleneintrag
      	
  	  }
    
	  SysLogger.writeLog( 0, "MemoryManager.loadProgram: Process has been allocated " + pageTable.size()+" from "+ this.addressSpaceSize+" pages at the beginning");
	  
  	  for(int i = pageTable.size(); i < this.addressSpaceSize; i++) {
  		  													//nun werden die restlichen Seiten erzeugen..
  		  String[] content = new String[this.pageSize];		//..damit die Seitenanzahl der Groesze des virtuellen Adressraumes entspricht
  		  													//In jeder Seite stehen leere Strings: ""
  		  for(int j = 0; j < this.pageSize; j++) {
  			  content[j] = "";
  		  }
  		  
  		  int swapAddr = this.swapFile.setPage(content);
  		  allocateTable.add(swapAddr);                 	
          pageTable.add(new PageTableEntry());			 
        	
  	  }

  }




  
  /**
   * <p><i>Hilfsmethode fuer loadProgram().</i></p>
   * Ueberprueft, ob: <br/>
   * 1. Das Argument vom Typ String wirklich eine Programmgroesze darstellt.<br/>
   * 2. Das Programm in den virtuellen Addressraum passt.
   * Denn der virtuelle Adressraum ist auf <code>addressSpaceSize</code> Seiten begrenzt.
   * <br/>
   * Falls einer der beiden Pruefungen fehlschlaegt, liefert die Methode -1 zurueck.
   * @param line Der String der die Programmgroesze enthaelt.
   * @return -1 bei Fehlschlag, sonst die Programmgroesze als int
   */
  private int checkProgramSize(String line) {
	  
      int size = -1;

	  try {
		  
		  size = Integer.valueOf( line );
	  
	  }catch(NumberFormatException e) {
		  
		  SysLogger.writeLog( 0, "MemoryManager.loadProgram: Failure. Cannot read the size of program.");
	  }
		  
	  if( size/this.pageSize > this.addressSpaceSize ) { //passt das Programm in den virtuellen Adressraum?
    		  
		  SysLogger.writeLog( 0, "MemoryManager.loadProgram: Failure. Size of program is too large.");
		  size = -1;
      }
	  
	  return size;
  }
  
  
  
  


  /**
   * Algorithmus fuer den Seitenfehler. Diese Methode wird von der MMU aufgerufen
   * wenn diese auf einen Seitenrahmen zugreifen will, der nicht im Hauptspeicher ist.
   * <p>
   * Die Arbeitsweise dieser Methode sieht wie folgt aus:<br/>
   * <ol>
   * <li>die angeforderte Seite wird aus der Auslagerungsdatei geholt</li>
   * <li>der entsprechene Eintrag in der Plattenzuordnungstabelle wird aktualisiert</li>
   * <li>es wird geprueft, ob noch Platz fuer einen Seitenrahmen im Hauptspeicher ist</li>
   * <li>wenn es Platz gibt,</li>
   * <ul>
   * <li>wird die angeforderte Seite in den Hauptspeicher geladen</li>
   * </ul>
   * <li>sonst,</li>
   * <ul>
   * <li>wird der Seitenersetzungsalgo. aufgerufen, der Infos zu der zu verdraengenden Seite zurueckgibt</li>
   * <li>diese Seite wird aus dem Hauptspeicher genommen</li>
   * <li>und auf die Auslagerungsdatei geschrieben</li>
   * <li>nun wird die angeforderte Seite in den Hauptspeicher geladen</li>
   * </ul>
   * <li>die neue Seite wird beim Seitenersetzungsalgorithmus vermerkt</li>
   * <ol>
   * </p>
   * @param pageNr Seitennummer der Seite die den Seitenfehler ausgeloest hat.
   * @param entry Der Seitentabelleneintrag dieser Seite
   * @param allocateTableAddr Adresse der Plattenzuordnungstabelle
   * @param pageTableAddr Adresse der Seitentabelle
   * @return pageFault() liefert die Rahmennummer zurueck von der Seite die sich nun im Hauptspeicher
   * befindet.
   * 
   * @see MemoryManager#getFrameFromMemory(int)
   * @see MemoryManager#getPageFromSwapFile(int, int)
   * @see MemoryManager#isMemoryFull()
   * @see MemoryManager#loadPageToMemory(int, String[])
   * @see MemoryManager#replacePage()
   */
  public int pageFault(int pageNr, PageTableEntry entry, int allocateTableAddr, int pageTableAddr) {
	
	  
	  String[] pageContent = this.getPageFromSwapFile(pageNr, allocateTableAddr);
	  
	  int frameNr = this.isMemoryFull();
	  
	  if(frameNr != -1) { 				//Im Hauptspeicher ist noch ein Rahmen frei
	  
		  this.loadPageToMemory(frameNr, pageContent);
	  }
	  
	  else {   								//Hauptspeicher ist voll
		  
		  SysLogger.writeLog( 0, "MemoryManager.pageFault: MainMemory is full, running Clock now");
          frameNr = this.replacePage();
		  this.loadPageToMemory(frameNr, pageContent);
	  }
	  
	  
	  
	  //Neue Seite dem Seitenersetzungsalgorithmus bekannt machen
	  entry.setFrameNr(frameNr);
	  this.pageReplacer.insert(allocateTableAddr, pageTableAddr, entry, pageNr);
	  
	  return frameNr;
	  
  }
  
  /**
   * <p><i>Hilfsmethode fuer pageFault().</i></p>
   * Verdreangt einen Seitenrahmen aus dem Hauptspeicher.
   * <p>
   * <ul>
   * <li>Mit Hilfe des Seitenersetzungsalgorithmus wird ein Seiterahmen ausgewaehlt.</li>
   * <li>Dieser Seitenrahmen wird dann zur Sicherung auf die Auslagerungsdatei geschrieben.</li>
   * <li>Die Festplattenadresse der Seite wird in der Plattenzuordungstabelle vermerkt</li>
   * <li>In der entsprechenden Seitetabelle wird die Seite als nicht praesent markiert</li>
   * <ul>
   * </p>
   * <p>
   * <b>Hinweis: </b> Der Seitenrahmen existiert nach Terminierung dieser Methode immer noch
   * im Hauptspeicher, kann aber nun ueberschrieben werden.
   * </p>
   * @return Gibt die Rahmennummer zurueck, von der Seite die verdraengt wurde.
   */
  private int replacePage() {
	  
	  int[] pageInfo = this.pageReplacer.execute(); 				//Seitenersetzungsalgo. starten
	  																//Informationen ueber die zu verdraengende Seite zwischenspeichern
	  int pageNr = pageInfo[0];
	  int frameNr = pageInfo[1];
	  int allocateTableAddr = pageInfo[2];
	  int pageTableAddr = pageInfo[3];
	  
	  
	  if(pageTables.get(pageTableAddr) == null) {
		  SysLogger.writeLog( 0, "doaskjdoasjdasoldpoajd");
	  }
	  
	  String[] frameContent = this.getFrameFromMemory(frameNr);			//Hole Seitenrahmen aus dem Speicher
	  int addr = this.swapFile.setPage(frameContent);					//Seite auf SwapFile sichern
	  this.allocateTables.get(allocateTableAddr).set(pageNr, addr);	    //Festplattenadresse der Seite in Plattenzuordnungstabelle schreiben
	  this.pageTables.get(pageTableAddr).get(pageNr).setFrameNr(-1);  	//Seite in der Seitentabelle als nicht present markieren 
	  
	  return frameNr;
  }
  
  
  
  /**
   * <p><i>Hilfsmethode fuer pageFault().</i></p>
   * Laedt den uebergebenen Seiteninhalt in den Hauptspeicher
   * an der Position framNr.
   * @param frameNr Rahmennummer, an die die Seite geladen werden soll
   * @param pageContent Der Seiteninhalt.
   */
  private void loadPageToMemory(int frameNr, String[] pageContent) {
	  
	  int index = frameNr * this.pageSize;
	  
	  for(int i = index, j = 0; j < this.pageSize; j++) {
		  this.memory.setContent(i + j, pageContent[j]);
	  }
  }
  
  
  
  /**
   * <p><i>Hilfsmethode fuer pageFault().</i></p>
   * Prueft ob im Hauptspeicher noch Platz fuer einen Seitenrahmen ist.
   * @return liefert die Rahmennummer des freien Rahmens, oder 
   * -1 wenn kein freier Rahmen mehr im Hauptspeicher ist.
   */
  private int isMemoryFull() {
	  
	  int result = -1;
	  
	  int memSize = this.memory.getSize();
	  int i = 0;
	  
	  while( i < memSize) {
		  
		  if( "".equals(this.memory.getContent(i)) ) { //..pruefen ob es eine freie Speicherzelle gibt
			  
			    result = i / this.pageSize;
			    break;
		   }
		   i = i + this.pageSize;						//der Hauptspeicher wird in pageSize-Schritten durchlaufen
	  }
	  
	  return result;
  }
  
  
  
  /**
   * <p><i>Hilfsmethode fuer pageFault().</i></p>
   * Holt einen Seitenrahmeninhalt vom Hauptspeicher
   * @param frameNr Die Rahmennummer des Seitenrahmens der geholt werden soll.
   * @return Der Seitenrahmeninhalt als String-Array
   */
  private String[] getFrameFromMemory(int frameNr) {
	  
	  String[] frameContent = new String[this.pageSize]; 
	  int index = frameNr * this.pageSize;
	  
	  for(int i = index, j = 0; j < this.pageSize; j++, i++) {
		
		  frameContent[j] = this.memory.getContent(i);
	  }
	  
	  return frameContent;
	  
  }

  
  
  /**
   * <p><i>Hilfsmethode fuer pageFault().</i></p>
   * Holt eine Seite aus der Auslagerungsdatei und loescht die Festplattenadresse
   * dieser Seite in der entsprechenden Plattenzuordnungstabelle.
   * @param pageNr Die Seitennummer der Seite, die geholt werden soll
   * @param allocateTableAddr Adresse der Plattenzuordnungstabelle
   * @return Liefert den Inhalt der Seite zurueck, die aus der Auslagerungsdatei 
   * genommen wurde.
   */
  private String[] getPageFromSwapFile(int pageNr, int allocateTableAddr) {
	  
	  ArrayList<Integer> allocateTable = this.allocateTables.get(allocateTableAddr); //Hole Plattenzuordnungstabelle
	  int pageSwapFileAddr = allocateTable.get(pageNr); //Hole Festplattenaddresse von der Seite
	  
	  allocateTable.set(pageNr, -1); //loesche Adresse in der Plattenzuordnungstabelle
	  
	  return this.swapFile.getPage(pageSwapFileAddr); // Hole die Seite von der Festplatte
	 
  }
  
}
