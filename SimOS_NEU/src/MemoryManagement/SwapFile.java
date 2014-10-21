package MemoryManagement;

import java.util.ArrayList;

import MainBoot.BootLoader;
import MainBoot.SysLogger;


/**
 * <b>Aufgabe:</b> Repraesentiert die Auslagerungsdatei der Festplatte.
 * <p>
 * Es gibt keine feste Auslagerungsbereiche
 * pro Prozess, sonder alle Prozesse teilen sich einen Bereich.
 * <br/>
 * Fuer jede Seite, egal von welchem Prozess, wird einzeln Platz auf
 * der Auslagerungsdatei reserviert, wenn sie ausgelagert wird.
 * Sobald die Seite wieder eingelagert wird, wird ihr Platz freigegeben.
 * <br/>
 * <br/>->Prozesse im Speicher belegen überhaupt keinen Platz auf der Festplatte.
 * <br/>->Jede Seite braucht ihre eigene Festplattenadresse, die in einer Plattenzuordnungstabelle
 * gespeichert werden muss.
 * 
 * </p>
 * @author Johann Mantler, Hoang Anh Duong, Hicham Belmoquadem
 *
 */
public class SwapFile {

	private ArrayList<SwapFileUnit> swap;
	private int swapFileSize;
	
	/**
	 * Erzeugt ein SwapFile mit der uebergebenen Groesze.
	 * @param size Groesze der Auslagerungsdatei
	 */
	public SwapFile(int size) {
		this.swapFileSize = size;
		this.swap = new ArrayList<SwapFileUnit>();
	}
	
	/**
	 * Erzeugt ein SwapFile mit der Default-Groesze.
	 * Die Default-Groesze ist typischerweise 320.
	 */
	public SwapFile() {
		this.swapFileSize = BootLoader.swapFileSize;
		this.swap = new ArrayList<SwapFileUnit>();
	}
	
	/**
	 * Die Auslagerungsdatei besteht aus einer Liste von
	 * diesen Objekten. Zusaetzlich zu jeder Seite wird die Information
	 * gespeichert ob diese Seite ueberschrieben werden darf oder nicht.
	 * @author Johann Mantler, Hoang Anh Duong, Hicham Belmoquadem
	 *
	 */
	private class SwapFileUnit {
		String[] page;
		boolean present;	//bei true, Seite ist nur Im SwapFile ->darf nicht ueberschrieben werden
		
		SwapFileUnit(String[] page) {
			this.page = page;
			present = true;
		}
	}
	
	
	/**
	 * Liefert eine Seite aus der Auslagerungsdatei, um sie
	 * dann im Hauptspeicher unterzubringen.
	 * <p>
	 * Die Seite befindet sich danach immer noch auf der Auslagerungsdatei,
	 * wird aber als nicht mehr praesent markiert ->
	 * das <code>present</code> Attribut der Seite wird auf false gesetzt.
	 * Nun kann die Seite ueberschrieben werden, um der Fragmentierung vorzubeugen
	 * und Plattenspeicher zu sparen.
	 * </p>
	 * <p>
	 * Wichtig! Der pageSwapFileAddr ist danach nicht mehr gueltig!
	 * </p>
	 * @param pageSwapFileAddr Adresse der Seite auf dem Swapbereich
	 * @return gibt die Seite als String-Array zurueck.
	 */
	public String[] getPage(int pageSwapFileAddr) {
	
		SwapFileUnit unit = this.swap.get(pageSwapFileAddr);
		unit.present = false;
		
		return unit.page;
	}

	
	/**
	 * Lagert eine Seite in die Auslagerungsdatei ein.
	 * Die Seite wird in die naechste freie Luecke eingefuegt.
	 * @param page die Seite, die eingelagert werden soll
	 * @return Die Festplattenadresse der Seite als int oder -1 wenn kein
	 * Platz mehr auf der Auslagerungsdatei vorhanden ist.
	 */
	public int setPage(String[] page) {
		
		boolean overwrite = false;
		int addr = -1;
		
		if(this.swap.size() >= this.swapFileSize) {
			
			SysLogger.writeLog( 0, "SwapFile.setPage: SwapFile is full!");
			return addr;
		}
		
		for(int i = 0; i < this.swap.size(); i++) {
			
			if ( ! this.swap.get(i).present ) {
				this.swap.set(i, new SwapFileUnit(page));
				overwrite = true;
				addr = i;
				break;
			}
	         
				
		}
		
		if(!overwrite) {
			this.swap.add(new SwapFileUnit(page));
			addr = this.swap.size()-1;
		}
		
		
		return addr;
	}
	
	
} //end class
