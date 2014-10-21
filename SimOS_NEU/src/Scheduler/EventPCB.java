package Scheduler;
import MemoryManagement.PCB;

/**
 * 
 * Container Klasse, um jedem Prozess genau ein Event zuzuordnen.
 * Durch diese Hiflsklasse kann der Scheduler
 * einfacher ein bestimmtes Ereignis(Event) zu einem
 * Prozess speichern. Dies ist z.B. sinnvoll bei der Verwaltung
 * der blockierten Prozesse. Jeder Prozess kann durch ein 
 * bestimmtes Event blockiert werden.
 *
 */
public class EventPCB {				// Container Klasse
    
    private PCB pcb;
    private Event event;
    
    public EventPCB( Event event, PCB pcb ) {
        this.pcb = pcb;
        this.event = event;
    }
    
    public void setPCB( PCB pcb ) {
        this.pcb = pcb;
    }
    
    public PCB getPCB() {
        return pcb;
    }
    
    public void setEvent( Event event ) {
        this.event = event;
    }
    
    public Event getEvent() {
        return event;
    }
    
    public String toString() {
        return "Der PCB: "+pcb.toString()+" Das Event: "+event.toString();
    }
}
