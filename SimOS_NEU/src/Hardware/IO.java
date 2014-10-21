package Hardware;

import MainBoot.SysLogger;
import Scheduler.Event;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

/**
 * Klasse die die Ein-/Ausgabe zwischen Usern und Prozessen verwaltet.
 * Mit Prozessen laesst sich ueber ihre Konsole kommunizieren.
 * Und Jede Kommunikation(Eingaben, die mit '\n' abgeschlossen wurden) wird einem 
 * Event zugerechnet.
 * Prozesse koennen auf eine Eingabe in der Konsole warten.
 * In diesem Fall gibt es einen Event vom Typ read in der ReadList.
 * Sobald eine Kommunikation stattfindet, wird das entsprechende Event
 * aus der readList geholt, und als fertig markiert, in der resultQueue
 * verschoben.
 *
 */
public class IO {
	
	
  ArrayList<Event> readList;
  ArrayList<Event> resultQueue;
  int consoleId;
  
  public IO() {
    readList = new ArrayList<Event>();
    resultQueue = new ArrayList<Event>();
    consoleId = 0;
  }
  
  
  /**
   * Erzeugt eine Konsole mit dem angegebenem Titel.
   * @param title der Titel, der in der Leiste angezeigt werden soll.
   * @return Liefert eine Referenz auf das SysConsole-Objekt zurueck.
   */
  public SysConsole createConsole( String title ) {
    consoleId++;
    SysLogger.writeLog( 0, "IO.createConsole: new console [id: " + consoleId + ", title: " + title + "]" );
    SysConsole frame = new SysConsole( this, consoleId, title );
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize( 475, 400 );
    frame.setVisible(true);
    
    return frame;
  }
  
  /**
   * Schreibt den text auf eine bestimmte Konsole.
   * @param console Die SysConsole, auf die der text soll.
   * @param text der Text, der geschrieben werden soll.
   */
  public void write( SysConsole console, String text ) {
    console.write( text );
  }
  
  /**
   * Schreibt einen Zeilenumbruch auf die Konsole.
   * @param console Die Konsole
   */
  public void writeln( SysConsole console ) {
    console.write( "\n" );
  }
  
  /**
   * Liest ein Ereignis (Event) ein.
   * Jedesmal wenn die CPU einen read-Befehl verarbeitet, wird
   * ein Event erzeugt und dann diese Methode mit dem Event aufgerufen.
   * 
   * @param event das Event.
   */
  
  public void read( Event event ) {
    SysLogger.writeLog( 0, "IO.read: adding event to read queue " + event.toString() );
    readList.add( event );
  }
  
  
  /**
   * Verarbeitet den Text, der auf einer Konsole eingegeben wird weiter.
   * Die empfangene Zeichenfolge wird in ein zugehoeriges Event eingetragen.
   * Wird immer aufgerufen wenn in der Konsole eine Eingabe bestaetigt wird.
   * <p>
   * Da die Eingabe zum Event, das zuletzt erzeugt wurde und auf der selben Konsole ist,
   * gehoeren muss, wird unter allen Events fuer dieselbe Konsole das juengste ausgewaehlt.
   * Diesem ausgewaehlten Event wird der Text entsprechend gesetzt und dann aus der readList in
   * die resultQueue eingetragen.
   * </p>
   * @param consoleId die Konsolen ID von der Konsole auf der die Eingabe erfolgt ist
   * @param text
   */
  public void receiveReadContent( int consoleId, String text ) {
    SysLogger.writeLog( 0, "IO.receiveReadContent: received: '" + text + "' from console " + consoleId );
    Event event = null;
    // Die empfangene Zeichenfolge wird in ein zugehöriges Event eingetragen.
    // Unter allen Events für dieselbe Konsole wird das jüngste ausgewählt.
    for( int i = readList.size()-1; i >= 0;  i-- ) {
    	
      if( readList.get(i).getConsole().getId() == consoleId ) {
	    event = readList.remove(i);
	    break;
      }
    }
    if( event != null ) {
      event.setContent( text );
      SysLogger.writeLog( 0, "IO.receiveReadContent: adding event to result queue " + event.toString() );
      resultQueue.add( event );
    }
  }
  
  
  /**
   * Holt das naechste Event aus der resultQueue.
   * 
   * Die resultQueue enthaelt alle Events vom typ read die abgearbeitet wurden.
   * Das heiszt bei diesen Events, wurde schon die entprechende Eingabe in der Konsole
   * getaetigt.
   * <p>
   * Z.B. Wenn die CPU von einem Prozess den read-Befehl abarbeitet, wird dieser
   * Prozess blockiert.
   * read bedeutet das der Prozess auf eine Eingabe in der Konsole wartet.
   * Es wird ein Event vom Typ read erzeugt und in der readList gespeichert.
   * Wenn nun der User in der Konsole die entsprechende Eingabe macht, wird das
   * Event aus der readList geholt, der Eingabetext im Event gesetzt und das Event in
   * die resultQueue hinverschoben.
   * </p>
   * @return das naechste Event aus der resultQueue.
   */
  public Event getNextEvent() {
    return resultQueue.size() > 0 ? resultQueue.remove(0) : null;
  }
  
}

