package MemoryManagement;

/**
 * 
 * @author Johann Mantler, Hicham Belmoquadem
 *
 */
public class Clock implements PageReplacementIF {

	private Cursor cursor;   //Uhrzeiger
	
	
	/**
	 * Repraesentiert ein Listenelement aus der Ringliste.
	 * @author Johann Mantler
	 */
	private class Cell {
		Cell next;					//Referenz auf Nachfolger
		int allocateTableAddr;		//Adresse der Plattenzuordnungstabelle
		int pageTableAddr;			//Adresse der Seitentabelle
		int pageNr;					//Seitennummer
		PageTableEntry entry;		//Seitentabelleneintrag
		
		Cell(int allocateTableAddr, int pageTableAddr, PageTableEntry entry, int pageNr) {
			this.entry = entry;
			this.allocateTableAddr = allocateTableAddr;
			this.pageNr = pageNr;
			this.pageTableAddr = pageTableAddr;
		}
	}
	
	
	
	/**
	 * Repraesentiert den Uhrzeiger im Clock-Algorithmus.
	 * <p>
	 * Neue Listenelemente werden immer hinter dem Uhrzeiger
	 * eingefuegt. Damit wird garantiert, dass die neusten Seiten als
	 * letztes vom Uhrzeiger besucht werden.
	 * </p>
	 * @author Johann Mantler
	 */
	private class Cursor {
		Cell prevCell;		//Zeiger auf Vorgaenger Zelle, ->Wird benoetigt zum Einfuegen!
		Cell actCell;		//Zeiger auf aktuelles Element.
		
		Cursor(Cell prev, Cell act) {
			this.prevCell = prev;
			this.actCell = act;
		}
	}
	

	
	/**
	 * Fuegt ein neues Listenelement hinter dem Uhrzeiger in der Ringliste ein.
	 * Damit wird garantiert, dass die neusten Seiten als
	 * letztes vom Uhrzeiger besucht werden.
	 *
	 */
	public void insert(int allocateTableAddr, int pageTableAddr, PageTableEntry entry, int pageNr) {
		Cell newCell = new Cell(allocateTableAddr, pageTableAddr, entry, pageNr);
		if(cursor != null) {
			cursor.prevCell.next = newCell;
			newCell.next = cursor.actCell;
			cursor.prevCell = newCell;
		
		} else {					//Liste ist leer..
			newCell.next = newCell;
			cursor = new Cursor(newCell, newCell);
	
		}
	}
	
	
	
	
	/**
	 * Ruecke mit dem Cursor eine Zelle weiter.
	 */
	private void next() {
		cursor.prevCell = cursor.actCell;
		cursor.actCell = cursor.actCell.next;
	}
	
	
	
	/**
	 * Entferne die Zelle aus der Ringliste, auf die der
	 * Cursor steht. Der Cursor steht anschliesend auf dem
	 * Nachfolgeelement.
	 */
	private void remove() {
		if(cursor.actCell != cursor.prevCell) {
			
			cursor.actCell = cursor.actCell.next;
			cursor.prevCell.next = cursor.actCell;
			
		} else { //es gibt nur eine Zelle..
			cursor = null;
		}
			
	}
	
	
	
	
	
	/**
	 * Wenn ein Seitenfehler auftritt, wird diese Methode aufgerufen.
	 * Zunaechst wird die Seite geprueft, auf die der Uhrzeiger(Cursor) steht.
	 * <ul>
	 * <li>Ist referenced = true -> mache es false und ruecke Uhrzeiger weiter</li>
	 * <li>Ist referenced = false-> entferne diese Seite</li>
	 * </ul>
	 * 
	 * <p>
	 * Info: Seite steht hier sinnbildlich fuer ein Listenelement um die Arbeitsweise
	 * von Clock besser nahezubringen.
	 * </p>
	 */
	public int[] execute() {
		
		if(cursor == null) {
			return null;
		}
		
		while( cursor.actCell.entry.isReferenced() ) {
			
			cursor.actCell.entry.setReferenced(false); //loesche ref
			this.next();							   //ruecke weiter
		}
		
		int[] result = new int[] { 
				cursor.actCell.pageNr,
				cursor.actCell.entry.getFrameNr(),
				cursor.actCell.allocateTableAddr,
				cursor.actCell.pageTableAddr
		};
		
		this.remove();
		return result;
	}
	
}
