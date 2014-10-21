package Hardware;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;


/**
 * Repraesentiert eine Konsole. Jeder Prozess hat in seinem
 * RegisterSet eine Konsole, ueber die der User mit dem Prozess
 * kommunizieren kann.
 */
@SuppressWarnings("serial")
public class SysConsole extends JFrame {
	
//  private JButton clearButton = new JButton("Clear");
	
  private JTextArea textArea;
  private int currentPos = 0;  //Position des Cursors im Textfeld
  private int refCount;        //Anzahl der Prozesse die auf die Konsole zugreifen
  private int id;
  
  /**
   * Im Konstruktor wird der JFrame mit seinen Komponenten aufgebaut.
   * Dazu kommt noch ein KeyListener, der, immer wenn '\n' eingelesen wird,
   * den aktuellen Befehl an das IO-Objekt schickt.
   * @param io das IO-Objekt, das die Ein-/Ausgabe regelt.
   * @param id die id, die diese Konsole identifiziert
   * @param title der Titel, der in der Leiste angezeigt werden soll.
   */
  public SysConsole( final IO io, int id, String title ) {
    textArea = new JTextArea(20, 40);
    refCount = 1;
    this.id = id;
    setTitle( title );
//    textArea.setFont( new Font("Courier", Font.PLAIN, 14) );

/*    clearButton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        textArea.setText("");
      }
    });
 */
    textArea.addKeyListener( new KeyListener() {
      @Override
      public void keyPressed( KeyEvent e ) {
        if( e.getKeyChar() == '\n' ) {
          io.receiveReadContent( getId(), read() );
        }
      }
      @Override
      public void keyTyped( KeyEvent e ) {}
      @Override
      public void keyReleased( KeyEvent e ) {}
    });
    Container cp = getContentPane();
    cp.setLayout( new FlowLayout() );
    cp.add( new JScrollPane(textArea) );
//    cp.add( clearButton );
  }
  
  /**
   * Schreibt eine Nachricht auf die Konsole.
   * @param message die Nachricht.
   */
  public void write( String message ) {
    textArea.append( message );
    currentPos = textArea.getDocument().getLength();
    textArea.setCaretPosition( currentPos );
  }
  
  /**
   * Liest einen Befehl von der Konsole.
   * @return der Befehl als String
   */
  private String read() {
    String text = textArea.getText().substring(currentPos);
    currentPos += text.length()+1;
    return text;
  }
  
  /**
   * Liefert die ID der Konsole.
   * @return die ID in int
   */
  public int getId() {
    return id;
  }
  
  /**
   * Erhoeht die Anzahl der Prozesse die diese Konsole
   * benutzen um 1.
   */
  public void incRefCount() {
    refCount++;
  }
  
  /**
   * Dekrementiert die Anzahl der Prozesse, die diese Konsole
   * benutzen um 1.
   */
  public void decRefcount() {
    if( refCount > 0 ) {
      refCount--;
    }
  }
  
  /**
   * Zeigt an, ob die Konsole zu keinem Prozess gehoert,
   * oder nicht.
   * @return true, wenn kein Prozess zur Konsole gehoehrt sonst false.
   */
  public boolean zeroRefCount() {
    return refCount == 0;
  }
}
