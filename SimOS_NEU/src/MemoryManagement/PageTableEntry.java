package MemoryManagement;

/**
 * <b>Aufgabe:</b> Klasse die einen Seitentabelleneintrag repraesentiert.
 * <br/>
 * <b>Info: </b><br/>
 * Das M-Bit (modified) brauchen wir nicht, da wir die Seiten niemals
 * redundant speichern. Die Seiten sind wenn sie Im Hauptspeicher sind, nicht mehr
 * in  der Auslagerungsdatei und umgekehrt.
 * Der Grund dafuer ist, das wir als Hintergrundspeicher einen dynamischen SwapFile
 * mit Plattenzuordnungstabelle gewaehlt haben.
 * @author Johann Mantler, Hicham Belmoquadem, Hoang Anh Duong
 *
 */
public class PageTableEntry {

	private int frameNr;
	private boolean referenced;    
	
	/**
	 * Beim erzeugen eines Seitentabelleneintrages, wird die
	 * Rahmennummer ersteinmal auf -1 gesetzt.
	 * Dies bedeutet, das der Rahmen sich nicht in der MainMemory befindet.
	 */
	public PageTableEntry() {
		
		this.frameNr = -1;
		this.referenced = false;
	}

	public final int getFrameNr() {
		return frameNr;
	}

	public final boolean isReferenced() {
		return referenced;
	}


	public final void setFrameNr(int frameNr) {
		this.frameNr = frameNr;
	}

	public final void setReferenced(boolean referenced) {
		this.referenced = referenced;
	}

	
}
