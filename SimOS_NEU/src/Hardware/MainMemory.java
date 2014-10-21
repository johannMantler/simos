

package Hardware;
import java.util.ArrayList;



/**
 * 
 * @author Johann Mantler, Hoang Anh Duong
 *
 */
public class MainMemory {
	  
	  private ArrayList<String> memory = new ArrayList<String>();  
	  
	  private int size;
	  
	  /**
	   * Erzeugt das Hauptspeicher - Objekt mit der angegebenen Groesze.
	   * @param size
	   */
	  public MainMemory( int size ) {
	    this.size = size;
	    for( int i = 0; i < size; i++ ) {
	      memory.add( "" );
	    }
	  }
	  
	  public String getContent( int address ){
	    return memory.get(address);
	    
	  }
	  
	  public void setContent( int address, String value ){
	    memory.set( address, value );
	  }
	  
	  public int getSize() {
	    return size;
	  }

	}
